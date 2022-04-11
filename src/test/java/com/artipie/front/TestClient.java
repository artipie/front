/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;

/**
 * Test http client extension.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TestClient {

    /**
     * Port for requests.
     */
    private final int port;

    /**
     * Test http client.
     */
    private final CloseableHttpClient client;

    /**
     * Ctor.
     * @param port Port for requests
     */
    public TestClient(final int port) {
        this.port = port;
        this.client = HttpClients.createDefault();
    }

    /**
     * Obtain user token from service.
     * @param name Username
     * @param pswd User password
     * @return The token
     */
    String token(final String name, final String pswd) {
        final HttpPost request = new HttpPost(
            String.format("http://localhost:%d/token", this.port)
        );
        request.addHeader(
            HttpHeader.CONTENT_TYPE.toString(),
            MimeTypes.Type.APPLICATION_JSON.asString()
        );
        request.setEntity(
            new ByteArrayEntity(
                Json.createObjectBuilder().add("name", name).add("pass", pswd).build().toString()
                    .getBytes(StandardCharsets.UTF_8)
            )
        );
        try (CloseableHttpResponse response = this.client.execute(request)) {
            return Json.createReader(new StringReader(EntityUtils.toString(response.getEntity())))
                .readObject().getString("token");
        } catch (final IOException err) {
            throw new UncheckedIOException(err);
        }
    }

    /**
     * Perform get request and return response body as string.
     * @param line Request line
     * @param token User token
     * @return Response body as string
     */
    String get(final String line, final String token) {
        final HttpGet request = new HttpGet(
            String.format("http://localhost:%d%s", this.port, line)
        );
        request.addHeader(HttpHeader.ACCEPT.toString(), MimeTypes.Type.APPLICATION_JSON.asString());
        request.addHeader(HttpHeader.AUTHORIZATION.toString(), token);
        try (CloseableHttpResponse response = this.client.execute(request)) {
            return EntityUtils.toString(response.getEntity());
        } catch (final IOException err) {
            throw new UncheckedIOException(err);
        }
    }

    /**
     * Perform head request and return response status.
     * @param line Request line
     * @param token User token
     * @return Response status
     */
    int head(final String line, final String token) {
        final HttpHead request = new HttpHead(
            String.format("http://localhost:%d%s", this.port, line)
        );
        request.addHeader(HttpHeader.AUTHORIZATION.toString(), token);
        try (CloseableHttpResponse response = this.client.execute(request)) {
            return response.getStatusLine().getStatusCode();
        } catch (final IOException err) {
            throw new UncheckedIOException(err);
        }
    }

    /**
     * Perform put request with provided body and return response status.
     * @param line Request line
     * @param token User token
     * @param body Response body
     * @return Response status
     */
    int put(final String line, final String token, final String body) {
        final HttpPut request = new HttpPut(
            String.format("http://localhost:%d%s", this.port, line)
        );
        request.addHeader(HttpHeader.AUTHORIZATION.toString(), token);
        request.addHeader(
            HttpHeader.CONTENT_TYPE.toString(),
            MimeTypes.Type.APPLICATION_JSON.asString()
        );
        request.setEntity(new ByteArrayEntity(body.getBytes(StandardCharsets.UTF_8)));
        try (CloseableHttpResponse response = this.client.execute(request)) {
            return response.getStatusLine().getStatusCode();
        } catch (final IOException err) {
            throw new UncheckedIOException(err);
        }
    }

    /**
     * Perform delete request and return response status.
     * @param line Request line
     * @param token User token
     * @return Response status
     */
    int delete(final String line, final String token) {
        final HttpDelete request = new HttpDelete(
            String.format("http://localhost:%d%s", this.port, line)
        );
        request.addHeader(HttpHeader.AUTHORIZATION.toString(), token);
        try (CloseableHttpResponse response = this.client.execute(request)) {
            return response.getStatusLine().getStatusCode();
        } catch (final IOException err) {
            throw new UncheckedIOException(err);
        }
    }

    /**
     * Close the client.
     * @throws IOException On error
     */
    void close() throws IOException {
        this.client.close();
    }
}
