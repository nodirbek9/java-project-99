FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Сначала только то, что нужно Gradle — чтобы слой с зависимостями кешировался
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

COPY build.gradle settings.gradle ./
COPY config ./config
COPY src ./src

# bootJar не тянет за собой test и checkstyle — сборка образа быстрая
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
