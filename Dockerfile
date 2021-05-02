FROM openjdk:11-jdk-slim
RUN apt-get update \
    && apt-get install -y postgresql-client
WORKDIR /app
EXPOSE 7000
COPY build/libs/*.jar app.jar
COPY bin/wait-for-db.sh wait-for-db.sh
CMD ["./wait-for-db.sh", "db", "java", "-jar", "app.jar"]