/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import javax.json.JsonArray;
import javax.json.JsonStructure;

/**
 * This interface if meant to manage repository permissions.
 * @since 0.1
 */
public interface RepoPermissions {

    /**
     * Read permissions of the repository.
     * @param repo Repository name
     * @return Permissions as json structure
     * @throws com.artipie.front.api.NotFoundException If repository does not exist
     */
    JsonStructure get(String repo);

    /**
     * Add repository permissions for user.
     * @param repo Repository name
     * @param uid User id (name)
     * @param perms Permissions to add
     * @throws com.artipie.front.api.NotFoundException If repository does not exist
     */
    void add(String repo, String uid, JsonArray perms);

    /**
     * Removes all the permissions for repository from user. Does nothing if user does not
     * have any permissions in the repository.
     * @param repo Repository name
     * @param uid User id (name)
     * @throws com.artipie.front.api.NotFoundException If repository does not exist
     */
    void delete(String repo, String uid);

    /**
     * Patch repository permissions.
     * @param repo Repository name
     * @param perms New permissions
     * @throws com.artipie.front.api.NotFoundException If repository does not exist
     */
    void patch(String repo, JsonStructure perms);
}
