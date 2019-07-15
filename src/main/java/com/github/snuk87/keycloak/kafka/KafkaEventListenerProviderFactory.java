package com.github.snuk87.keycloak.kafka;

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
	private String topic;
	private String clientId;
	private String[] events;

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		if (instance == null) {
			instance = new KafkaEventListenerProvider(bootstrapServers, clientId, topic, events);
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
		topic = config.get("topic");
		clientId = config.get("clientId", "keycloak");
		bootstrapServers = config.get("bootstrapServers");

		String eventsString = config.get("events");
		events = eventsString.split(",");

		if (topic == null) {
			throw new NullPointerException("topic must not be null.");
		}

		if (clientId == null) {
			throw new NullPointerException("clientId must not be null.");
		}

		if (bootstrapServers == null) {
			throw new NullPointerException("bootstrapServers must not be null");
		}

		if(events == null || events.length == 0) {
			events = new String[1];
			events[0] = "REGISTER";
		}
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
