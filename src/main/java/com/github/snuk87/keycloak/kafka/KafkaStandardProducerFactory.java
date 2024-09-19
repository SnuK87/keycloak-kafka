package com.github.snuk87.keycloak.kafka;

import java.util.Map;
import java.util.Properties;

import com.github.snuk87.keycloak.kafka.serializer.JsonSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KafkaStandardProducerFactory implements KafkaProducerFactory {

	private static final Logger log = LoggerFactory.getLogger(KafkaStandardProducerFactory.class);

	@Override
	public Producer<String, Object> createProducer(String clientId, String bootstrapServer,
			Map<String, Object> optionalProperties) {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
		props.putAll(optionalProperties);

		return new KafkaProducer<>(props);
	}
}
