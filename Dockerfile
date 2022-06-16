# The MIT License (MIT) Copyright (c) 2022 artipie.com
# https://github.com/artipie/front/LICENSE.txt

FROM openjdk:17-oracle
ARG JAR_FILE
ENV JVM_OPTS=""

LABEL description="Artipie front service"
LABEL maintainer="g4s8.public@gmail.com"

RUN groupadd -r -g 2020 artipie && \
    adduser -M -r -g artipie -u 2021 -s /sbin/nologin artipie && \
    mkdir -p /etc/artipie /usr/lib/web-service /var/artipie && \
    chown artipie:artipie -R /etc/artipie /usr/lib/web-service /var/artipie
USER 2021:2020

COPY target/dependency  /usr/lib/web-service/lib
COPY target/${JAR_FILE} /usr/lib/web-service/app.jar
COPY _config.yml /etc/artipie/artipie.yml

WORKDIR /var/web-service
HEALTHCHECK --interval=10s --timeout=3s \
  CMD curl -f http://localhost:8080/.health || exit 1

EXPOSE 8080
CMD [ \
  "java", \
  "--add-opens", "java.base/java.util=ALL-UNNAMED", \
  "--add-opens", "java.base/java.security=ALL-UNNAMED", \
  "-cp", "/usr/lib/web-service/app.jar:/usr/lib/web-service/lib/*", \
  "com.artipie.front.Service", \
  "--port=8080", \
  "--config=/etc/artipie/artipie.yml" \
]
