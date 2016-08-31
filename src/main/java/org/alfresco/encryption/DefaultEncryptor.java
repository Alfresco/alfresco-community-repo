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

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.PropertyCheck;

/**
 * @author Derek Hulley
 * @since 4.0
 */
public class DefaultEncryptor extends AbstractEncryptor
{
    private boolean cacheCiphers = true;
    private final ThreadLocal<Map<CipherKey, CachedCipher>> threadCipher;

    /**
     * Default constructor for IOC
     */
    public DefaultEncryptor()
    {
        threadCipher = new ThreadLocal<Map<CipherKey, CachedCipher>>();
    }
    
    /**
     * Convenience constructor for tests
     */
    /* package */ DefaultEncryptor(KeyProvider keyProvider, String cipherAlgorithm, String cipherProvider)
    {
        this();
        setKeyProvider(keyProvider);
        setCipherAlgorithm(cipherAlgorithm);
        setCipherProvider(cipherProvider);
    }
    
    public void init()
    {
        super.init();
        PropertyCheck.mandatory(this, "cipherAlgorithm", cipherAlgorithm);
    }
    
    public void setCacheCiphers(boolean cacheCiphers)
    {
        this.cacheCiphers = cacheCiphers;
    }

    protected Cipher createCipher(int mode, String algorithm, String provider, Key key, AlgorithmParameters params)
    throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException
    {
        Cipher cipher = null;

        if (cipherProvider == null)
        {
            cipher = Cipher.getInstance(algorithm);
        }
        else
        {
            cipher = Cipher.getInstance(algorithm, provider);
        }
        cipher.init(mode, key, params);
        
        return cipher;
    }

    protected Cipher getCachedCipher(String keyAlias, int mode, AlgorithmParameters params, Key key)
    throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException
    {
        CachedCipher cipherInfo = null;
        Cipher cipher = null;

        Map<CipherKey, CachedCipher> ciphers = threadCipher.get();
        if(ciphers == null)
        {
            ciphers = new HashMap<CipherKey, CachedCipher>(5);
            threadCipher.set(ciphers);
        }
        cipherInfo = ciphers.get(new CipherKey(keyAlias, mode));
        if(cipherInfo == null)
        {
            cipher = createCipher(mode, cipherAlgorithm, cipherProvider, key, params);
            ciphers.put(new CipherKey(keyAlias, mode), new CachedCipher(cipher, key));

            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("Cipher constructed: alias=" + keyAlias + "; mode=" + mode + ": " + cipher);
            }
        }
        else
        {
            // the key has changed, re-construct the cipher
            if(cipherInfo.getKey() != key)
            {
                // key has changed, rendering the cached cipher out of date. Re-create the cipher with
                // the new key.
                cipher = createCipher(mode, cipherAlgorithm, cipherProvider, key, params);
                ciphers.put(new CipherKey(keyAlias, mode), new CachedCipher(cipher, key));
            }
            else
            {
                cipher = cipherInfo.getCipher();
            }
        }
        
        return cipher;
    }

    @Override
    public Cipher getCipher(String keyAlias, AlgorithmParameters params, int mode)
    {
        Cipher cipher = null;

        // Get the encryption key
        Key key = keyProvider.getKey(keyAlias);
        if(key == null)
        {
            // No encryption possible
            return null;
        }

        try
        {
            if(cacheCiphers)
            {
                cipher = getCachedCipher(keyAlias, mode, params, key);
            }
            else
            {
                cipher = createCipher(mode, cipherAlgorithm, cipherProvider, key, params);
            }
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to construct cipher: alias=" + keyAlias + "; mode=" + mode,
                    e);
        }

        return cipher;
    }
    
    public boolean keyAvailable(String keyAlias)
    {
        return keyProvider.getKey(keyAlias) != null;
    }

    private static class CipherKey
    {
        private String keyAlias;
        private int mode;

        public CipherKey(String keyAlias, int mode)
        {
            super();
            this.keyAlias = keyAlias;
            this.mode = mode;
        }
        
        public String getKeyAlias()
        {
            return keyAlias;
        }

        public int getMode()
        {
            return mode;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((keyAlias == null) ? 0 : keyAlias.hashCode());
            result = prime * result + mode;
            return result;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if(this == obj)
            {
                return true;
            }
            
            if(!(obj instanceof CipherKey))
            {
                return false;
            }

            CipherKey other = (CipherKey)obj;
            if(keyAlias == null)
            {
                if (other.keyAlias != null)
                {
                    return false;
                }
            }
            else if(!keyAlias.equals(other.keyAlias))
            {
                return false;
            }
            
            if(mode != other.mode)
            {
                return false;
            }

            return true;
        }
    }

    /*
     * Stores a cipher and the key used to construct it.
     */
    private static class CachedCipher
    {
        private Key key;
        private Cipher cipher;

        public CachedCipher(Cipher cipher, Key key)
        {
            super();
            this.cipher = cipher;
            this.key = key;
        }
        
        public Cipher getCipher()
        {
            return cipher;
        }

        public Key getKey()
        {
            return key;
        }
    }
}
