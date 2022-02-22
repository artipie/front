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
                    ).add(
                        "Mark", Yaml.createYamlMappingBuilder().add("pass", "sha256:xxx").build()
                    ).build()
                ).build().toString()
            ).handle(Mockito.mock(Request.class), Mockito.mock(Response.class)),
            String.join(
                "\n",
                "{",
                "  \"Alice\": {\"email\":\"alice@example.com\"},",
                "  \"John\": {\"groups\":[\"reader\",\"dev-lead\"]},",
                "  \"Mark\": {} ",
                "}"
            ),
            true
        );
    }

    @Test
    void writesEmptyWhenAbsent() throws JSONException {
        JSONAssert.assertEquals(
            new Users(Optional.empty())
                .handle(Mockito.mock(Request.class), Mockito.mock(Response.class)),
            "{}",
            true
        );
    }

}
