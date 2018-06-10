# audisp-kafka

Linux audit event multiplexor (audisp) plugin for transferring audit event information to [Apache Kafka](http://kafka.apache.org/)

## Getting Started

These instructions will get you a copy of the project up and running on your linux box.

### Prerequisites

Before using audisp-kafka please check that required audit packages and Java8+ are installed with

```
yum list audit audit-libs audispd-plugins
echo "Checking Java version"
java -version
```


### Installing

Build with

```
mvn install
```
Then run as root supplied `install.sh` or run commands below

```
AUDISP_HOME=/opt/a2/agents/audisp

mkdir -p $AUDISP_HOME/lib
cp target/lib/commons-io-2.6.jar $AUDISP_HOME/lib
cp target/lib/kafka-clients-1.1.0.jar $AUDISP_HOME/lib
cp target/lib/log4j-1.2.17.jar $AUDISP_HOME/lib
cp target/lib/lz4-java-1.4.jar $AUDISP_HOME/lib
cp target/lib/slf4j-api-1.7.25.jar $AUDISP_HOME/lib
cp target/lib/snappy-java-1.1.7.1.jar $AUDISP_HOME/lib

cp target/audisp-kafka-0.1.0.jar $AUDISP_HOME
cp audisp-kafka $AUDISP_HOME
cp audisp-kafka.conf $AUDISP_HOME
cp log4j.properties $AUDISP_HOME

chmod +x $AUDISP_HOME/audisp-kafka
```

Edit `/etc/audisp/plugins.d/au-remote.conf`, this files should looks like

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

Edit `audisp-kafka.conf`, this files should looks like

```
a2.worker.count = 32
a2.kafka.servers = dwh.a2-solutions.eu:9092
a2.kafka.topic = audisp-test
a2.kafka.client.id = a2.audit.ai.linux
```
`a2.worker.count` - number of threads for transferring messaged to Kafka cluster
`a2.kafka.servers` - hostname/IP address and port of Kafka installation
`a2.kafka.topic` - value must match name of Kafka topic created on previous step
`a2.kafka.client.id` - use any valid string value for identifying this Kafka producer
 

## Running 

Add some rules to `/etc/audit/auditd.rules`, for example to audit all command issued by root (i.e. [EXECVE](http://man7.org/linux/man-pages/man2/execve.2.html) call) add

```
-a always,exit -F arch=b64 -S execve -F euid=0 -F key=root-commands
-a always,exit -F arch=b32 -S execve -F euid=0 -F key=root-commands
```
to audit other users activity add the same rules with different `euid` 
For auditing file system activity you can add for directory `/data/oracle/adump`

```
-w /data/oracle/adump -k rdbms-audit-file-watch
```
For other examples please see `man` pages, `unistd.h` and other relevant information sources.
Restart `auditd` with

```
service auditd restart
```
See Linux audit log at [Kafka](http://kafka.apache.org/)'s side with command line consumer

```
bin/kafka-console-consumer.sh --from-beginning --zookeeper localhost:2181 --topic audisp-test
```

## Deployment

Please size [Kafka](http://kafka.apache.org/)'s settings for production environment

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Statement of direction
Kerberos/SSL support

## Authors

* **Aleksej Veremeev** - *Initial work* - [A2 Solutions](http://a2-solutions.eu/)

## License

This project is licensed under the Apache License - see the [LICENSE](LICENSE) file for details

