#FROM openjdk:8
#COPY cb-pores-service-0.0.1-SNAPSHOT.jar /opt/
#CMD ["java", "-XX:+PrintFlagsFinal", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "/opt/cb-pores-service-0.0.1-SNAPSHOT.jar"]


# Step 1: Use Maven image to build the Spring Boot application
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline
# Copy the entire project
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Use a lightweight OpenJDK image to run the application
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Copy all environment files
#COPY ./env-files /env-files/
# Default to no specific .env file, requiring it to be specified at runtime
#ENV ENV_FILE=/outbound/src/main/resources/default.env
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+PrintFlagsFinal"
EXPOSE 7001
#ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:+PrintFlagsFinal", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
ENTRYPOINT ["/bin/sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]

