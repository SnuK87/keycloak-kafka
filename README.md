# Keycloak Kafka Module
Simple module for [Keycloak](https://www.keycloak.org/) to produce JSON events to [Kafka](https://kafka.apache.org/) when a `REGISTER` event in Keycloak was triggered.
The payload of the kafka event looks like:

`{"userId":"f3e3988a-63eb-49c6-932f-482f37cae74e","email":"testi@mctestface.com"}`


Tested with 

Kafka version: `2.12-2.1.0`
 
Keycloak version: `4.8.3`

Java version: `11`


## Installation

Add a new provider in `standalone.xml` under `<subsystem xmlns="urn:jboss:domain:keycloak-server:1.1">`.

```xml
            <providers>
                <provider>classpath:${jboss.home.dir}/providers/*</provider>
                <provider>module:com.github.snuk87.keycloak.keycloak-kafka</provider>
            </providers>
```


Create a new folder in `$KEYCLOAK_HOME/modules/system/layers/keycloak/ai/atlaslabs/keycloak/keycloak-kafka/main`

Copy `keycloak-kafka-1.0.0.jar` into the `main` folder and create a new file `module.xml` with the following content:

```xml
<?xml version="1.0" ?>
<module xmlns="urn:jboss:module:1.3" name="com.github.snuk87.keycloak.keycloak-kafka">
 <resources>
  <resource-root path="keycloak-kafka-1.0.0.jar" />
 </resources>
 <dependencies>
  <module name="org.keycloak.keycloak-core"/>
  <module name="org.keycloak.keycloak-server-spi"/>
  <module name="org.keycloak.keycloak-server-spi-private"/>
  <module name="com.fasterxml.jackson.core.jackson-core"/>
  <module name="com.fasterxml.jackson.core.jackson-databind"/>
  <module name="org.jboss.logging"/>
  <module name="org.apache.kafka.kafka-clients"/>
 </dependencies>
</module>
```

Create a new folder in `$KEYCLOAK_HOME/modules/system/layers/keycloak/org/apache/kafka/kafka-clients/main`
and create a new file `module.xml`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.0" name="org.apache.kafka.kafka-clients">
    <resources>
        <resource-root path="kafka-clients-2.2.0.jar"/>
        <resource-root path="lz4-java-1.5.0.jar"/>
        <resource-root path="snappy-java-1.1.7.2.jar"/>
    </resources>
    <dependencies>
        <module name="org.slf4j"/>
    </dependencies>
</module>
```

Download the .jar files listed under `<resources>` from [MVN Repository](https://mvnrepository.com/) and put them into the same folder.

```
.
├── kafka-clients-2.2.0.jar
├── lz4-java-1.5.0.jar
├── module.xml
├── snappy-java-1.1.7.2.jar
└── zstd-jni-1.3.8-1.jar
```

## Configuration

### Enable Events
1. Open administration console
2. Choose realm
3. Go to Events
4. Open `Config` tab and add `kafka` to Event Listeners. If you can't choose `kafka` you have to restart the keycloak server first.


### Kafka module
Add the following content to your `standalone.xml`:

```xml
            <spi name="eventsListener">
                <provider name="kafka" enabled="true">
                    <properties>
                        <property name="topic" value="keycloak"/>
                        <property name="clientId" value="keycloak"/>
                        <property name="bootstrapServers" value="192.168.0.1:9092,192.168.0.2:9092"/>
                    </properties>
                </provider>
            </spi>
```

`topic`: The name of the kafka topic to where the events will be produced to.

`clientId`: The `client.id` used to identify the client in kafka.

`bootstrapServer`: A comma separated list of available brokers.