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
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

/**
 * Repository data management.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
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
     * Move data when repository is renamed: from location by the old name to location with
     * new name.
     * @param name Repository name
     * @param uid User name
     * @param nname New repository name
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
     * @throws UncheckedIOException On IO errors
     */
    private Storage asto(final String name, final String uid) {
        try {
            final YamlMapping yaml = Yaml.createYamlInput(
                new String(this.stn.value(name, uid), StandardCharsets.UTF_8)
            ).readYamlMapping().yamlMapping("repo");
            YamlMapping res = yaml.yamlMapping("storage");
            if (res == null) {
                res = this.storageYamlByAlias(name, uid, yaml.string("storage"));
            }
            return new YamlStorage(res).storage();
        } catch (final IOException err) {
            throw new UncheckedIOException(err);
        }
    }

    /**
     * Find storage settings by alias, considering two file extensions and two locations.
     * @param name Repository name
     * @param uid User name
     * @param alias Storage settings yaml by alias
     * @return Yaml storage settings found by provided alias
     * @throws IllegalStateException If storage with given alias not found
     * @throws UncheckedIOException On IO errors
     * @checkstyle LineLengthCheck (2 lines)
     */
    private YamlMapping storageYamlByAlias(final String name, final String uid, final String alias) {
        final Key repo = new Key.From(this.stn.name(name, uid));
        final Key yml = new Key.From("_storage.yaml");
        final Key yaml = new Key.From("_storage.yml");
        Optional<YamlMapping> res = Optional.empty();
        final Optional<Key> location = Stream.of(
            new Key.From(repo, yaml), new Key.From(repo, yml),
            repo.parent().<Key>map(item -> new Key.From(item, yaml)).orElse(yaml),
            repo.parent().<Key>map(item -> new Key.From(item, yml)).orElse(yml)
        ).filter(this.stn.repoConfigsStorage()::exists).findFirst();
        if (location.isPresent()) {
            try {
                res = Optional.of(
                    Yaml.createYamlInput(
                        new String(
                            this.stn.repoConfigsStorage().value(location.get()),
                            StandardCharsets.UTF_8
                        )
                    ).readYamlMapping().yamlMapping("storages").yamlMapping(alias)
                );
            } catch (final IOException err) {
                throw new UncheckedIOException(err);
            }
        }
        return res.orElseThrow(
            () -> new IllegalStateException(String.format("Storage alias %s not found", alias))
        );
    }
}
