package com.github.snuk87.keycloak.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.keycloak.Config.SystemPropertiesConfigProvider;

class KafkaProducerConfigTests {

	@Test
	void shouldReturnMapWithConfigWhenPropertyExists() {
		System.setProperty("keycloak.retry.backoff.ms", "1000");
		System.setProperty("keycloak.max.block.ms", "5000");
		System.setProperty("keycloak.foo", "bar");

		Map<String, Object> config = KafkaProducerConfig.init(new SystemPropertiesConfigProvider().scope());
		Map<String, Object> expected = Map.of("retry.backoff.ms", "1000", "max.block.ms", "5000");

		assertEquals(expected, config);
	}
}
