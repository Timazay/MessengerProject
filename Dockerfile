# Build stage
FROM eclipse-temurin:21-alpine AS build
WORKDIR /app

# Копируем только файлы для зависимостей (для кэширования)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Даем права на выполнение gradlew
RUN chmod +x gradlew

# Скачиваем зависимости (кэшируется, если файлы не менялись)
RUN ./gradlew dependencies --no-daemon

# Копируем исходный код
COPY src src

# Собираем приложение
RUN ./gradlew bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-alpine AS runtime
WORKDIR /app

# Создаем непривилегированного пользователя
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Копируем только jar из build стадии
COPY --from=build /app/build/libs/*.jar app.jar

# Копируем .env файл (опционально)
COPY --chown=spring:spring .env .env

ENTRYPOINT ["java", "-jar", "app.jar"]