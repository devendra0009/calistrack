package com.davendra.calistrack_backend.keepalive.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Keep-alive / anti-idle pinger for free-tier hosts (Render, Railway, Fly.io, …).
 * <p>
 * Extensibility:
 * <ul>
 *   <li>{@code windows} — default active windows for every day</li>
 *   <li>{@code weekday-windows} / {@code weekend-windows} — optional overrides</li>
 *   <li>{@code schedules} — day-specific multi-window rules (highest priority when non-empty)</li>
 *   <li>{@code enabled} + profile-specific property files for env-specific behaviour</li>
 *   <li>{@code provider} — informational tag for logs / future provider-specific behaviour</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "calistrack.keep-alive")
public class KeepAliveProperties {

	/**
	 * Master switch. Prefer {@code false} locally; enable on the free-tier host.
	 */
	private boolean enabled = false;

	/**
	 * Public base URL of this service (e.g. {@code https://calistrack.onrender.com}).
	 * Must be the externally reachable URL so the host counts the request as activity.
	 */
	private String baseUrl = "";

	/**
	 * Lightweight health path appended to {@link #baseUrl}.
	 */
	private String healthPath = "/health";

	/**
	 * Zone used when evaluating active windows.
	 */
	private ZoneId timeZone = ZoneId.of("Asia/Kolkata");

	/**
	 * Delay between pings (ISO-8601 duration or Spring-friendly units, e.g. {@code 10m}, {@code PT10M}).
	 */
	@DurationUnit(ChronoUnit.MINUTES)
	private Duration interval = Duration.ofMinutes(10);

	/**
	 * Deployment provider hint ({@code render}, {@code railway}, {@code fly}, …). Logged only for now.
	 */
	private String provider = "render";

	/**
	 * Default windows used when no day-specific schedule matches.
	 */
	private List<TimeWindow> windows = new ArrayList<>(List.of(
			new TimeWindow(LocalTime.of(5, 0), LocalTime.of(23, 0))
	));

	/**
	 * Optional weekday override (Mon–Fri). Empty → fall back to {@link #windows}.
	 */
	private List<TimeWindow> weekdayWindows = new ArrayList<>();

	/**
	 * Optional weekend override (Sat–Sun). Empty → fall back to {@link #windows}.
	 */
	private List<TimeWindow> weekendWindows = new ArrayList<>();

	/**
	 * Optional explicit day schedules. When non-empty, matching entries take precedence
	 * over weekday/weekend/default windows.
	 */
	private List<DaySchedule> schedules = new ArrayList<>();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getHealthPath() {
		return healthPath;
	}

	public void setHealthPath(String healthPath) {
		this.healthPath = healthPath;
	}

	public ZoneId getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(ZoneId timeZone) {
		this.timeZone = timeZone;
	}

	public Duration getInterval() {
		return interval;
	}

	public void setInterval(Duration interval) {
		this.interval = interval;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public List<TimeWindow> getWindows() {
		return windows;
	}

	public void setWindows(List<TimeWindow> windows) {
		this.windows = windows != null ? windows : new ArrayList<>();
	}

	public List<TimeWindow> getWeekdayWindows() {
		return weekdayWindows;
	}

	public void setWeekdayWindows(List<TimeWindow> weekdayWindows) {
		this.weekdayWindows = weekdayWindows != null ? weekdayWindows : new ArrayList<>();
	}

	public List<TimeWindow> getWeekendWindows() {
		return weekendWindows;
	}

	public void setWeekendWindows(List<TimeWindow> weekendWindows) {
		this.weekendWindows = weekendWindows != null ? weekendWindows : new ArrayList<>();
	}

	public List<DaySchedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<DaySchedule> schedules) {
		this.schedules = schedules != null ? schedules : new ArrayList<>();
	}

	/**
	 * Resolves which windows apply for the given day (schedules → weekday/weekend → default).
	 */
	public List<TimeWindow> resolveWindows(DayOfWeek dayOfWeek) {
		if (!schedules.isEmpty()) {
			for (DaySchedule schedule : schedules) {
				if (schedule.appliesTo(dayOfWeek) && !schedule.getWindows().isEmpty()) {
					return schedule.getWindows();
				}
			}
		}

		boolean weekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
		if (weekend && !weekendWindows.isEmpty()) {
			return weekendWindows;
		}
		if (!weekend && !weekdayWindows.isEmpty()) {
			return weekdayWindows;
		}
		return windows;
	}

	public String pingUrl() {
		String base = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
		String path = healthPath == null || healthPath.isBlank() ? "/health" : healthPath;
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return base + path;
	}

	/**
	 * Inclusive start, exclusive end within a single calendar day.
	 * Overnight windows (e.g. 22:00 → 06:00) are supported.
	 */
	public static class TimeWindow {

		private LocalTime start = LocalTime.of(5, 0);
		private LocalTime end = LocalTime.of(23, 0);

		public TimeWindow() {
		}

		public TimeWindow(LocalTime start, LocalTime end) {
			this.start = start;
			this.end = end;
		}

		public LocalTime getStart() {
			return start;
		}

		public void setStart(LocalTime start) {
			this.start = start;
		}

		public LocalTime getEnd() {
			return end;
		}

		public void setEnd(LocalTime end) {
			this.end = end;
		}

		public boolean contains(LocalTime time) {
			if (start.equals(end)) {
				return true; // full-day window
			}
			if (start.isBefore(end)) {
				return !time.isBefore(start) && time.isBefore(end);
			}
			// Overnight: active from start→midnight OR midnight→end
			return !time.isBefore(start) || time.isBefore(end);
		}

		@Override
		public String toString() {
			return start + "-" + end;
		}
	}

	/**
	 * Day-of-week specific schedule (e.g. weekdays only, or Mon+Wed+Fri).
	 */
	public static class DaySchedule {

		private Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
		private List<TimeWindow> windows = new ArrayList<>();

		public Set<DayOfWeek> getDays() {
			return days;
		}

		public void setDays(Set<DayOfWeek> days) {
			this.days = days != null ? days : EnumSet.noneOf(DayOfWeek.class);
		}

		public List<TimeWindow> getWindows() {
			return windows;
		}

		public void setWindows(List<TimeWindow> windows) {
			this.windows = windows != null ? windows : new ArrayList<>();
		}

		public boolean appliesTo(DayOfWeek dayOfWeek) {
			return days.contains(dayOfWeek);
		}
	}
}
