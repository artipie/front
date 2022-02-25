<img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/>

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/artipie/front)](http://www.rultor.com/p/artipie/front)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![Javadoc](http://www.javadoc.io/badge/com.artipie/front.svg)](http://www.javadoc.io/doc/com.artipie/front)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/com.artipie/front/blob/master/LICENSE.txt)
[![codecov](https://codecov.io/gh/artipie/front/branch/master/graph/badge.svg)](https://codecov.io/gh/artipie/front)
[![Hits-of-Code](https://hitsofcode.com/github/artipie/front)](https://hitsofcode.com/view/github/artipie/front)
[![Maven Central](https://img.shields.io/maven-central/v/com.artipie/front.svg)](https://maven-badges.herokuapp.com/maven-central/com.artipie/front)
[![PDD status](http://www.0pdd.com/svg?name=artipie/front)](http://www.0pdd.com/p?name=artipie/front)

# Artipie front service

Front web service provides API and UI pages for managing Artipie configuration, such as repositories, users, permissions.


## Public API

### Repositories API

> **GET** /repositories

Returns list of the existing repositories as json array:
```json
[
  {"fullName" : "Jane/maven"},
  {"fullName" : "Mark/conda-repo"},
  {"fullName" : "docker-proxy"}
]
```

> **GET** /repository/{name}

Returns repository called `{name}` settings as json object, repository permissions are not included:
```json
{
  "repo": {
    "type": "npm-proxy",
    "url": "http://artipie-proxy:8080/my-npm-proxy",
    "path": "my-npm-proxy",
    "storage": {
      "type": "fs",
      "path": "/var/artipie/data/"
    },
    "settings": {
      "remote": {
        "url": "http://artipie:8080/my-npm"
      }
    }
  }
}
```
If repository called `{name}` does not exist, `404` status is returned.

> **HEAD** /repository/{name} 

Returns response status `200` if repository `{name}` exists, status `404` otherwise.

> **PUT** /repository/{name}

Creates new repository with name `{name}`, if such repository already exists, status `409` is returned. 
Json request body is expected, minimal format is:
```json
{
  "repo": {
    "type": "npm-proxy",
    "storage": {
      "type": "fs",
      "path": "/var/artipie/data/"
    }
  }
}
```
Root field `repo`, fields `type` (repository type) and `storage` (repository storage definition) 
are required. Besides, `repo` section can contain `permission` and/or `setting` sections to set 
permissions to access repository or add some repository-specific settings. Check supported 
repositories [examples](https://github.com/artipie/artipie/tree/master/examples) 
to learn more about different repositories settings.

> **DELETE** /repository/{name} 

Deletes repository called `{name}`, returns status `200` on success, status `404` if repository does
not exist.

