/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.auth.Users;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `PUT` request to create user.
 * @since 0.1
 * @checkstyle ReturnCountCheck (500 lines)
 */
public final class PutUser implements Route {

    /**
     * Artipie users.
     */
    private final Users users;

    /**
     * Ctor.
     * @param users Artipie users
     */
    public PutUser(final Users users) {
        this.users = users;
    }

    @Override
    @SuppressWarnings("PMD.OnlyOneReturn")
    public Object handle(final Request request, final Response response) {
        final String name = request.params(GetUser.USER_PARAM);
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
