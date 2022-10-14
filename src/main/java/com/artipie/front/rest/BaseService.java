/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.front.settings.ArtipieEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Base rest-service.
 * @since 1.0
 */
public class BaseService {
    /**
     * Json object mapper.
     * @checkstyle AvoidFieldNameMatchingMethodName (2 lines)
     */
    private final ObjectMapper mapper;

    /**
     * Artipie rest URL.
     */
    private final String artipieurl;

    /**
     * Ctor.
     * @param endpoint Artipie endpoint configuration.
     */
    public BaseService(final ArtipieEndpoint endpoint) {
        this.mapper = new ObjectMapper();
        this.artipieurl = String.format(
            "%s://%s:%s",
            // @checkstyle AvoidInlineConditionalsCheck (1 line)
            endpoint.isSecure() ? "https" : "http",
            endpoint.getHost(),
            endpoint.getPort()
        );
    }

    /**
     * Gets json object mapper.
     * @return Json object mapper.
     */
    public ObjectMapper mapper() {
        return this.mapper;
    }

    /**
     * Gets artipie rest URL.
     * @return Artipie rest URL.
     */
    public String getArtipieUrl() {
        return this.artipieurl;
    }

    /**
     * Gets uri to artipie rest resource.
     * @param path Absolute path to rest resource.
     * @return Artipie rest resource URI.
     * @throws URISyntaxException if there is syntax error.
     */
    public URI uri(final String path) throws URISyntaxException {
        return new URI(String.format("%s/%s", this.getArtipieUrl(), path));
    }
}
