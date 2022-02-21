/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.amihaiemil.eoyaml.Yaml;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Users}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class UsersTest {

    @Test
    void writesUsers() throws JSONException {
        JSONAssert.assertEquals(
            new Users(
                Yaml.createYamlMappingBuilder().add(
                    "credentials",
                    Yaml.createYamlMappingBuilder().add(
                        "Alice",
                        Yaml.createYamlMappingBuilder().add("pass", "plain:123")
                            .add("email", "alice@example.com").build()
                    ).add(
                        "John",
                        Yaml.createYamlMappingBuilder().add("type", "sha256").add("pass", "xxx")
                            .add(
                                "groups",
                                Yaml.createYamlSequenceBuilder().add("reader")
                                    .add("dev-lead").build()
                            ).build()
                    ).build()
                ).build().toString(),
                "Jane",
                false
            ).handle(Mockito.mock(Request.class), Mockito.mock(Response.class)),
            String.join(
                "\n",
                "{\"file\":",
                "{",
                "  \"Alice\":{\"email\":\"alice@example.com\"},",
                "  \"John\":{\"groups\":[\"reader\",\"dev-lead\"]}",
                "},",
                "\"env\":{\"Jane\":{}},",
                "\"github\":{\"enabled\":false}}"
            ),
            true
        );
    }

    @Test
    void doesNotWriteFileOrEnvWhenAbsent() throws JSONException {
        JSONAssert.assertEquals(
            new Users(Optional.empty(), Optional.empty(), true)
                .handle(Mockito.mock(Request.class), Mockito.mock(Response.class)),
            "{\"github\": {\"enabled\": true }}",
            true
        );
    }

}
