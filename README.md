<a href="http://artipie.com"><img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/></a>

[![Join our Telegramm group](https://img.shields.io/badge/Join%20us-Telegram-blue?&logo=telegram&?link=http://right&link=http://t.me/artipie)](http://t.me/artipie)

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/artipie/front)](http://www.rultor.com/p/artipie/front)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/com.artipie/front/blob/master/LICENSE.txt)
[![Hits-of-Code](https://hitsofcode.com/github/artipie/front)](https://hitsofcode.com/view/github/artipie/front)
[![PDD status](http://www.0pdd.com/svg?name=artipie/front)](http://www.0pdd.com/p?name=artipie/front)

# Artipie front service

Front web service provides API and UI pages for managing [Artipie](https://github.com/artipie/artipie) configuration, 
such as repositories, users, permissions.

If you have any question or suggestions, do not hesitate to [create an issue](https://github.com/artipie/front/issues/new) or contact us in
[Telegram](https://t.me/artipie).  
Artipie [roadmap](https://github.com/orgs/artipie/projects/3).

## Quick start

````
$ TAG=artipie-front
$ docker build . -t $TAG
$ docker run -p8080:8080 -eARTIPIE_REST=http://registry.local:8086 $TAG
````

## Build notes

### Building with maven

````
mvn clean install -Pqulice
````
(the qulice profile do not exists any more ?)

To avoid build errors use Maven 3.2+. (the 3.8 looks like working too)

### Export pom dependencies inside a folder

````
mvn dependency:copy-dependencies -DoutputDirectory=target/dependencies/
````

## Dockerfile notes

the Dockerfile use two layers, one build layer and the run layer (built with copying libraries from the build layer)

## Environment variables (not exhaustive list)

- ARTIPIE_REST : (default : http://localhost:8086) url to the artipie API (example : http://registry.local:8086), 
- ARTIPIE_PORT : (default : 8080) port on which the server will listen to 

## How to contribute

Please read [contributing rules](https://github.com/artipie/artipie/blob/master/CONTRIBUTING.md).

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build: