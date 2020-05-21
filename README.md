# Keycloak Kafka Module
Simple module for [Keycloak](https://www.keycloak.org/) to produce keycloak events to [Kafka](https://kafka.apache.org/).

- [Keycloak Kafka Module](#keycloak-kafka-module)
  * [Build](#build)
  * [Installation](#installation)
  * [Configuration](#configuration)
    + [Enable Events in keycloak](#enable-events-in-keycloak)
    + [Kafka module](#kafka-module)
  * [Docker Container](#configuration)  
  * [Sample Client](#sample-client)

**Tested with** 

Kafka version: `2.12-2.1.x`, `2.12-2.4.x`, `2.12-2.5.x`

Keycloak version: `4.8.3`, `6.0.x`, `7.0.0`, `9.0.x`, `10.0.x`

Java version: `11`, `13`


## Build

`mvn clean package`

## Installation
To install the keycloak-kafka module to your keycloak server you have to first configure the module and then deploy the module.
If you deploy the module without configuration your keycloak server will fail to start up with a NullPointerException.

### Module configuration
Download the [CLI script](kafka-module.cli) from this repository and edit the properties to fit your environment. Also make sure that you use the right
server config (line 1). As a default the script will change the `standalone.xml`.

Currently the following properties are available and should be changed to  fit your environemnt:

`topicEvents`: The name of the kafka topic to where the events will be produced to.

`clientId`: The `client.id` used to identify the client in kafka.

`bootstrapServer`: A comma separated list of available brokers.

`events`: (Optional; default=REGISTER) The events that will be send to kafka.

`topicAdminEvents`: (Optional) The name of the kafka topic to where the admin events will be produced to.

Run the CLI script using the following command and check the output on the console. You should see some server logs and 6 lines of `{"outcome" => "success"}`.
```bash
$KEYCLOAK_HOME/bin/jboss-cli.sh --file=/path/to/kafka-module.cli
```

### Module deployment
Copy the `keycloak-kafka-<version>-jar-with-dependencies.jar` into the $KEYCLOAK_HOME/standalone/deployments folder. Keycloak will automatically 
install the module with all dependencies on start up. To verify that the deployment of the module was successful you can check if a new file 
with the name `keycloak-kafka-<version>-jar-with-dependencies.jar.deployed` was created in the same folder. 

## Keycloak Configuration

### Enable Events in keycloak
1. Open administration console
2. Choose realm
3. Go to Events
4. Open `Config` tab and add `kafka` to Event Listeners.


## Docker Container
The simplest way to enable the kafka module in a docker container is to create a custom docker image from the [keycloak base image](https://hub.docker.com/r/jboss/keycloak/).
The `keycloak-kafka-<version>-jar-with-dependencies.jar` must be added to the `/standalone/deployments` folder and the CLI script must be added to the `/opt/jboss/startup-scripts/` folder
as explained in [Installation](#installation). The only difference is that the CLI script will be executed automatically in start up and doesn't have to be executed manually.
An example can be found in this [Dockerfile](Dockerfile).

## Sample Client

The following snippet shows a minimal Spring Boot Kafka client to consume keycloak events. Additional properties can be added to `KeycloakEvent`.

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