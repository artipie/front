/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Storages.Get}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class StoragesGetTest {

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
        "olga,my-nuget,olga/my-nuget/_storages.yml",
        "olga,,olga/_storages.yml",
        ",internal-rpm,internal-rpm/_storages.yaml",
        ",,_storages.yml"
    })
    void returnsStorageAliasDetails(final String uid, final String repo, final String key)
        throws JSONException {
        StoragesGetAllTest.createSettings(this.blsto, new Key.From(key));
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn(repo);
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn(uid);
        Mockito.when(rqs.params(Storages.ST_ALIAS.toString())).thenReturn("temp");
        JSONAssert.assertEquals(
            new Storages.Get(this.blsto).handle(rqs, resp),
            "{\"type\": \"file\", \"path\": \"/data/temp\" }",
            true
        );
    }

    @ParameterizedTest
    @CsvSource({
        "ivan,my-maven,ivan/my-maven/_storages.yml",
        "ivan,,ivan/_storages.yml",
        ",internal-gem,internal-gem/_storages.yaml",
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
        new Storages.Get(this.blsto).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.NOT_FOUND_404);
    }

}
