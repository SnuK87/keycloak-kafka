package ai.atlaslabs.keycloak.kafka;

import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaEventListenerProvider implements EventListenerProvider {

	private static final Logger LOG = Logger.getLogger(KafkaEventListenerProvider.class);

	private String topic;

	private Producer<String, String> producer;

	private ObjectMapper mapper;

	public KafkaEventListenerProvider(String bootstrapServers, String clientId, String topic) {
		this.topic = topic;
		producer = KafkaProducerFactory.createProducer(clientId, bootstrapServers);
		mapper = new ObjectMapper();
	}

	@Override
	public void onEvent(Event event) {
		if (event.getType() == EventType.REGISTER) {
			LOG.info("Received event");

			RegisterEvent registerEvent = new RegisterEvent(event.getUserId(), event.getDetails().get("email"));

			try {
				LOG.info("Produce to topic: " + topic + " ...");
				ProducerRecord<String, String> record = new ProducerRecord<>(topic,
						mapper.writeValueAsString(registerEvent));
				Future<RecordMetadata> metaData = producer.send(record);
				RecordMetadata recordMetadata = metaData.get();
				LOG.info("Produced to topic: " + recordMetadata.topic());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		// ignore
	}

	@Override
	public void close() {
		// ignore
	}
}
