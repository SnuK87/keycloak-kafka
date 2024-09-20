package com.github.snuk87.keycloak.kafka;

import java.util.Map;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;

public interface KafkaProducerFactory {

	Producer<String, SpecificRecordBase> createProducer(String clientId, String bootstrapServer,
														Map<String, Object> optionalProperties);

}
