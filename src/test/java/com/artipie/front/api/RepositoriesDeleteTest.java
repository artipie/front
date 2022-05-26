/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.fs.FileStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.RequestAttr;
import com.artipie.front.settings.RepoData;
import com.artipie.front.settings.RepoSettings;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Repositories.Delete}.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
class RepositoriesDeleteTest {

    /**
     * Temp dir.
     * @checkstyle VisibilityModifierCheck (500 lines)
     */
    @TempDir
    Path temp;

    /**
     * Test storage.
     */
    private BlockingStorage blsto;

    /**
     * Data storage.
     */
    private Storage asto;

    @BeforeEach
    void init() {
        this.blsto = new BlockingStorage(new InMemoryStorage());
        this.asto = new FileStorage(this.temp);
    }

    @ParameterizedTest
    @CsvSource({
        "flat,binary-repo.yaml,binary-repo",
        "org,Alice/docker.yml,docker"
    })
    void removesRepository(final String layout, final String key, final String name)
        throws InterruptedException {
        final String uid = "Alice";
        final RepoSettings stn = new RepoSettings(layout, this.blsto);
        this.blsto.save(new Key.From(key), this.repoSettings().getBytes(StandardCharsets.UTF_8));
        this.asto.save(new Key.From(stn.name(name, uid), "data"), Content.EMPTY).join();
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn(name);
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn(uid);
        MatcherAssert.assertThat(
            "Failed to process request",
            new Repositories.Delete(stn, new RepoData(stn))
                .handle(rqs, Mockito.mock(Response.class)),
            new IsNot<>(new IsNull<>())
        );
        Thread.sleep(3000);
        MatcherAssert.assertThat(
            "Settings item was not removed from storage",
            this.blsto.exists(new Key.From(key)),
            new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
            "Data item was not removed from storage",
            this.asto.list(Key.ROOT).join().isEmpty()
        );
    }

    private String repoSettings() {
        return String.join(
            System.lineSeparator(),
            "repo:",
            "  type: rpm",
            "  storage:",
            "    type: fs",
            String.format("    path: %s", this.temp.toString())
        );
    }

}
