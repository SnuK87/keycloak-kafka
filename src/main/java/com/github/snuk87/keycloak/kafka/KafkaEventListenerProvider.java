package com.github.snuk87.keycloak.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaEventListenerProvider implements EventListenerProvider {

	private static final Logger LOG = Logger.getLogger(KafkaEventListenerProvider.class);

	private String topic;

	private List<EventType> events;

	private Producer<String, String> producer;

	private ObjectMapper mapper;

	public KafkaEventListenerProvider(String bootstrapServers, String clientId, String topic, String[] events) {
		this.topic = topic;
		this.events = new ArrayList<>();

		for(int i = 0; i < events.length; i++) {
			try {
				EventType eventType = EventType.valueOf(events[i].toUpperCase());
				this.events.add(eventType);
			} catch(IllegalArgumentException e) {
				LOG.info("Ignoring event >" + events[i] + "<. Event does not exist.");
			}
		}

		producer = KafkaProducerFactory.createProducer(clientId, bootstrapServers);
		mapper = new ObjectMapper();
	}

	@Override
	public void onEvent(Event event) {
		if(events.contains(event.getType())) {
			LOG.info("Received event of type: " + event.getType());

			try {
				LOG.info("Produce to topic: " + topic + " ...");
				ProducerRecord<String, String> record = new ProducerRecord<>(topic,
						mapper.writeValueAsString(event));
				Future<RecordMetadata> metaData = producer.send(record);
				RecordMetadata recordMetadata = metaData.get();
				LOG.info("Produced to topic: " + recordMetadata.topic());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		// ignore
	}

	@Override
	public void close() {
		// ignore
	}
}
