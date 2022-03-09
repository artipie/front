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

### Repository permissions API

> **GET** /repositories/{repo}/permissions

Returns status `404` if repository `{repo}` does not exist, otherwise returns permissions if json format:
```json
{ 
  "permissions": {
    "Jane": ["read", "write"],
    "Mark": ["*"],
    "/readers": ["read"]
  }
}
```

> **PUT** /repositories/{repo}/permissions/{uid}

Adds permissions for user `{uid}` in the repository `{repo}`. This method overrides all previously 
existed permissions for the user in the repository. Json array with permissions list is expected in
the request body:
```json
["read", "tag"]
```
If permissions were successfully added, status `201` is returned, if repository `{repo}` does not exist,
status `404` is returned.

> **DELETE** /repositories/{repo}/permissions/{uid}

Revokes all permissions for user `{uid}` in the repository `{repo}`, returns status `200` on success. 
If repository `{repo}` does not exist or user `{uid}` does not have any permissions in the repository,
status `404` is returned.

> **PATCH** /repositories/{repo}/permissions

Patches permissions of the repository `{repo}`. Json body is expected in the request:
```json
{
  "grant": {
    "bob": ["read"],
    "john": ["read"]
  },
  "revoke": {
    "john": ["write"]
  }
}
```
In this example we grant `read` permission to bob and john but revoke `write` permission for john. 
All other already granted permissions for user are kept as is. For example, if john already has 
`tag` permission for the repository, after `PATCH` operation with request body from the example,
he will have `read` and `tag` permissions.

If repository `{repo}` does not exist, `404` status is returned.

> **Note**  
> When Artipie layout is `org`, each repository belongs to some user, thus repository name path
> parameter `{repo}` actually consists of two parts: `{repository_owner_name}/{repository_name}`.
> When layout is `flat`, `{repo}` is just the name of the repository.

### Users API

> **GET** /users

Returns list of the existing users in json format:
```json
{
  "John": {},
  "Jane": {
    "email": "jane@work.com"
  },
  "mark": {
    "email": "mark@example.com",
    "groups": ["dev", "admin"]
  }
}
```
Fields `email` and `groups` are optional.

> **GET** /user/{name}

Returns info of the user with name `{name}` as json object:

```json
{
  "mark": {
    "email": "mark@example.com",
    "groups": ["dev", "admin"]
  }
}
```
Fields `email` and `groups` are optional, if user does not exist `404` status is returned.

> **HEAD** /user/{name}

Returns response status `200` if user with name `{name}` exists, status `404` otherwise.

> **PUT** /user/{name}

Creates new user with name `{name}`, json request body is expected:
```json
{
  "Alice": {
    "type": "plain",
    "pass": "123",
    "email": "alice@example.com",
    "groups": ["admin", "dev-lead"]
  }
}
```
Field `type` is required, can be either `plain` or `sha256`, field `pass` is also required, 
if `type` is `plain` not-encoded password is expected in `pass` field,
if `type` is `sha256` sha-256 checksum of the password is expected in `pass` field.
Fields `email` and `groups` are optional. 
If user with name `{name}` already exists, status `409` is returned.

> **DELETE** /user/{name}

Removed user with name `{name}`, returns status `200` on success, status `404` if user does
not exist.