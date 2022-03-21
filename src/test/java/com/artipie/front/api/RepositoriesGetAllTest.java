/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.settings.RepoSettings;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Repositories.GetAll}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class RepositoriesGetAllTest {

    @Test
    void listsRepositories() throws JSONException {
        final BlockingStorage blsto = new BlockingStorage(new InMemoryStorage());
        blsto.save(new Key.From("Jane", "maven.yaml"), new byte[]{});
        blsto.save(new Key.From("Jane", "python.yaml"), new byte[]{});
        blsto.save(new Key.From("Jane", "binary.yaml"), new byte[]{});
        blsto.save(new Key.From("John", "anaconda.yml"), new byte[]{});
        blsto.save(new Key.From("John", "maven.yml"), new byte[]{});
        blsto.save(new Key.From("rpm.yml"), new byte[]{});
        final var resp = Mockito.mock(Response.class);
        JSONAssert.assertEquals(
            new Repositories.GetAll(new RepoSettings("org", blsto))
                .handle(Mockito.mock(Request.class), resp),
            String.join(
                "\n", "[",
                "{\"fullName\":\"Jane/binary\"},",
                "{\"fullName\":\"Jane/maven\"},",
                "{\"fullName\":\"Jane/python\"},",
                "{\"fullName\":\"John/anaconda\"},",
                "{\"fullName\":\"John/maven\"},",
                "{\"fullName\":\"rpm\"}",
                "]"
            ),
            true
        );
        Mockito.verify(resp).type("application/json");
    }

}
