package com.github.snuk87.keycloak.kafka.serializer.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificData;

public abstract class JacksonIgnoreAvroPropertiesMixIn {

    @JsonIgnore
    public abstract Schema getSchema();

    @JsonIgnore
    public abstract SpecificData getSpecificData();

}
