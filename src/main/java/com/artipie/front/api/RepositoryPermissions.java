/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.misc.RequestPath;
import com.artipie.front.settings.RepoPermissions;
import java.io.StringReader;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Repository permissions endpoint.
 * @since 0.1
 */
public final class RepositoryPermissions {

    /**
     * Name of the user to update permissions for.
     */
    public static final RequestPath.Param NAME = new RequestPath.Param("uname");

    /**
     * Private ctor.
     */
    private RepositoryPermissions() {
    }

    /**
     * Repository name from request.
     * @param request Spark request
     * @return Repository name
     */
    private static String repoNameFromRq(final Request request) {
        return Optional.ofNullable(Users.USER_PARAM.parse(request)).map(usr -> usr.concat("/"))
            .orElse("").concat(GetRepository.REPO_PARAM.parse(request));
    }

    /**
     * Handle `PUT` request to add repository permissions, request line format:
     * PUT /repo/{owner_name}/{repo_name}/permissions/{uname}
     * where {owner_name} is required for `org` layout only, {uname} is the name of
     * the user to add permission for.
     * @since 0.1
     */
    public static final class Put implements Route {

        /**
         * Repository permissions.
         * @since 0.1
         */
        private final RepoPermissions perms;

        /**
         * Ctor.
         * @param perms Repository permissions
         */
        public Put(final RepoPermissions perms) {
            this.perms = perms;
        }

        @Override
        public Object handle(final Request request, final Response response) {
            this.perms.add(
                RepositoryPermissions.repoNameFromRq(request),
                RepositoryPermissions.NAME.parse(request),
                Json.createReader(new StringReader(request.body())).readArray()
            );
            response.status(HttpStatus.CREATED_201);
            return null;
        }
    }

    /**
     * Handle `GET` request to add repository permissions, request line format:
     * PUT /repo/{owner_name}/{repo_name}/permissions
     * where {owner_name} is required for `org` layout only.
     * @since 0.1
     */
    public static final class Get implements Route {

        /**
         * Repository settings section `permissions` name.
         */
        private static final String PERMISSIONS = "permissions";

        /**
         * Repository settings.
         */
        private final RepoPermissions stn;

        /**
         * Ctor.
         * @param stn Repository settings
         */
        public Get(final RepoPermissions stn) {
            this.stn = stn;
        }

        @Override
        public String handle(final Request request, final Response response) {
            final JsonObject res = this.stn.get(RepositoryPermissions.repoNameFromRq(request));
            response.type(MimeTypes.Type.APPLICATION_JSON.toString());
            return Json.createObjectBuilder().add(Get.PERMISSIONS, res)
                .build().toString();
        }
    }

    /**
     * Handle `DELETE` request to remove repository permissions, request line format:
     * DELETE /repo/{owner_name}/{repo_name}/permissions/{uname}
     * where {owner_name} is required for `org` layout only, {uname} is the name of
     * the user to add permission for.
     * @since 0.1
     */
    public static final class Delete implements Route {

        /**
         * Repository permissions.
         * @since 0.1
         */
        private final RepoPermissions perms;

        /**
         * Ctor.
         * @param perms Repository permissions
         */
        public Delete(final RepoPermissions perms) {
            this.perms = perms;
        }

        @Override
        public Object handle(final Request request, final Response response) {
            this.perms.delete(
                RepositoryPermissions.repoNameFromRq(request),
                RepositoryPermissions.NAME.parse(request)
            );
            response.status(HttpStatus.OK_200);
            return null;
        }
    }
}
