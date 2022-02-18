/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.front.misc.Yaml2Json;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `GET` request to obtain repository setings.
 * @since 0.1
 */
public final class GetRepository implements Route {

    /**
     * Repository name request parameter.
     */
    public static final String PARAM = ":name";

    /**
     * Repository settings yaml secton `repo` name.
     */
    private static final String REPO = "repo";

    /**
     * Repositories settings storage.
     */
    private final BlockingStorage repos;

    /**
     * Ctor.
     * @param repos Repositories settings storage
     */
    public GetRepository(final BlockingStorage repos) {
        this.repos = repos;
    }

    @Override
    public String handle(final Request request, final Response response) {
        final JsonObject repo = new Yaml2Json().apply(
            new String(
                this.repos.value(this.find(request.params(GetRepository.PARAM))),
                StandardCharsets.UTF_8
            )
        ).asJsonObject().getJsonObject(GetRepository.REPO);
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (final Map.Entry<String, JsonValue> item : repo.entrySet()) {
            if (!"permissions".equals(item.getKey())) {
                builder = builder.add(item.getKey(), item.getValue());
            }
        }
        response.type("application/json");
        return Json.createObjectBuilder().add(GetRepository.REPO, builder.build())
            .build().toString();
    }

    /**
     * Find repository settings key.
     * @param name Repo name
     * @return Key if such repository exists, throws exception if not
     */
    private Key find(final String name) {
        Key res = new Key.From(String.format("%s.yaml", name));
        if (!this.repos.exists(res)) {
            res = new Key.From(String.format("%s.yml", name));
            if (!this.repos.exists(res)) {
                throw new IllegalArgumentException("Repository not found");
            }
        }
        return res;
    }
}
