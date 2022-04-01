/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test for {@link AccessPermissions.FromYaml}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class AccessPermissionsFromYamlTest {

    @ParameterizedTest
    @CsvSource({
        "/repositories/my-python,GET,repo-read;god",
        "/users,HEAD,users-read;users-write;god",
        "/users/jane,DELETE,users-write;god",
        "/storages/default,PATCH,god",
        "/any,PUT,god"
    })
    void returnsPermissionsList(final String line, final String method, final String list)
        throws IOException {
        MatcherAssert.assertThat(
            new AccessPermissions.FromYaml(this.yaml()).get(line, method),
            Matchers.containsInAnyOrder(list.split(";"))
        );
    }

    private YamlMapping yaml() throws IOException {
        return Yaml.createYamlInput(
            String.join(
                "\n",
                "'/repositories.*':",
                "  \"GET|HEAD\":",
                "    - repo-read",
                "  \"PUT|DELETE\":",
                "    - repo-write",
                "\"/users.*\":",
                "  \"GET|HEAD\":",
                "    - users-read",
                "    - users-write",
                "  \"PUT|DELETE|POST\":",
                "    - users-write",
                "\".*\":",
                "  \".*\":",
                "    - god"
            )
        ).readYamlMapping();
    }

}
