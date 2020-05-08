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
	private String securityProtocol;
	private String saslMechanism;
	private String acks;
	private String sslTruststoreLocation;
	private String sslTruststorePassword;

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		if (instance == null) {
			instance = new KafkaEventListenerProvider(bootstrapServers, clientId, topicEvents, events,
					topicAdminEvents, saslUsername, saslPassword, saslMechanism, securityProtocol, acks, sslTruststoreLocation, sslTruststorePassword);
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
		saslUsername = config.get("saslUsername", "");
		saslPassword = config.get("saslPassword", "");
		sslTruststoreLocation = config.get("sslTruststoreLocation", "");
		sslTruststorePassword = config.get("sslTruststorePassword", "");
		saslMechanism = config.get("saslMechanism", "");
		acks = config.get("acks", "");
		switch(config.get("securityProtocol", "")) {
			case "Plaintext":
				securityProtocol = SecurityProtocol.PLAINTEXT.name;
				break;
			case "PLAINTEXT":
				securityProtocol = SecurityProtocol.PLAINTEXT.name;
				break;
			case "Ssl":
				securityProtocol = SecurityProtocol.SSL.name;
				break;
			case "SSL":
				securityProtocol = SecurityProtocol.SSL.name;
				break;
			case "SaslPlaintext":
				securityProtocol = SecurityProtocol.SASL_PLAINTEXT.name;
				break;
			case "SASL_PLAINTEXT":
				securityProtocol = SecurityProtocol.SASL_PLAINTEXT.name;
				break;
			case "SaslSsl":
				securityProtocol = SecurityProtocol.SASL_SSL.name;
				break;
			case "SASL_SSL":
				securityProtocol = SecurityProtocol.SASL_SSL.name;
				break;
			default:
				securityProtocol = "";
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
