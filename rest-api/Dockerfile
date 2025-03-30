FROM eclipse-temurin:17-jre-alpine

RUN mkdir /app

COPY build/libs/*.jar /app/app.jar

WORKDIR /app

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
