/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.ArtipieException;
import com.artipie.front.settings.ArtipieEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

/**
 * Base rest-service.
 * @since 1.0
 */
@SuppressWarnings({"PMD.DataClass", "PMD.AvoidFieldNameMatchingMethodName"})
public class BaseService {
    /**
     * Authorization header.
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * Authorization header.
     */
    public static final int SUCCESS = 200;

    /**
     * Json object mapper.
     * @checkstyle AvoidFieldNameMatchingMethodName (5 lines)
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

    /**
     * Bearer for authorization header.
     * @param token Token.
     * @return Bearer for authorization header
     */
    protected static String bearer(final String token) {
        return String.format("Bearer %s", token);
    }

    /**
     * Checks response status.
     * @param expected Expected status code.
     * @param response Response.
     * @throws ArtipieException In case mismatch expected status code in response.
     */
    protected static void checkStatus(final int expected, final HttpResponse<String> response)
        throws ArtipieException {
        if (response.statusCode() != expected) {
            throw new ArtipieException(
                String.format(
                    "Expected %s result code, but received %s", expected, response.statusCode()
                )
            );
        }
    }
}
