/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

import com.amihaiemil.eoyaml.Yaml;
import com.artipie.front.RequestAttr;
import com.artipie.front.api.GetRepository;
import com.artipie.front.misc.RouteWrap;
import com.artipie.front.settings.RepoSettings;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Dashboard repo page.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RepoPage extends RouteWrap.TemplateViewRoute {

    /**
     * Wrap route.
     *
     * @param repos Repository settings
     */
    public RepoPage(final RepoSettings repos) {
        super(
            new HbPage(
                "repo",
                req -> {
                    final String uid = RequestAttr.Standard.USER_ID.readOrThrow(req);
                    final String name = GetRepository.REPO_PARAM.parse(req);
                    try {
                        return Map.of(
                            "user", uid, "title", uid, "name", name,
                            "type",
                            Yaml.createYamlInput(
                                new ByteArrayInputStream(repos.value(name, uid))
                            ).readYamlMapping().yamlMapping("repo").string("type")
                        );
                    } catch (final IOException err) {
                        throw new UncheckedIOException(err);
                    }
                }
            )
        );
    }
}
