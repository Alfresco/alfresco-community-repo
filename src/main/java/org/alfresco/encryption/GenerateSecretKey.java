package org.alfresco.encryption;

import java.security.SecureRandom;

import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * 
 * Generate a secret key for use by the repository.
 * 
 * @since 4.0
 *
 */
public class GenerateSecretKey
{
    public byte[] generateKeyData()
    {
        try
        {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(System.currentTimeMillis());
            byte bytes[] = new byte[DESedeKeySpec.DES_EDE_KEY_LEN];
            random.nextBytes(bytes);
            return bytes;
        }
        catch(Exception e)
        {
            throw new RuntimeException("Unable to generate secret key", e);
        }
    }
    
    public static void main(String args[])
    {
        try
        {
            GenerateSecretKey gen = new GenerateSecretKey();
            byte[] bytes = gen.generateKeyData();
            System.out.print(Base64.encodeBase64String(bytes));
        }
        catch(Throwable e)
        {
            e.printStackTrace();
        }
    }
}
