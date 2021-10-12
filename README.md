# Keycloak Kafka Module
Simple module for [Keycloak](https://www.keycloak.org/) to produce keycloak events to [Kafka](https://kafka.apache.org/).

- [Keycloak Kafka Module](#keycloak-kafka-module)
  * [Build](#build)
  * [Installation](#installation)
  * [Module Configuration](#module-configuration)
    + [Kafka client configuration](#kafka-client-configuration)
    + [Kafka client using secure connection](#kafka-client-using-secure-connection)
  * [Module Deployment](#module-deployment)
  * [Keycloak Configuration](#keycloak-configuration)
     + [Enable Events in keycloak](#enable-events-in-keycloak)
  * [Docker Container](#docker-container)  
  * [Sample Client](#sample-client)

**Tested with** 

Kafka version: `2.12-2.1.x`, `2.12-2.4.x`, `2.12-2.5.x`, `2.13-2.8`

Keycloak version: `4.8.3`, `6.0.x`, `7.0.0`, `9.0.x`, `10.0.x`, `13.0.x`, `14.0.x` `15.0.x`

Java version: `11`, `13`


## Build
You can simply use Maven to build the jar file. Thanks to the assembly plugin the build process will create a fat jar that includes all dependencies and makes the deployment quite easy.
Just use the following command to build the jar file.

```bash
mvn clean package
```

## Installation
First you need to build or [download](https://github.com/SnuK87/keycloak-kafka/releases) the keycloak-kafka module.

To install the module to your keycloak server you have to configure the module and deploy it.
If you deploy the module without configuration, your keycloak server will fail to start throwing a `NullPointerException`.

If you want to install the module manually as described in the initial version you can follow this [guide](https://github.com/SnuK87/keycloak-kafka/wiki/Manual-Installation).

## Module Configuration
Download the [CLI script](add-kafka-config.cli) from this repository and edit the properties to fit your environment. Also make sure to use the right
server config (line 1). As default the script will configure the module in the `standalone.xml`. (Be aware that the docker image uses the `standalone-ha.xml` by default)

The following properties can be set via environment variables (e.g. `${env.KAFKA_TOPIC}`) or as static values.

`topicEvents`: The name of the kafka topic to where the events will be produced to.

`clientId`: The `client.id` used to identify the client in kafka.

`bootstrapServer`: A comma separated list of available brokers.

`events`: The events that will be send to kafka.

`topicAdminEvents`: (Optional) The name of the kafka topic to where the admin events will be produced to. No events will be produced when this property isn't set.

A list of available events can be found [here](https://www.keycloak.org/docs/latest/server_admin/#event-types)

Run the CLI script using the following command and check the output on the console. You should see some server logs and lines of `{"outcome" => "success"}`.

```bash
$KEYCLOAK_HOME/bin/jboss-cli.sh --file=/path/to/add-kafka-config.cli
```

If you want to remove the configuration of the keycloak-kafka module from your server you can run [this](remove-kafka-config.cli).

###  Kafka client configuration
It's also possible to configure the kafka client by adding parameters to the cli script. This makes it possible to connect this module to a kafka broker that requires SSL/TLS connections.
For example to change the timeout of how long the producer will block the thread to 10 seconds you just have to add the following line to the cli script.

```
/subsystem=keycloak-server/spi=eventsListener/provider=kafka:map-put(name=properties,key=max.block.ms,value=10000)
```

Note the difference of `kafka:map-put` for kafka client parameters compared to `kafka:write-attribute` for module parameters.
A full list of available configurations can be found in the [official kafka docs](https://kafka.apache.org/documentation/#producerconfigs).

### Kafka client using secure connection
As mentioned above the kafka client can be configured through the cli script. To make the kafka open a SSL/TLS secured connection you can add the following lines to the script:

```
/subsystem=keycloak-server/spi=eventsListener/provider=kafka:map-put(name=properties,key=security.protocol,value=SSL)
/subsystem=keycloak-server/spi=eventsListener/provider=kafka:map-put(name=properties,key=ssl.truststore.location,value=kafka.client.truststore.jks)
/subsystem=keycloak-server/spi=eventsListener/provider=kafka:map-put(name=properties,key=ssl.truststore.password,value=test1234)
```

## Module Deployment
Copy the `keycloak-kafka-<version>-jar-with-dependencies.jar` into the `$KEYCLOAK_HOME/standalone/deployments` folder. Keycloak will automatically 
install the module with all it's dependencies on start up. To verify that the deployment of the module was successful you can check if a new file 
with the name `keycloak-kafka-<version>-jar-with-dependencies.jar.deployed` was created in the same folder. 


## Keycloak Configuration

### Enable Events in keycloak
1. Open administration console
2. Choose realm
3. Go to Events
4. Open `Config` tab and add `kafka` to Event Listeners.

![Admin console config](images/event_config.png)

## Docker Container
The simplest way to enable the kafka module in a docker container is to create a custom docker image from the [keycloak base image](https://hub.docker.com/r/jboss/keycloak/).
The `keycloak-kafka-<version>-jar-with-dependencies.jar` must be added to the `/standalone/deployments` folder and the CLI script must be added to the `/opt/jboss/startup-scripts/` folder
as explained in [Installation](#installation). The only difference is that the CLI script will be executed automatically on start up and doesn't have to be executed manually.
An example can be found in this [Dockerfile](Dockerfile).

## Sample Client

The following snippet shows a minimal Spring Boot Kafka client to consume keycloak events. Additional properties can be added to the `KeycloakEvent` class.

```java
@SpringBootApplication
@Log4j2
public class KafkaConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaConsumerApplication.class, args);
	}

	@KafkaListener(topics = "keycloak-events", groupId = "event-consumer")
	public void handleKeycloakEvent(KeycloakEvent event) {
		log.info("Consumed event: " + event);
	}

	@KafkaListener(topics = "keycloak-admin-events", groupId = "event-consumer")
	public void handleKeycloakAdminEvent(KeycloakAdminEvent event) {
		log.info("Consumed admin event: " + event);
	}

	@Bean
	public StringJsonMessageConverter jsonConverter() {
		return new StringJsonMessageConverter();
	}
}

@Data
class KeycloakEvent {
	private String userId;
	private String type;
}

@Data
class KeycloakAdminEvent {
	private String realmId;
	private String operationType;
}
```

## Contribution

Any kind of contributions are welcome.
