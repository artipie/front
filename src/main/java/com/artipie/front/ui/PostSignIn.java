/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

import java.util.Objects;
import java.util.Optional;
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
     * Password authenticator.
     */
    private final PasswordAuthenticator auth;

    /**
     * New signin form processor.
     * @param auth Password auth
     */
    public PostSignIn(final PasswordAuthenticator auth) {
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
        final var uid = this.auth.authenticate(
            req.queryParamOrDefault("username", ""),
            req.queryParamOrDefault("password", "")
        );
        uid.ifPresentOrElse(
            val -> req.session().attribute("uid", val),
            () -> Spark.halt(HttpStatus.UNAUTHORIZED_401, "bad credentials")
        );
        return "OK";
    }

    /**
     * User name authentication function.
     * @since 1.0
     */
    @FunctionalInterface
    public interface PasswordAuthenticator {

        /**
         * Authenticate user by password.
         * @param user Login
         * @param password Password
         * @return User ID if found
         */
        Optional<String> authenticate(String user, String password);
    }
}
