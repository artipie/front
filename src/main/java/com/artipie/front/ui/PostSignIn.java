/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

import com.artipie.ArtipieException;
import com.artipie.front.rest.AuthService;
import java.util.Objects;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * Signin form POST handler.
 *
 * @checkstyle AvoidDuplicateLiterals (500 lines)
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class PostSignIn implements Route {
    /**
     * Auth service.
     */
    private final AuthService auth;

    /**
     * New signin form processor.
     *
     * @param auth Auth service.
     */
    public PostSignIn(final AuthService auth) {
        this.auth = auth;
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
        String result = null;
        try {
            final String token = this.auth.getJwtToken(
                new AuthService.AuthUser(
                    req.queryParamOrDefault("username", ""),
                    req.queryParamOrDefault("password", "")
                )
            );
            req.session().attribute("uid", "");
            req.session().attribute("uname", req.queryParamOrDefault("username", ""));
            req.session().attribute("token", token);
            rsp.redirect("/dashboard/repository/list");
            result = "OK";
        } catch (final ArtipieException exception) {
            Spark.halt(HttpStatus.UNAUTHORIZED_401, "bad credentials");
        }
        return result;
    }
}
