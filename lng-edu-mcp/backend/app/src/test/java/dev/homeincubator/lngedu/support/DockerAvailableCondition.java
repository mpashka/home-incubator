package dev.homeincubator.lngedu.support;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * JUnit 5 {@link ExecutionCondition} backing {@link EnabledIfDockerAvailable}. It asks
 * {@link DockerClientFactory} whether a Docker daemon is reachable and disables (skips) the element
 * when it is not.
 *
 * <p>{@code DockerClientFactory.instance().isDockerAvailable()} probes the daemon once and caches
 * the answer, swallowing any connection failure and returning {@code false} instead of throwing.
 * Evaluating it here — before Spring builds its context and before any {@code PostgreSQLContainer}
 * is started — is what turns "no Docker" into a clean skip rather than an error.
 */
public class DockerAvailableCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED =
            ConditionEvaluationResult.enabled("Docker is available");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            if (DockerClientFactory.instance().isDockerAvailable()) {
                return ENABLED;
            }
            return ConditionEvaluationResult.disabled("Docker is not available - skipping Testcontainers test");
        } catch (Throwable t) {
            // Defensive: never let the probe fail the build; treat any error as "no Docker".
            return ConditionEvaluationResult.disabled(
                    "Docker availability probe failed (" + t.getClass().getSimpleName() + ") - skipping");
        }
    }
}
