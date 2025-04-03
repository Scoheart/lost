FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# 复制Maven配置文件
COPY settings.xml /root/.m2/settings.xml

# 优先复制pom.xml利用Docker缓存机制
COPY pom.xml .
# 先下载依赖，如果pom.xml未变，可以使用缓存
RUN mvn dependency:go-offline

# 之后再复制源代码
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
# 创建必要的目录
RUN mkdir -p /app/logs /app/uploads && \
    addgroup -S appgroup && adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

WORKDIR /app
COPY --from=builder /app/target/lost.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m"

USER appuser
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 