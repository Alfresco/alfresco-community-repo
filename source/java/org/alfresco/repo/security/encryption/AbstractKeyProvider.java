package org.alfresco.repo.security.encryption;

import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.KeyGenerator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.PasswordGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public abstract class AbstractKeyProvider /*extends AbstractLifecycleBean*/ implements KeyProvider
{
    private static final Log logger = LogFactory.getLog(KeyProvider.class);
    
    private static int KEY_SIZE = 256; // this requires unlimited strength policy files
    private static int DEFAULT_KEY_SIZE = 128; // default key size should work if KEY_SIZE doesn't
    private static String KEY_ALGORITHM = "AES";

    protected PasswordGenerator passwordGenerator;
    
    private Key key;

    public void setKey(Key key)
    {
        this.key = key;
    }
    
    
    public PasswordGenerator getPasswordGenerator()
    {
        return passwordGenerator;
    }
    
    public void setPasswordGenerator(PasswordGenerator passwordGenerator)
    {
        this.passwordGenerator = passwordGenerator;
    }

    public Key getKey()
    {
        return key;
    }
    
    protected KeyGenerator getKeyGenerator()
    {
        KeyGenerator keyGenerator = null;

        try
        {
            keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        }
        catch(NoSuchAlgorithmException e)
        {
            Security.addProvider(new BouncyCastleProvider());
            try
            {
                keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            }
            catch(NoSuchAlgorithmException e1)
            {
                throw new AlfrescoRuntimeException("Unable to initialise encryption engine, no key generator is available", e1);
            }            
        }

        if(keyGenerator == null)
        {
            throw new AlfrescoRuntimeException("Unable to initialise encryption engine, no key generator is available");
        }

        try
        {
            keyGenerator.init(KEY_SIZE);
        }
        catch(InvalidParameterException e)
        {
            logger.warn(KEY_SIZE + " bits key size is not supported, trying " + DEFAULT_KEY_SIZE + " bits");
            try
            {
                // try a smaller key size
                keyGenerator.init(DEFAULT_KEY_SIZE);
            }
            catch(InvalidParameterException e1)
            {
                throw new AlfrescoRuntimeException("Unable to initialise encryption engine, no key generator is available", e1);
            }
        }

        return keyGenerator;
    }

    protected Key generateSecretKey()
    {
        KeyGenerator keyGenerator = getKeyGenerator();
        return keyGenerator.generateKey();
    }
}
