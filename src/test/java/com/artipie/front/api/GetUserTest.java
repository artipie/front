/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.amihaiemil.eoyaml.Yaml;
import com.artipie.front.auth.YamlCredentials;
import com.artipie.front.auth.YamlCredentialsTest;
import java.util.Optional;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import spark.Request;
import spark.Response;

/**
 * Test for {@link GetUser}.
 * @since 0.1
 */
class GetUserTest {

    @ParameterizedTest
    @CsvSource(value = {
        "Alice;{\"Alice\":{\"email\":\"alice@example.com\",\"groups\":[]}}",
        "John;{\"John\":{\"groups\":[\"reader\",\"dev-lead\"]}}",
        "Mark;{\"Mark\":{\"email\":\"Mark@example.com\",\"groups\":[\"admin\",\"writer\"]}}"
        }, delimiterString = ";")
    void returnsUserInfo(final String name, final String res) throws JSONException {
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetUser.USER_PARAM)).thenReturn(name);
        JSONAssert.assertEquals(
            new GetUser(
                new YamlCredentials(
                    YamlCredentialsTest.credYaml(
                        YamlCredentialsTest.PasswordFormat.SIMPLE,
                        // @checkstyle LineLengthCheck (5 lines)
                        new YamlCredentialsTest.User("Alice", Optional.of("alice@example.com")),
                        new YamlCredentialsTest.User("John", Optional.empty(), "reader", "dev-lead"),
                        new YamlCredentialsTest.User("Mark", Optional.of("Mark@example.com"), "writer", "admin")
                    )
                )
            ).handle(rqs, Mockito.mock(Response.class)),
            res,
            true
        );
    }

    @Test
    void returnNotFound() {
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetUser.USER_PARAM)).thenReturn("any");
        final Response resp = Mockito.mock(Response.class);
        new GetUser(
            new YamlCredentials(
                Yaml.createYamlMappingBuilder()
                    .add("credentials", Yaml.createYamlMappingBuilder().build()).build()
            )
        ).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.NOT_FOUND_404);
    }

}
