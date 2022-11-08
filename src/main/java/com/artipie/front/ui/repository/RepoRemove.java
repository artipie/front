/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.artipie.front.misc.RouteWrap;
import com.artipie.front.rest.RepositoryName;
import com.artipie.front.rest.RepositoryService;
import com.artipie.front.rest.SettingsService;
import com.artipie.front.ui.HbPage;
import java.util.Map;

/**
 * Repository remove POST-handler.
 * Removes repository and shows result of on page.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RepoRemove extends RouteWrap.TemplateViewRoute {
    /**
     * Repository delete.
     *
     * @param repository Repository service.
     * @param settings Settings service.
     */
    public RepoRemove(final RepositoryService repository, final SettingsService settings) {
        super(
            new HbPage(
                "repository/result",
                req -> {
                    final RepositoryName rname = new RepositoryName.FromRequest(
                        req,
                        settings.layout()
                    );
                    return Map.of(
                        "title", String.format("Repository %s", rname),
                        "result", repository.remove(req.session().attribute("token"), rname),
                        "redirectUrl", "/dashboard/repository/list",
                        "redirectMessage", "Continue"
                    );
                }
            )
        );
    }
}

