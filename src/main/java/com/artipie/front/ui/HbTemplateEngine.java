/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.cache.GuavaTemplateCache;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.io.RuntimeIOException;
import spark.ModelAndView;
import spark.TemplateEngine;

/**
 * Renders HTML from Route output using
 * https://github.com/jknack/handlebars.java.
 * @since 0.1
 * @checkstyle DesignForExtensionCheck (500 lines)
 */
public class HbTemplateEngine extends TemplateEngine {

    /**
     * Handlebars object.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    protected Handlebars handlebars;

    /**
     * Constructs a handlebars template engine.
     *
     * @param resource The resource root
     */
    public HbTemplateEngine(final String resource) {
        this.handlebars = HbTemplateEngine.setup(resource);
    }

    @Override
    public String render(final ModelAndView model) {
        try {
            return this.handlebars.compile(model.getViewName()).apply(model.getModel());
        } catch (final IOException err) {
            throw new RuntimeIOException(err);
        }
    }

    /**
     * Sets up {@link Handlebars} object, mostly as in this example
     * https://github.com/perwendel/spark-template-engines/blob/master/spark-template-handlebars/src/main/java/spark/template/handlebars/HandlebarsTemplateEngine.java
     * Some extra render template helpers are added.
     * @param resource Resource root.
     * @return Handlebars
     * @checkstyle MagicNumberCheck (30 lines)
     */
    private static Handlebars setup(final String resource) {
        final TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix(resource);
        loader.setSuffix(null);
        final Handlebars res = new Handlebars(loader);
        res.with(
            new GuavaTemplateCache(
                CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
                    .maximumSize(1000).build()
            )
        );
        res.registerHelpers(ConditionalHelpers.class);
        return res;
    }
}
