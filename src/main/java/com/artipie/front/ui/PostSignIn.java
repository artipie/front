/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

import com.artipie.front.rest.AuthService;
import io.vavr.Tuple3;
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
        final Tuple3<Integer, String, String> res = this.auth.getJwtToken(
            req.queryParamOrDefault("username", ""),
            req.queryParamOrDefault("password", "")
        );
        if (res._3 == null) {
            req.session().attribute("uid", "");
            req.session().attribute("uname", req.queryParamOrDefault("username", ""));
            req.session().attribute("token", res._2);
            rsp.redirect("/dashboard");
            result = "OK";
        } else {
            if (res._1 == HttpStatus.UNAUTHORIZED_401) {
                Spark.halt(HttpStatus.UNAUTHORIZED_401, "bad credentials");
            } else {
                Spark.halt(res._1, res._3);
            }
        }
        return result;
    }
}
