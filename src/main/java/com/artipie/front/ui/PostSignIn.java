/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

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
        PostSignIn.checkCrsf(req);
        this.receiveToken(req, rsp);
        return "Ok";
    }

    /**
     * Check crsf.
     * @param req Request.
     */
    private static void checkCrsf(final Request req) {
        final String crsf = req.session().attribute("crsf");
        req.session().removeAttribute("crsf");
        final var valid = Objects.equals(
            req.queryParamOrDefault("_crsf", ""), crsf
        );
        if (!valid) {
            Spark.halt(HttpStatus.BAD_REQUEST_400, "CRSF validation failed");
        }
    }

    /**
     * Receives JWT-token.
     * @param req Request.
     * @param rsp Response.
     */
    private void receiveToken(final Request req, final Response rsp) {
        final String token = this.auth.getJwtToken(
            req.queryParamOrDefault("username", ""),
            req.queryParamOrDefault("password", "")
        );
        req.session().attribute("token", token);
        req.session().attribute("uid", deleteBySlash(req.queryParamOrDefault("username", "")));
        rsp.redirect("/dashboard");
    }

    /**
     * Delete all characters by slash if string contains slash.
     * @param str String.
     * @return String with deleted characters by slash.
     */
    private static String deleteBySlash(final String str) {
        final String result;
        if (str.contains("/")) {
            result = str.substring(str.lastIndexOf('/') + 1);
        } else {
            result = str;
        }
        return result;
    }
}
