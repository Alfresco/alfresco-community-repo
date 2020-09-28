/*
 * Copyright 2005-2010 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Tests that the EncryptingOutputStream and EncryptingInputStream classes work correctly.
 */
public class EncryptingOutputStreamTest extends TestCase
{

    /**
     * Tests encryption / decryption by attempting to encrypt and decrypt the bytes forming this class definition and
     * comparing it with the unencrypted bytes.
     * 
     * @throws Exception
     *             an exception
     */
    public void testEncrypt() throws Exception
    {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        final byte[] seed = getClass().getName().getBytes("UTF-8");
        random.setSeed(seed);
        keyGen.initialize(1024, random);
        final KeyPair pair = keyGen.generateKeyPair();

        final ByteArrayOutputStream buff = new ByteArrayOutputStream(2048);
        final OutputStream encrypting = new EncryptingOutputStream(buff, pair.getPublic(), random);
        final ByteArrayOutputStream plainText1 = new ByteArrayOutputStream(2048);

        final InputStream in = getClass().getResourceAsStream(getClass().getSimpleName() + ".class");
        final byte[] inbuff = new byte[17];
        int bytesRead;
        while ((bytesRead = in.read(inbuff)) != -1)
        {
            encrypting.write(inbuff, 0, bytesRead);
            plainText1.write(inbuff, 0, bytesRead);
        }
        in.close();
        encrypting.close();
        plainText1.close();
        final InputStream decrypting = new DecryptingInputStream(new ByteArrayInputStream(buff.toByteArray()), pair
                .getPrivate());
        final ByteArrayOutputStream plainText2 = new ByteArrayOutputStream(2048);
        while ((bytesRead = decrypting.read(inbuff)) != -1)
        {
            plainText2.write(inbuff, 0, bytesRead);
        }

        assertTrue(Arrays.equals(plainText1.toByteArray(), plainText2.toByteArray()));

    }
}
