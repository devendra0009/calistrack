package com.davendra.calistrack_backend.keepalive.support;

import com.davendra.calistrack_backend.keepalive.config.KeepAliveProperties;
import com.davendra.calistrack_backend.keepalive.config.KeepAliveProperties.TimeWindow;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Decides whether the keep-alive pinger should run at a given instant.
 * Wired as a {@code @Bean} from {@link com.davendra.calistrack_backend.keepalive.config.KeepAliveConfig}.
 */
public class ActiveWindowEvaluator {

	private final KeepAliveProperties properties;
	private final Clock clock;

	public ActiveWindowEvaluator(KeepAliveProperties properties) {
		this(properties, Clock.systemUTC());
	}

	public ActiveWindowEvaluator(KeepAliveProperties properties, Clock clock) {
		this.properties = Objects.requireNonNull(properties, "properties");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	public Evaluation evaluate() {
		ZonedDateTime now = ZonedDateTime.now(clock.withZone(properties.getTimeZone()));
		List<TimeWindow> windows = properties.resolveWindows(now.getDayOfWeek());
		boolean active = windows.stream().anyMatch(w -> w.contains(now.toLocalTime()));
		return new Evaluation(active, now.toLocalDateTime(), windows);
	}

	public record Evaluation(boolean active, LocalDateTime localDateTime, List<TimeWindow> windows) {
	}
}
