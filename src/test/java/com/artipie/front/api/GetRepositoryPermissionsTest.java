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
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import spark.Request;
import spark.Response;

/**
 * Test for {@link GetRepositoryPermissions}.
 * @since 0.1
 */
class GetRepositoryPermissionsTest {

    /**
     * Test storage.
     */
    private BlockingStorage blsto;

    /**
     * Test repository settings.
     */
    private RepoSettings snt;

    @BeforeEach
    void init() {
        this.blsto = new BlockingStorage(new InMemoryStorage());
        this.snt = new RepoSettings("flat", this.blsto);
    }

    @Test
    void returnsPermissions() throws JSONException {
        this.blsto.save(
            new Key.From("my-maven.yaml"),
            new TestResource("GetRepositoryPermissionsTest/my-maven.yaml").asBytes()
        );
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.PARAM)).thenReturn("my-maven");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn("any");
        JSONAssert.assertEquals(
            new GetRepositoryPermissions(this.snt).handle(rqs, Mockito.mock(Response.class)),
            String.join(
                "\n",
                "{\"permissions\": {",
                "  \"Jane\": [\"read\", \"write\"],",
                "  \"Mark\": [\"*\"],",
                "  \"/readers\": [\"read\"]",
                "}}"
            ),
            true
        );
    }

    @Test
    void returnEmptyWhenPermissionsAreNotSet() throws JSONException {
        this.blsto.save(
            new Key.From("my-python.yaml"),
            String.join(
                "\n",
                "repo:",
                "  type: pypi"
            ).getBytes(StandardCharsets.UTF_8)
        );
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.PARAM)).thenReturn("my-python");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn("alice");
        JSONAssert.assertEquals(
            new GetRepositoryPermissions(this.snt).handle(rqs, Mockito.mock(Response.class)),
            "{\"permissions\": {}}",
            true
        );
    }
}
