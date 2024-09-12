# Aaltogether Backend

Aaltogether's incredible backend. Written in Java using Spring Boot.

Stay tuned. 

## Getting Started

### Requirements

- Docker & docker compose: `sudo apt install docker.io docker-compose-v2`
- Pull the Official PostgreSQL image: `sudo docker pull postgres`
- Make sure OpenJDK 22 is installed on your machine (IntelliJ can do it for you)

### Create a .env file

```bash
nano .env
```

Add the following lines:

```bash
DB_URL=jdbc:postgresql://<DB_IP>:5432/postgres # put "postgresql" as the DB_IP if you wanna use docker compose, "localhost" otherwise
DB_USER=postgres
DB_PASSWORD=password
DB_DRIVER=org.postgresql.Driver

HOST_IP=127.0.0.1

HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
```

### Lazy run

```bash
sudo docker compose up
```

Add `-d` to run in detached mode

### Run without docker compose (create a container for the DB)

```bash
sudo docker run -e POSTGRES_PASSWORD='password' -e POSTGRES_USER='postgres' -e POSTGRES_DB='postgres' -p 5432:5432 postgres
```
