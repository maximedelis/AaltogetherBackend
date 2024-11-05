# Build environment
FROM amazoncorretto:22-alpine-jdk AS builder

RUN cd /opt

COPY build.gradle settings.gradle gradlew ./
COPY src ./src
COPY gradle ./gradle

RUN chmod +x ./gradlew

RUN ./gradlew clean bootJar

# Minimal RE
FROM amazoncorretto:22-alpine-jdk

RUN apk upgrade --no-cache && \
apk add --no-cache postgresql-client bash openssl libgcc libstdc++ ncurses-libs
RUN cd /opt

COPY --from=builder ./build/libs/aaltogether-backend-0.0.1-SNAPSHOT.jar aaltogether-backend.jar

EXPOSE ${HOST_PORT}
EXPOSE ${SOCKET_PORT}

CMD ["java", "-jar", "aaltogether-backend.jar"]
