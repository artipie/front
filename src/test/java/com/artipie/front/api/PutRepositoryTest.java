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
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsAnything;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link PutRepository}.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class PutRepositoryTest {

    /**
     * Test storage.
     */
    private BlockingStorage blsto;

    @BeforeEach
    void init() {
        this.blsto = new BlockingStorage(new InMemoryStorage());
    }

    @Test
    void addsNewRepo() {
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.NAME_PARAM.toString())).thenReturn("my-rpm");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn("any");
        Mockito.when(rqs.body()).thenReturn(
            new String(
                new TestResource("PutRepositoryTest/request.json").asBytes(),
                StandardCharsets.UTF_8
            )
        );
        MatcherAssert.assertThat(
            "Failed to return empty response",
            new PutRepository(new RepoSettings("flat", this.blsto)).handle(rqs, resp),
            new IsAnything<>()
        );
        Mockito.verify(resp).status(HttpStatus.CREATED_201);
        MatcherAssert.assertThat(
            new String(this.blsto.value(new Key.From("my-rpm.yml")), StandardCharsets.UTF_8),
            new IsEqual<>(
                String.join(
                    System.lineSeparator(),
                    "repo:",
                    "  type: rpm",
                    "  storage:",
                    "    type: fs",
                    "    path: /var/artipie/data/",
                    "  settings:",
                    "    digest: sha1",
                    "    filelists: false",
                    "  permissions:",
                    "    alice:",
                    "      - read",
                    "      - write",
                    "    bob:",
                    "      - read"
                )
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"repo\": {}}", "{\"repo\": {\"type\": \"maven\"}}"})
    void returnsBadRequestWhenReqIsInvalid(final String body) {
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.NAME_PARAM.toString())).thenReturn("my-maven");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn("any");
        Mockito.when(rqs.body()).thenReturn(body);
        new PutRepository(new RepoSettings("org", this.blsto)).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    void returnsConflictWhenRepoExists() {
        final String jane = "Jane";
        this.blsto.save(new Key.From(jane, "my-docker.yml"), new byte[]{});
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.NAME_PARAM.toString())).thenReturn("my-docker");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn(jane);
        new PutRepository(new RepoSettings("org", this.blsto)).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.CONFLICT_409);
    }

}
