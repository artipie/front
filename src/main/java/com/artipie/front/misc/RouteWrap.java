/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.misc;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Decorator for Spark route.
 * @since 1.0
 */
public abstract class RouteWrap implements Route {

    /**
     * Origin route.
     */
    private final Route origin;

    /**
     * Wrap origin route.
     * @param origin Route
     */
    protected RouteWrap(final Route origin) {
        this.origin = origin;
    }

    @Override
    public final Object handle(final Request req, final Response rsp) throws Exception {
        return this.origin.handle(req, rsp);
    }
}
