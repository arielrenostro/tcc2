FROM maven:3.8.5-eclipse-temurin-11-focal

RUN mkdir /app
WORKDIR /app

COPY pom.xml pom.xml

RUN mvn clean install -T=12

COPY src src

RUN mvn clean install -T=12

ENTRYPOINT ["java", "-jar", "/app/target/chat-1.0-SNAPSHOT-jar-with-dependencies.jar"]
