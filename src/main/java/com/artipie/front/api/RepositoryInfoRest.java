/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.ui.repository.RepositoryInfo;
import java.io.StringReader;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.eclipse.jetty.http.MimeTypes;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Endpoint to obtain repository info. Accepts GET request with json body
 * with fields `name` and `type` and returns json with `content` field.
 *
 * @since 1.0
 */
public final class RepositoryInfoRest implements Route {
    /**
     * Repository info.
     */
    private final RepositoryInfo info;

    /**
     * Ctor.
     *
     * @param info Repository info
     */
    public RepositoryInfoRest(final RepositoryInfo info) {
        this.info = info;
    }

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public String handle(final Request req, final Response res) {
        final JsonObject json = Json.createReader(new StringReader(req.body())).readObject();
        final String type = json.getString("type");
        res.type(MimeTypes.Type.APPLICATION_JSON.toString());
        return Json.createObjectBuilder().add(
            "content", this.info.render(
                type,
                Map.of(
                    "user", req.session().attribute("uid"),
                    "repo", json.getString("name"),
                    "type", type
                )
            )
        ).build().toString();
    }
}

