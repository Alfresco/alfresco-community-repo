package org.alfresco.repo.security.encryption;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Provides the system-wide secret key for symmetric database encryption from a key store
 * in the filesystem.
 * 
 */
public class KeystoreKeyProvider extends AbstractKeyProvider
{
    private static final Log logger = LogFactory.getLog(KeyProvider.class);

    private static String KEY_STORE_TYPE = "JCEKS";
    private static String SECRET_KEY_ALIAS = "secret";

    // key store holding the secret key for encrypting and decrypting repository properties
    private String keyStoreFile;

    // key store passwords
    private char[] keyStorePassword;
    private char[] secretKeyPassword;

    public void setKeyStoreFile(String keyStoreFile)
    {
        this.keyStoreFile = keyStoreFile;
    }

    public void setKeyStorePassword(String keyStorePassword)
    {
        this.keyStorePassword = keyStorePassword.toCharArray();
    }
    
    public void setSecretKeyPassword(String secretKeyPassword)
    {
        this.secretKeyPassword = secretKeyPassword.toCharArray();
    }

    public Key getKey()
    {
        return super.getKey();
    }
    
    protected void saveKeyStore(KeyStore ks) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException
    {
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(keyStoreFile);
            ks.store(fos, keyStorePassword);
        }
        finally
        {
            if(fos != null)
            {
                fos.close();
            }
        }
    }
    
    /*
     * Create a new secret key and store it in the keystore ks
     */
    protected void createSecretKey(KeyStore ks) throws Exception
    {
        Key key = generateSecretKey();
        if(key == null)
        {
            throw new AlfrescoRuntimeException("Unable to generate secret key");
        }

        byte[] encoded = key.getEncoded();

        logger.debug("secret key size = " + (encoded.length * 8) + " bits");

        KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry((SecretKey)key);

        ks.setEntry(SECRET_KEY_ALIAS, skEntry, new KeyStore.PasswordProtection(secretKeyPassword));

        saveKeyStore(ks);
    }

    protected void loadKeyStore()
    {
        InputStream is = null;
        KeyStore ks = null;
        
        try
        {
            ks = KeyStore.getInstance(KEY_STORE_TYPE);
            
            File f = new File(keyStoreFile);
            if(!f.exists())
            {
                // no keystore, create one and save it
                ks.load(null, keyStorePassword);
                
                // generate a secret key
                createSecretKey(ks);
            }
            else
            {
                is = new BufferedInputStream(new FileInputStream(keyStoreFile));
                ks.load(is, keyStorePassword);
            }
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Unable to load keystore from " + keyStoreFile, e);            
        }
        finally
        {
            if (is != null)
            
            {
                try
                {
                    is.close();
                }
                catch(IOException e)
                {
                }
            }
        }

        try
        {
            Key key = ks.getKey(SECRET_KEY_ALIAS, secretKeyPassword);
            if(key == null)
            {
                createSecretKey(ks);
            }

            setKey(key);
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException(
            "Unable to get secret key from " + keyStoreFile, e);            
        }
    }

    public void init()
    {
        ParameterCheck.mandatory("keyStoreFile", keyStoreFile);
        ParameterCheck.mandatory("passwordGenerator", passwordGenerator);

        loadKeyStore();
    }

}
