# ---- Stage 1 – Build jar file ----
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY . .
RUN mvn -B -DskipTests clean package

# ---- Stage 2 – Run lightweight image ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar

# Mongo connection (will be overridden in k8s)
ENV MONGO_URI=mongodb://mongo-svc:27017/jobmanagerdb

EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]
