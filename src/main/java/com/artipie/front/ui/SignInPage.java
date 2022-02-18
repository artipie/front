/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

import com.artipie.front.misc.RouteWrap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Sign in page.
 * @since 1.0
 * @checkstyle AvoidDuplicateLiterals (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class SignInPage extends RouteWrap.TemplateViewRoute {

    /**
     * New sign-in page.
     */
    public SignInPage() {
        super(
            new HbPage(
                "signin",
                req -> {
                    final var crsf = Optional.<String>ofNullable(
                        req.session(true).attribute("crsf")
                    ).orElseGet(() -> UUID.randomUUID().toString());
                    req.session(true).attribute("crsf", crsf);
                    return Map.of("title", "Sign in", "crsf", crsf);
                }
            )
        );
    }
}
