# ───────────────────────────────────────────────
# Stage 1: Build (Maven)
# ───────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml trước để cache dependency
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ───────────────────────────────────────────────
# Stage 2: Runtime (JRE only)
# ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
