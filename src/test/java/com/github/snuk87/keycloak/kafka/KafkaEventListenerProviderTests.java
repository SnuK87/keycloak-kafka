package com.github.snuk87.keycloak.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringSerializer;
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

	@Test
	void shouldNotBlockWhenKafkaIsUnavailable() throws Exception {
		// Create a non-auto-completing MockProducer to simulate Kafka unavailability
		MockProducer<String, String> slowProducer = new MockProducer<>(false, new StringSerializer(), new StringSerializer());
		
		KafkaProducerFactory slowFactory = new KafkaProducerFactory() {
			@Override
			public Producer<String, String> createProducer(String clientId, String bootstrapServer,
					Map<String, Object> optionalProperties) {
				return slowProducer;
			}
		};
		
		KafkaEventListenerProvider slowListener = new KafkaEventListenerProvider("", "", "", 
				new String[] { "REGISTER" }, "admin-events", Map.of(), slowFactory);
		
		Event event = new Event();
		event.setType(EventType.REGISTER);
		
		// Track execution time - should return almost immediately (non-blocking)
		CountDownLatch latch = new CountDownLatch(1);
		long startTime = System.currentTimeMillis();
		
		Thread eventThread = new Thread(() -> {
			slowListener.onEvent(event);
			latch.countDown();
		});
		eventThread.start();
		
		// Wait max 1 second for the method to return (it should be nearly instant)
		boolean completed = latch.await(1, TimeUnit.SECONDS);
		long elapsed = System.currentTimeMillis() - startTime;
		
		assertTrue(completed, "onEvent should return immediately without blocking");
		assertTrue(elapsed < 1000, "onEvent took too long: " + elapsed + "ms - it should be non-blocking");
		
		// Verify the message was queued (even though not yet completed)
		assertEquals(1, slowProducer.history().size());
	}

}
