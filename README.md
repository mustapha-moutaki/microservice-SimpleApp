This is a comprehensive, updated **README.md** that covers the specific scenarios we discussed: running services in a single root folder (Best Practice) versus running them from different paths using a shared network.

---

# 🛡️ Microservices Security Master Guide (Keycloak & Spring Boot 4)

This project demonstrates how to build and secure a Microservices architecture using **Keycloak**, **Spring Cloud Gateway**, and **Spring Boot 4**.

---

## 🏗️ 1. Choose Your Architecture Strategy

### Scenario A: The "Same Root" Approach (Best Practice)
**Recommended for:** Most projects, easy management, and simple CI/CD.
All services live in one folder. One `docker-compose.yml` controls everything.
*   **Path:** `/home/user/my-project/`

### Scenario B: The "Shared Network" Approach
**Recommended for:** Different teams working on different folders/paths.
Each service has its own folder and its own `docker-compose.yml`, but they talk over a shared "bridge".

---

## 📂 2. Project Structure (Scenario A)
```text
/root-folder
│
├── api-gateway/
│   ├── Dockerfile
│   └── src/main/resources/application.yml
│
├── employee-service/
│   ├── Dockerfile
│   └── src/main/resources/application.yml
│
├── keycloak-service/ (Optional folder for Docker)
│   └── docker-compose.yml
│
└── pom.xml (Parent POM)
```

### Every Service Folder must contain:
1.  **Dockerfile**: To package the app.
2.  **application.yml**: To configure ports and security.
3.  **SecurityConfig.java**: To handle JWT validation.
4.  **JwtAuthConverter.java**: To convert Keycloak roles to Spring roles.

---

## 🐋 3. Docker Configuration

### Case 1: Same Root (Single `docker-compose.yml`)
Place this in your **Root Folder**. It defines the **Keycloak Service** and the others.

```yaml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    environment:
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: admin
    command: start-dev
    ports:
      - "8080:8080"
    networks:
      - micro-network

  employee-service:
    build: ./employee-service
    ports:
      - "8081:8081"
    environment:
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://keycloak:8080/realms/springboot
    depends_on:
      - keycloak
    networks:
      - micro-network

  api-gateway:
    build: ./api-gateway
    ports:
      - "9090:9090"
    environment:
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://keycloak:8080/realms/springboot
    depends_on:
      - employee-service
    networks:
      - micro-network

networks:
  micro-network:
    driver: bridge
```

---

### Case 2: Different Paths (Shared External Network)
If your services are in different folders (e.g., `/home/mustapha/api-gateway` and `/home/mustapha/keycloak`), follow this:

1.  **Create the network manually**:
    ```bash
    docker network create my-shared-network
    ```
2.  **In each service's `docker-compose.yml`**, add:
    ```yaml
    networks:
      my-shared-network:
        external: true
    ```
3.  **Connect them**: Each service can now reach the other using their `container_name` (e.g., `http://keycloak:8080`).

---

## ⚙️ 4. Essential Service Configurations

### API Gateway (`application.yml`)
**Crucial:** Use service names, not `localhost` for Docker.
```yaml
server:
  port: 9090
spring:
  cloud:
    gateway:
      routes:
        - id: employee-service
          uri: http://employee-service:8081  # Service Name
          predicates:
            - Path=/employees/**
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/springboot
```

---

## 🔑 5. Keycloak Setup Guide
1.  **URL**: `http://localhost:8080` (Admin/Admin).
2.  **Realm**: Create `springboot`.
3.  **Client**: Create `springboot-client`.
    *   Set **Client Authentication** to `ON`.
    *   Set **Direct Access Grants** to `ON`.
4.  **Roles**: Create a Realm Role named `USER` (Uppercase).
5.  **User**: Create `user_o1`, set a password, and assign the `USER` role.

---

## 🧪 6. How to Test (Step-by-Step)
1.  **Build Apps**: Run `mvn clean package -DskipTests` in all folders.
2.  **Start Docker**: Run `docker compose up -d --build`.
3.  **Get Token**: POST to Keycloak `/protocol/openid-connect/token` to get an `access_token`.
4.  **Access API**: GET `http://localhost:9090/employees` using **Bearer Token** in Postman.

---

## ✅ 7. Best Practices
1.  **Unified Root**: Keep microservices in a single root folder (Mono-repo) for easier local development.
2.  **Naming**: Always use the Docker service name (e.g., `keycloak`) in your configs.
3.  **Port Management**: Never use the same port twice. (Keycloak: 8080, Gateway: 9090, Services: 8081+).
4.  **Role Mapping**: Always use a `JwtAuthConverter` to handle the `realm_access` claim in Keycloak JWTs.
5.  **Security**: Never hardcode the `client-secret` in production; use environment variables.

---
*Generated Documentation for the Microservice Security Project.*