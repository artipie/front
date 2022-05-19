/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.RequestAttr;
import com.artipie.front.misc.Json2Yaml;
import com.artipie.front.misc.RequestPath;
import com.artipie.front.misc.Yaml2Json;
import com.artipie.front.settings.RepoSettings;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Repositories API endpoints.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Repositories {

    /**
     * Repository name request parameter.
     */
    public static final RequestPath.Param REPO_PARAM = new RequestPath.Param("repo");

    /**
     * Repository settings yaml section `repo` name.
     */
    private static final String REPO = "repo";

    /**
     * Ctor.
     */
    private Repositories() {
    }

    /**
     * Handle `GET` request to obtain repositories list, request line example:
     * GET /repositories
     * Returns repositories list.
     * @since 0.1
     */
    public static final class GetAll implements Route {

        /**
         * Artipie repositories settings.
         */
        private final RepoSettings stngs;

        /**
         * Ctor.
         *
         * @param storage Artipie repositories settings storage
         */
        public GetAll(final RepoSettings storage) {
            this.stngs = storage;
        }

        @Override
        public String handle(final Request request, final Response response) {
            JsonArrayBuilder json = Json.createArrayBuilder();
            for (final String name : this.stngs.list(Optional.empty())) {
                json = json.add(Json.createObjectBuilder().add("fullName", name).build());
            }
            response.type(MimeTypes.Type.APPLICATION_JSON.toString());
            return json.build().toString();
        }
    }

    /**
     * Handle `GET` request to obtain repository details, request line example:
     * GET /repositories/{repo_name}
     * where {repo_name} is the name of the repository. In the case of `org` layout,
     * repository owner is obtained from request attributes.
     *
     * @since 0.1
     */
    public static final class Get implements Route {

        /**
         * Repository settings.
         */
        private final RepoSettings stn;

        /**
         * Ctor.
         * @param stn Repository settings
         */
        public Get(final RepoSettings stn) {
            this.stn = stn;
        }

        @Override
        public String handle(final Request request, final Response response) {
            final JsonObject repo = new Yaml2Json().apply(
                new String(
                    this.stn.value(
                        REPO_PARAM.parse(request),
                        RequestAttr.Standard.USER_ID.readOrThrow(request)
                    ),
                    StandardCharsets.UTF_8
                )
            ).asJsonObject().getJsonObject(Repositories.REPO);
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for (final Map.Entry<String, JsonValue> item : repo.entrySet()) {
                if (!"permissions".equals(item.getKey())) {
                    builder = builder.add(item.getKey(), item.getValue());
                }
            }
            response.type(MimeTypes.Type.APPLICATION_JSON.toString());
            return Json.createObjectBuilder().add(Repositories.REPO, builder.build())
                .build().toString();
        }
    }

    /**
     * Handle `DELETE` request to delete repository, request line example:
     * DELETE /repositories/{repo_name}
     * where {repo_name} is the name of the repository. In the case of `org` layout,
     * repository owner is obtained from request attributes.
     *
     * @since 0.1
     */
    public static final class Delete implements Route {

        /**
         * Repository settings.
         */
        private final RepoSettings stn;

        /**
         * Ctor.
         * @param stn Repository settings
         */
        public Delete(final RepoSettings stn) {
            this.stn = stn;
        }

        @Override
        public Object handle(final Request request, final Response response) {
            this.stn.delete(
                REPO_PARAM.parse(request),
                RequestAttr.Standard.USER_ID.readOrThrow(request)
            );
            return "";
        }
    }

    /**
     * Handle `HEAD` request to check if repository exists, request line example:
     * HEAD /repositories/{repo_name}
     * where {repo_name} is the name of the repository. In the case of `org` layout,
     * repository owner is obtained from request attributes.
     *
     * @since 0.1
     */
    public static final class Head implements Route {

        /**
         * Repositories settings.
         */
        private final RepoSettings stn;

        /**
         * Ctor.
         * @param stn Repositories settings
         */
        public Head(final RepoSettings stn) {
            this.stn = stn;
        }

        @Override
        public Object handle(final Request request, final Response response) {
            this.stn.key(
                REPO_PARAM.parse(request),
                RequestAttr.Standard.USER_ID.readOrThrow(request)
            );
            return "";
        }
    }

    /**
     * Handle `PUT` request to add the repository, request line example:
     * PUT /repositories/{repo_name}
     * where {repo_name} is the name of the repository. In the case of `org` layout,
     * repository owner is obtained from request attributes. Json with repository
     * seettings is expected in the request body.
     *
     * @since 0.1
     * @checkstyle ReturnCountCheck (500 lines)
     */
    public static final class Put implements Route {

        /**
         * Repository settings.
         */
        private final RepoSettings stn;

        /**
         * Ctor.
         * @param stn Repository settings
         */
        public Put(final RepoSettings stn) {
            this.stn = stn;
        }

        @Override
        @SuppressWarnings("PMD.OnlyOneReturn")
        public Object handle(final Request request, final Response response) {
            final String param = REPO_PARAM.parse(request);
            final String uid = RequestAttr.Standard.USER_ID.readOrThrow(request);
            if (this.stn.exists(param, uid)) {
                response.status(HttpStatus.CONFLICT_409);
                return String.format("Repository %s already exists", param);
            }
            final JsonObject body = Json.createReader(new StringReader(request.body()))
                .readObject();
            final JsonObject repo = body.getJsonObject(Repositories.REPO);
            if (repo == null) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return "Section `repo` is required";
            }
            if (!repo.containsKey("type")) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return "Repository type is required";
            }
            if (!repo.containsKey("storage")) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return "Repository storage is required";
            }
            this.stn.save(
                param, uid,
                new Json2Yaml().apply(body.toString()).toString().getBytes(StandardCharsets.UTF_8)
            );
            response.status(HttpStatus.CREATED_201);
            return "";
        }
    }

    /**
     * Handle `PUT` request to rename the repository, request line example:
     * PUT /repositories/{repo_name}/move
     * where {repo_name} is the name of the repository. In the case of `org` layout,
     * repository owner is obtained from request attributes. Json with repository
     * new name (field `new_name`) is expected in the request body.
     *
     * @since 0.1
     * @checkstyle ReturnCountCheck (500 lines)
     */
    public static final class Move implements Route {

        /**
         * Repository settings.
         */
        private final RepoSettings stn;

        /**
         * Ctor.
         * @param stn Repository settings
         */
        public Move(final RepoSettings stn) {
            this.stn = stn;
        }

        @Override
        @SuppressWarnings("PMD.OnlyOneReturn")
        public Object handle(final Request request, final Response response) {
            final String param = REPO_PARAM.parse(request);
            final String uid = RequestAttr.Standard.USER_ID.readOrThrow(request);
            if (!this.stn.exists(param, uid)) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return String.format("Repository does not %s exist", param);
            }
            final String nname = Json.createReader(new StringReader(request.body()))
                .readObject().getString("new_name");
            if (nname == null) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return "Field `new_name` is required";
            }
            this.stn.move(param, uid, nname);
            response.status(HttpStatus.OK_200);
            return "";
        }
    }
}
