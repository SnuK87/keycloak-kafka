# https://www.keycloak.org/server/containers
FROM quay.io/keycloak/keycloak:21.0.0
COPY ./target/keycloak-kafka-1.1.5-jar-with-dependencies.jar /opt/keycloak/providers/keycloak-kafka-1.1.5-jar-with-dependencies.jar
#RUN curl -sL https://github.com/SnuK87/keycloak-kafka/releases/download/1.1.5/keycloak-kafka-1.1.5-jar-with-dependencies.jar -o /opt/keycloak/providers/keycloak-kafka-1.1.5-jar-with-dependencies.jar
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]