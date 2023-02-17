/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.front.Layout;
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
         * Layout.
         */
        private final Layout layout;

        /**
         * Ctor.
         *
         * @param request Request
         * @param layout Layout
         */
        public FromRequest(final Request request, final Layout layout) {
            this.request = request;
            this.layout = layout;
        }

        /**
         * Provides string representation of repository name in toString() method.
         * <ul>
         *     <li>'reponame' for flat layout</li>
         *     <li>'username/reponame' for org layout</li>
         * </ul>
         *
         * @checkstyle NoJavadocForOverriddenMethodsCheck (10 lines)
         */
        @Override
        public String toString() {
            final String reponame;
            if (this.layout == Layout.FLAT) {
                reponame = new Flat(
                    this.request.params(RepositoryName.REPO)
                ).toString();
            } else {
                reponame = new Org(
                    this.request.params(RepositoryName.REPO),
                    this.request.session().attribute("uid")
                ).toString();
            }
            return reponame;
        }
    }

    /**
     * Repository name for flat layout.
     *
     * @since 1.0
     */
    class Flat implements RepositoryName {

        /**
         * Repository name.
         */
        private final String repo;

        /**
         * Ctor.
         *
         * @param repo Name
         */
        public Flat(final String repo) {
            this.repo = repo;
        }

        @Override
        public String toString() {
            return this.repo;
        }
    }

    /**
     * Repository name for org layout is combined from username and reponame:
     * 'username/reponame'.
     *
     * @since 1.0
     */
    class Org implements RepositoryName {

        /**
         * Repository name.
         */
        private final String repo;

        /**
         * User name.
         */
        private final String user;

        /**
         * Ctor.
         *
         * @param repo Repository name
         * @param user User name
         */
        public Org(final String repo, final String user) {
            this.repo = repo;
            this.user = user;
        }

        @Override
        public String toString() {
            return String.format("%s/%s", this.user, this.repo);
        }
    }
}
