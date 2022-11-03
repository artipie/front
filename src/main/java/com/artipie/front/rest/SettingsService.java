/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.front.Layout;
import java.util.Optional;
import javax.json.JsonObject;

/**
 * Settings-service.
 *
 * @since 1.0
 */
public class SettingsService extends BaseService {
    /**
     * Path to layout rest-api.
     */
    private static final String LAYOUT_PATH = "/api/v1/settings/layout";

    /**
     * Ctor.
     * @param rest Artipie rest endpoint.
     */
    public SettingsService(final String rest) {
        super(rest);
    }

    /**
     * Obtain Artipie layout.
     * @return Artipie layout
     */
    public Layout layout() {
        return BaseService.handle(
            this.httpGet(Optional.empty(), SettingsService.LAYOUT_PATH),
            res -> {
                final JsonObject json = BaseService.jsonObject(res);
                return Layout.byName(json.getString("layout"));
            }
        );
    }
}
