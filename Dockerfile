FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY src/main/java ./src/main/java
RUN mkdir -p out/main \
    && javac -Xlint:all -Werror -d out/main $(find src/main/java -name '*.java' -print) \
    && jar --create --file /out.jar --main-class com.sky.eventbus.Main -C out/main .

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app --uid 10001 app
COPY --from=build --chown=app:app /out.jar ./sky-event-bus.jar
USER 10001:10001
ENTRYPOINT ["java", "-jar", "sky-event-bus.jar"]
