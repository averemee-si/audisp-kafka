#!/bin/sh
if [[ `id -u` -ne 0 ]] ; then
	 echo "Please run as root" ; exit 1 ;
fi

AUDISP_HOME=/opt/a2/agents/audisp

mkdir -p $AUDISP_HOME/lib
cp target/lib/commons-io-2.6.jar $AUDISP_HOME/lib
cp target/lib/kafka-clients-1.1.0.jar $AUDISP_HOME/lib
cp target/lib/log4j-1.2.17.jar $AUDISP_HOME/lib
cp target/lib/lz4-java-1.4.jar $AUDISP_HOME/lib
cp target/lib/slf4j-api-1.7.25.jar $AUDISP_HOME/lib
cp target/lib/snappy-java-1.1.7.1.jar $AUDISP_HOME/lib

cp audisp-kafka $AUDISP_HOME
cp audisp-kafka.conf $AUDISP_HOME
cp log4j.properties $AUDISP_HOME

chmod +x $AUDISP_HOME/audisp-kafka

