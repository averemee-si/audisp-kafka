#!/bin/sh
JAVA_HOME=/hadoop/jdk1.8.0_144
PATH=$JAVA_HOME/bin:${PATH}
A2_AGENT_HOME=/opt/a2/agents/audisp

export JAVA_HOME PATH

nohup $JAVA_HOME/bin/java \
    -cp $(for i in $A2_AGENT_HOME/lib/*.jar ; do echo -n $i: ; done)$A2_AGENT_HOME/a2agent.jar \
    -Da2.log4j.configuration=$A2_AGENT_HOME/log4j.properties \
    eu.solutions.a2.audit.linux.audisp.AudispKafka </dev/null 2>&1 | tee AudispKafka.log &

