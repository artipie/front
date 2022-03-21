/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.auth.Credentials;
import com.artipie.front.auth.User;
import com.artipie.front.misc.RequestPath;
import java.io.StringReader;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Users API endpoints.
 * @since 0.1
 */
public final class Users {

    /**
     * User request line parameter.
     */
    public static final RequestPath.Param USER_PARAM = new RequestPath.Param("user");

    /**
     * Field `email`.
     */
    private static final String EMAIL = "email";

    /**
     * Field `groups`.
     */
    private static final String GROUPS = "groups";

    /**
     * Ctor.
     */
    private Users() {
    }

    /**
     * Handle `GET` request to obtain users list, request line example:
     * GET /users
     * Returns json object with users list.
     * @since 0.1
     */
    public static final class GetAll implements Route {

        /**
         * Artipie users.
         */
        private final com.artipie.front.auth.Users ausers;

        /**
         * Ctor.
         *
         * @param creds Artipie users
         */
        public GetAll(final com.artipie.front.auth.Users creds) {
            this.ausers = creds;
        }

        @Override
        public String handle(final Request request, final Response response) {
            final JsonObjectBuilder res = Json.createObjectBuilder();
            for (final User usr : this.ausers.list()) {
                final JsonObjectBuilder builder = Json.createObjectBuilder();
                usr.email().ifPresent(email -> builder.add(Users.EMAIL, email));
                if (!usr.groups().isEmpty()) {
                    final JsonArrayBuilder arr = Json.createArrayBuilder();
                    usr.groups().forEach(arr::add);
                    builder.add(Users.GROUPS, arr);
                }
                res.add(usr.uid(), builder.build());
            }
            return res.build().toString();
        }
    }

    /**
     * Handle `GET` request to obtain user details, request line example:
     * GET /users/{uid}
     * where {uid} is the name of the user.
     * @since 0.1
     * @checkstyle ReturnCountCheck (500 lines)
     */
    public static final class GetUser implements Route {

        /**
         * Credentials.
         */
        private final Credentials creds;

        /**
         * Ctor.
         * @param creds Users credentials
         */
        public GetUser(final Credentials creds) {
            this.creds = creds;
        }

        @Override
        @SuppressWarnings("PMD.OnlyOneReturn")
        public String handle(final Request request, final Response response) {
            final String name = USER_PARAM.parse(request);
            final Optional<User> user = this.creds.user(name);
            if (user.isPresent()) {
                final JsonObjectBuilder json = Json.createObjectBuilder();
                user.get().email().ifPresent(email -> json.add(Users.EMAIL, email));
                final JsonArrayBuilder arr = Json.createArrayBuilder();
                user.get().groups().forEach(arr::add);
                json.add(Users.GROUPS, arr.build());
                return Json.createObjectBuilder().add(name, json.build()).build().toString();
            } else {
                response.status(HttpStatus.NOT_FOUND_404);
                return null;
            }
        }
    }

    /**
     * Handle `DELETE` request to delete the user, request line example:
     * DELETE /users/{uid}
     * where {uid} is the name of the user.
     * @since 0.1
     */
    public static final class Delete implements Route {

        /**
         * Artipie users.
         */
        private final com.artipie.front.auth.Users users;

        /**
         * Ctor.
         * @param users Artipie users
         */
        public Delete(final com.artipie.front.auth.Users users) {
            this.users = users;
        }

        @Override
        public Object handle(final Request request, final Response response) {
            this.users.remove(USER_PARAM.parse(request));
            response.status(HttpStatus.OK_200);
            return null;
        }
    }

    /**
     * Handle `HEAD` request to check if the user exists, request line example:
     * HEAD /users/{uid}
     * where {uid} is the name of the user.
     * @since 0.1
     */
    public static final class Head implements Route {

        /**
         * Credentials.
         */
        private final Credentials creds;

        /**
         * Ctor.
         * @param creds Credentials
         */
        public Head(final Credentials creds) {
            this.creds = creds;
        }

        @Override
        public Object handle(final Request request, final Response response) {
            if (this.creds.user(USER_PARAM.parse(request)).isEmpty()) {
                response.status(HttpStatus.NOT_FOUND_404);
            } else {
                response.status(HttpStatus.OK_200);
            }
            return null;
        }
    }

    /**
     * Handle `PUT` request create user, request line example:
     * PUT /users/{uid}
     * where {uid} is the name of the user, json with user info is expected in the
     * request body.
     * @since 0.1
     * @checkstyle ReturnCountCheck (500 lines)
     */
    public static final class Put implements Route {

        /**
         * Artipie users.
         */
        private final com.artipie.front.auth.Users users;

        /**
         * Ctor.
         * @param users Artipie users
         */
        public Put(final com.artipie.front.auth.Users users) {
            this.users = users;
        }

        @Override
        @SuppressWarnings("PMD.OnlyOneReturn")
        public Object handle(final Request request, final Response response) {
            final String name = USER_PARAM.parse(request);
            if (this.users.list().stream().anyMatch(usr -> name.equals(usr.uid()))) {
                response.status(HttpStatus.CONFLICT_409);
                return String.format("User %s already exists", name);
            }
            final JsonObject user = Json.createReader(new StringReader(request.body())).readObject()
                .getJsonObject(name);
            if (user == null) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return "User info json is expected";
            }
            if (!user.containsKey("type")) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return "Password type field `type` is required";
            }
            if (!user.containsKey("pass")) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return "Password field `pass` is required";
            }
            this.users.add(user, name);
            response.status(HttpStatus.CREATED_201);
            return null;
        }
    }
}
