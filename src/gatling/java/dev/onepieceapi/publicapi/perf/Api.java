package dev.onepieceapi.publicapi.perf;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.core.CoreDsl.percent;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.randomSwitch;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.header;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * What the simulations share: the target, the requests and the realistic traffic mix of
 * plan D16 (70% detail, 20% list, 10% list with a search; half of the answers
 * revalidated).
 */
final class Api {

	/** Where the service is reached: the local port-forward by default. */
	private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8084");

	private static final List<String> LANGUAGES = List.of("it", "en");

	private static final List<String> SEARCH_TERMS = List.of("fuoco", "fire", "frutto", "gomu", "ombra", "no mi");

	/** The pages of 100 that the discovery draws from: 5,000 contents seeded. */
	private static final int DISCOVERABLE_PAGES = 50;

	private static final int REQUESTS_PER_USER = 5;

	/**
	 * Long enough for the JIT and the database cache to settle before anything is
	 * measured.
	 */
	private static final int WARM_UP_SECONDS = 30;

	private static final String LIST_URI = "/v1/#{lang}/devil-fruit-types";

	static final HttpProtocolBuilder PROTOCOL = http.baseUrl(BASE_URL)
		.acceptHeader("application/json")
		.acceptEncodingHeader("gzip")
		// Gatling would answer repeated GETs from its own browser-like cache, without
		// sending them;
		// the simulations revalidate explicitly, so every request must reach the service.
		.disableCaching();

	/** A client learns ids and slugs by listing; so does a virtual user, once. */
	static final ChainBuilder DISCOVER = exec(Api::chooseLanguage)
		.exec(session -> session.set("page", random(DISCOVERABLE_PAGES)))
		.exec(http("discover").get(LIST_URI + "?size=100&page=#{page}")
			.check(status().is(200), jmesPath("content[].id").ofList().saveAs("ids"),
					jmesPath("content[].slug").ofList().saveAs("slugs")))
		.exitHereIfFailed();

	static final ChainBuilder DETAIL = exec(Api::chooseLanguage)
		.exec(session -> session.set("target",
				random(2) == 0 ? pick(session.getList("ids")) : pick(session.getList("slugs"))))
		.exec(revalidated("detail", n -> http(n).get(LIST_URI + "/#{target}")));

	static final ChainBuilder LIST = exec(Api::chooseLanguage).exec(session -> session.set("page", random(5)))
		.exec(revalidated("list", n -> http(n).get(LIST_URI + "?page=#{page}&sort=name,asc")));

	static final ChainBuilder SEARCH = exec(Api::chooseLanguage)
		.exec(session -> session.set("term", pick(SEARCH_TERMS)))
		.exec(revalidated("search", n -> http(n).get(LIST_URI).queryParam("q", "#{term}")));

	static final ChainBuilder MIX = randomSwitch().on(percent(70.0).then(DETAIL), percent(20.0).then(LIST),
			percent(10.0).then(SEARCH));

	/** What one user does: learns the ids, then makes requests of the mix. */
	static final ChainBuilder VISIT = exec(DISCOVER).exec(repeat(REQUESTS_PER_USER).on(MIX));

	private Api() {
	}

	/**
	 * The load ramped up to {@code rate}, its requests grouped apart so that the measured
	 * ones, and the assertions on them, start on a warm service.
	 */
	static PopulationBuilder warmUp(double rate) {
		return scenario("warm-up").exec(group("warm-up").on(VISIT))
			.injectOpen(rampUsersPerSec(1).to(rate).during(WARM_UP_SECONDS));
	}

	/**
	 * The request and, half of the time, the same request again with the ETag it was
	 * given.
	 */
	private static ChainBuilder revalidated(String name, Function<String, HttpRequestActionBuilder> request) {
		return exec(request.apply(name).check(status().is(200), header("ETag").saveAs("etag")))
			.exec(doIf(session -> !session.isFailed() && random(2) == 0)
				.then(exec(request.apply(name + "-304").header("If-None-Match", "#{etag}").check(status().is(304)))));
	}

	private static Session chooseLanguage(Session session) {
		return session.set("lang", pick(LANGUAGES));
	}

	private static int random(int bound) {
		return ThreadLocalRandom.current().nextInt(bound);
	}

	private static <T> T pick(List<T> values) {
		return values.get(random(values.size()));
	}

}
