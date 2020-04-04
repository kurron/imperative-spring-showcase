FROM azul/zulu-openjdk:11 AS builder
WORKDIR /tmp
ARG APPJAR=build/libs/*.jar
COPY ${APPJAR} app.jar
RUN jar -xf ./app.jar

FROM azul/zulu-openjdk:11
VOLUME /tmp
ARG DEPENDENCY=/tmp
COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","org.kurron.imperative.ShowcaseApplicationKt"]