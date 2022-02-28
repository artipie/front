/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.artipie.front.misc.Json2Yaml;
import com.artipie.front.settings.ArtipieYaml;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `PUT` request to create user.
 * @since 0.1
 * @checkstyle ReturnCountCheck (500 lines)
 */
public final class PutUser implements Route {

    /**
     * Artipie yaml settings.
     */
    private final ArtipieYaml artipie;

    /**
     * Ctor.
     * @param artipie Artipie yaml settings
     */
    public PutUser(final ArtipieYaml artipie) {
        this.artipie = artipie;
    }

    @Override
    @SuppressWarnings("PMD.OnlyOneReturn")
    public Object handle(final Request request, final Response response) {
        if (this.artipie.fileCredentialsKey().isEmpty()) {
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return "Authorization type `file` is not configured, cannot add user";
        }
        final String name = request.params(GetUser.USER_PARAM);
        if (this.artipie.credentials().user(name).isPresent()) {
            response.status(HttpStatus.CONFLICT_409);
            return String.format("User %s already exists", name);
        }
        final JsonObject user = Json.createReader(new StringReader(request.body())).readObject()
            .getJsonObject(name);
        if (user == null) {
            response.status(HttpStatus.BAD_REQUEST_400);
            return "User info json is expected";
        }
        if (!user.containsKey("type")) {
            response.status(HttpStatus.BAD_REQUEST_400);
            return "Password type field `type` is required";
        }
        if (!user.containsKey("pass")) {
            response.status(HttpStatus.BAD_REQUEST_400);
            return "Password field `pass` is required";
        }
        this.update(user, name);
        response.status(HttpStatus.CREATED_201);
        return null;
    }

    /**
     * Updates credentials file by adding new user.
     * @param obj Info of the new user
     * @param name New username
     */
    private void update(final JsonObject obj, final String name) {
        YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
        final Optional<YamlMapping> creds = this.artipie.fileCredentials()
            .map(yaml -> yaml.yamlMapping(ArtipieYaml.NODE_CREDENTIALS));
        if (creds.isPresent()) {
            for (final YamlNode node : creds.get().keys()) {
                final String val = node.asScalar().value();
                builder = builder.add(val, creds.get().yamlMapping(val));
            }
        }
        builder = builder.add(name, new Json2Yaml().apply(obj.toString()));
        this.artipie.storage().save(
            this.artipie.fileCredentialsKey().get(),
            Yaml.createYamlMappingBuilder().add(ArtipieYaml.NODE_CREDENTIALS, builder.build())
                .build().toString().getBytes(StandardCharsets.UTF_8)
        );
    }
}
