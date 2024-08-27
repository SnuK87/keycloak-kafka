package com.github.snuk87.keycloak.kafka;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.util.concurrent.atomic.AtomicReference;

public class KeycloakSessionHelper {

    private static final Logger LOG = Logger.getLogger(KeycloakSessionHelper.class);
    private final KeycloakSession keycloakSession;

    public KeycloakSessionHelper(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }


    private String extractRealmId(String eventAsString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(eventAsString);
            String realmId = rootNode.path("realmId").asText(null); // null if not present

            if (realmId != null) {
                LOG.info("Extracted realmId: " + realmId);
            } else {
                LOG.warn("Failed to extract realmId");
            }
            return realmId;

        } catch (JsonProcessingException e) {
            LOG.error("Failed to parse event JSON", e);
            return null;
        }

    }

    public String getRealmName(String eventAsString) {
        String realmId = extractRealmId(eventAsString);
        AtomicReference<String> realmNameRef = new AtomicReference<>(null);
        KeycloakModelUtils.runJobInTransaction(keycloakSession.getKeycloakSessionFactory(), (KeycloakSession transactionalSession) -> {

            RealmProvider realmProvider = transactionalSession.realms();
            RealmModel realm = realmProvider.getRealm(realmId);
            if (realm != null) {
                realmNameRef.set(realm.getName());
            }
        });
        return realmNameRef.get();
    }


}
