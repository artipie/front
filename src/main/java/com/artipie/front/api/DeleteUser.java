/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.auth.Users;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `DELETE` request to remove user.
 * @since 0.1
 */
public final class DeleteUser implements Route {

    /**
     * Artipie users.
     */
    private final Users users;

    /**
     * Ctor.
     * @param users Artipie users
     */
    public DeleteUser(final Users users) {
        this.users = users;
    }

    @Override
    public Object handle(final Request request, final Response response) {
        this.users.remove(request.params(GetUser.USER_PARAM));
        response.status(HttpStatus.OK_200);
        return null;
    }
}
