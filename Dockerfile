FROM openjdk:17 as builder
COPY . .
RUN ./mvnw clean package -DskipTests

FROM openjdk:17
COPY --from=builder target/due-management-0.0.1-SNAPSHOT.jar Nodue.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "Nodue.jar"]