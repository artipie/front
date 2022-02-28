/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.auth.Credentials;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `HEAD` request to check if user exists.
 * @since 0.1
 */
public final class HeadUser implements Route {

    /**
     * Credentials.
     */
    private final Credentials creds;

    /**
     * Ctor.
     * @param creds Credentials
     */
    public HeadUser(final Credentials creds) {
        this.creds = creds;
    }

    @Override
    public Object handle(final Request request, final Response response) {
        if (this.creds.user(request.params(GetUser.USER_PARAM)).isEmpty()) {
            response.status(HttpStatus.NOT_FOUND_404);
        } else {
            response.status(HttpStatus.OK_200);
        }
        return null;
    }
}
