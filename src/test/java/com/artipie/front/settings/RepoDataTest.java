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
        this.data.save(new Key.From(RepoDataTest.REPO, "first.txt"), new byte[]{});
        this.data.save(new Key.From(RepoDataTest.REPO, "second.txt"), new byte[]{});
        this.stngs.save(
            new Key.From(String.format("%s.yml", RepoDataTest.REPO)),
            this.repoSettings().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    void removesData() {
        new RepoData(new RepoSettings("flat", this.stngs)).remove(RepoDataTest.REPO, "any")
            .toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Repository data are removed",
            this.data.list(Key.ROOT).isEmpty()
        );
    }

    @Test
    void movesData() {
        final String nrepo = "new-repo";
        new RepoData(new RepoSettings("flat", this.stngs)).move(RepoDataTest.REPO, "any", nrepo)
            .toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Repository data are moved",
            this.data.list(Key.ROOT).stream().map(Key::string)
                .collect(Collectors.toList()),
            Matchers.contains("new-repo/first.txt", "new-repo/second.txt")
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
}
