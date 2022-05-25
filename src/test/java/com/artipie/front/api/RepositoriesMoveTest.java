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
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsAnything;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Repositories.Move}.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class RepositoriesMoveTest {

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

    @Test
    void movesRepo() throws InterruptedException {
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        final Key.From key = new Key.From("my-rpm.yml");
        this.blsto.save(key, this.repoSettings().getBytes(StandardCharsets.UTF_8));
        final String name = "my-rpm";
        this.asto.save(new Key.From(name, "data"), Content.EMPTY).join();
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn(name);
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn("any");
        Mockito.when(rqs.body()).thenReturn("{ \"new_name\": \"alice-rpm\"}");
        final RepoSettings stn = new RepoSettings("flat", this.blsto);
        MatcherAssert.assertThat(
            "Failed to return empty response",
            new Repositories.Move(stn, new RepoData(stn)).handle(rqs, resp),
            new IsAnything<>()
        );
        Mockito.verify(resp).status(HttpStatus.OK_200);
        Thread.sleep(3000);
        MatcherAssert.assertThat(
            "Failed to read repository settings",
            new String(this.blsto.value(new Key.From("alice-rpm.yml")), StandardCharsets.UTF_8),
            new IsEqual<>(this.repoSettings())
        );
        MatcherAssert.assertThat(
            "Repo with old name should not exist",
            this.blsto.exists(key),
            new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
            "Data were moved",
            this.asto.list(Key.ROOT).join().stream().map(Key::string).collect(Collectors.toList()),
            Matchers.contains("alice-rpm/data")
        );
    }

    @Test
    void returnsBadRequestWhenReqIsInvalid() {
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn("my-maven");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn("any");
        Mockito.when(rqs.body()).thenReturn("{ }");
        final RepoSettings stn = new RepoSettings("org", this.blsto);
        new Repositories.Move(stn, new RepoData(stn)).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    void returnsBadRequestWhenRepoDoesNotExist() {
        final String jane = "Jane";
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Repositories.REPO_PARAM.toString())).thenReturn("my-docker");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn(jane);
        final RepoSettings stn = new RepoSettings("org", this.blsto);
        new Repositories.Move(stn, new RepoData(stn)).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.BAD_REQUEST_400);
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
