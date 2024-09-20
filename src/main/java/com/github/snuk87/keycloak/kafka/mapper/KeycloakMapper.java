package com.github.snuk87.keycloak.kafka.mapper;

import com.github.snuk87.keycloak.kafka.dto.AuthDetails;
import com.github.snuk87.keycloak.kafka.dto.KeycloakAdminEvent;
import com.github.snuk87.keycloak.kafka.dto.KeycloakEvent;
import com.github.snuk87.keycloak.kafka.dto.OperationType;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import java.util.Map;
import java.util.Optional;

public class KeycloakMapper {

    public static KeycloakAdminEvent mapEventToKeycloakEvent(final AdminEvent event){
        return KeycloakAdminEvent.newBuilder()
                .setId(event.getId())
                .setTime(event.getTime())
                .setRealmId(event.getRealmId())
                .setRealmName(event.getRealmName())
                .setError(event.getError())
                .setResourceType(event.getResourceTypeAsString())
                .setResourcePath(event.getResourcePath())
                .setRepresentation(event.getRepresentation())
                .setOperationType(OperationType.valueOf(Optional.ofNullable(event.getOperationType()).map(ot -> ot.toString()).orElse(OperationType.ACTION.toString())))
                .setAuthDetails(
                        Optional.ofNullable(event.getAuthDetails()).map(authDetails -> AuthDetails.newBuilder()
                                .setClientId(authDetails.getClientId())
                                .setIpAddress(authDetails.getIpAddress())
                                .setRealmId(authDetails.getRealmId())
                                .setRealmName(authDetails.getRealmName())
                                .setUserId(authDetails.getUserId())
                                .build())
                                .orElse(null)
                ).build();
    }

    public static KeycloakEvent mapEventToKeycloakEvent(final Event event){
        return KeycloakEvent.newBuilder()
                .setId(event.getId())
                .setTime(event.getTime())
                .setRealmId(event.getRealmId())
                .setRealmName(event.getRealmName())
                .setError(event.getError())
                .setClientId(event.getClientId())
                .setUserId(event.getUserId())
                .setSessionId(event.getSessionId())
                .setIpAddress(event.getIpAddress())
                .setDetails(event.getDetails())
                .build();
    }

}
