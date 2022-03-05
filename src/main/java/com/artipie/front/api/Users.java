/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.auth.User;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `GET` request to obtain users list.
 * @since 0.1
 */
public final class Users implements Route {

    /**
     * Artipie users.
     */
    private final com.artipie.front.auth.Users ausers;

    /**
     * Ctor.
     * @param creds Artipie users
     */
    public Users(final com.artipie.front.auth.Users creds) {
        this.ausers = creds;
    }

    @Override
    public String handle(final Request request, final Response response) {
        final JsonObjectBuilder res = Json.createObjectBuilder();
        for (final User usr : this.ausers.list()) {
            final JsonObjectBuilder builder = Json.createObjectBuilder();
            usr.email().ifPresent(email -> builder.add("email", email));
            if (!usr.groups().isEmpty()) {
                final JsonArrayBuilder arr = Json.createArrayBuilder();
                usr.groups().forEach(arr::add);
                builder.add("groups", arr);
            }
            res.add(usr.uid(), builder.build());
        }
        return res.build().toString();
    }
}
