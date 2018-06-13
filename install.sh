#!/bin/sh
#
# Copyright (c) 2018-present, http://a2-solutions.eu
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is
# distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
# the License for the specific language governing permissions and limitations under the License.
#
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

cp target/audisp-kafka-0.1.0.jar $AUDISP_HOME
cp audisp-kafka $AUDISP_HOME
cp audisp-kafka.conf $AUDISP_HOME
cp log4j.properties $AUDISP_HOME

chmod +x $AUDISP_HOME/audisp-kafka

