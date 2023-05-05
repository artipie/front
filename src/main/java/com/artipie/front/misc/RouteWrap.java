/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.misc;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * Decorator for Spark route.
 * @param <T> Handle object type
 * @since 1.0
 */
public abstract class RouteWrap<T> {

    /**
     * Origin route.
     */
    private final Router<T> origin;

    /**
     * Wrap origin route.
     * @param origin Route
     */
    protected RouteWrap(final Router<T> origin) {
        this.origin = origin;
    }

    /**
     * This methos is implementation for {@link spark.Route#handle(Request, Response)}.
     * @param req Request
     * @param rsp Response
     * @return Object
     * @throws Exception On error
     */
    public final T handle(final Request req, final Response rsp) throws Exception {
        return this.origin.handle(req, rsp);
    }

    /**
     * Router function.
     * @param <T> Handle result
     * @since 1.0
     */
    @FunctionalInterface
    public interface Router<T> {

        /**
         * Generic handle.
         * @param req Request
         * @param rsp Response
         * @return Result
         * @throws Exception Error
         */
        T handle(Request req, Response rsp) throws Exception;
    }

    /**
     * Wrap Spark route.
     * @since 1.0
     */
    public static class Route extends RouteWrap<Object> implements spark.Route {

        /**
         * Wrap route.
         * @param route Origin
         */
        protected Route(final spark.Route route) {
            super((req, rsp) -> route.handle(req, rsp));
        }
    }

    /**
     * Wrap Spark template view route.
     * @since 1.0
     */
    public static class TemplateViewRoute extends RouteWrap<ModelAndView>
        implements spark.TemplateViewRoute {

        /**
         * Wrap route.
         * @param route Origin
         */
        protected TemplateViewRoute(final spark.TemplateViewRoute route) {
            super((req, rsp) -> route.handle(req, rsp));
        }
    }
}
