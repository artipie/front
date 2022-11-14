/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.front.Layout;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import javax.json.JsonObject;

/**
 * Settings-service.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class SettingsService extends BaseService {
    /**
     * Path to port rest-api.
     */
    private static final String PORT_PATH = "/api/v1/settings/port";

    /**
     * Path to layout rest-api.
     */
    private static final String LAYOUT_PATH = "/api/v1/settings/layout";

    /**
     * Port.
     */
    private final AtomicReference<Integer> port;

    /**
     * Layout.
     */
    private final AtomicReference<Layout> layout;

    /**
     * Ctor.
     * @param rest Artipie rest endpoint.
     */
    public SettingsService(final String rest) {
        super(rest);
        this.port = new AtomicReference<>();
        this.layout = new AtomicReference<>();
    }

    /**
     * Obtain Artipie server port.
     * @return Artipie server port
     */
    public int port() {
        return this.value(
            this.port,
            SettingsService.PORT_PATH,
            json -> json.getInt("port")
        );
    }

    /**
     * Obtain Artipie layout.
     * @return Artipie layout
     */
    public Layout layout() {
        return this.value(
            this.layout,
            SettingsService.LAYOUT_PATH,
            json -> Layout.byName(json.getString("layout"))
        );
    }

    /**
     * Obtain Artipie setting's value.
     * @param ref Reference to setting value
     * @param path Path to rest service
     * @param handler Handler of json content
     * @param <T> Resulting type of handler
     * @return Artipie layout
     */
    private <T> T value(final AtomicReference<T> ref, final String path,
        final Function<JsonObject, T> handler) {
        if (ref.get() == null) {
            final T value = BaseService.handle(
                this.httpGet(Optional.empty(), path),
                res -> {
                    final JsonObject json = BaseService.jsonObject(res);
                    return handler.apply(json);
                }
            );
            ref.compareAndSet(null, value);
        }
        return ref.get();
    }
}
