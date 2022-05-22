/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.artipie.asto.Copy;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.SubStorage;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Repository data management.
 * @since 0.1
 */
public final class RepoData {

    /**
     * Repository settings.
     */
    private final RepoSettings stn;

    /**
     * Ctor.
     * @param stn Repository settings
     */
    public RepoData(final RepoSettings stn) {
        this.stn = stn;
    }

    /**
     * Remove data from the repository.
     * @param name Repository name
     * @param uid User name
     * @return Completable action of the remove operation
     */
    public CompletionStage<Void> remove(final String name, final String uid) {
        final String repo = this.stn.name(name, uid);
        return this.asto(name, uid).deleteAll(new Key.From(repo)).thenAccept(
            nothing -> Logger.info(this, String.format("Removed data from repository %s", repo))
        );
    }

    /**
     * Remove data from the repository.
     * @param name Repository name
     * @param uid User name
     * @param nname New name
     * @return Completable action of the remove operation
     */
    public CompletionStage<Void> move(final String name, final String uid, final String nname) {
        final Key repo = new Key.From(this.stn.name(name, uid));
        final String nrepo = this.stn.name(nname, uid);
        final Storage asto = this.asto(name, uid);
        return new SubStorage(repo, asto).list(Key.ROOT).thenCompose(
            list -> new Copy(new SubStorage(repo, asto), list)
                .copy(new SubStorage(new Key.From(nrepo), asto))
        ).thenCompose(nothing -> asto.deleteAll(new Key.From(repo))).thenAccept(
            nothing ->
                Logger.info(this, String.format("Moved data from repository %s to %s", repo, nrepo))
        );
    }

    /**
     * Obtain storage from repository settings.
     * @param name Repository name
     * @param uid User name
     * @return Abstract storage
     */
    private Storage asto(final String name, final String uid) {
        final Storage asto;
        try {
            final YamlMapping yaml = Yaml.createYamlInput(
                new String(this.stn.value(name, uid), StandardCharsets.UTF_8)
            ).readYamlMapping().yamlMapping("repo");
            final YamlMapping storage = yaml.yamlMapping("storage");
            if (storage == null) {
                throw new NotImplementedException("Not implemented yet");
            } else {
                asto = new YamlStorage(storage).storage();
            }
            return asto;
        } catch (final IOException err) {
            throw new UncheckedIOException(err);
        }
    }
}
