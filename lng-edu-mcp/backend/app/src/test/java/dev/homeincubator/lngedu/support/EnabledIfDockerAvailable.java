package dev.homeincubator.lngedu.support;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test class (or method) as requiring a working Docker daemon. When Docker is not
 * available the annotated element is reported as SKIPPED, not failed.
 *
 * <p>Rationale: this environment has no Docker. We deliberately do NOT use the {@code @Testcontainers}
 * JUnit extension for gating, because its container lifecycle tries to reach Docker during setup and
 * would ERROR rather than skip. Instead a plain {@link org.junit.jupiter.api.extension.ExecutionCondition}
 * ({@link DockerAvailableCondition}) probes {@code DockerClientFactory} once, before any container is
 * created, so the whole class is skipped cleanly and {@code :backend:check} stays green here while the
 * scenario still runs wherever Docker exists.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DockerAvailableCondition.class)
public @interface EnabledIfDockerAvailable {
}
