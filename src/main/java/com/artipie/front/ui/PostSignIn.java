/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

import com.artipie.front.rest.AuthService;
import com.artipie.front.rest.BaseService;
import com.artipie.front.settings.ArtipieEndpoint;
import java.util.Objects;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * Signin form POST handler.
 * @since 1.0
 * @checkstyle AvoidDuplicateLiterals (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class PostSignIn implements Route {
    /**
     * Auth service.
     */
    private final AuthService auth;

    /**
     * New signin form processor.
     * @param endpoint Endpoint.
     */
    public PostSignIn(final ArtipieEndpoint endpoint) {
        this.auth = new AuthService(
            new BaseService.Opts()
                .setHost(endpoint.getHost())
                .setPort(endpoint.getPort())
                .setSecure(endpoint.isSecure())
        );
    }

    @Override
    public Object handle(final Request req, final Response rsp) throws Exception {
        if (req.session() == null) {
            Spark.halt(HttpStatus.BAD_REQUEST_400, "session is empty");
        }
        final String crsf = req.session().attribute("crsf");
        req.session().removeAttribute("crsf");
        final var valid = Objects.equals(
            req.queryParamOrDefault("_crsf", ""), crsf
        );
        if (!valid) {
            Spark.halt(HttpStatus.BAD_REQUEST_400, "CRSF validation failed");
        }
        return this.auth.getJwtToken(
            new AuthService.AuthUser()
                .setName(req.queryParamOrDefault("username", ""))
                .setPass(req.queryParamOrDefault("password", ""))
        ).thenApply(
            token -> {
                req.session().attribute("uid", "");
                req.session().attribute("token", token.getToken());
                rsp.redirect("/dashboard");
                return "OK";
            }
        ).exceptionally(
            exception -> {
                Spark.halt(HttpStatus.UNAUTHORIZED_401, "bad credentials");
                return null;
            }
        ).join();
    }
}
