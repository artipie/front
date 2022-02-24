/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.settings.YamlCredentialsTest;
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
                YamlCredentialsTest.credYaml(
                    YamlCredentialsTest.PasswordFormat.SIMPLE,
                    // @checkstyle LineLengthCheck (1 line)
                    new YamlCredentialsTest.User("Alice", "plain", "123", Optional.of("alice@example.com")),
                    new YamlCredentialsTest.User("John", "sha256", "xxx", "reader", "dev-lead"),
                    new YamlCredentialsTest.User("Mark", "sha256", "xxx")
                ).toString()
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
