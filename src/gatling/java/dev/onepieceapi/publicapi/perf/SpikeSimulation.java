package dev.onepieceapi.publicapi.perf;

import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.scenario;

/**
 * The load jumps to five times the steady rate for a while and comes back (plan D16): the
 * service must answer through it, without errors, and recover. Set the steady rate with
 * {@code -Drate}.
 */
public class SpikeSimulation extends Simulation {

	private static final double RATE = Double.parseDouble(System.getProperty("rate", "2"));

	private static final int SPIKE_FACTOR = 5;

	private static final int PHASE_SECONDS = 30;

	{
		setUp(Api.warmUp(RATE)
			.andThen(scenario("spike").exec(Api.VISIT)
				.injectOpen(constantUsersPerSec(RATE).during(PHASE_SECONDS),
						constantUsersPerSec(RATE * SPIKE_FACTOR).during(PHASE_SECONDS),
						constantUsersPerSec(RATE).during(PHASE_SECONDS))))
			.protocols(Api.PROTOCOL)
			.assertions(global().failedRequests().percent().lt(0.1));
	}

}
