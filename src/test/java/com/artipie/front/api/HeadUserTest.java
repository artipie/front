/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.settings.YamlCredentials;
import com.artipie.front.settings.YamlCredentialsTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link HeadUser}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class HeadUserTest {

    @Test
    void returnsOkWhenUserFound() {
        final var rqs = Mockito.mock(Request.class);
        final String uid = "Alice";
        Mockito.when(rqs.params(GetUser.USER_PARAM)).thenReturn(uid);
        final Response resp = Mockito.mock(Response.class);
        new HeadUser(
            new YamlCredentials(
                YamlCredentialsTest.credYaml(
                    YamlCredentialsTest.PasswordFormat.SIMPLE,
                    new YamlCredentialsTest.User(uid, "plain", "123")
                )
            )
        ).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.OK_200);
    }

    @Test
    void returnsNotFoundWhenUserDoesNotExists() {
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetUser.USER_PARAM)).thenReturn("Someone");
        final Response resp = Mockito.mock(Response.class);
        new HeadUser(
            new YamlCredentials(
                YamlCredentialsTest.credYaml(
                    YamlCredentialsTest.PasswordFormat.SIMPLE,
                    new YamlCredentialsTest.User("John", "plain", "123")
                )
            )
        ).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.NOT_FOUND_404);
    }

}
