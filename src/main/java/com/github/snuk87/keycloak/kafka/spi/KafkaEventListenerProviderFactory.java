package com.github.snuk87.keycloak.kafka.spi;

import java.util.Map;

import com.github.snuk87.keycloak.kafka.KafkaProducerConfig;
import com.github.snuk87.keycloak.kafka.KafkaStandardProducerFactory;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class KafkaEventListenerProviderFactory implements EventListenerProviderFactory {

	private static final Logger LOG = Logger.getLogger(KafkaEventListenerProviderFactory.class);
	private static final String ID = "kafka";

	private KafkaEventListenerProvider instance;

	private String bootstrapServers;
	private String topicEvents;
	private String topicAdminEvents;
	private String clientId;
	private String[] events;
	private Map<String, Object> kafkaProducerProperties;

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		if (instance == null) {
			instance = new KafkaEventListenerProvider(bootstrapServers, clientId, topicEvents, events, topicAdminEvents,
					kafkaProducerProperties, new KafkaStandardProducerFactory());
		}

		return instance;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(Scope config) {
		LOG.info("Init kafka module ...");
		topicEvents = config.get("topicEvents", System.getenv("KAFKA_TOPIC"));
		clientId = config.get("clientId", System.getenv("KAFKA_CLIENT_ID"));
		bootstrapServers = config.get("bootstrapServers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
		topicAdminEvents = config.get("topicAdminEvents", System.getenv("KAFKA_ADMIN_TOPIC"));

		String eventsString = config.get("events", System.getenv("KAFKA_EVENTS"));

		if (eventsString != null) {
			events = eventsString.split(",");
		}

		if (topicEvents == null) {
			throw new NullPointerException("topic must not be null.");
		}

		if (clientId == null) {
			throw new NullPointerException("clientId must not be null.");
		}

		if (bootstrapServers == null) {
			throw new NullPointerException("bootstrapServers must not be null");
		}

		if (events == null || events.length == 0) {
			events = new String[1];
			events[0] = "REGISTER";
		}

		kafkaProducerProperties = KafkaProducerConfig.init(config);
	}

	@Override
	public void postInit(KeycloakSessionFactory arg0) {
		// ignore
	}

	@Override
	public void close() {
		// ignore
	}
}
