/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `GET` request to obtain repositories list.
 * @since 0.1
 */
public final class Repositories implements Route {

    /**
     * Artipie repositories settings storage.
     */
    private final BlockingStorage storage;

    /**
     * Ctor.
     * @param storage Artipie repositories settings storage
     */
    public Repositories(final BlockingStorage storage) {
        this.storage = storage;
    }

    @Override
    public String handle(final Request request, final Response response) {
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (final Key key : this.storage.list(Key.ROOT)) {
            final String name = key.string();
            if (name.endsWith(".yaml") || name.endsWith(".yml")) {
                json = json.add(
                    Json.createObjectBuilder()
                        .add("fullName", name.replaceAll("\\.yaml|\\.yml", "")).build()
                );
            }
        }
        response.type("application/json");
        return json.build().toString();
    }
}
