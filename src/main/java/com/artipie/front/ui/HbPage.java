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
import spark.TemplateViewRoute;

/**
 * Handlebars page.
 * @since 1.0
 */
public final class HbPage implements TemplateViewRoute {

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
    public HbPage(final String template, final Map<String, ? extends Object> params) {
        this(template, req -> params);
    }

    /**
     * New page.
     * @param template Template name
     * @param params Create params from request
     */
    public HbPage(final String template,
        final Function<? super Request, ? extends Map<String, ? extends Object>> params) {
        this.template = template;
        this.params = params;
    }

    @Override
    public ModelAndView handle(final Request req, final Response rsp) throws Exception {
        return new ModelAndView(this.params.apply(req), this.template);
    }
}
