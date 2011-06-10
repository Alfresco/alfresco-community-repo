package org.alfresco.repo.security.encryption;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

public class DefaultEncryptionEngineTest extends TestCase
{
    private DefaultEncryptionEngine encryptionEngine;
    
    public void setUp() throws Exception
    {
        encryptionEngine = new DefaultEncryptionEngine();
        encryptionEngine.setKeyProvider(new TestKeyProvider());
    }
    
    public void testBasic()
    {
        try
        {
            String testString = "Hello World";

            byte[] bytes = encryptionEngine.encryptString(testString);
            String output = encryptionEngine.decryptAsString(bytes);
            assertEquals("", testString, output);
        }
        catch(UnsupportedEncodingException ex)
        {
            fail("Unexpected exception: " + ex);
        }
    }
}
