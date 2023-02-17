/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import java.util.Optional;
import spark.Request;

/**
 * Request attribute accessor.
 * @param <T> Attribute type
 * @since 1.0
 */
public interface RequestAttr<T> {

    /**
     * Read request attribute.
     * @param req Spark request
     * @return Optional with attribute value if found
     */
    Optional<T> read(Request req);

    /**
     * Read request attribute, throws exception if attribute not found.
     * @param req Spark request
     * @return Attribute value if found
     */
    default T readOrThrow(final Request req) {
        return this.read(req).orElseThrow(
            () -> new IllegalStateException("Required request parameter is not found")
        );
    }

    /**
     * Write attribute value.
     * @param req Spark request
     * @param val Attribute value
     */
    void write(Request req, T val);

    /**
     * Remove attribute.
     * @param req Spark request
     */
    void remove(Request req);

    /**
     * Standard attributes.
     * @since 1.0
     */
    enum Standard implements RequestAttr<String> {
        /**
         * User ID attribute.
         */
        USER_ID("uid");

        /**
         * Attribute name.
         */
        private final String name;

        /**
         * Private enum ctor.
         * @param name Attr name
         */
        Standard(final String name) {
            this.name = name;
        }

        /**
         * Attribute name.
         * @return The name
         */
        public String attrName() {
            return this.name;
        }

        @Override
        public Optional<String> read(final Request req) {
            return Optional.of(req.attribute(this.name));
        }

        @Override
        public void write(final Request req, final String val) {
            req.attribute(this.name, val);
        }

        @Override
        public void remove(final Request req) {
            req.raw().removeAttribute(this.name);
        }
    }
}
