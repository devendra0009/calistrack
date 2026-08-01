package com.davendra.calistrack_backend.keepalive.support;

import com.davendra.calistrack_backend.keepalive.config.KeepAliveProperties;
import com.davendra.calistrack_backend.keepalive.config.KeepAliveProperties.TimeWindow;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Decides whether the keep-alive pinger should run at a given instant.
 * Extracted so weekday/weekend and multi-window rules stay testable and swappable.
 */
@Component
@ConditionalOnProperty(prefix = "calistrack.keep-alive", name = "enabled", havingValue = "true")
public class ActiveWindowEvaluator {

	private final KeepAliveProperties properties;
	private final Clock clock;

	public ActiveWindowEvaluator(KeepAliveProperties properties) {
		this(properties, Clock.systemUTC());
	}

	ActiveWindowEvaluator(KeepAliveProperties properties, Clock clock) {
		this.properties = properties;
		this.clock = clock;
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
