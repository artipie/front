/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.misc;

import com.artipie.asto.test.TestResource;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Test for {@link Yaml2Json}.
 * @since 0.1
 */
class Yaml2JsonTest {

    @Test
    void convertsYamlToJson() throws JSONException {
        JSONAssert.assertEquals(
            new Yaml2Json().apply(
                new String(
                    new TestResource("Yaml2JsonTest/test.yaml").asBytes(), StandardCharsets.UTF_8
                )
            ).toString(),
            new String(
                new TestResource("Yaml2JsonTest/test.json").asBytes(), StandardCharsets.UTF_8
            ),
            true
        );
    }

}
