/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.ArtipieException;
import com.artipie.front.settings.ArtipieEndpoint;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.JacksonFactory;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Base rest-service.
 * @since 1.0
 */
public class BaseService {
    /**
     * Artipie endpoint configuration.
     */
    private final ArtipieEndpoint endpoint;

    /**
     * Ctor.
     * @param endpoint Artipie endpoint configuration.
     */
    public BaseService(final ArtipieEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Sends http-request.
     * @param method Http-method.
     * @param path Path.
     * @return Http-response.
     */
    public CompletableFuture<HttpResponse<Buffer>> send(final HttpMethod method,
        final String path) {
        return this.send(method, path, Optional.empty(), Optional.empty());
    }

    /**
     * Sends http-request.
     * @param method Http-method.
     * @param path Path.
     * @param body Json-body.
     * @return Http-response.
     */
    public CompletableFuture<HttpResponse<Buffer>> send(final HttpMethod method, final String path,
        final Optional<JsonObject> body) {
        return this.send(method, path, body, Optional.empty());
    }

    /**
     * Sends http-request.
     * @param method Http-method.
     * @param path Path.
     * @param body Json-body.
     * @param token Token.
     * @return Http-response.
     * @checkstyle ParameterNumberCheck (4 lines)
     */
    public CompletableFuture<HttpResponse<Buffer>> send(final HttpMethod method, final String path,
        final Optional<JsonObject> body, final Optional<String> token) {
        final HttpRequest<Buffer> request = this.createWebClient().request(
            method,
            this.endpoint.getPort(),
            this.endpoint.getHost(),
            path
        );
        token.ifPresent(request::bearerTokenAuthentication);
        final Future<HttpResponse<Buffer>> response;
        if (body.isPresent()) {
            response = request.sendJsonObject(body.get());
        } else {
            response = request.send();
        }
        response.onFailure(
            exc -> {
                throw new ArtipieException(exc.getMessage());
            }
        );
        return response.toCompletionStage().toCompletableFuture();
    }

    /**
     * Sends sync http-request.
     * @param method Http-method.
     * @param path Path.
     * @param body Json-body.
     * @return Http-response.
     */
    public HttpResponse<Buffer> sendSync(final HttpMethod method, final String path,
        final Optional<JsonObject> body) {
        return this.sendSync(method, path, body, Optional.empty());
    }

    /**
     * Sends sync http-request.
     * @param method Http-method.
     * @param path Path.
     * @param body Json-body.
     * @param token Token.
     * @return Http-response.
     * @checkstyle ParameterNumberCheck (4 lines)
     */
    public HttpResponse<Buffer> sendSync(final HttpMethod method, final String path,
        final Optional<JsonObject> body, final Optional<String> token) {
        return this.send(method, path, body, token).join();
    }

    /**
     * Encodes object to JsonObject.
     * @param object Object.
     * @return JsonObject.
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static JsonObject toJsonObject(final Object object) {
        return new JsonObject(JacksonFactory.CODEC.toBuffer(object));
    }

    /**
     * Encodes JsonObject to Object of specified type.
     * @param json JsonObject.
     * @param clazz Target object type.
     * @param <T> type of target object.
     * @return Object of specified type.
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static <T> T toObject(final JsonObject json, final Class<T> clazz) {
        return JacksonFactory.CODEC.fromValue(json, clazz);
    }

    /**
     * Creates WebClient.
     * @return WebClient.
     */
    protected WebClient createWebClient() {
        final WebClientOptions webopts = new WebClientOptions()
            .setUserAgent("artipie-front");
        if (this.endpoint.isSecure()) {
            webopts
                .setSsl(true)
                .setTrustAll(true);
        }
        return WebClient.create(Vertx.vertx(), webopts);
    }
}
