/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

/**
 * Not found error, to be thrown when any requested item is not found.
 * This exception will be handled as HTTP 404 status.
 * @since 0.1
 */
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Ctor.
     * @param msg Error message
     * @param err Cause
     */
    public NotFoundException(final String msg, final Throwable err) {
        super(msg, err);
    }

    /**
     * Ctor.
     * @param msg Error message
     */
    public NotFoundException(final String msg) {
        super(msg);
    }

}
