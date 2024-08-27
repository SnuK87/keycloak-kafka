package com.github.snuk87.keycloak.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.Producer;
import org.jboss.logging.Logger;

import java.util.*;

public class KafkaProducerInitializer {

    private static final Logger LOG = Logger.getLogger(KafkaProducerInitializer.class);

    private final KafkaProducerFactory factory;
    private final KafkaConfigService kafkaConfigService;
    private final Map<String, Object> kafkaProducerProperties;
    private final Map<String, Producer<String, String>> kafkaProducers = new HashMap<>();
    private final Map<String, List<String>> kafkaTopics = new HashMap<>();

    public KafkaProducerInitializer(KafkaProducerFactory factory, KafkaConfigService kafkaConfigService,
                                    Map<String, Object> kafkaProducerProperties) {

        this.factory = Objects.requireNonNull(factory, "KafkaProducerFactory must not be null");
        this.kafkaConfigService = Objects.requireNonNull(kafkaConfigService, "KafkaConfigService must not be null");
        this.kafkaProducerProperties = Objects.requireNonNull(kafkaProducerProperties, "KafkaProducerProperties must not be null");
    }

    public void initialize() {
        try {
            if (kafkaConfigService.getConfigJson().isArray()) {
                for (JsonNode node : kafkaConfigService.getConfigJson()) {
                    String realmName = node.get("realmName").asText();
                    String brokerIp = node.get("brokerIp").asText();
                    String brokerPort = node.get("brokerPort").asText();
                    String topic = node.get("topic").asText();

                    Producer<String, String> producer = factory.createProducer(
                            kafkaConfigService.getClientId() + "_" + realmName, brokerIp + ":" + brokerPort, kafkaProducerProperties);

                    kafkaProducers.put(realmName, producer);

                    List<String> topics = new ArrayList<>();
                    topics.add(topic);
                    kafkaTopics.put(realmName, topics);

                    LOG.info("Initialized producer for realm: " + realmName + " with topic: " + topic);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to initialize Kafka producers", e);
        }
    }

    public  List<String>  getKafkaTopicsByRealmName (String realmName)
    {
        return kafkaTopics.get(realmName);
    }

    public  Producer<String, String>  getKafkaProducerByRealmName (String realmName)
    {
        return kafkaProducers.get(realmName);
    }
}
