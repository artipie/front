/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.auth.ApiTokens;
import com.artipie.front.auth.AuthByPassword;
import java.io.StringReader;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Endpoint to generate token for user. Accepts GET request with json body
 * with fields `name` and `pass` and returns json with `token` field.
 * @since 0.1
 */
public final class GetToken implements Route {

    /**
     * Password-based authentication.
     */
    private final AuthByPassword auth;

    /**
     * Tokens.
     */
    private final ApiTokens tkn;

    /**
     * Ctor.
     * @param auth Password-based authentication
     * @param tkn Tokens
     */
    public GetToken(final AuthByPassword auth, final ApiTokens tkn) {
        this.auth = auth;
        this.tkn = tkn;
    }

    @Override
    public String handle(final Request request, final Response response) {
        final JsonObject json = Json.createReader(new StringReader(request.body())).readObject();
        final Optional<String> usr =
            this.auth.authenticate(json.getString("name"), json.getString("pass"));
        final JsonObject res;
        response.type(MimeTypes.Type.APPLICATION_JSON.toString());
        if (usr.isEmpty()) {
            response.status(HttpStatus.UNAUTHORIZED_401);
            res = Json.createObjectBuilder().add("err", "Invalid credentials").build();
        } else {
            response.status(HttpStatus.CREATED_201);
            res = Json.createObjectBuilder().add(
                // @checkstyle MagicNumberCheck (1 line)
                "token", this.tkn.token(usr.get(), Instant.now().plus(30, ChronoUnit.DAYS))
            ).build();
        }
        return res.toString();
    }
}
