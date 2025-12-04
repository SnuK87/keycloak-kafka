package com.github.snuk87.keycloak.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.kafka.clients.producer.MockProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.events.admin.OperationType;

class KafkaEventListenerProviderTests {

	private KafkaEventListenerProvider listener;
	private KafkaProducerFactory factory;

	@BeforeEach
	void setUp() throws Exception {
		factory = new KafkaMockProducerFactory();
		listener = new KafkaEventListenerProvider("", "", "", new String[] { "REGISTER" }, "admin-events", new String[] {}, Map.of(),
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
		listener = new KafkaEventListenerProvider("", "", "", new String[] { "REGISTER" }, null, new String[] {}, Map.of(), factory);
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

	@Test
	void shouldProduceEventForMatchingEventWhenStrictEventMatchingFilterApplied() throws Exception {

		listener = new KafkaEventListenerProvider("", "", "", new String[] { "REGISTER" }, "admin_events", new String[] {"CREATE__GROUP_MEMBERSHIP"}, Map.of(), factory);
    MockProducer<?, ?> producer = getProducerUsingReflection();

		AdminEvent matchingEvent = new AdminEvent();
		matchingEvent.setOperationType(OperationType.CREATE);
		matchingEvent.setResourceType(ResourceType.GROUP_MEMBERSHIP);

		AdminEvent nonMatchingEvent = new AdminEvent();
		nonMatchingEvent.setOperationType(OperationType.DELETE);
		nonMatchingEvent.setResourceType(ResourceType.GROUP_MEMBERSHIP);

		listener.onEvent(matchingEvent, false);
		listener.onEvent(nonMatchingEvent, false);

		assertEquals(1, producer.history().size());
		assertTrue(producer.history().get(0).value().toString().contains(String.format("\"operationType\":\"%s\"", matchingEvent.getOperationType().name())));
		assertTrue(producer.history().get(0).value().toString().contains(String.format("\"resourceType\":\"%s\"", matchingEvent.getResourceTypeAsString())));
	}
}
