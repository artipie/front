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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Repositories.Head}.
 * @since 0.1
 */
class RepositoriesHeadTest {

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
        "org,Mark/pypi.yml,pypi"
    })
    void returnOkWhenRepoFound(final String layout, final String key, final String name) {
        final String uid = "Mark";
        this.blsto.save(new Key.From(key), new byte[]{});
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn(name);
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn(uid);
        MatcherAssert.assertThat(
            new Repositories.Head(new RepoSettings(layout, this.blsto))
                .handle(rqs, Mockito.mock(Response.class)),
            new IsAnything<>()
        );
    }

    @Test
    void throwsExceptionWhenNotFound() {
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn("my-repo");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn("any");
        Assertions.assertThrows(
            NotFoundException.class,
            () -> new Repositories.Head(new RepoSettings("flat", this.blsto))
                .handle(rqs, Mockito.mock(Response.class))
        );
    }

}
