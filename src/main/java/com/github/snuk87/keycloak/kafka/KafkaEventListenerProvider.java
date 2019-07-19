package com.github.snuk87.keycloak.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

	private String topic;

	private List<EventType> events;

	private boolean enableAdminEvents;

	private Producer<String, String> producer;

	private ObjectMapper mapper;

	public KafkaEventListenerProvider(String bootstrapServers, String clientId, String topic, String[] events,
			boolean enableAdminEvents) {
		this.topic = topic;
		this.events = new ArrayList<>();
		this.enableAdminEvents = enableAdminEvents;

		for (int i = 0; i < events.length; i++) {
			try {
				EventType eventType = EventType.valueOf(events[i].toUpperCase());
				this.events.add(eventType);
			} catch (IllegalArgumentException e) {
				LOG.debug("Ignoring event >" + events[i] + "<. Event does not exist.");
			}
		}

		producer = KafkaProducerFactory.createProducer(clientId, bootstrapServers);
		mapper = new ObjectMapper();
	}

	private void produceEvent(String eventAsString) throws InterruptedException, ExecutionException {
		LOG.debug("Produce to topic: " + topic + " ...");
		ProducerRecord<String, String> record = new ProducerRecord<>(topic, eventAsString);
		Future<RecordMetadata> metaData = producer.send(record);
		RecordMetadata recordMetadata = metaData.get();
		LOG.debug("Produced to topic: " + recordMetadata.topic());
	}

	@Override
	public void onEvent(Event event) {
		if (events.contains(event.getType())) {
			try {
				produceEvent(mapper.writeValueAsString(event));
			} catch (JsonProcessingException | ExecutionException e) {
				LOG.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		if (enableAdminEvents) {
			try {
				produceEvent(mapper.writeValueAsString(event));
			} catch (JsonProcessingException | ExecutionException e) {
				LOG.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void close() {
		// ignore
	}
}
