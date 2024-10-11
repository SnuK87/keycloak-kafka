package com.github.snuk87.keycloak.kafka;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.keycloak.models.*;
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

    private String extractUserId(String eventAsString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(eventAsString);
            String userId = rootNode.path("userId").asText(null); // null if not present

            if (userId != null) {
                LOG.info("Extracted userId: " + userId);
            } else {
                LOG.warn("Failed to extract realmId");
            }
            return userId;

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

    public UserInfo  getUserInfo(String eventAsString) {
        String userId = extractUserId(eventAsString);
        String realmId = extractRealmId(eventAsString);
        UserInfo userInfo = new UserInfo();

        AtomicReference<UserInfo> userRef = new AtomicReference<>(new UserInfo());
        KeycloakModelUtils.runJobInTransaction(keycloakSession.getKeycloakSessionFactory(), (KeycloakSession transactionalSession) -> {

            RealmProvider realmProvider = transactionalSession.realms();
            RealmModel realm = realmProvider.getRealm(realmId);
            UserProvider userProvider = transactionalSession.users();
            UserModel user = userProvider.getUserById(realm,userId);

            if(user != null){
                userInfo.setEmail(user.getEmail());
                userInfo.setFirstName(user.getFirstName());
                userInfo.setLastName(user.getLastName());
                userRef.set(userInfo);
            }

        });

        return userRef.get();

    }


}
