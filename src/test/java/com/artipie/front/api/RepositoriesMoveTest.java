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
import java.nio.charset.StandardCharsets;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsAnything;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Repositories.Move}.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class RepositoriesMoveTest {

    /**
     * Test repository settings.
     */
    private static final String REPO = String.join(
        System.lineSeparator(),
        "repo:",
        "  type: rpm",
        "  storage:",
        "    type: fs",
        "    path: /var/artipie/data/"
    );

    /**
     * Test storage.
     */
    private BlockingStorage blsto;

    @BeforeEach
    void init() {
        this.blsto = new BlockingStorage(new InMemoryStorage());
    }

    @Test
    void movesRepo() {
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        final Key.From key = new Key.From("my-rpm.yml");
        this.blsto.save(key, RepositoriesMoveTest.REPO.getBytes(StandardCharsets.UTF_8));
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn("my-rpm");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn("any");
        Mockito.when(rqs.body()).thenReturn("{ \"new_name\": \"alice-rpm\"}");
        MatcherAssert.assertThat(
            "Failed to return empty response",
            new Repositories.Move(new RepoSettings("flat", this.blsto)).handle(rqs, resp),
            new IsAnything<>()
        );
        Mockito.verify(resp).status(HttpStatus.OK_200);
        MatcherAssert.assertThat(
            "Failed to read repository settings",
            new String(this.blsto.value(new Key.From("alice-rpm.yml")), StandardCharsets.UTF_8),
            new IsEqual<>(RepositoriesMoveTest.REPO)
        );
        MatcherAssert.assertThat(
            "Repo with old name should not exist",
            this.blsto.exists(key),
            new IsEqual<>(false)
        );
    }

    @Test
    void returnsBadRequestWhenReqIsInvalid() {
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn("my-maven");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn("any");
        Mockito.when(rqs.body()).thenReturn("{ }");
        new Repositories.Move(new RepoSettings("org", this.blsto)).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    void returnsBadRequestWhenRepoDoesNotExist() {
        final String jane = "Jane";
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn("my-docker");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn(jane);
        new Repositories.Move(new RepoSettings("org", this.blsto)).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.BAD_REQUEST_400);
    }

}
