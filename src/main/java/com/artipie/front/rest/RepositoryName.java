/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import spark.Request;

/**
 * Repository name.
 *
 * @since 1.0
 */
public interface RepositoryName {
    /**
     * Repository name path parameter name.
     */
    String REPO = ":repo";

    /**
     * The name of the repository.
     *
     * @return String name
     */
    String toString();

    /**
     * Repository name from request.
     *
     * @since 1.0
     */
    class FromRequest implements RepositoryName {
        /**
         * Request.
         */
        private final Request request;

        /**
         * Ctor.
         *
         * @param request Request
         */
        public FromRequest(final Request request) {
            this.request = request;
        }

        @Override
        public String toString() {
            return this.request.params(RepositoryName.REPO);
        }
    }
}
