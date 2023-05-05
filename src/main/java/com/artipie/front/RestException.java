/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import com.artipie.ArtipieException;

/**
 * Exception should be used in wrong result of rest-invocation.
 *
 * @since 1.0
 * @implNote RestException is unchecked exception, but it's a good
 *  practice to document it via {@code throws} tag in JavaDocs.
 */
@SuppressWarnings("PMD.OnlyOneConstructorShouldDoInitialization")
public class RestException extends ArtipieException {
    private static final long serialVersionUID = 1L;

    /**
     * Status code.
     */
    private final int code;

    /**
     * New exception with message and base cause.
     * @param code Http status code
     * @param msg Message
     * @param cause Cause
     */
    public RestException(final int code, final String msg, final Throwable cause) {
        super(msg, cause);
        this.code = code;
    }

    /**
     * New exception with base cause.
     * @param code Http status code
     * @param cause Cause
     */
    public RestException(final int code, final Throwable cause) {
        super(cause);
        this.code = code;
    }

    /**
     * New exception with message.
     * @param code Http status code
     * @param msg Message
     */
    public RestException(final int code, final String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * Get http status code of rest invocation's result .
     * @return Status code.
     */
    public int statusCode() {
        return this.code;
    }
}

