/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.misc;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.front.api.NotFoundException;

/**
 * Repository settings. While searching for repo settings key or value,
 * this class takes into account:
 * <p>
 *  - artipie layout: when layout if flat, repo settings are stored in the storage root,
 *    when layout is org - in the username subdir: {uid}/{name.yaml}
 * </p>
 * <p>
 *  - .yaml or .yml extension, both are considered
 * </p>
 * @since 0.1
 */
public final class RepoSettings {

    /**
     * Artipie layout.
     */
    private final String layout;

    /**
     * Repositories settings storage.
     */
    private final BlockingStorage repos;

    /**
     * Ctor.
     * @param layout Artipie layout
     * @param repos Repositories settings storage
     */
    public RepoSettings(final String layout, final BlockingStorage repos) {
        this.layout = layout;
        this.repos = repos;
    }

    /**
     * Find repository settings key by repository name and username, throws exception
     * if settings file is not found.
     * @param name Repository name
     * @param uid User id (=name)
     * @return Repository settings
     * @throws NotFoundException If such repository does not exist
     */
    public Key key(final String name, final String uid) {
        String first = "";
        if (this.layout.equals("org")) {
            first = String.format("%s/", uid);
        }
        Key res = new Key.From(String.format("%s%s.yaml", first, name));
        if (!this.repos.exists(res)) {
            res = new Key.From(String.format("%s%s.yml", first, name));
            if (!this.repos.exists(res)) {
                throw new NotFoundException(String.format("Repository %s not found", name));
            }
        }
        return res;
    }

    /**
     * Find repository settings by repository name and username, throws exception
     * if settings file is not found.
     * @param name Repository name
     * @param uid User id (=name)
     * @return Repository settings
     * @throws NotFoundException If such repository does not exist
     */
    public byte[] value(final String name, final String uid) {
        return this.repos.value(this.key(name, uid));
    }

}
