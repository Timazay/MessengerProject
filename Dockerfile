FROM eclipse-temurin:21-alpine AS build
WORKDIR /app
COPY . /app/.
RUN chmod +x gradlew && ./gradlew clean build

# Runtime target
FROM eclipse-temurin:21-alpine AS develop-runtime
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
COPY .env .env
ENTRYPOINT ["java", "-jar", "app.jar"]