# Aaltogether Backend

Aaltogether's incredible backend. Written in Java using Spring Boot.

Stay tuned. 

## Getting Started

The project can be run in three ways:
1. Using `docker compose` to run a PostgreSQL instance and the backend
2. Using a local PostgreSQL instance and running the backend
3. Running the backend only (in memory database)

**For method 1 and 2**, please generate a `.env` file in the root directory of the project. The `.env` file should contain the following variables:
 
```bash
DB_URL=jdbc:postgresql://<DB_IP>:5432/postgres # put "postgresql" as the DB_IP if you wanna use docker compose, "localhost" otherwise
DB_USER=postgres
DB_PASSWORD=password
DB_DRIVER=org.postgresql.Driver

HOST_IP=127.0.0.1

HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect

MAIL_USERNAME=<MAILTRAP_EMAIL>
MAIL_PASSWORD=<MAILTRAP_PASSWORD>
```

### 1. Using `docker compose`

Requirements:
- `docker` and `docker compose` 

Run the following command to start the backend and a PostgreSQL instance:

```bash
sudo docker compose up
```

Add `-d` to run in detached mode

### 2. Using a local PostgreSQL instance

Requirements:
- JDK 22
- `docker`

If you don't want to use `docker compose`, you can run the following command to create a local PostgreSQL instance:

```bash
sudo docker run -e POSTGRES_PASSWORD='password' -e POSTGRES_USER='postgres' -e POSTGRES_DB='postgres' -p 5432:5432 postgres
```

Then, run the backend using the following command:

```bash
./gradlew bootRun
```

### 3. Running the backend only 

Requirements:
- JDK 22

Run the backend using the following command:

```bash
./gradlew bootRun
```