# Build environment
FROM amazoncorretto:22-alpine-jdk AS builder

RUN cd /opt

COPY build.gradle settings.gradle gradlew ./
COPY src ./src
COPY gradle ./gradle

RUN ./gradlew clean bootJar

# Minimal RE
FROM amazoncorretto:22-alpine-jdk

RUN cd /opt

COPY --from=builder ./build/libs/aaltogether-backend-0.0.1-SNAPSHOT.jar aaltogether-backend.jar

EXPOSE 8080

CMD ["java", "-jar", "aaltogether-backend.jar"]
