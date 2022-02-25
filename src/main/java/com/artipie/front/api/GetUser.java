/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.auth.Credentials;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `GET` request to obtain user details.
 * @since 0.1
 * @checkstyle ReturnCountCheck (500 lines)
 */
public final class GetUser implements Route {

    /**
     * User parameter name.
     */
    public static final String USER_PARAM = ":user";

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
        final String name = request.params(GetUser.USER_PARAM);
        final Optional<Credentials.User> user = this.creds.user(name);
        if (user.isPresent()) {
            final JsonObjectBuilder json = Json.createObjectBuilder();
            user.get().email().ifPresent(email -> json.add("email", email));
            final JsonArrayBuilder arr = Json.createArrayBuilder();
            user.get().groups().forEach(arr::add);
            json.add("groups", arr.build());
            return Json.createObjectBuilder().add(name, json.build()).build().toString();
        } else {
            response.status(HttpStatus.NOT_FOUND_404);
            return null;
        }
    }
}
