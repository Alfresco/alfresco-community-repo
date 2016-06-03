package org.alfresco.encryption;

import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.KeyException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.Pair;

/**
 * @since 4.0
 */
public class EncryptorTest extends TestCase
{
    private DefaultEncryptor encryptor;
    
    public void setUp() throws Exception
    {
        encryptor = new DefaultEncryptor(
                KeyStoreKeyProviderTest.getTestKeyStoreProvider(),
                "DESede/CBC/PKCS5Padding",
                null);
        encryptor.init();        // Not currently necessary
    }
    
    public void testBasicBytes_NoKey()
    {
        byte[] bytes = new byte[] {11, 12, 13};

        Pair<byte[], AlgorithmParameters> encryptedPair = encryptor.encrypt("fluff", null, bytes);
        byte[] decrypted = encryptor.decrypt(
                "fluff",
                encryptedPair.getSecond(),
                encryptedPair.getFirst());
        assertTrue("Encryption round trip failed. ", Arrays.equals(bytes, decrypted));
    }
    
    public void testBasicBytes_WithKey()
    {
        byte[] bytes = new byte[] {11, 12, 13};

        Pair<byte[], AlgorithmParameters> encryptedPair = encryptor.encrypt("mykey1", null, bytes);
        byte[] decrypted = encryptor.decrypt(
                "mykey1",
                encryptedPair.getSecond(),
                encryptedPair.getFirst());
        assertTrue("Encryption round trip failed. ", Arrays.equals(bytes, decrypted));
    }
    
    public void testBasicObject()
    {
        Object testObject = "   This is a string, but will be serialized    ";

        Pair<byte[], AlgorithmParameters> encryptedPair = encryptor.encryptObject("mykey2", null, testObject);
        Object output = encryptor.decryptObject(
                "mykey2",
                encryptedPair.getSecond(),
                encryptedPair.getFirst());
        assertEquals("Encryption round trip failed. ", testObject, output);
    }
    
    public void testSealedObject()
    {
        Serializable testObject = "   This is a string, but will be serialized    ";

        Serializable sealedObject = encryptor.sealObject("mykey2", null, testObject);
        try
        {
        	Object output = encryptor.unsealObject("mykey2", sealedObject);
            assertEquals("Encryption round trip failed. ", testObject, output);
        }
        catch(KeyException e)
        {
        	throw new AlfrescoRuntimeException("", e);
        }
    }
}
