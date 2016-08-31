/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
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
    protected static final Log logger = LogFactory.getLog(Encryptor.class);
    protected String cipherAlgorithm;
    protected String cipherProvider;

    protected KeyProvider keyProvider;

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
    
    public KeyProvider getKeyProvider()
    {
        return keyProvider;
    }
    
    public void init()
    {
        PropertyCheck.mandatory(this, "keyProvider", keyProvider);
    }
    
    /**
     * Factory method to be written by implementations to construct <b>and initialize</b>
     * physical ciphering objects.
     * 
     * @param keyAlias              the key alias
     * @param params                algorithm-specific parameters
     * @param mode                  the cipher mode
     * @return Cipher
     */
    protected abstract Cipher getCipher(String keyAlias, AlgorithmParameters params, int mode);

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
//            cipher.init(Cipher.ENCRYPT_MODE, key, params);
            throw new AlfrescoRuntimeException("Decryption failed for key alias: " + keyAlias, e);
        }
    }
    
    protected void resetCipher()
    {
        
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
     */
    @Override
    public InputStream decrypt(String keyAlias, AlgorithmParameters params, InputStream input)
    {
        Cipher cipher = getCipher(keyAlias, params, Cipher.DECRYPT_MODE);
        if (cipher == null)
        {
            return input;
        }

        try
        {
            return new CipherInputStream(input, cipher);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Decryption failed for key alias: " + keyAlias, e);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p/>
     * Serializes and {@link #encrypt(String, AlgorithmParameters, byte[]) encrypts} the input data.
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
     * {@link #decrypt(String, AlgorithmParameters, byte[]) Decrypts} and deserializes the input data
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
    public Serializable sealObject(String keyAlias, AlgorithmParameters params, Serializable input)
    {
        if (input == null)
        {
            return null;
        }
        Cipher cipher = getCipher(keyAlias, params, Cipher.ENCRYPT_MODE);
        if (cipher == null)
        {
            return input;
        }
        try
        {
            return new SealedObject(input, cipher);
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Failed to seal object", e);
        }
    }

    @Override
    public Serializable unsealObject(String keyAlias, Serializable input) throws InvalidKeyException
    {
        if (input == null)
        {
            return input;
        }
        // Don't unseal it if it is not sealed
        if (!(input instanceof SealedObject))
        {
            return input;
        }
        // Get the Key, rather than a Cipher
        Key key = keyProvider.getKey(keyAlias);
        if (key == null)
        {
            // The client will be expecting to unseal the object
            throw new IllegalStateException("No key matching " + keyAlias + ".  Cannot unseal object.");
        }
        // Unseal it using the key
        SealedObject sealedInput = (SealedObject) input;
        try
        {
            Serializable output = (Serializable) sealedInput.getObject(key);
            // Done
            return output;
        }
        catch(InvalidKeyException e)
        {
            // let these through, can be useful to client code to know this is the cause
            throw e;
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Failed to unseal object", e);
        }
    }

    public void setCipherAlgorithm(String cipherAlgorithm)
    {
        this.cipherAlgorithm = cipherAlgorithm;
    }
    
    public String getCipherAlgorithm()
    {
        return this.cipherAlgorithm;
    }

    public void setCipherProvider(String cipherProvider)
    {
        this.cipherProvider = cipherProvider;
    }
    
    public String getCipherProvider()
    {
        return this.cipherProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlgorithmParameters decodeAlgorithmParameters(byte[] encoded)
    {
        try
        {
            AlgorithmParameters p = null;
            String algorithm = "DESede";
            if(getCipherProvider() != null)
            {
                p = AlgorithmParameters.getInstance(algorithm, getCipherProvider());
            }
            else
            {
                p = AlgorithmParameters.getInstance(algorithm);
            }
            p.init(encoded);
            return p;
        }
        catch(Exception e)
        {
            throw new AlfrescoRuntimeException("", e);
        }
    }
}
