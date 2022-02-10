FROM openjdk:17-oracle
ARG JAR_FILE
ENV JVM_OPTS=""

LABEL description="Artipie front service"
LABEL maintainer="g4s8.public@gmail.com"

RUN groupadd -r -g 2020 artipie && \
    adduser -M -r -g artipie -u 2021 -s /sbin/nologin artipie && \
    mkdir -p /usr/lib/web-service /var/web-service && \
    chown artipie:artipie -R /usr/lib/web-service /var/web-service
USER 2021:2020

COPY target/dependency  /usr/lib/web-service/lib
COPY target/${JAR_FILE} /usr/lib/web-service/app.jar

WORKDIR /var/web-service
EXPOSE 8080
CMD [ \
  "java", \
  "--add-opens", "java.base/java.util=ALL-UNNAMED", \
  "--add-opens", "java.base/java.security=ALL-UNNAMED", \
  "-cp", "/usr/lib/web-service/app.jar:/usr/lib/web-service/lib/*", \
  "com.artipie.front.Service", \
  "--port=8080" \
]
