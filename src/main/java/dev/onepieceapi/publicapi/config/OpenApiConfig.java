package dev.onepieceapi.publicapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Document-level OpenAPI metadata. Anonymous, so no security scheme; the server is the
 * API root alone, which keeps the committed spec independent of the host and of the
 * gateway prefix (plan D10).
 */
@Configuration
class OpenApiConfig {

	private static final String CONTROLLER_SUFFIX = "Controller";

	@Bean
	OpenAPI openApi(@Value("${spring.application.name}") String applicationName) {
		return new OpenAPI().info(new Info().title(applicationName).version("v1"))
			.addServersItem(new Server().url("/"));
	}

	/**
	 * Scopes each operationId by its controller ({@code LanguageController#list} becomes
	 * {@code languageList}): the handlers are short verbs meant to be read within their
	 * controller, which would collide in the spec's flat namespace.
	 */
	@Bean
	OperationCustomizer controllerScopedOperationIds() {
		return (operation, handlerMethod) -> {
			var controller = handlerMethod.getBeanType().getSimpleName().replace(CONTROLLER_SUFFIX, "");
			var method = StringUtils.capitalize(handlerMethod.getMethod().getName());
			return operation.operationId(StringUtils.uncapitalize(controller) + method);
		};
	}

}
