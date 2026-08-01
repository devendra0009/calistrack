package com.davendra.calistrack_backend.keepalive.scheduler;

import com.davendra.calistrack_backend.keepalive.config.KeepAliveProperties;
import com.davendra.calistrack_backend.keepalive.support.ActiveWindowEvaluator;
import com.davendra.calistrack_backend.keepalive.support.ActiveWindowEvaluator.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Periodically GETs this service's health URL during configured active hours
 * so free-tier hosts (Render, etc.) do not spin the instance down mid-day.
 */
@Component
@ConditionalOnProperty(prefix = "calistrack.keep-alive", name = "enabled", havingValue = "true")
public class KeepAliveScheduler {

	private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);

	private final KeepAliveProperties properties;
	private final ActiveWindowEvaluator windowEvaluator;
	private final RestClient keepAliveRestClient;

	public KeepAliveScheduler(
			KeepAliveProperties properties,
			ActiveWindowEvaluator windowEvaluator,
			@Qualifier("keepAliveRestClient") RestClient keepAliveRestClient
	) {
		this.properties = properties;
		this.windowEvaluator = windowEvaluator;
		this.keepAliveRestClient = keepAliveRestClient;
	}

	@Scheduled(
			fixedDelayString = "${calistrack.keep-alive.interval:10m}",
			initialDelayString = "${calistrack.keep-alive.initial-delay:60s}"
	)
	public void ping() {
		Evaluation evaluation = windowEvaluator.evaluate();
		String url = properties.pingUrl();

		if (!evaluation.active()) {
			log.info(
					"Keep-alive ping skipped: outside active window localTime={} zone={} windows={} provider={}",
					evaluation.localDateTime(),
					properties.getTimeZone(),
					evaluation.windows(),
					properties.getProvider()
			);
			return;
		}

		if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
			log.warn(
					"Keep-alive ping skipped: calistrack.keep-alive.base-url is blank (provider={})",
					properties.getProvider()
			);
			return;
		}

		log.info(
				"Keep-alive ping started url={} localTime={} provider={}",
				url,
				evaluation.localDateTime(),
				properties.getProvider()
		);

		try {
			ResponseEntity<Void> response = keepAliveRestClient.get()
					.uri(normalizePath(properties.getHealthPath()))
					.retrieve()
					.toBodilessEntity();

			log.info(
					"Keep-alive ping successful url={} status={} localTime={} provider={}",
					url,
					response.getStatusCode().value(),
					evaluation.localDateTime(),
					properties.getProvider()
			);
		}
		catch (RestClientException ex) {
			log.warn(
					"Keep-alive ping failed url={} localTime={} provider={} reason={}",
					url,
					evaluation.localDateTime(),
					properties.getProvider(),
					ex.getMessage()
			);
		}
		catch (RuntimeException ex) {
			log.warn(
					"Keep-alive ping failed unexpectedly url={} localTime={} provider={} reason={}",
					url,
					evaluation.localDateTime(),
					properties.getProvider(),
					ex.getMessage()
			);
		}
	}

	private static String normalizePath(String healthPath) {
		if (healthPath == null || healthPath.isBlank()) {
			return "/health";
		}
		return healthPath.startsWith("/") ? healthPath : "/" + healthPath;
	}
}
