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

	private List<String> adminEventResourceTypes;

	private List<String> adminEventOperationTypes;

	private List<String> adminStrictEventTypes;

	public KafkaEventListenerProvider(String bootstrapServers, String clientId, String topicEvents, String[] events,
			String topicAdminEvents, String[] adminEventResourceTypes, String[] adminEventOperationTypes, String[] adminStrictEventTypes,
			Map<String, Object> kafkaProducerProperties, KafkaProducerFactory factory) {
		this.topicEvents = topicEvents;
		this.events = new ArrayList<>();
		this.topicAdminEvents = topicAdminEvents;
		this.adminEventResourceTypes = new ArrayList<>();
		this.adminEventOperationTypes = new ArrayList<>();
		this.adminStrictEventTypes = new ArrayList<>();

		for (String event : events) {
			try {
				EventType eventType = EventType.valueOf(event.toUpperCase());
				this.events.add(eventType);
			} catch (IllegalArgumentException e) {
				LOG.debug("Ignoring event >" + event + "<. Event does not exist.");
			}
		}
		for (String operationType : adminEventOperationTypes) {
		  this.adminEventOperationTypes.add(operationType);
		}
    for (String resourceType : adminEventResourceTypes) {
		  this.adminEventResourceTypes.add(resourceType);
		}
    for (String strictEventType : adminStrictEventTypes) {
		  this.adminStrictEventTypes.add(strictEventType);
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

  private boolean shouldProcessAdminEvent(AdminEvent event) {
    if (topicAdminEvents == null) {
      return false;
    }

    // Strict matching: both operation type AND resource type must match
    if (adminStrictEventTypes != null && !adminStrictEventTypes.isEmpty()) {
      return matchesStrictEventTypes(event);
    }

    // Flexible matching: either operation type OR resource type can match
    return matchesFlexibleEventTypes(event);
  }

  private boolean matchesStrictEventTypes(AdminEvent event) {
    if (event.getOperationType() == null) {
      return false;
    }

    String eventOperationType = event.getOperationType().name();
    String eventResourceType = event.getResourceTypeAsString();

    for (String eventType : adminStrictEventTypes) {
      String[] parts = eventType.split("__", 2); // Limit split to 2 parts
      if (parts.length < 2) {
        continue;
      }

      if (parts[0].equals(eventOperationType) && parts[1].equals(eventResourceType)) {
        return true;
      }
    }

    return false;
  }

  private boolean matchesFlexibleEventTypes(AdminEvent event) {
    boolean resourceTypesNotSet = adminEventResourceTypes == null || adminEventResourceTypes.isEmpty();
    boolean operationTypesNotSet = adminEventOperationTypes == null || adminEventOperationTypes.isEmpty();

    // If both filters are empty, accept all events
    if (resourceTypesNotSet && operationTypesNotSet) {
      return true;
    }

    boolean resourceTypeMatches = !resourceTypesNotSet &&
                                  adminEventResourceTypes.contains(event.getResourceTypeAsString());

    boolean operationTypeMatches = !operationTypesNotSet &&
                                   event.getOperationType() != null &&
                                   adminEventOperationTypes.contains(event.getOperationType().name());

    // Accept if either filter matches (when only one filter is set) or both match (when both are set)
    return (resourceTypesNotSet && operationTypeMatches) ||
           (operationTypesNotSet && resourceTypeMatches) ||
           (resourceTypeMatches && operationTypeMatches);
  }

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		if (shouldProcessAdminEvent(event)) {
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
