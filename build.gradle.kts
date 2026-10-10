plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.spring.javaformat") version "0.0.48"
	checkstyle
	// Load tests (plan D16), run by hand: ./gradlew gatlingRun --simulation=<class>
	id("io.gatling.gradle") version "3.16.0"
}

group = "dev.onepieceapi"
version = "0.0.1-SNAPSHOT"
description = "One Piece API - anonymous read-only API over the published content"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

// GitHub Packages requires authentication even to resolve a public package: gpr.user/gpr.token
// in ~/.gradle/gradle.properties for local dev (a PAT classic with read:packages only),
// GITHUB_ACTOR/GITHUB_TOKEN in CI. Same setup as one-piece-content-service.
fun RepositoryHandler.gitHubPackages(repository: String) = maven {
	name = "GitHubPackages-$repository"
	url = uri("https://maven.pkg.github.com/one-piece-api/$repository")
	credentials {
		username = providers.gradleProperty("gpr.user").orElse(providers.environmentVariable("GITHUB_ACTOR")).orNull
		password = providers.gradleProperty("gpr.token").orElse(providers.environmentVariable("GITHUB_TOKEN")).orNull
	}
	// Only the project's own artifacts are looked up there, not every dependency.
	content { includeGroup("dev.onepieceapi") }
}

repositories {
	mavenCentral()
	gitHubPackages("one-piece-exception")
	gitHubPackages("one-piece-content-service")
}

dependencies {
	implementation("dev.onepieceapi:one-piece-exception:0.5.0")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	// OpenAPI spec (no Swagger UI: nobody needs a page, the committed openapi/openapi.yaml is the
	// contract). Same springdoc line as the other services, built against Boot 4.1.
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:3.1.1")
	runtimeOnly("org.postgresql:postgresql")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	// The tests build content-service's real schema, "published" views included, from its
	// published migrations (docs/adr/0001-public-read-service.md). Flyway exists only here:
	// this service never migrates anything.
	testImplementation("org.springframework.boot:spring-boot-starter-flyway")
	testRuntimeOnly("org.flywaydb:flyway-database-postgresql")
	testRuntimeOnly("dev.onepieceapi:one-piece-content-service-migrations:13")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

checkstyle {
	toolVersion = "14.3.0"
}

// Rewrites openapi/openapi.yaml from the current controllers instead of failing on a
// mismatch - the one command to run after an API change (see OpenApiSpecTest).
tasks.register<Test>("updateOpenApiSpec") {
	description = "Regenerates openapi/openapi.yaml from the current controllers."
	group = "documentation"
	testClassesDirs = sourceSets.test.get().output.classesDirs
	classpath = sourceSets.test.get().runtimeClasspath
	filter { includeTestsMatching("*OpenApiSpecTest") }
	systemProperty("openapi.update", "true")
	outputs.upToDateWhen { false }
}

// The plugin starts the load generator with whatever "java" is first on the PATH, not with the
// project toolchain. The simulations use nothing newer than Java 17, so they are compiled for
// it and run on any JDK from there up.
tasks.named<JavaCompile>("compileGatlingJava") {
	options.release = 17
}
