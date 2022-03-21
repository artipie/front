/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.front.misc.RequestPath;
import com.artipie.front.settings.YamlStorages;
import java.io.StringReader;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Storages API endpoints.
 * @since 0.1
 */
public final class Storages {

    /**
     * Storage alias path param.
     */
    public static final RequestPath.Param ST_ALIAS = new RequestPath.Param("alias");

    /**
     * Ctor.
     */
    private Storages() {
    }

    /**
     * Get repository key from request.
     * @param request Spark request
     * @return Repository key if present
     */
    private static Optional<Key> repoFromRq(final Request request) {
        final Optional<Key> usr = Optional.ofNullable(Users.USER_PARAM.parse(request))
            .map(Key.From::new);
        Optional<Key> repo = Optional.ofNullable(Repositories.REPO_PARAM.parse(request))
            .map(Key.From::new);
        if (usr.isPresent()) {
            repo = repo.<Key>map(key -> new Key.From(usr.get(), key)).or(() -> usr);
        }
        return repo;
    }

    /**
     * Handle `GET` request to obtain storages list, request line example:
     * GET /repositories/{owner_name}/{repo_name}/storages
     * GET /storages/{owner_name}
     * for common or repository storages, {owner_name} is required only for `org` layout.
     * @since 0.1
     */
    public static final class Get implements Route {

        /**
         * Artipie storage.
         */
        private final BlockingStorage strgs;

        /**
         * Ctor.
         *
         * @param strgs Artipie storages
         */
        public Get(final BlockingStorage strgs) {
            this.strgs = strgs;
        }

        @Override
        public String handle(final Request request, final Response response) {
            final JsonObjectBuilder res = Json.createObjectBuilder();
            new YamlStorages(Storages.repoFromRq(request), this.strgs).list()
                .forEach(item -> res.add(item.alias(), item.info()));
            response.type(MimeTypes.Type.APPLICATION_JSON.asString());
            return Json.createObjectBuilder().add("storages", res.build()).build().toString();
        }
    }

    /**
     * Handle `DELETE` request to delete storage alias, request line example:
     * DELETE /repositories/{owner_name}/{repo}/storages/{alias}
     * DELETE /storages/{owner_name}/{alias}
     * for repository and common storages, {owner_name} is required only for `org` layout,
     * {alias} if the name of storage alias to delete.
     * @since 0.1
     */
    public static final class Delete implements Route {

        /**
         * Artipie storage.
         */
        private final BlockingStorage strgs;

        /**
         * Ctor.
         *
         * @param strgs Artipie storages
         */
        public Delete(final BlockingStorage strgs) {
            this.strgs = strgs;
        }

        @Override
        public Object handle(final Request request, final Response response) {
            new YamlStorages(Storages.repoFromRq(request), this.strgs)
                .remove(Storages.ST_ALIAS.parse(request));
            response.status(HttpStatus.OK_200);
            return null;
        }
    }

    /**
     * Handle `PUT` request to add storage alias, request line example:
     * PUT /repositories/{owner_name}/{repo}/storages/{alias}
     * PUT /storages/{owner_name}/{alias}
     * for repository and common storages, {owner_name} is required only for `org` layout,
     * {alias} if the name of storage alias to delete.
     * Request body is expected to have new storage alias settings in json format.
     * @since 0.1
     */
    public static final class Put implements Route {

        /**
         * Artipie storage.
         */
        private final BlockingStorage strgs;

        /**
         * Ctor.
         *
         * @param strgs Artipie storages
         */
        public Put(final BlockingStorage strgs) {
            this.strgs = strgs;
        }

        @Override
        public Object handle(final Request request, final Response response) {
            final YamlStorages storages =
                new YamlStorages(Storages.repoFromRq(request), this.strgs);
            final String alias = Storages.ST_ALIAS.parse(request);
            if (storages.list().stream().anyMatch(item -> item.alias().equals(alias))) {
                response.status(HttpStatus.CONFLICT_409);
            } else {
                storages.add(
                    alias, Json.createReader(new StringReader(request.body())).readObject()
                );
                response.status(HttpStatus.CREATED_201);
            }
            return null;
        }
    }

}
