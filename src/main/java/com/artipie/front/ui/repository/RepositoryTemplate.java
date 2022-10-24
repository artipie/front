/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.artipie.front.ui.HbTemplateEngine;
import java.util.List;
import java.util.Map;
import spark.ModelAndView;

/**
 * Repository template renderer.
 * Provides 'yaml'-template of repository configuration.
 * Provides 'yaml'-template with repository configuration of specified type.
 * All templates are situated in '/template'-resource folder.
 *
 * @since 1.0
 */
public final class RepositoryTemplate {
    /**
     * Default template.
     */
    private static final String DEFAULT_TEMPLATE = "default";

    /**
     * Format for template.
     */
    private static final String FORMAT = "%s.template.yaml";

    /**
     * Pre-defined template names.
     */
    private static final List<String> PREDEFINED = List.of("maven-group", "maven-proxy");

    /**
     * Template engine.
     */
    private final HbTemplateEngine template;

    /**
     * Ctor.
     */
    public RepositoryTemplate() {
        this.template = new HbTemplateEngine("/template");
    }

    /**
     * Render template.
     *
     * @param type Repository type.
     * @param model Model.
     * @return Rendered yaml-template for repository type.
     */
    public String render(final String type, final Map<String, Object> model) {
        final String content;
        if (RepositoryTemplate.PREDEFINED.contains(type)) {
            content = this.template.render(
                new ModelAndView(
                    model,
                    String.format(RepositoryTemplate.FORMAT, type)
                )
            );
        } else {
            content = this.template.render(
                new ModelAndView(
                    model,
                    String.format(RepositoryTemplate.FORMAT, RepositoryTemplate.DEFAULT_TEMPLATE)
                )
            );
        }
        return content;
    }
}
