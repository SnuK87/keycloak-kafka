package ai.atlaslabs.keycloak.kafka;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class KafkaEventListenerFactory implements EventListenerProviderFactory {

	private static final Logger LOG = Logger.getLogger(KafkaEventListenerFactory.class);

	private KafkaEventListenerProvider instance;

	private String bootstrapServers;
	private String topic;
	private String clientId;

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		if (instance == null) {
			instance = new KafkaEventListenerProvider(bootstrapServers, clientId, topic);
		}

		return instance;
	}

	@Override
	public String getId() {
		return "kafka";
	}

	@Override
	public void init(Scope config) {
		LOG.info("Init kafka module ...");
		topic = config.get("topic");
		clientId = config.get("clientId", "keycloak");
		bootstrapServers = config.get("bootstrapServers");

		if (topic == null) {
			throw new NullPointerException("topic must not be null.");
		}

		if (clientId == null) {
			throw new NullPointerException("clientId must not be null.");
		}

		if (bootstrapServers == null) {
			throw new NullPointerException("bootstrapServers must not be null");
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
