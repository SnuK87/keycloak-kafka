package com.github.snuk87.keycloak.kafka;

import org.keycloak.kafka.dto.KeycloakAdminEvent;
import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaUtils;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KeycloakEventAvroSerializerTest {

    @Test
    void eventCanBeSerialized() throws Exception {

        final AdminEvent adminevent = new AdminEvent();
        adminevent.setOperationType(OperationType.CREATE);

        final Event event = new Event();
        event.setType(EventType.REGISTER);

        final Schema schema1 = AvroSchemaUtils.getSchema(adminevent, true, false, false);
        final Schema schema2= AvroSchemaUtils.getSchema(event, true, true, true);


        System.out.println(schema1);
        System.out.println(schema2);

        assertNotNull(schema1);
        assertNotNull(schema2);
    }

}
