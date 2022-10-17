/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.ArtipieException;
import com.artipie.front.settings.ArtipieEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Base rest-service.
 * @since 1.0
 */
@SuppressWarnings({"PMD.DataClass", "PMD.AvoidFieldNameMatchingMethodName", "PMD.TooManyMethods"})
public class BaseService {
    /**
     * Authorization header.
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * Http request timeout.
     */
    private static final Duration TIMEOUT = Duration.of(1, ChronoUnit.MINUTES);

    /**
     * Artipie rest URL.
     */
    private final String artipieurl;

    /**
     * Ctor.
     * @param endpoint Artipie endpoint configuration.
     */
    public BaseService(final ArtipieEndpoint endpoint) {
        this.artipieurl = endpoint.url();
    }

    /**
     * Gets artipie rest URL.
     * @return Artipie rest URL.
     */
    protected String getArtipieUrl() {
        return this.artipieurl;
    }

    /**
     * Gets uri to artipie rest resource.
     * @param path Absolute path to rest resource.
     * @return Artipie rest resource URI.
     * @throws URISyntaxException if there is syntax error.
     */
    protected URI uri(final String path) throws URISyntaxException {
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

    /**
     * Invokes GET http request.
     * @param path Path in URL.
     * @return Http response.
     */
    protected HttpResponse<String> httpGet(final String path) {
        try {
            return HttpClient.newBuilder()
                .build()
                .send(
                    this.createGetRequest(path),
                    HttpResponse.BodyHandlers.ofString()
                );
        } catch (final IOException | InterruptedException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Invokes GET http request.
     * @param token JWT token.
     * @param path Path in URL.
     * @return Http response.
     */
    protected HttpResponse<String> httpGet(final String token, final String path) {
        try {
            return HttpClient.newBuilder()
                .build()
                .send(
                    this.createGetRequest(token, path),
                    HttpResponse.BodyHandlers.ofString()
                );
        } catch (final IOException | InterruptedException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Invokes POST http request.
     * @param path Path in URL.
     * @param payload Payload supplier.
     * @return Http response.
     */
    protected HttpResponse<String> httpPost(final String path, final Supplier<String> payload) {
        try {
            return HttpClient.newBuilder()
                .build()
                .send(
                    this.createPostRequest(path, payload),
                    HttpResponse.BodyHandlers.ofString()
                );
        } catch (final IOException | InterruptedException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Invokes POST http request.
     * @param token JWT token.
     * @param path Path in URL.
     * @param payload Payload supplier.
     * @return Http response.
     */
    protected HttpResponse<String> httpPost(final String token, final String path,
        final Supplier<String> payload) {
        try {
            return HttpClient.newBuilder()
                .build()
                .send(
                    this.createPostRequest(token, path, payload),
                    HttpResponse.BodyHandlers.ofString()
                );
        } catch (final IOException | InterruptedException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Creates GET http request.
     * @param token JWT token.
     * @param path Path in URL.
     * @return Http request.
     */
    protected HttpRequest createGetRequest(final String token, final String path) {
        try {
            return HttpRequest.newBuilder()
                .uri(this.uri(path))
                .GET()
                .timeout(BaseService.TIMEOUT)
                .header(BaseService.AUTHORIZATION, bearer(token))
                .build();
        } catch (final URISyntaxException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Creates GET http request.
     * @param path Path in URL.
     * @return Http request.
     */
    protected HttpRequest createGetRequest(final String path) {
        try {
            return HttpRequest.newBuilder()
                .uri(this.uri(path))
                .GET()
                .timeout(BaseService.TIMEOUT)
                .build();
        } catch (final URISyntaxException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Creates POST http request.
     * @param path Path in URL.
     * @param payload Payload supplier.
     * @return Http request.
     */
    protected HttpRequest createPostRequest(final String path, final Supplier<String> payload) {
        try {
            return HttpRequest.newBuilder()
                .uri(this.uri(path))
                .POST(HttpRequest.BodyPublishers.ofString(payload.get()))
                .timeout(BaseService.TIMEOUT)
                .build();
        } catch (final URISyntaxException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Creates POST http request.
     * @param token JWT token.
     * @param path Path in URL.
     * @param payload Payload supplier.
     * @return Http request.
     */
    protected HttpRequest createPostRequest(final String token, final String path,
        final Supplier<String> payload) {
        try {
            return HttpRequest.newBuilder()
                .uri(this.uri(path))
                .POST(HttpRequest.BodyPublishers.ofString(payload.get()))
                .header(BaseService.AUTHORIZATION, bearer(token))
                .timeout(BaseService.TIMEOUT)
                .build();
        } catch (final URISyntaxException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Reads response body as json-object.
     * @param response Response.
     * @return JsonObject.
     */
    protected static JsonObject jsonObject(final HttpResponse<String> response) {
        return Json.createReader(new StringReader(response.body())).readObject();
    }

    /**
     * Reads response body as json-array.
     * @param response Response.
     * @return JsonArray.
     */
    protected static JsonArray jsonArray(final HttpResponse<String> response) {
        return Json.createReader(new StringReader(response.body())).readArray();
    }
}
