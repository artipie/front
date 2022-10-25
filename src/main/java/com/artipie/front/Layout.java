/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

/**
 * Repository layout.
 *
 * @since 1.0
 */
public enum Layout {
    /**
     * Flat layout.
     */
    FLAT("flat"),
    /**
     * Org layout.
     */
    ORG("org");

    /**
     * Name of layout.
     */
    private final String name;

    /**
     * Ctor.
     * @param name Name of layout.
     */
    Layout(final String name) {
        this.name = name;
    }

    /**
     * The name of the layout.
     * @return String name
     */
    public String toString() {
        return this.name;
    }
}
