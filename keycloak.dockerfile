# https://www.keycloak.org/server/containers
<<<<<<< Updated upstream
FROM quay.io/keycloak/keycloak:21.0.0
COPY ./target/antiope-jar-with-dependencies.jar /opt/keycloak/providers/antiope-jar-with-dependencies.jar
#RUN curl -sL https://github.com/SnuK87/keycloak-kafka/releases/download/1.1.5/keycloak-kafka-1.1.5-jar-with-dependencies.jar -o /opt/keycloak/providers/keycloak-kafka-1.1.5-jar-with-dependencies.jar
=======
# build the image
## docker build -t antiope -f keycloak.dockerfile .
#FROM ubuntu:22.04 as plugins
#
#RUN apt update
#RUN apt install -y curl
#
##RUN curl -sL https://github.com/inowattio/antiope/releases/download/0.1.46/antiope.jar -o /antiope.jar

FROM quay.io/keycloak/keycloak:23.0.0

#COPY --from=plugins /antiope.jar ./opt/keycloak/providers/antiope.jar
COPY target/antiope-jar-with-dependencies.jar ./opt/keycloak/providers/antiope-jar-with-dependencies.jar

>>>>>>> Stashed changes
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]