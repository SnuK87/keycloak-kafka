package com.github.snuk87.keycloak.kafka;

import java.util.Map;

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

    private Map<String, Object> kafkaProducerProperties;

    KafkaConfigService kafkaConfigService;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        if (instance == null) {
            instance = new KafkaEventListenerProvider(new KeycloakSessionHelper(session), kafkaConfigService,
                     new KafkaProducerInitializer(new KafkaStandardProducerFactory(),kafkaConfigService, kafkaProducerProperties));
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
        kafkaConfigService = new KafkaConfigService(config);
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
