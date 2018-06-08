# audisp-kafka

Linux audit event multiplexor (audisp) plugin for transferring audit event information to [Apache Kafka](http://kafka.apache.org/)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Before using audisp-kafka please check that required audit packages and Java8+ are installed with

```
yum list audit audit-libs audispd-plugins
echo "Checking Java version"
java -version
```


### Installing

After building audisp-kafka with maven - TODO
Edit /etc/audisp/plugins.d/au-remote.conf, this files should looks like

```
active = yes
direction = out
path = /opt/a2/agents/audisp/audisp-kafka
type = always
format = string

```
Create Kafka's server topic for audit test with a single partition and only one replica: 

```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic audisp-test
```

Edit audisp-kafka.conf, this files should looks like

```
a2.worker.count = 32
a2.kafka.servers = dwh.a2-solutions.eu:9092
a2.kafka.topic = audisp-test
a2.kafka.client.id = a2.audit.ai.linux
```

End with an example of getting some data out of the system or using it for a little demo

## Running the tests

Explain how to run the automated tests for this system

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Aleksej Veremeev** - *Initial work* - [A2 Solutions](http://a2-solutions.eu/)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
