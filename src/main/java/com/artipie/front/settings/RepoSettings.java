/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.front.api.NotFoundException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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
        final Pair<Key, Key> pair = this.keys(name, uid);
        Key res = pair.getLeft();
        if (!this.repos.exists(res)) {
            res = pair.getRight();
            if (!this.repos.exists(res)) {
                throw new NotFoundException(String.format("Repository %s not found", name));
            }
        }
        return res;
    }

    /**
     * Checks if repository settings exists key by repository name and username.
     * @param name Repository name
     * @param uid User id (=name)
     * @return True if found
     */
    public boolean exists(final String name, final String uid) {
        return this.repos.exists(this.keys(name, uid).getLeft())
            || this.repos.exists(this.keys(name, uid).getRight());
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

    /**
     * Saves repository settings to repository settings storage.
     * @param name Repository name
     * @param uid User id (=name)
     * @param value Settings body
     */
    public void save(final String name, final String uid, final byte[] value) {
        this.repos.save(this.keys(name, uid).getRight(), value);
    }

    /**
     * Removes repository settings.
     * @param name Repository name
     * @param uid User id (=name)
     * @throws NotFoundException If such repository does not exist
     */
    public void delete(final String name, final String uid) {
        this.repos.delete(this.key(name, uid));
    }

    /**
     * Returns a pair of keys, these keys are possible repository settings names.
     * @param name Repository name
     * @param uid User id (=name)
     * @return Pair of keys
     */
    private Pair<Key, Key> keys(final String name, final String uid) {
        String first = "";
        if (this.layout.equals("org")) {
            first = String.format("%s/", uid);
        }
        return new ImmutablePair<>(
            new Key.From(String.format("%s%s.yaml", first, name)),
            new Key.From(String.format("%s%s.yml", first, name))
        );
    }

}
