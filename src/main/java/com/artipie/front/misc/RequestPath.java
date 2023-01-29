/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.misc;

import spark.Request;

/**
 * Request path for spark with params.
 * @since 1.0
 */
public final class RequestPath {

    /**
     * Path route.
     */
    private final String path;

    /**
     * New empty path.
     */
    public RequestPath() {
        this("");
    }

    /**
     * New path.
     * @param path With intial path
     */
    public RequestPath(final String path) {
        this.path = path;
    }

    /**
     * With param.
     * @param param Path param
     * @return New request path
     */
    public RequestPath with(final Param param) {
        return new RequestPath(String.join("/", this.path, param.toString()));
    }

    /**
     * With plain path part.
     * @param part Path part
     * @return New request path
     */
    public RequestPath with(final String part) {
        return new RequestPath(String.join("/", this.path, part));
    }

    @Override
    public String toString() {
        final String res;
        if (this.path.isEmpty()) {
            res = "";
        } else {
            res = this.path;
        }
        return res;
    }

    /**
     * Path request param.
     * @since 1.0
     */
    public static final class Param {

        /**
         * Param name.
         */
        private final String name;

        /**
         * New param.
         * @param name With name
         */
        public Param(final String name) {
            this.name = ":".concat(name);
        }

        /**
         * Parse Spark request.
         * @param req Request
         * @return Param value
         */
        public String parse(final Request req) {
            return req.params(this.name);
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
