# ── 1-bosqich: build ──────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Avval faqat dependency fayllarni ko'chirish (layer cache uchun)
COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Manba kodini ko'chirish va build qilish
COPY src src
RUN ./mvnw package -DskipTests -q

# ── 2-bosqich: runtime ────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/slide_payment.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
