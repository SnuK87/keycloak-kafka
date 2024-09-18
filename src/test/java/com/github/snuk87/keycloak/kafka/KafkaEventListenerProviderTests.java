package com.github.snuk87.keycloak.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;

import com.github.snuk87.keycloak.kafka.spi.KafkaEventListenerProvider;
import org.apache.kafka.clients.producer.MockProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

class KafkaEventListenerProviderTests {

	private KafkaEventListenerProvider listener;
	private KafkaProducerFactory factory;

	@BeforeEach
	void setUp() throws Exception {
		factory = new KafkaMockProducerFactory();
		listener = new KafkaEventListenerProvider("", "", "", new String[] { "REGISTER" }, "admin-events", Map.of(),
				factory);
	}

	@Test
	void shouldProduceEventWhenTypeIsDefined() throws Exception {
		Event event = new Event();
		event.setType(EventType.REGISTER);
		MockProducer<?, ?> producer = getProducerUsingReflection();

		listener.onEvent(event);

		assertEquals(1, producer.history().size());
	}

	@Test
	void shouldDoNothingWhenTypeIsNotDefined() throws Exception {
		Event event = new Event();
		event.setType(EventType.CLIENT_DELETE);
		MockProducer<?, ?> producer = getProducerUsingReflection();

		listener.onEvent(event);

		assertTrue(producer.history().isEmpty());
	}

	@Test
	void shouldProduceEventWhenTopicAdminEventsIsNotNull() throws Exception {
		AdminEvent event = new AdminEvent();
		MockProducer<?, ?> producer = getProducerUsingReflection();

		listener.onEvent(event, false);

		assertEquals(1, producer.history().size());
	}

	@Test
	void shouldDoNothingWhenTopicAdminEventsIsNull() throws Exception {
		listener = new KafkaEventListenerProvider("", "", "", new String[] { "REGISTER" }, null, Map.of(), factory);
		AdminEvent event = new AdminEvent();
		MockProducer<?, ?> producer = getProducerUsingReflection();

		listener.onEvent(event, false);

		assertTrue(producer.history().isEmpty());
	}

	private MockProducer<?, ?> getProducerUsingReflection() throws Exception {
		Field producerField = KafkaEventListenerProvider.class.getDeclaredField("producer");
		producerField.setAccessible(true);
		return (MockProducer<?, ?>) producerField.get(listener);
	}

}
