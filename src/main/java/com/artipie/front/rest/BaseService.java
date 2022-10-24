/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.ArtipieException;
import com.artipie.front.misc.Json2Yaml;
import com.artipie.front.settings.ArtipieEndpoint;
import com.google.common.net.HttpHeaders;
import io.vavr.Tuple3;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletResponse;

/**
 * Base rest-service.
 * @since 1.0
 */
@SuppressWarnings({"PMD.DataClass", "PMD.AvoidFieldNameMatchingMethodName", "PMD.TooManyMethods"})
public class BaseService {
    /**
     * Application json content-type value.
     */
    public static final String APPLICATION_JSON = "application/json";

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
     * Invokes GET http request.
     * @param token JWT token.
     * @param path Path in URL.
     * @return Http response.
     */
    protected HttpResponse<String> httpGet(final Optional<String> token, final String path) {
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
     * @param token JWT token.
     * @param path Path in URL.
     * @param payload Payload supplier.
     * @return Http response.
     */
    protected HttpResponse<String> httpPost(final Optional<String> token, final String path,
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
     * Invokes POST http request.
     * @param token JWT token.
     * @param path Path in URL.
     * @param payload Payload supplier.
     * @return Http response.
     */
    protected HttpResponse<String> httpPut(final Optional<String> token, final String path,
        final Supplier<String> payload) {
        try {
            return HttpClient.newBuilder()
                .build()
                .send(
                    this.createPutRequest(token, path, payload),
                    HttpResponse.BodyHandlers.ofString()
                );
        } catch (final IOException | InterruptedException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Invokes DELETE http request.
     * @param token JWT token.
     * @param path Path in URL.
     * @return Http response.
     */
    protected HttpResponse<String> httpDelete(final Optional<String> token, final String path) {
        try {
            return HttpClient.newBuilder()
                .build()
                .send(
                    this.createDeleteRequest(token, path),
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
    protected HttpRequest createGetRequest(final Optional<String> token, final String path) {
        try {
            final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(this.uri(path))
                .GET()
                .header(HttpHeaders.ACCEPT, BaseService.APPLICATION_JSON)
                .timeout(BaseService.TIMEOUT);
            token.ifPresent(value -> builder.header(HttpHeaders.AUTHORIZATION, bearer(value)));
            return builder.build();
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
    protected HttpRequest createPostRequest(final Optional<String> token, final String path,
        final Supplier<String> payload) {
        try {
            final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(this.uri(path))
                .POST(HttpRequest.BodyPublishers.ofString(payload.get()))
                .header(HttpHeaders.ACCEPT, BaseService.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, BaseService.APPLICATION_JSON)
                .timeout(BaseService.TIMEOUT);
            token.ifPresent(value -> builder.header(HttpHeaders.AUTHORIZATION, bearer(value)));
            return builder.build();
        } catch (final URISyntaxException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Creates PUT http request.
     * @param token JWT token.
     * @param path Path in URL.
     * @param payload Payload supplier.
     * @return Http request.
     */
    protected HttpRequest createPutRequest(final Optional<String> token, final String path,
        final Supplier<String> payload) {
        try {
            final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(this.uri(path))
                .PUT(HttpRequest.BodyPublishers.ofString(payload.get()))
                .header(HttpHeaders.ACCEPT, BaseService.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, BaseService.APPLICATION_JSON)
                .timeout(BaseService.TIMEOUT);
            token.ifPresent(value -> builder.header(HttpHeaders.AUTHORIZATION, bearer(value)));
            return builder.build();
        } catch (final URISyntaxException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Creates DELETE http request.
     * @param token JWT token.
     * @param path Path in URL.
     * @return Http request.
     */
    protected HttpRequest createDeleteRequest(final Optional<String> token, final String path) {
        try {
            final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(this.uri(path))
                .DELETE()
                .header(HttpHeaders.ACCEPT, BaseService.APPLICATION_JSON)
                .timeout(BaseService.TIMEOUT);
            token.ifPresent(value -> builder.header(HttpHeaders.AUTHORIZATION, bearer(value)));
            return builder.build();
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

    /**
     * Convert response json-body to yaml.
     * @param response Response.
     * @return Yaml content.
     */
    protected static String toYaml(final HttpResponse<String> response) {
        return new Json2Yaml().apply(BaseService.jsonObject(response).toString())
            .toString();
    }

    /**
     * Strip leading and ending quotes.
     * @param str String with leading and ending quotes or without them.
     * @return Stripped string.
     */
    protected static String stripQuotes(final String str) {
        String result = str;
        if (str.length() > 1 && str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
            result = str.substring(1, str.length() - 1);
        }
        return result;
    }

    /**
     * Check response result code and forms triple-tuple:
     *   1. response status code.
     *   2. resulting content in case success result code
     *   3. error message based on response content in case receiveing of error in status code.
     * @param response Response.
     * @param map Map-function to form resulting content in case successful status code.
     * @param <V> Type of resulting of map-function.
     * @return Triple-tuple.
     */
    protected static <V> Tuple3<Integer, V, String> handle(final HttpResponse<String> response,
        final Function<HttpResponse<String>, V> map) {
        return handle(HttpServletResponse.SC_OK, response, map);
    }

    /**
     * Check response result code and forms triple-tuple:
     *   1. response status code.
     *   2. resulting content in case success result code
     *   3. error message based on response content in case receiveing of error in status code.
     * @param success Expected success result code.
     * @param response Response.
     * @param map Map-function to form resulting content in case successful status code.
     * @param <V> Type of resulting of map-function.
     * @return Triple-tuple.
     */
    protected static <V> Tuple3<Integer, V, String> handle(final int success,
        final HttpResponse<String> response, final Function<HttpResponse<String>, V> map) {
        V content = null;
        String error = null;
        if (success == response.statusCode()) {
            content = map.apply(response);
        } else {
            error = response.body();
        }
        return new Tuple3<>(response.statusCode(), content, error);
    }

    /**
     * Join path-parts.
     * @param parts Parts of path.
     * @return Path as joined by '/'-symbol path-parts.
     */
    protected static String path(final Object...parts) {
        final StringBuilder builder = new StringBuilder();
        for (final Object path : parts) {
            builder.append(path).append('/');
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }
}
