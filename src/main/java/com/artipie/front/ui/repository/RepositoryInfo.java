/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.artipie.front.ui.HbTemplateEngine;
import java.util.Map;
import spark.ModelAndView;

/**
 * Info-template renderer.
 * Provides 'html'-template with information about repository usage of specified type.
 * All info-templates are situated in '/info'-resource folder.
 *
 * @since 1.0
 */
public final class RepositoryInfo {
    /**
     * Format for info-template.
     */
    private static final String FORMAT = "%s.info.html";

    /**
     * Type-aliases.
     */
    private static final Map<String, String> ALIAS = Map.of("binary", "file");

    /**
     * Template engine.
     */
    private final HbTemplateEngine template;

    /**
     * Ctor.
     */
    public RepositoryInfo() {
        this.template = new HbTemplateEngine("/info");
    }

    /**
     * Renders template.
     * @param type Repository type.
     * @param model Model.
     * @return Rendered template
     */
    public String render(final String type, final Map<String, Object> model) {
        return this.template.render(
            new ModelAndView(
                model,
                String.format(RepositoryInfo.FORMAT, RepositoryInfo.resolveAlias(type))
            )
        );
    }

    /**
     * Resolve alias-type to type.
     * @param alias Alias-type.
     * @return Resolved type.
     */
    private static String resolveAlias(final String alias) {
        String type = alias;
        if (RepositoryInfo.ALIAS.containsKey(alias)) {
            type = RepositoryInfo.ALIAS.get(alias);
        }
        return type;
    }
}
