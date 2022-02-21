/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.RequestAttr;
import com.artipie.front.misc.RepoSettings;
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
 * Handle `GET` request to obtain repository settings.
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
     * Repository settings.
     */
    private final RepoSettings stn;

    /**
     * Ctor.
     * @param stn Repository settings
     */
    public GetRepository(final RepoSettings stn) {
        this.stn = stn;
    }

    @Override
    public String handle(final Request request, final Response response) {
        final JsonObject repo = new Yaml2Json().apply(
            new String(
                this.stn.value(
                    request.params(GetRepository.PARAM),
                    RequestAttr.Standard.USER_ID.readOrThrow(request)
                ),
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
}
