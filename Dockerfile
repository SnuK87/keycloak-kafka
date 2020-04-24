FROM jboss/keycloak:9.0.3

ADD ./keycloak-module/keycloak-kafka-1.0.0.jar /opt/jboss/keycloak/modules/system/layers/keycloak/com/github/snuk87/keycloak/keycloak-kafka/main/
ADD ./keycloak-module/module.xml /opt/jboss/keycloak/modules/system/layers/keycloak/com/github/snuk87/keycloak/keycloak-kafka/main/
ADD ./kafka-clients/kafka-clients-2.2.0.jar /opt/jboss/keycloak/modules/system/layers/keycloak/org/apache/kafka/kafka-clients/main/
ADD ./kafka-clients/lz4-java-1.5.0.jar /opt/jboss/keycloak/modules/system/layers/keycloak/org/apache/kafka/kafka-clients/main/
ADD ./kafka-clients/snappy-java-1.1.7.2.jar /opt/jboss/keycloak/modules/system/layers/keycloak/org/apache/kafka/kafka-clients/main/
ADD ./kafka-clients/zstd-jni-1.3.8-1.jar /opt/jboss/keycloak/modules/system/layers/keycloak/org/apache/kafka/kafka-clients/main/
ADD ./kafka-clients/module.xml /opt/jboss/keycloak/modules/system/layers/keycloak/org/apache/kafka/kafka-clients/main/

ADD kafka-module.cli /opt/jboss/startup-scripts/

#ADD realm-export.json /init/

# to prevent 'java.nio.file.DirectoryNotEmptyException: /opt/jboss/keycloak/standalone/configuration/standalone_xml_history/current' on startup
RUN rm -rf /opt/jboss/keycloak/standalone/configuration/standalone_xml_history

# from base image
USER 1000

EXPOSE 8080
EXPOSE 8443

ENTRYPOINT [ "/opt/jboss/tools/docker-entrypoint.sh" ]

CMD ["-b", "0.0.0.0", "-Dkeycloak.import=/init/realm-export.json"]