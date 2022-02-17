/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

import java.util.Map;
import java.util.function.Function;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.handlebars.HandlebarsTemplateEngine;

/**
 * Handlebars page.
 * @since 1.0
 */
final class HbPage implements Route {

    /**
     * Template engine.
     */
    private static final HandlebarsTemplateEngine ENGINE = new HandlebarsTemplateEngine("/html");

    /**
     * Template name.
     */
    private final String template;

    /**
     * Template params.
     */
    private final Function<? super Request, ? extends Map<String, ? extends Object>> params;

    /**
     * New page.
     * @param template Template name
     * @param params Params
     */
    HbPage(final String template, final Map<String, ? extends Object> params) {
        this(template, req -> params);
    }

    /**
     * New page.
     * @param template Template name
     * @param params Create params from request
     */
    HbPage(final String template,
        final Function<? super Request, ? extends Map<String, ? extends Object>> params) {
        this.template = template;
        this.params = params;
    }

    @Override
    public Object handle(final Request req, final Response rsp) throws Exception {
        rsp.type("text/html");
        return HbPage.ENGINE.render(new ModelAndView(this.params.apply(req), this.template));
    }
}
