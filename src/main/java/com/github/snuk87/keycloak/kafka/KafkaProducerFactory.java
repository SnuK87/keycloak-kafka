package com.github.snuk87.keycloak.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.clients.CommonClientConfigs;

public final class KafkaProducerFactory {

	private KafkaProducerFactory() {

	}

	public static Producer<String, String> createProducer(String clientId, String bootstrapServer, String saslUsername, String saslPassword, String saslMechanism, String securityProtocol, String acks, String sslTruststoreLocation, String sslTruststorePassword) {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		if(!securityProtocol.isEmpty()){
			props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
		}
		if(WithSasl(securityProtocol) && !saslMechanism.isEmpty() && !saslUsername.isEmpty() && !saslPassword.isEmpty()) {
			props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
			if(saslMechanism.equals("SCRAM-SHA-512") || saslMechanism.equals("SCRAM-SHA-256")) {
				props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + saslUsername + "\" password=\"" + saslPassword + "\";");
			}
			if(saslMechanism.equals("PLAIN") || saslMechanism.equals("GSSAPI")) {
				props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + saslUsername + "\" password=\"" + saslPassword + "\";");
			}
		}
		if(WithSsl(securityProtocol) && !sslTruststoreLocation.isEmpty() && !sslTruststorePassword.isEmpty()) {
			props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocation);
			props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePassword);
		}
		if(!acks.isEmpty()){
			props.put(ProducerConfig.ACKS_CONFIG, acks);
		}

		// fix Class org.apache.kafka.common.serialization.StringSerializer could not be
		// found. see https://stackoverflow.com/a/50981469
		Thread.currentThread().setContextClassLoader(KafkaProducerFactory.class.getClassLoader());
		return new KafkaProducer<>(props);
	}

	private static boolean WithSasl(String securityProtocol) {
		return securityProtocol.equals("SASL_PLAINTEXT") || securityProtocol.equals("SASL_SSL");
	}

	private static boolean WithSsl(String securityProtocol) {
		return securityProtocol.equals("SSL") || securityProtocol.equals("SASL_SSL");
	}
}
