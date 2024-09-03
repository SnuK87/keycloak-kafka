package com.github.snuk87.keycloak.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.Config;
import java.util.Optional;
import java.util.Base64;

public class KafkaConfigService {
    private static final String DEFAULT_EVENT_REGISTER = "REGISTER";
    private static final String DEFAULT_EVENT_DELETE_ACCOUNT = "DELETE_ACCOUNT";
    private static final String DEFAULT_EVENT_LOGIN = "LOGIN";

    private final JsonNode configJson;
    private final String topicAdminEvents;
    private  String[] events;

    public KafkaConfigService( Config.Scope config ) {

        this.topicAdminEvents = getConfigValue(config, "topicAdminEvents", "KAFKA_ADMIN_TOPIC")
                .orElse(null);

        String base64Config = System.getenv("ANTIOPE_CONFIG");
        this.configJson = base64Config != null ? decodeBase64ToJson(base64Config) : null;

        String eventsString = getConfigValue(config, "events", "KAFKA_EVENTS").orElse(null);
        this.events = initializeEvents(eventsString);

    }

    private Optional<String> getConfigValue(Config.Scope config, String key, String envVariable) {
        return Optional.ofNullable(config.get(key, System.getenv(envVariable)));
    }

    private String[] initializeEvents(String eventsString) {
        if (eventsString != null && !eventsString.isEmpty()) {
            return eventsString.split(",");
        }
        return new String[]{DEFAULT_EVENT_REGISTER, DEFAULT_EVENT_DELETE_ACCOUNT, DEFAULT_EVENT_LOGIN};
    }

    private JsonNode decodeBase64ToJson(String base64String) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            String decodedString = new String(decodedBytes);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(decodedString);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode and parse KAFKA_CONFIG", e);
        }
    }

    public JsonNode getConfigJson() {
        return configJson;
    }

    public String getTopicAdminEvents() {
        return topicAdminEvents;
    }

    public String[] getEvents() {
        return events;
    }

}
