FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN mkdir -p /app/logs /app/uploads/avatars /app/uploads/item-images
WORKDIR /app
COPY --from=builder /app/target/lost.jar app.jar
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod \
    FILE_UPLOAD_DIR=/app/uploads \
    FILE_BASE_URL=http://localhost:8080/api \
    JAVA_OPTS="-Xms512m -Xmx1024m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 