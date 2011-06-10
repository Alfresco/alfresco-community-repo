package org.alfresco.repo.security.encryption;

import java.security.Key;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.alfresco.error.AlfrescoRuntimeException;

public class TestKeyProvider implements KeyProvider
{
    public Key getKey()
    {
        try
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecretKey key = keyGenerator.generateKey();
            return key;
            //        return Hex.decode("80000000000000000000000000000000");
        }
        catch(Exception e)
        {
            throw new AlfrescoRuntimeException("Unexpected exception generating secret key", e);
        }
    }
}
