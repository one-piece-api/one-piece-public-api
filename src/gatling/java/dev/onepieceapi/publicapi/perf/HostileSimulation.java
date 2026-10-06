package dev.onepieceapi.publicapi.perf;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.forAll;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * The requests that cost the most (plan D16): a search at the maximum length, the sort
 * that cannot use an index, the deepest and the largest pages. All are valid, so all must
 * answer {@code 200} and none may be slower than a normal list by much.
 */
public class HostileSimulation extends Simulation {

	private static final double RATE = Double.parseDouble(System.getProperty("rate", "5"));

	/** No costly request may be slower than plan D16 allows a search to be. */
	private static final int SLOWEST_LIST_P95_MS = 150;

	private static final String LIST = "/v1/en/devil-fruit-types";

	private static final String MAX_LENGTH_QUERY = "fire".repeat(25);

	private final ScenarioBuilder attackers = scenario("hostile requests")
		.exec(http("search at maximum length").get(LIST).queryParam("q", MAX_LENGTH_QUERY).check(status().is(200)))
		.exec(http("search of a common letter").get(LIST)
			.queryParam("q", "o")
			.queryParam("size", "100")
			.check(status().is(200)))
		.exec(http("deep page").get(LIST + "?page=100000&size=100").check(status().is(200)))
		.exec(http("last real page").get(LIST + "?page=49&size=100&sort=publishedAt,desc").check(status().is(200)))
		.exec(http("largest page by romaji").get(LIST + "?size=100&sort=romaji,desc").check(status().is(200)));

	{
		setUp(this.attackers.injectOpen(constantUsersPerSec(RATE).during(30))).protocols(Api.PROTOCOL)
			.assertions(forAll().failedRequests().percent().lt(0.1),
					forAll().responseTime().percentile(95).lt(SLOWEST_LIST_P95_MS));
	}

}
