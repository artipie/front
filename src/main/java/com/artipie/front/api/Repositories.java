/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.settings.RepoSettings;
import java.util.Optional;
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
     * Artipie repositories settings.
     */
    private final RepoSettings stngs;

    /**
     * Ctor.
     * @param storage Artipie repositories settings storage
     */
    public Repositories(final RepoSettings storage) {
        this.stngs = storage;
    }

    @Override
    public String handle(final Request request, final Response response) {
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (final String name : this.stngs.list(Optional.empty())) {
            json = json.add(Json.createObjectBuilder().add("fullName", name).build());
        }
        response.type("application/json");
        return json.build().toString();
    }
}
