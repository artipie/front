/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.internal;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Health route.
 * Returns 200 OK if service is healthy.
 * @since 1.0
 */
public final class HealthRoute implements Route {

    @Override
    public Object handle(final Request request, final Response response) throws Exception {
        // @checkstyle MagicNumberCheck (1 line)
        response.status(200);
        return "OK";
    }
}
