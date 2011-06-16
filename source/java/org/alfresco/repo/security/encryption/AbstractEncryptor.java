package org.alfresco.repo.security.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic support for encryption engines.
 * 
 * @since 4.0
 */
public abstract class AbstractEncryptor implements Encryptor
{
    private static final Log logger = LogFactory.getLog(AbstractEncryptor.class);
    
    private KeyProvider keyProvider;
    
    /**
     * Constructs with defaults
     */
    protected AbstractEncryptor()
    {
    }
    
    /**
     * @param keyProvider               provides encryption keys based on aliases
     */
    public void setKeyProvider(KeyProvider keyProvider)
    {
        this.keyProvider = keyProvider;
    }
    
    public void init()
    {
        PropertyCheck.mandatory(this, "keyProvider", keyProvider);
    }

    @Override
    public Cipher getCipher(String keyAlias, AlgorithmParameters params, int mode)
    {
        // Get the encryption key
        Key key = keyProvider.getKey(keyAlias);
        if (key == null)
        {
            // No encryption possible
            return null;
        }
        try
        {
            Cipher cipher = getCipher(key, params, mode);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("Cipher constructed: alias=" + keyAlias + "; mode=" + mode + ": " + cipher);
            }
            return cipher;
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to construct cipher: alias=" + keyAlias + "; mode=" + mode,
                    e);
        }
    }
    
    /**
     * Factory method to be written by implementations to construct <b>and initialize</b>
     * physical ciphering objects.
     * 
     * @param keyAlias              the key alias
     * @param params                algorithm-specific parameters
     * @param mode                  the cipher mode
     * @return
     */
    protected abstract Cipher getCipher(Key key, AlgorithmParameters params, int mode) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<byte[], AlgorithmParameters> encrypt(String keyAlias, AlgorithmParameters params, byte[] input)
    {
        Cipher cipher = getCipher(keyAlias, params, Cipher.ENCRYPT_MODE);
        if (cipher == null)
        {
            return new Pair<byte[], AlgorithmParameters>(input, null);
        }
        try
        {
            byte[] output = cipher.doFinal(input);
            params = cipher.getParameters();
            return new Pair<byte[], AlgorithmParameters>(output, params);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Decryption failed for key alias: " + keyAlias, e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] decrypt(String keyAlias, AlgorithmParameters params, byte[] input)
    {
        Cipher cipher = getCipher(keyAlias, params, Cipher.DECRYPT_MODE);
        if (cipher == null)
        {
            return input;
        }
        try
        {
            return cipher.doFinal(input);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Decryption failed for key alias: " + keyAlias, e);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p/>
     * Serializes and {@link #encrypt(byte[]) encrypts} the input data.
     */
    @Override
    public Pair<byte[], AlgorithmParameters> encryptObject(String keyAlias, AlgorithmParameters params, Object input)
    {
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(input);
            byte[] unencrypted = bos.toByteArray();
            return encrypt(keyAlias, params, unencrypted);
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Failed to serialize or encrypt object", e);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p/>
     * {@link #decrypt(byte[]) Decrypts} and deserializes the input data
     */
    @Override
    public Object decryptObject(String keyAlias, AlgorithmParameters params, byte[] input)
    {
        try
        {
            byte[] unencrypted = decrypt(keyAlias, params, input);
            ByteArrayInputStream bis = new ByteArrayInputStream(unencrypted);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object obj = ois.readObject();
            return obj;
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Failed to deserialize or decrypt object", e);
        }
    }

    @Override
    public SealedObject sealObject(String keyAlias, AlgorithmParameters params, Serializable input)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Serializable unsealObject(String keyAlias, SealedObject input)
    {
        throw new UnsupportedOperationException();
    }    
}
