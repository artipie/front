/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import java.util.Collection;
import javax.json.JsonObject;

/**
 * Atripie storages settings.
 * @since 0.1
 */
public interface Storages {

    /**
     * List artipie storages.
     * @return Collection of {@link Storage} instances
     */
    Collection<? extends Storage> list();

    /**
     * Add storage to artipie storages.
     * @param alias Storage alias
     * @param info Storage settings
     */
    void add(String alias, JsonObject info);

    /**
     * Remove storage from settings.
     * @param alias Storage alias
     * @throws com.artipie.front.api.NotFoundException If such storage does not exist
     */
    void remove(String alias);

    /**
     * Artipie storage.
     * @since 0.1
     */
    interface Storage {

        /**
         * Storage alias.
         * @return Alias
         */
        String alias();

        /**
         * Storage settings.
         * @return Settings in json format
         */
        JsonObject info();
    }
}
