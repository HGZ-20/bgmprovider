/*
 * Copyright (c) 2021, Huawei Technologies Co., Ltd. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Huawei designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Huawei in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please visit https://gitee.com/openeuler/bgmprovider if you need additional
 * information or have any questions.
 */

package org.openeuler;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static org.junit.Assert.assertArrayEquals;

/**
 * SM2 test
 */
public class SM2Test {
    private static final byte[] INFO = "SM2 test".getBytes();
    private static final byte[] PUBLIC_KEY_BYTES = new byte[]{
            48, 89, 48, 19, 6, 7, 42, -122, 72, -50, 61, 2, 1, 6, 8, 42,
            -127, 28, -49, 85, 1, -126, 45, 3, 66, 0, 4, 10, -36, -22, -20, 17,
            26, 86, -114, -52, -78, 79, -22, 116, -47, -70, -33, 112, 32, -18, 92, -45,
            -58, 20, 36, -5, 55, 68, -95, -57, -121, 10, 33, -76, 54, 24, -119, -104,
            61, -24, -113, 46, -57, 36, -78, -37, -95, -113, -52, -88, -5, 22, -67, 101,
            94, 37, 2, -58, 55, -35, 15, -21, 31, -49, -80
    };
    private static final byte[] PRIVATE_KEY_BYTES = new byte[]{
            48, -127, -109, 2, 1, 0, 48, 19, 6, 7, 42, -122, 72, -50, 61, 2,
            1, 6, 8, 42, -127, 28, -49, 85, 1, -126, 45, 4, 121, 48, 119, 2,
            1, 1, 4, 32, -104, 71, 54, -41, 24, 66, 82, -45, 114, -113, -121, -105,
            -35, 35, 9, 49, -8, 119, 44, 118, 80, -20, 47, -38, -69, -47, 121, -8,
            -73, -33, 4, 54, -96, 10, 6, 8, 42, -127, 28, -49, 85, 1, -126, 45,
            -95, 68, 3, 66, 0, 4, 10, -36, -22, -20, 17, 26, 86, -114, -52, -78,
            79, -22, 116, -47, -70, -33, 112, 32, -18, 92, -45, -58, 20, 36, -5, 55,
            68, -95, -57, -121, 10, 33, -76, 54, 24, -119, -104, 61, -24, -113, 46, -57,
            36, -78, -37, -95, -113, -52, -88, -5, 22, -67, 101, 94, 37, 2, -58, 55,
            -35, 15, -21, 31, -49, -80
    };

    private static final byte[] ENCRYPTED_BYTES = new byte[]{
            48, 113, 2, 33, 0, -91, 51, 29, -122, -26, 120, 43, 27, 115, -57, -98,
            -124, 114, -30, -83, 69, -69, -38, -54, -38, 127, 90, -89, -40, 114, -9, 99,
            111, 121, 55, -81, 109, 2, 32, 6, -103, 108, -59, -11, -108, -7, 116, 34,
            -8, -29, 58, -43, -109, -121, -66, -62, -82, 92, 117, 100, -28, 63, -103, -32,
            -81, 10, 4, -46, 114, 49, 34, 4, 32, 18, 66, 110, 22, -3, -101, -122,
            46, 21, 25, 29, 35, -82, -119, 38, -10, -19, -30, 69, -100, -118, -105, 116,
            -105, -65, -110, -24, -42, -17, 84, -66, 82, 4, 8, 7, 14, 4, 64, 95, 31, 87, 93
    };

    private static PrivateKey privateKey;

    private static PublicKey publicKey;

    /**
     * Init private key and public key
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        Security.insertProviderAt(new BGMJCEProvider(), 1);
        KeyFactory keyFactory = KeyFactory.getInstance("SM2");
        publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(PUBLIC_KEY_BYTES));
        privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(PRIVATE_KEY_BYTES));
    }

    /**
     * Test private key decryption
     */
    @Test
    public void testDecryptByPrivateKey() throws Exception {
        byte[] decryptBytes = decrypt(privateKey, ENCRYPTED_BYTES);
        assertArrayEquals(INFO, decryptBytes);
    }

    /**
     * Test public key encryption and private key decryption
     */
    @Test
    public void testEncryptByPublicKey() throws Exception {
        byte[] encryptBytes = encrypt(publicKey, INFO);
        byte[] decryptBytes = decrypt(privateKey, encryptBytes);
        assertArrayEquals(INFO, decryptBytes);
    }

    /**
     * Test private key encryption and public key decryption
     */
    @Test(expected = InvalidKeyException.class)
    public void testEncryptByPrivateKey() throws Exception {
        encrypt(privateKey, INFO);
    }

    @Test
    public void testWrapAndUnwrap()
            throws Exception {
        KeyPair keyPair = generateKeyPair();
        KeyPair wrapKeyPair = generateKeyPair();
        Cipher cipher = Cipher.getInstance("SM2");
        cipher.init(Cipher.WRAP_MODE, keyPair.getPublic());
        byte[] wrappedKeyBytes = cipher.wrap(wrapKeyPair.getPublic());
        cipher.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
        Key unWrappedKey = cipher.unwrap(wrappedKeyBytes, "SM2", Cipher.PUBLIC_KEY);
        assertArrayEquals(wrapKeyPair.getPublic().getEncoded(), unWrappedKey.getEncoded());
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("SM2");
        return keyPairGenerator.generateKeyPair();
    }

    private byte[] doCrypt(int opmode, Key key, byte[] input) throws Exception {
        Cipher cipher = Cipher.getInstance("SM2");
        cipher.init(opmode, key);
        cipher.update(input);
        return cipher.doFinal();
    }

    private byte[] encrypt(Key key, byte[] input) throws Exception {
        return doCrypt(Cipher.ENCRYPT_MODE, key, input);
    }

    private byte[] decrypt(Key key, byte[] input) throws Exception {
        return doCrypt(Cipher.DECRYPT_MODE, key, input);
    }
}
