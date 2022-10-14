/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository.org;

import com.artipie.front.misc.RouteWrap;
import com.artipie.front.rest.RepositoryService;
import com.artipie.front.ui.HbPage;
import java.util.Map;

/**
 * List of user repositories page.
 *
 * @checkstyle AvoidDuplicateLiterals (500 lines)
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RepoListByUser extends RouteWrap.TemplateViewRoute {
    /**
     * List of user repositories page.
     *
     * @param repository Repository service.
     */
    public RepoListByUser(final RepositoryService repository) {
        super(
            new HbPage(
                "repository/org/list",
                req -> {
                    String error = "";
                    final String uname = req.session().attribute("uname");
                    if (!uname.equals(req.params(":uname"))) {
                        error = "Access denied";
                    }
                    return Map.of(
                        "title", String.format("Repository list by %s", uname),
                        "uname", uname,
                        "repos", repository.list(req.session().attribute("token"), uname),
                        "error", error
                    );
                }
            )
        );
    }
}
