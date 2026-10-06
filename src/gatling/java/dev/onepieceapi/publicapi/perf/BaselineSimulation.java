package dev.onepieceapi.publicapi.perf;

import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.Simulation;

import java.util.Map;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.scenario;

/**
 * The realistic mix at a steady rate (plan D16): the numbers later versions of the
 * service are compared with. The default rate is the measured capacity of one replica on
 * the local cluster; set another with {@code -Drate=<users/s>} (one user makes about 8.5
 * requests) and the duration with {@code -Dseconds}.
 */
public class BaselineSimulation extends Simulation {

	private static final double RATE = Double.parseDouble(System.getProperty("rate", "7"));

	private static final int SECONDS = Integer.getInteger("seconds", 60);

	/**
	 * p95 ceilings in ms: about twice the baseline of 2026-10-06 (README, "Performance"),
	 * all within the targets of plan D16.
	 */
	private static final Map<String, Integer> P95_CEILINGS = Map.of("detail", 15, "detail-304", 15, "list", 35,
			"list-304", 15, "search", 110, "search-304", 15);

	{
		setUp(Api.warmUp(RATE)
			.andThen(scenario("realistic mix").exec(Api.VISIT).injectOpen(constantUsersPerSec(RATE).during(SECONDS))))
			.protocols(Api.PROTOCOL)
			.assertions(P95_CEILINGS.entrySet().stream().flatMap(BaselineSimulation::targets).toList());
	}

	private static Stream<Assertion> targets(Map.Entry<String, Integer> ceiling) {
		return Stream.of(details(ceiling.getKey()).responseTime().percentile(95).lt(ceiling.getValue()),
				details(ceiling.getKey()).failedRequests().percent().lt(0.1));
	}

}
