package com.github.snuk87.keycloak.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

	private String[] adminEventResourceTypes;

	private String[] adminEventOperationTypes;

	public KafkaEventListenerProvider(String bootstrapServers, String clientId, String topicEvents, String[] events,
			String topicAdminEvents, String[] adminEventResourceTypes, String[] adminEventOperationTypes, Map<String, Object> kafkaProducerProperties, KafkaProducerFactory factory) {
		this.topicEvents = topicEvents;
		this.events = new ArrayList<>();
		this.topicAdminEvents = topicAdminEvents;
		this.adminEventResourceTypes = adminEventResourceTypes
		this.adminEventOperationTypes = adminEventOperationTypes

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

	private void produceEvent(String eventAsString, String topic)
			throws InterruptedException, ExecutionException, TimeoutException {
		LOG.debug("Produce to topic: " + topicEvents + " ...");
		ProducerRecord<String, String> record = new ProducerRecord<>(topic, eventAsString);
		Future<RecordMetadata> metaData = producer.send(record);
		RecordMetadata recordMetadata = metaData.get(30, TimeUnit.SECONDS);
		LOG.debug("Produced to topic: " + recordMetadata.topic());
	}

	@Override
	public void onEvent(Event event) {
		if (events.contains(event.getType())) {
			try {
				produceEvent(mapper.writeValueAsString(event), topicEvents);
			} catch (JsonProcessingException | ExecutionException | TimeoutException e) {
				LOG.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		if (topicAdminEvents != null && 
		    (this.adminEventResourceTypes == null || 
			 this.adminEventResourceTypes.length == 0 || 
			 this.adminEventResourceTypes.contains(event.resourceType)) && 
			 (this.adminEventOperationTypes == null || 
			 this.adminEventOperationTypes.length == 0 || 
			 this.adminEventOperationTypes.contains(event.operationType))) {
			try {
				produceEvent(mapper.writeValueAsString(event), topicAdminEvents);
			} catch (JsonProcessingException | ExecutionException | TimeoutException e) {
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
