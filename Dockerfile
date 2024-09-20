# https://www.keycloak.org/server/containers
FROM quay.io/keycloak/keycloak:latest
COPY target/keycloak-kafka-1.2.0-jar-with-dependencies.jar /opt/keycloak/providers/keycloak-kafka-1.2.0-jar-with-dependencies.jar
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]