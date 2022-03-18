/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.misc;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * Receives values from body of response.
 * @since 0.1
 */
public final class ValueFromBody {
    /**
     * Information which was passed in the body.
     */
    private final String cpayload;

    /**
     * Charset of body.
     */
    private final Charset charset;

    /**
     * Ctor.
     * @param payload Information which was passed in the body
     * @param charset Charset of body
     */
    public ValueFromBody(final String payload, final Charset charset) {
        this.cpayload = payload;
        this.charset = charset;
    }

    /**
     * Ctor.
     * @param payload Information which was passed in the body
     */
    public ValueFromBody(final String payload) {
        this(payload, StandardCharsets.UTF_8);
    }

    /**
     * Obtains payload.
     * @return Payload of passed body.
     */
    public String payload() {
        return this.cpayload;
    }

    /**
     * Obtains value from body by name or throw exception in case of absence.
     * @param name Key name for obtaining value
     * @return Value from body by name.
     * @throws IllegalStateException In case of absence of the specified key.
     */
    public String byNameOrThrow(final String name) {
        return this.byName(name).orElseThrow(
            () -> new IllegalStateException(
                String.format("Failed to find '%s' in payload", name)
            )
        );
    }

    /**
     * Obtains value from body by name.
     * @param name Key name for obtaining value
     * @return Value by name if this name exists, empty otherwise.
     */
    public Optional<String> byName(final String name) {
        Optional<String> res = Optional.empty();
        final List<NameValuePair> params;
        params = URLEncodedUtils.parse(this.cpayload, this.charset);
        for (final NameValuePair param : params) {
            if (param.getName().equals(name)) {
                res = Optional.of(param.getValue());
            }
        }
        return res;
    }
}

