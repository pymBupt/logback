#!/usr/bin/env bash
mvn clean install -DskipTests -N -U;
if [ $? -ne 0 ]; then exit; fi

mvn clean install -DskipTests -U -f logback-core/pom.xml
if [ $? -ne 0 ]; then exit; fi

mvn clean install -DskipTests -U -f logback-classic/pom.xml
if [ $? -ne 0 ]; then exit; fi