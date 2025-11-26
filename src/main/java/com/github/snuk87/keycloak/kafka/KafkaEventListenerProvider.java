package com.github.snuk87.keycloak.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaEventListenerProvider implements EventListenerProvider {

	private static final Logger LOG = Logger.getLogger(KafkaEventListenerProvider.class);

	private String topicEvents;

	private List<EventType> events;

	private String topicAdminEvents;

	private Producer<String, String> producer;

	private ObjectMapper mapper;

	public KafkaEventListenerProvider(String bootstrapServers, String clientId, String topicEvents, String[] events,
			String topicAdminEvents, Map<String, Object> kafkaProducerProperties, KafkaProducerFactory factory) {
		this.topicEvents = topicEvents;
		this.events = new ArrayList<>();
		this.topicAdminEvents = topicAdminEvents;

		for (String event : events) {
			try {
				EventType eventType = EventType.valueOf(event.toUpperCase());
				this.events.add(eventType);
			} catch (IllegalArgumentException e) {
				LOG.debug("Ignoring event >" + event + "<. Event does not exist.");
			}
		}

		producer = factory.createProducer(clientId, bootstrapServers, kafkaProducerProperties);
		mapper = new ObjectMapper();
	}

	private void produceEvent(String eventAsString, String topic) {
		LOG.debug("Produce to topic: " + topic + " ...");
		ProducerRecord<String, String> record = new ProducerRecord<>(topic, eventAsString);
		producer.send(record, new Callback() {
			@Override
			public void onCompletion(RecordMetadata metadata, Exception exception) {
				if (exception != null) {
					LOG.error("Failed to send event to Kafka topic " + topic + ": " + exception.getMessage(), exception);
				} else {
					LOG.debug("Produced to topic: " + metadata.topic());
				}
			}
		});
	}

	@Override
	public void onEvent(Event event) {
		if (events.contains(event.getType())) {
			try {
				produceEvent(mapper.writeValueAsString(event), topicEvents);
			} catch (JsonProcessingException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		if (topicAdminEvents != null) {
			try {
				produceEvent(mapper.writeValueAsString(event), topicAdminEvents);
			} catch (JsonProcessingException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void close() {
		// ignore
	}
}
