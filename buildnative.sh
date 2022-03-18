#! /bin/bash
export GRAALVM_HOME=/opt/graalvm-ce-java11-22.0.0.2
export JAVA_HOME=${GRAALVM_HOME}
export PATH=${GRAALVM_HOME}/bin:$PATH
cp web.xml.prod src/main/resources/META-INF/web.xml
./mvnw clean generate-sources
sleep 10
./mvnw compile
./mvnw package -Pnative
cp web.xml.dev src/main/resources/META-INF/web.xml
sudo docker image rm barais/grade-scope-istic
sudo docker build -f src/main/docker/Dockerfile.native -t barais/grade-scope-istic .

