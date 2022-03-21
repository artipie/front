/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.RequestAttr;
import com.artipie.front.settings.RepoSettings;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsAnything;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Repositories.Delete}.
 * @since 0.1
 */
class RepositoriesDeleteTest {

    /**
     * Test storage.
     */
    private BlockingStorage blsto;

    @BeforeEach
    void init() {
        this.blsto = new BlockingStorage(new InMemoryStorage());
    }

    @ParameterizedTest
    @CsvSource({
        "flat,binary-repo.yaml,binary-repo",
        "org,Alice/docker.yml,docker"
    })
    void removesRepository(final String layout, final String key, final String name) {
        final String uid = "Alice";
        this.blsto.save(new Key.From(key), new byte[]{});
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn(name);
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn(uid);
        MatcherAssert.assertThat(
            "Failed to process request",
            new Repositories.Delete(new RepoSettings(layout, this.blsto))
                .handle(rqs, Mockito.mock(Response.class)),
            new IsAnything<>()
        );
        MatcherAssert.assertThat(
            "Item was not removed from storage",
            this.blsto.exists(new Key.From(key)),
            new IsEqual<>(false)
        );
    }

}
