/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.asto.test.TestResource;
import com.artipie.front.RequestAttr;
import com.artipie.front.settings.RepoSettings;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Repositories.Get}.
 * @since 0.1
 */
class RepositoriesGetTest {

    @ParameterizedTest
    @CsvSource({
        "yaml,my-maven",
        "yml,my-npm-proxy"
    })
    void returnsRepositorySettings(final String ext, final String name) throws Exception {
        final BlockingStorage blsto = new BlockingStorage(new InMemoryStorage());
        final String file = String.format("%s.%s", name, ext);
        blsto.save(
            new Key.From(file),
            new TestResource(String.format("GetRepositoryTest/%s", file)).asBytes()
        );
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn(name);
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn("any");
        JSONAssert.assertEquals(
            new Repositories.Get(new RepoSettings("flat", blsto)).handle(rqs, resp),
            new String(
                new TestResource(String.format("GetRepositoryTest/%s.json", name)).asBytes(),
                StandardCharsets.UTF_8
            ),
            true
        );
        Mockito.verify(resp).type("application/json");
    }

}
