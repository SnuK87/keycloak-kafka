package com.github.snuk87.keycloak.kafka;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.apache.kafka.common.security.auth.SecurityProtocol;

public class KafkaEventListenerProviderFactory implements EventListenerProviderFactory {

	private static final Logger LOG = Logger.getLogger(KafkaEventListenerProviderFactory.class);
	private static final String ID = "kafka";

	private KafkaEventListenerProvider instance;

	private String bootstrapServers;
	private String topicEvents;
	private String topicAdminEvents;
	private String clientId;
	private String[] events;
	private String saslUsername;
	private String saslPassword;
	private String saslProtocol;
	private String saslMechanism;
	private String sslTruststoreLocation;
	private String sslTruststorePassword;

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		if (instance == null) {
			instance = new KafkaEventListenerProvider(bootstrapServers, clientId, topicEvents, events,
					topicAdminEvents, saslUsername, saslPassword, saslMechanism, saslProtocol, sslTruststoreLocation, sslTruststorePassword);
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
		topicEvents = config.get("topicEvents");
		clientId = config.get("clientId", "keycloak");
		bootstrapServers = config.get("bootstrapServers");
		topicAdminEvents = config.get("topicAdminEvents");
		saslUsername = config.get("saslUsername");
		saslPassword = config.get("saslPassword");
		sslTruststoreLocation = config.get("sslTruststoreLocation");
		sslTruststorePassword = config.get("sslTruststorePassword");
		switch(config.get("saslProtocol")) {
			case "Plaintext":
				saslProtocol = SecurityProtocol.PLAINTEXT.name;
				break;
			case "Ssl":
				saslProtocol = SecurityProtocol.SSL.name;
				break;
			case "SaslPlaintext":
				saslProtocol = SecurityProtocol.SASL_PLAINTEXT.name;
				break;
			case "SaslSsl":
				saslProtocol = SecurityProtocol.SASL_SSL.name;
				break;
			default:
				saslProtocol = null;
		}
		switch(saslProtocol) {
			case "SASL_PLAINTEXT":
			case "SASL_SSL":
				saslMechanism = "SCRAM-SHA-512";
				break;
			default:
				saslMechanism = null;
				break;
		}

		String eventsString = config.get("events");

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

		if (saslUsername == null) {
			throw new NullPointerException("saslUsername must not be null");
		}

		if (saslPassword == null) {
			throw new NullPointerException("saslPassword must not be null");
		}

		if (saslProtocol == null) {
			throw new NullPointerException("saslProtocol must not be null. Options: Plaintext, Ssl, SaslPlaintext, SaslSsl");
		}

		if (saslMechanism == null) {
			throw new NullPointerException("saslMechanism must not be null");
		}
	
		if (sslTruststoreLocation == null) {
			throw new NullPointerException("sslTruststoreLocation must not be null");
		}

		if (sslTruststorePassword == null) {
			throw new NullPointerException("sslTruststorePassword must not be null");
		}

		if (events == null || events.length == 0) {
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
