/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

import com.artipie.front.auth.AuthByPassword;
import com.artipie.front.rest.AuthService;
import io.vavr.Tuple3;
import java.util.Objects;
import java.util.Optional;
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
     * Password authenticator.
     */
    private final AuthByPassword authpwd;

    /**
     * Auth service.
     */
    private final AuthService auth;

    /**
     * New signin form processor.
     * @param auth Auth service.
     * @param authpwd Password authentication
     */
    public PostSignIn(final AuthService auth, final AuthByPassword authpwd) {
        this.auth = auth;
        this.authpwd = authpwd;
    }

    @Override
    public Object handle(final Request req, final Response rsp) throws Exception {
        if (req.session() == null) {
            Spark.halt(HttpStatus.BAD_REQUEST_400, "session is empty");
        }
        PostSignIn.checkCrsf(req);
        this.authenticate(req);
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
     * Authenticate by username and password.
     * Redirect to '/dashboard' if JWT-token successful received.
     * @param req Request.
     */
    private void authenticate(final Request req) {
        final Optional<String> uid = this.authpwd.authenticate(
            req.queryParamOrDefault("username", ""),
            req.queryParamOrDefault("password", "")
        );
        uid.ifPresentOrElse(
            val -> {
                req.session().attribute("uid", val);
                req.session().attribute("uname", req.queryParamOrDefault("username", ""));
            },
            () -> Spark.halt(HttpStatus.UNAUTHORIZED_401, "bad credentials")
        );
    }

    /**
     * Receives JWT-token.
     * @param req Request.
     * @param rsp Response.
     */
    private void receiveToken(final Request req, final Response rsp) {
        final Tuple3<Integer, String, String> res = this.auth.getJwtToken(
            req.queryParamOrDefault("username", ""),
            req.queryParamOrDefault("password", "")
        );
        if (res._3 == null) {
            req.session().attribute("token", res._2);
            rsp.redirect("/dashboard");
        } else {
            if (res._1 == HttpStatus.UNAUTHORIZED_401) {
                Spark.halt(
                    HttpStatus.UNAUTHORIZED_401,
                    "Cannot receive JWT-token: bad credentials"
                );
            } else {
                Spark.halt(res._1, res._3);
            }
        }
    }
}
