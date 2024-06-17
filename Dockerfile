# The MIT License (MIT) Copyright (c) 2022 artipie.com
# https://github.com/artipie/front/LICENSE.txt

FROM openjdk:21-slim-bookworm as build

ENV JVM_OPTS=""

LABEL description="Artipie front service"
LABEL maintainer="g4s8.public@gmail.com"

# may optimize but require DOCKER_BUILDKIT=1
# RUN --mount=target=/var/lib/apt/lists,type=cache \
#     --mount=target=/var/cache/apt,type=cache \
#     --mount=target=/root/.m2,type=cache 

RUN apt-get update

RUN apt-get install -y --no-install-recommends \
    maven

# RUN useradd -ms /sbin/nologin artipie -g artipie -u 2021
COPY . /usr/local/src
WORKDIR /usr/local/src

RUN mvn clean install -Pqulice
RUN mvn dependency:copy-dependencies -DoutputDirectory=target/dependencies/

FROM openjdk:21-oracle as run

RUN groupadd -r -g 2020 artipie
RUN adduser -M -r -g artipie -u 2021 -s /sbin/nologin artipie
RUN mkdir -p /etc/artipie /usr/lib/web-service /var/artipie && \
    chown artipie:artipie -R /etc/artipie /usr/lib/web-service /var/artipie

USER artipie:artipie
ARG JAR_FILE=front-*-SNAPSHOT.jar

COPY --from=build /usr/local/src/target/dependencies/*  /usr/lib/web-service/lib/
COPY --from=build /usr/local/src/target/${JAR_FILE} /usr/lib/web-service/app.jar

WORKDIR /var/web-service
HEALTHCHECK --interval=10s --timeout=3s \
  CMD curl -f http://localhost:8080/.health || exit 1

EXPOSE 8080
CMD [ \
  "java", \
  "--add-opens", "java.base/java.util=ALL-UNNAMED", \
  "--add-opens", "java.base/java.security=ALL-UNNAMED", \
  "-cp", "/usr/lib/web-service/app.jar:/usr/lib/web-service/lib/*", \
  "com.artipie.front.Service" \
]
