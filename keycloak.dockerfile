# https://www.keycloak.org/server/containers
# build the image
# docker build -t antiope -f keycloak.dockerfile .
FROM ubuntu:22.04 as plugins

RUN apt update
RUN apt install -y curl

RUN curl -sL https://github.com/inowattio/antiope/releases/download/0.1.46/antiope.jar -o /antiope.jar

FROM quay.io/keycloak/keycloak:23.0.0

COPY --from=plugins /antiope.jar ./opt/keycloak/providers/antiope.jar
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]