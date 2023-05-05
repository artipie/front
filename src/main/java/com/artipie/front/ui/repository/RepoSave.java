/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.artipie.front.misc.RouteWrap;
import com.artipie.front.rest.RepositoryName;
import com.artipie.front.rest.RepositoryService;
import com.artipie.front.ui.HbPage;
import java.util.Map;

/**
 * Repository configuration saver.
 * Saves repository configuration and shows result of saving on page.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RepoSave extends RouteWrap.TemplateViewRoute {
    /**
     * List of repositories page.
     *
     * @param repository Repository service.
     */
    public RepoSave(final RepositoryService repository) {
        super(
            new HbPage(
                "repository/result",
                req -> {
                    final RepositoryName rname = new RepositoryName.FromRequest(req);
                    return Map.of(
                        "title", String.format("Repository %s", rname), "result",
                        repository.save(
                            req.session().attribute("token"), rname, req.queryParams("config")
                        ),
                        "redirectUrl", String.format("/dashboard/repository/edit/%s", rname),
                        "redirectMessage", "Continue"
                    );
                }
            )
        );
    }
}

