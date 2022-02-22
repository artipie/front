/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.misc.Yaml2Json;
import java.util.Map;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `GET` request to obtain users list.
 * @since 0.1
 */
public final class Users implements Route {

    /**
     * Credentials yaml mapping string.
     */
    private final Optional<String> yaml;

    /**
     * Ctor.
     * @param creds Credentials yaml mapping
     */
    public Users(final Optional<String> creds) {
        this.yaml = creds;
    }

    /**
     * Ctor.
     * @param creds Credentials yaml mapping
     */
    public Users(final String creds) {
        this(Optional.of(creds));
    }

    @Override
    public String handle(final Request request, final Response response) {
        JsonObjectBuilder res = Json.createObjectBuilder();
        if (this.yaml.isPresent()) {
            final JsonObject all = new Yaml2Json().apply(this.yaml.get())
                .asJsonObject().getJsonObject("credentials");
            for (final Map.Entry<String, JsonValue> item : all.entrySet()) {
                JsonObjectBuilder user = Json.createObjectBuilder();
                // @checkstyle LineLengthCheck (1 line)
                for (final Map.Entry<String, JsonValue> val : item.getValue().asJsonObject().entrySet()) {
                    if (!"type".equals(val.getKey()) && !"pass".equals(val.getKey())) {
                        user = user.add(val.getKey(), val.getValue());
                    }
                }
                res = res.add(item.getKey(), user.build());
            }
        }
        return res.build().toString();
    }
}
