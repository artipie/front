/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Storages.Head}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class StoragesHeadTest {

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
        "richard,my-conan,richard/my-conan/_storages.yml",
        "richard,,richard/_storages.yml",
        ",internal-bin,internal-bin/_storages.yaml",
        ",,_storages.yml"
    })
    void returnsStorageAliasDetails(final String uid, final String repo, final String key) {
        StoragesGetAllTest.createSettings(this.blsto, new Key.From(key));
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn(repo);
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn(uid);
        Mockito.when(rqs.params(Storages.ST_ALIAS.toString())).thenReturn("default");
        new Storages.Head(this.blsto).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.FOUND_302);
    }

    @ParameterizedTest
    @CsvSource({
        "jane,my-maven,jane/my-maven/_storages.yml",
        "jane,,jane/_storages.yml",
        ",internal-python,internal-python/_storages.yaml",
        ",,_storages.yml"
    })
    void returnsNotFoundWhenAliasNotExists(final String uid, final String repo,
        final String key) {
        StoragesGetAllTest.createSettings(this.blsto, new Key.From(key));
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn(repo);
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn(uid);
        Mockito.when(rqs.params(Storages.ST_ALIAS.toString())).thenReturn("local");
        new Storages.Head(this.blsto).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.NOT_FOUND_404);
    }

}
