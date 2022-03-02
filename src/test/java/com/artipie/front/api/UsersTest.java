/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.auth.YamlCredentialsTest;
import com.artipie.front.auth.YamlUsers;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
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

    /**
     * Credentials file.
     */
    private static final Key CREDS = new Key.From("creds.yaml");

    /**
     * Test storage.
     */
    private BlockingStorage blsto;

    /**
     * Test users.
     */
    private com.artipie.front.auth.Users users;

    @BeforeEach
    void init() {
        this.blsto = new BlockingStorage(new InMemoryStorage());
        this.users = new YamlUsers(UsersTest.CREDS, this.blsto);
    }

    @Test
    void writesUsers() throws JSONException {
        this.blsto.save(
            UsersTest.CREDS,
            YamlCredentialsTest.credYaml(
                YamlCredentialsTest.PasswordFormat.SIMPLE,
                // @checkstyle LineLengthCheck (1 line)
                new YamlCredentialsTest.User("Alice", "plain", "123", Optional.of("alice@example.com")),
                new YamlCredentialsTest.User("John", "sha256", "xxx", "reader", "dev-lead"),
                new YamlCredentialsTest.User("Mark", "sha256", "xxx")
            ).toString().getBytes(StandardCharsets.UTF_8)
        );
        JSONAssert.assertEquals(
            new Users(this.users)
                .handle(Mockito.mock(Request.class), Mockito.mock(Response.class)),
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
            new Users(this.users)
                .handle(Mockito.mock(Request.class), Mockito.mock(Response.class)),
            "{}",
            true
        );
    }

}
