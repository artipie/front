/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.fs.FileStorage;
import com.artipie.asto.memory.InMemoryStorage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link RepoData}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class RepoDataTest {

    /**
     * Test repository name.
     */
    private static final String REPO = "my-repo";

    /**
     * Temp dir.
     * @checkstyle VisibilityModifierCheck (500 lines)
     */
    @TempDir
    Path temp;

    /**
     * Test settings storage.
     */
    private BlockingStorage stngs;

    /**
     * Test data storage.
     */
    private BlockingStorage data;

    @BeforeEach
    void init() {
        this.stngs = new BlockingStorage(new InMemoryStorage());
        this.data = new BlockingStorage(new FileStorage(this.temp));
    }

    @Test
    void removesData() {
        this.stngs.save(
            new Key.From(String.format("%s.yml", RepoDataTest.REPO)),
            this.repoSettings().getBytes(StandardCharsets.UTF_8)
        );
        this.data.save(new Key.From(RepoDataTest.REPO, "first.txt"), new byte[]{});
        this.data.save(new Key.From(RepoDataTest.REPO, "second.txt"), new byte[]{});
        new RepoData(new RepoSettings("flat", this.stngs))
            .remove(RepoDataTest.REPO, "any").toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Repository data are removed",
            this.data.list(Key.ROOT).isEmpty()
        );
    }

    @Test
    void movesData() {
        this.stngs.save(
            new Key.From(String.format("%s.yml", RepoDataTest.REPO)),
            this.repoSettings().getBytes(StandardCharsets.UTF_8)
        );
        this.data.save(new Key.From(RepoDataTest.REPO, "first.txt"), new byte[]{});
        this.data.save(new Key.From(RepoDataTest.REPO, "second.txt"), new byte[]{});
        final String nrepo = "new-repo";
        new RepoData(new RepoSettings("flat", this.stngs))
            .move(RepoDataTest.REPO, "any", nrepo).toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Repository data are moved",
            this.data.list(Key.ROOT).stream().map(Key::string)
                .collect(Collectors.toList()),
            Matchers.contains("new-repo/first.txt", "new-repo/second.txt")
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"_storage.yaml", "_storage.yml", "my-repo/_storage.yaml", "my-repo/_storage.yml"}
    )
    void movesDataWithAliasAndFlatLayout(final String key) {
        this.stngs.save(
            new Key.From(String.format("%s.yml", RepoDataTest.REPO)),
            this.repoSettingsWithAlias().getBytes(StandardCharsets.UTF_8)
        );
        this.stngs.save(
            new Key.From(key),
            this.storageAlias().getBytes(StandardCharsets.UTF_8)
        );
        this.data.save(new Key.From(RepoDataTest.REPO, "first.txt"), new byte[]{});
        this.data.save(new Key.From(RepoDataTest.REPO, "second.txt"), new byte[]{});
        final String nrepo = "new-repo";
        new RepoData(new RepoSettings("flat", this.stngs))
            .move(RepoDataTest.REPO, "any", nrepo).toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Repository data are moved",
            this.data.list(Key.ROOT).stream().map(Key::string).collect(Collectors.toList()),
            Matchers.contains("new-repo/first.txt", "new-repo/second.txt")
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"_storage.yaml", "_storage.yml", "my-repo/_storage.yaml", "my-repo/_storage.yml"}
    )
    void movesDataWithAliasAndOrgLayout(final String key) {
        final String uid = "john";
        this.stngs.save(
            new Key.From(uid, String.format("%s.yml", RepoDataTest.REPO)),
            this.repoSettingsWithAlias().getBytes(StandardCharsets.UTF_8)
        );
        this.stngs.save(
            new Key.From(uid, key),
            this.storageAlias().getBytes(StandardCharsets.UTF_8)
        );
        this.data.save(new Key.From(uid, RepoDataTest.REPO, "first.txt"), new byte[]{});
        this.data.save(new Key.From(uid, RepoDataTest.REPO, "second.txt"), new byte[]{});
        final String nrepo = "new-repo";
        new RepoData(new RepoSettings("org", this.stngs))
            .move(RepoDataTest.REPO, uid, nrepo).toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Repository data are moved",
            this.data.list(Key.ROOT).stream().map(Key::string).collect(Collectors.toList()),
            Matchers.contains("john/new-repo/first.txt", "john/new-repo/second.txt")
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"_storage.yaml", "_storage.yml", "my-repo/_storage.yaml", "my-repo/_storage.yml"}
    )
    void removesDataWithAliasAndFlatLayout(final String key) {
        this.stngs.save(
            new Key.From(String.format("%s.yml", RepoDataTest.REPO)),
            this.repoSettingsWithAlias().getBytes(StandardCharsets.UTF_8)
        );
        this.stngs.save(
            new Key.From(key),
            this.storageAlias().getBytes(StandardCharsets.UTF_8)
        );
        this.data.save(new Key.From(RepoDataTest.REPO, "first.txt"), new byte[]{});
        this.data.save(new Key.From(RepoDataTest.REPO, "second.txt"), new byte[]{});
        new RepoData(new RepoSettings("flat", this.stngs))
            .remove(RepoDataTest.REPO, "any").toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Repository data are moved",
            this.data.list(Key.ROOT).isEmpty()
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"_storage.yaml", "_storage.yml", "my-repo/_storage.yaml", "my-repo/_storage.yml"}
    )
    void removesDataWithAliasAndOrgLayout(final String key) {
        final String uid = "john";
        this.stngs.save(
            new Key.From(uid, String.format("%s.yml", RepoDataTest.REPO)),
            this.repoSettingsWithAlias().getBytes(StandardCharsets.UTF_8)
        );
        this.stngs.save(
            new Key.From(uid, key),
            this.storageAlias().getBytes(StandardCharsets.UTF_8)
        );
        this.data.save(new Key.From(uid, RepoDataTest.REPO, "first.txt"), new byte[]{});
        this.data.save(new Key.From(uid, RepoDataTest.REPO, "second.txt"), new byte[]{});
        new RepoData(new RepoSettings("org", this.stngs))
            .remove(RepoDataTest.REPO, uid).toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Repository is empty",
            this.data.list(Key.ROOT).isEmpty()
        );
    }

    private String repoSettings() {
        return String.join(
            System.lineSeparator(),
            "repo:",
            "  type: binary",
            "  storage:",
            "    type: fs",
            String.format("    path: %s", this.temp.toString())
        );
    }

    private String repoSettingsWithAlias() {
        return String.join(
            System.lineSeparator(),
            "repo:",
            "  type: binary",
            "  storage: local"
        );
    }

    private String storageAlias() {
        return String.join(
            System.lineSeparator(),
            "storages:",
            "  default:",
            "    type: fs",
            "    path: /usr/def",
            "  local:",
            "    type: fs",
            String.format("    path: %s", this.temp.toString())
        );
    }
}
