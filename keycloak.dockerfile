# https://www.keycloak.org/server/containers
FROM quay.io/keycloak/keycloak:19.0.3
RUN curl -sL https://github.com/SnuK87/keycloak-kafka/releases/download/1.1.1/keycloak-kafka-1.1.1-jar-with-dependencies.jar -o /opt/keycloak/providers/keycloak-kafka-1.1.1-jar-with-dependencies.jar
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]