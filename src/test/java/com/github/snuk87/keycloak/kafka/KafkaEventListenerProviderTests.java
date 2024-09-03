package com.github.snuk87.keycloak.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Collections;
import org.apache.kafka.clients.producer.MockProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.mockito.Mockito;



class KafkaEventListenerProviderTests {

	private KafkaEventListenerProvider listener;
	private KeycloakSessionHelper mockSessionHelper;
	private KafkaConfigService kafkaConfigService;
	private KafkaProducerInitializer kafkaProducerInitializer;
	private MockProducer<String, String> mockProducer;
	//private Map<String, Object> kafkaProducerProperties = new HashMap<>();

	@BeforeEach
	void setUp() throws Exception {
		mockSessionHelper= Mockito.mock(KeycloakSessionHelper.class);
		kafkaConfigService= Mockito.mock(KafkaConfigService.class);
		kafkaProducerInitializer= Mockito.mock(KafkaProducerInitializer.class);

		mockProducer = new MockProducer<>(
				true,                   // autoComplete
				new StringSerializer(), // keySerializer
				new StringSerializer()  // valueSerializer
		);

		Mockito.when(kafkaProducerInitializer.getKafkaProducerByRealmName(Mockito.anyString())).thenReturn(mockProducer);
		Mockito.when(mockSessionHelper.getRealmName(Mockito.anyString())).thenReturn("realmTest");
		Mockito.when(kafkaConfigService.getEvents()).thenReturn(new String[]{"REGISTER", "LOGIN"});
		Mockito.when(kafkaConfigService.getTopicAdminEvents()).thenReturn("event");
		Mockito.when(kafkaProducerInitializer.getKafkaTopicsByRealmName(Mockito.anyString())).thenReturn("testTopic");


		listener = new KafkaEventListenerProvider(mockSessionHelper,kafkaConfigService, kafkaProducerInitializer);
	}

	@Test
	void shouldProduceEventWhenTypeIsDefined() throws Exception {
		Event event = new Event();
		event.setType(EventType.REGISTER);

		listener.onEvent(event);

		assertEquals(1, mockProducer.history().size());
	}

	@Test
	void shouldDoNothingWhenTypeIsNotDefined() throws Exception {
		Event event = new Event();
		event.setType(EventType.CLIENT_DELETE);

		listener.onEvent(event);

		assertTrue(mockProducer.history().isEmpty());
	}

	@Test
	void shouldProduceEventWhenTopicAdminEventsIsNotNull() throws Exception {
		AdminEvent event = new AdminEvent();

		listener.onEvent(event, false);

		assertEquals(1, mockProducer.history().size());
	}

	@Test
	void shouldDoNothingWhenTopicAdminEventsIsNull() throws Exception {
		listener = new KafkaEventListenerProvider(mockSessionHelper,kafkaConfigService, kafkaProducerInitializer);
		AdminEvent event = new AdminEvent();
		Mockito.when(kafkaConfigService.getTopicAdminEvents()).thenReturn(null);

		listener.onEvent(event, false);

		assertTrue(mockProducer.history().isEmpty());
	}

	/*private Map<String,MockProducer<?, ?>> getProducerUsingReflection() throws Exception {
		Field producerField = KafkaProducerInitializer.class.getDeclaredField("kafkaProducers");
		producerField.setAccessible(true);
		return (Map<String,MockProducer<?, ?>>) producerField.get(listener);
	}*/

}
