/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

/**
 * Tokens for API.
 * <p>
 * This class generates tokens for user with specified
 * expiration time. User name and expiration time are
 * embedded into token value, then added randome 4-byte nonce
 * and then 20-byte HMAC (SHA1) based on server key.
 * Token structure is: first byte is user name length (user name max 256 bytes),
 * then user name UTF-8 encoded string, then 4 byte of expiration time
 * epoch seconds, then 4 bytes nonce, then 20 bytes HMAC.
 * This class provides API for generating and
 * validating token. Also, it contains helper object to parse
 * token back.
 * </p>
 * @since 1.0
 * @checkstyle MagicNumberCheck (500 lines)
 * @checkstyle TrailingCommentCheck (500 lines)
 */
public final class ApiTokens {

    /**
     * HMAC key.
     */
    private final byte[] key;

    /**
     * Random number generator.
     */
    private final Random rng;

    /**
     * New tokens object.
     * @param key HMAC key
     * @param rng Random number generator
     */
    public ApiTokens(final byte[] key, final Random rng) {
        this.key = Arrays.copyOf(key, key.length);
        this.rng = rng;
    }

    /**
     * Generate new token for user.
     * @param user User ID
     * @param expiration Token expiration time
     * @return HEX encoded token with HMAC
     */
    public String token(final String user, final Instant expiration) {
        final var ubin = user.getBytes(StandardCharsets.UTF_8);
        if (ubin.length > 256) {
            throw new IllegalStateException("user name is too long");
        }
        final byte[] nonce = new byte[4];
        this.rng.nextBytes(nonce);
        final var buf = ByteBuffer.allocate(1 + ubin.length + 8 + 20);
        buf.put((byte) ubin.length);
        buf.put(ubin);
        buf.putInt((int) (expiration.toEpochMilli() / 1000));
        buf.put(nonce);
        final var dup = buf.duplicate();
        dup.rewind();
        dup.limit(dup.limit() - 20);
        final byte[] hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, this.key).hmac(dup);
        buf.put(hmac);
        buf.rewind();
        return Hex.encodeHexString(buf);
    }

    /**
     * Validate token.
     * @param token Token to be validated.
     * @return True if HMAC is valid
     * @checkstyle ReturnCountCheck (20 lines)
     * @checkstyle MethodBodyCommentsCheck (20 lines)
     */
    @SuppressWarnings("PMD.OnlyOneReturn")
    public boolean validate(final String token) {
        final byte[] bin;
        try {
            bin = Hex.decodeHex(token);
        } catch (final DecoderException ignore) {
            // token is not valid if hex string is malformed
            return false;
        }
        final byte[] unsigned = new byte[bin.length - 20];
        final byte[] signature = new byte[20];
        System.arraycopy(bin, 0, unsigned, 0, unsigned.length);
        System.arraycopy(bin, unsigned.length, signature, 0, 20);
        final byte[] hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, this.key).hmac(unsigned);
        return Arrays.equals(hmac, signature);
    }

    /**
     * Token helper object.
     * @since 1.0
     */
    public static final class Token {

        /**
         * Token data.
         */
        private final byte[] data;

        /**
         * Private constructor.
         * @param data Token data
         */
        private Token(final byte[] data) {
            this.data = Arrays.copyOf(data, data.length);
        }

        /**
         * Extract user ID from token.
         * @return User ID string
         */
        public String user() {
            final int len = this.data[0] & 0xFF; // unsigned length byte
            final byte[] bin = new byte[len];
            System.arraycopy(this.data, 1, bin, 0, len);
            return new String(bin, StandardCharsets.UTF_8);
        }

        /**
         * Check if token was expired.
         * @param now Current time
         * @return True if token was expired
         */
        public boolean expired(final Instant now) {
            final int len = this.data[0] & 0xFF; // unsigned length byte
            final byte[] bin = new byte[4];
            System.arraycopy(this.data, 1 + len, bin, 0, 4);
            final int epoch = ByteBuffer.wrap(bin).getInt();
            return Instant.ofEpochMilli(epoch * 1000).isBefore(now);
        }

        /**
         * Parse token from hex string.
         * @param token Token string
         * @return Token object
         * @throws IllegalArgumentException if token is malformed
         */
        @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
        public static Token parse(final String token) {
            try {
                return new Token(Hex.decodeHex(token));
            } catch (final DecoderException err) {
                throw new IllegalArgumentException("invalid token", err);
            }
        }
    }
}
