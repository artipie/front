/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

import com.artipie.front.RequestAttr;
import com.artipie.front.misc.RouteWrap;
import com.artipie.front.settings.RepoSettings;
import java.util.Map;
import java.util.Optional;

/**
 * Dashboard user page.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class UserPage extends RouteWrap.TemplateViewRoute {

    /**
     * New user page.
     * @param repos Artipie repository settings
     */
    public UserPage(final RepoSettings repos) {
        super(
            new HbPage(
                "user",
                req -> {
                    final String uid = RequestAttr.Standard.USER_ID.readOrThrow(req);
                    return Map.of(
                        "user", uid, "title", uid,
                        "repos", repos.list(Optional.of(uid))
                    );
                }
            )
        );
    }
}
