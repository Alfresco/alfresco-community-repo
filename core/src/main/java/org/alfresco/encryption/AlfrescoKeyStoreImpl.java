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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.management.openmbean.KeyAlreadyExistsException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.alfresco.encryption.EncryptionKeysRegistry.KEY_STATUS;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This wraps a Java Keystore and caches the encryption keys. It manages the loading and caching of the encryption keys
 * and their registration with and validation against the encryption key registry.
 * 
 * @since 4.0
 *
 */
public class AlfrescoKeyStoreImpl implements AlfrescoKeyStore
{
    private static final Log logger = LogFactory.getLog(AlfrescoKeyStoreImpl.class);
    
    protected KeyStoreParameters keyStoreParameters;
    protected KeyStoreParameters backupKeyStoreParameters;
    protected KeyResourceLoader keyResourceLoader;
    protected EncryptionKeysRegistry encryptionKeysRegistry;

    protected KeyMap keys;
    protected KeyMap backupKeys;
    protected final WriteLock writeLock;
    protected final ReadLock readLock;

    private Set<String> keysToValidate;
    protected boolean validateKeyChanges = false;

    public AlfrescoKeyStoreImpl()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        writeLock = lock.writeLock();
        readLock = lock.readLock();
        this.keys = new KeyMap();
        this.backupKeys = new KeyMap();
    }

    public AlfrescoKeyStoreImpl(KeyStoreParameters keyStoreParameters, KeyResourceLoader keyResourceLoader)
    {
        this();

        this.keyResourceLoader = keyResourceLoader;
        this.keyStoreParameters = keyStoreParameters;

        safeInit();
    }
    
    public void init()
    {
        writeLock.lock();
        try
        {
            safeInit();
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    public void setEncryptionKeysRegistry(
            EncryptionKeysRegistry encryptionKeysRegistry)
    {
        this.encryptionKeysRegistry = encryptionKeysRegistry;
    }

    public void setValidateKeyChanges(boolean validateKeyChanges)
    {
        this.validateKeyChanges = validateKeyChanges;
    }

    public void setKeysToValidate(Set<String> keysToValidate)
    {
        this.keysToValidate = keysToValidate;
    }

    public void setKeyStoreParameters(KeyStoreParameters keyStoreParameters)
    {
        this.keyStoreParameters = keyStoreParameters;
    }

    public void setBackupKeyStoreParameters(
            KeyStoreParameters backupKeyStoreParameters)
    {
        this.backupKeyStoreParameters = backupKeyStoreParameters;
    }

    public void setKeyResourceLoader(KeyResourceLoader keyResourceLoader)
    {
        this.keyResourceLoader = keyResourceLoader;
    }

    public KeyStoreParameters getKeyStoreParameters()
    {
        return keyStoreParameters;
    }

    public KeyStoreParameters getBackupKeyStoreParameters()
    {
        return backupKeyStoreParameters;
    }

    public KeyResourceLoader getKeyResourceLoader()
    {
        return keyResourceLoader;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return keyStoreParameters.getName();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void validateKeys() throws InvalidKeystoreException, MissingKeyException
    {
        validateKeys(keys, backupKeys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists()
    {
        return keyStoreExists(getKeyStoreParameters().getLocation());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void reload() throws InvalidKeystoreException, MissingKeyException
    {
        KeyMap keys = loadKeyStore(getKeyStoreParameters());
        KeyMap backupKeys = loadKeyStore(getBackupKeyStoreParameters());

        validateKeys(keys, backupKeys);

        // all ok, reload the keys
        writeLock.lock();
        try
        {
            this.keys = keys;
            this.backupKeys = backupKeys;
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getKeyAliases()
    {
        return new HashSet<String>(keys.getKeyAliases());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void backup()
    {
        writeLock.lock();
        try
        {
            for(String keyAlias : keys.getKeyAliases())
            {
                backupKeys.setKey(keyAlias, keys.getKey(keyAlias));
            }
            createKeyStore(backupKeyStoreParameters, backupKeys);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void create()
    {
        createKeyStore(keyStoreParameters, keys);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Key getKey(String keyAlias)
    {
        readLock.lock();
        try
        {
            return keys.getCachedKey(keyAlias).getKey();
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public long getKeyTimestamp(String keyAlias)
    {
        readLock.lock();
        try
        {
            CachedKey cachedKey = keys.getCachedKey(keyAlias);
            return cachedKey.getTimestamp();
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Key getBackupKey(String keyAlias)
    {
        readLock.lock();
        try
        {
            return backupKeys.getCachedKey(keyAlias).getKey();
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public KeyManager[] createKeyManagers()
    {
        KeyInfoManager keyInfoManager = null;

        try
        {
            keyInfoManager = getKeyInfoManager(getKeyMetaDataFileLocation());
            KeyStore ks = loadKeyStore(keyStoreParameters, keyInfoManager);

            logger.debug("Initializing key managers");
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            
            String keyStorePassword = keyInfoManager.getKeyStorePassword();
            kmfactory.init(ks, keyStorePassword != null ? keyStorePassword.toCharArray(): null);
            return kmfactory.getKeyManagers(); 
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to create key manager", e);
        }
        finally
        {
            if(keyInfoManager != null)
            {
                keyInfoManager.clear();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TrustManager[] createTrustManagers()
    {
        KeyInfoManager keyInfoManager = null;

        try
        {
            keyInfoManager = getKeyInfoManager(getKeyMetaDataFileLocation());
            KeyStore ks = loadKeyStore(getKeyStoreParameters(), keyInfoManager);

            logger.debug("Initializing trust managers");
            TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmfactory.init(ks);
            return tmfactory.getTrustManagers();
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to create key manager", e);
        }
        finally
        {
            if(keyInfoManager != null)
            {
                keyInfoManager.clear();
            }
        }
    }
    
    protected String getKeyMetaDataFileLocation()
    {
        return keyStoreParameters.getKeyMetaDataFileLocation();
    }
    
    protected InputStream getKeyStoreStream(String location) throws FileNotFoundException
    {
        if(location == null)
        {
            return null;
        }
        return keyResourceLoader.getKeyStore(location);
    }
    
    protected OutputStream getKeyStoreOutStream() throws FileNotFoundException
    {
        return new FileOutputStream(getKeyStoreParameters().getLocation());
    }
    
    protected KeyInfoManager getKeyInfoManager(String metadataFileLocation) throws FileNotFoundException, IOException
    {
        return new KeyInfoManager(metadataFileLocation, keyResourceLoader);
    }

    protected KeyMap cacheKeys(KeyStore ks, KeyInfoManager keyInfoManager)
    throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException
    {
        KeyMap keys = new KeyMap();

        // load and cache the keys
        for(Entry<String, KeyInformation> keyEntry : keyInfoManager.getKeyInfo().entrySet())
        {
            String keyAlias = keyEntry.getKey();

            KeyInformation keyInfo = keyInfoManager.getKeyInformation(keyAlias);
            String passwordStr = keyInfo != null ? keyInfo.getPassword() : null;

            // Null is an acceptable value (means no key)
            Key key = null;

            // Attempt to get the key
            key = ks.getKey(keyAlias, passwordStr == null ? null : passwordStr.toCharArray());
            if(key != null)
            {
                keys.setKey(keyAlias, key);
            }
            // Key loaded
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Retrieved key from keystore: \n" +
                        "   Location: " + getKeyStoreParameters().getLocation() + "\n" +
                        "   Provider: " + getKeyStoreParameters().getProvider() + "\n" +
                        "   Type:     " + getKeyStoreParameters().getType() + "\n" +
                        "   Alias:    " + keyAlias + "\n" +
                        "   Password?: " + (passwordStr != null));

                Certificate[] certs = ks.getCertificateChain(keyAlias);
                if(certs != null)
                {
                    logger.debug("Certificate chain '" + keyAlias + "':");
                    for(int c = 0; c < certs.length; c++)
                    {
                        if(certs[c] instanceof X509Certificate)
                        {
                            X509Certificate cert = (X509Certificate)certs[c];
                            logger.debug(" Certificate " + (c + 1) + ":");
                            logger.debug("  Subject DN: " + cert.getSubjectDN());
                            logger.debug("  Signature Algorithm: " + cert.getSigAlgName());
                            logger.debug("  Valid from: " + cert.getNotBefore() );
                            logger.debug("  Valid until: " + cert.getNotAfter());
                            logger.debug("  Issuer: " + cert.getIssuerDN());
                        }
                    }
                }
            }
        }

        return keys;
    }

    protected KeyStore initialiseKeyStore(String type, String provider)
    {
        KeyStore ks = null;

        try
        {
            if(provider == null || provider.equals(""))
            {
                ks = KeyStore.getInstance(type);
            }
            else
            {
                ks = KeyStore.getInstance(type, provider);
            }

            ks.load(null, null);

            return ks;
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to intialise key store", e);
        }
    }

    protected KeyStore loadKeyStore(KeyStoreParameters keyStoreParameters, KeyInfoManager keyInfoManager)
    {
        String pwdKeyStore = null;

        try
        {
            KeyStore ks = initialiseKeyStore(keyStoreParameters.getType(), keyStoreParameters.getProvider());

            // Load it up
            InputStream is = getKeyStoreStream(keyStoreParameters.getLocation());
            if (is != null)
            {
                try
                {
                    // Get the keystore password
                    pwdKeyStore = keyInfoManager.getKeyStorePassword();
                    ks.load(is, pwdKeyStore == null ? null : pwdKeyStore.toCharArray());
                }
                finally
                {
                    try {is.close(); } catch (Throwable e) {}
                }
            }
            else
            {
                // this is ok, the keystore will contain no keys.
                logger.warn("Keystore file doesn't exist: " + keyStoreParameters.getLocation());
            }

            return ks;
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to load key store: " + keyStoreParameters.getLocation(), e);
        }
        finally
        {
            pwdKeyStore = null;
        }
    }

    /**
     * Initializes class
     */
    private void safeInit()
    {
        PropertyCheck.mandatory(this, "location", getKeyStoreParameters().getLocation());

        // Make sure we choose the default type, if required
        if(getKeyStoreParameters().getType() == null)
        {
            keyStoreParameters.setType(KeyStore.getDefaultType());
        }

        writeLock.lock();
        try
        {
            keys = loadKeyStore(keyStoreParameters);
            backupKeys = loadKeyStore(backupKeyStoreParameters);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private KeyMap loadKeyStore(KeyStoreParameters keyStoreParameters)
    {
        InputStream is = null;
        KeyInfoManager keyInfoManager = null;
        KeyStore ks = null;

        if(keyStoreParameters == null)
        {
            // empty key map
            return new KeyMap();
        }

        try
        {
            keyInfoManager = getKeyInfoManager(keyStoreParameters.getKeyMetaDataFileLocation());
            ks = loadKeyStore(keyStoreParameters, keyInfoManager);
            // Loaded
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to initialize keystore: \n" +
                    "   Location: " + getKeyStoreParameters().getLocation() + "\n" +
                    "   Provider: " + getKeyStoreParameters().getProvider() + "\n" +
                    "   Type:     " + getKeyStoreParameters().getType(),
                    e);
        }
        finally
        {
            if(keyInfoManager != null)
            {
                keyInfoManager.clearKeyStorePassword();
            }

            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Throwable e)
                {
                    
                }
            }
        }
        
        try
        {
            // cache the keys from the keystore
            KeyMap keys = cacheKeys(ks, keyInfoManager);

            if(logger.isDebugEnabled())
            {
                logger.debug(
                        "Initialized keystore: \n" +
                        "   Location: " + getKeyStoreParameters().getLocation() + "\n" +
                        "   Provider: " + getKeyStoreParameters().getProvider() + "\n" +
                        "   Type:     " + getKeyStoreParameters().getType() + "\n" +
                        keys.numKeys() + " keys found");
            }

            return keys;
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to retrieve keys from keystore: \n" +
                    "   Location: " + getKeyStoreParameters().getLocation() + "\n" +
                    "   Provider: " + getKeyStoreParameters().getProvider() + "\n" +
                    "   Type:     " + getKeyStoreParameters().getType() + "\n",
                    e);
        }
        finally
        {
            // Clear key information
            keyInfoManager.clear();
        }
    }
    
    protected void createKey(String keyAlias)
    {
        KeyInfoManager keyInfoManager = null;

        try
        {
            keyInfoManager = getKeyInfoManager(getKeyMetaDataFileLocation());
            Key key = getSecretKey(keyInfoManager.getKeyInformation(keyAlias));
            encryptionKeysRegistry.registerKey(keyAlias, key);
            keys.setKey(keyAlias, key);

            KeyStore ks = loadKeyStore(getKeyStoreParameters(), keyInfoManager);
            ks.setKeyEntry(keyAlias, key, keyInfoManager.getKeyInformation(keyAlias).getPassword().toCharArray(), null);
            OutputStream keyStoreOutStream = getKeyStoreOutStream();
            ks.store(keyStoreOutStream, keyInfoManager.getKeyStorePassword().toCharArray());
            // Workaround for MNT-15005
            keyStoreOutStream.close();

            logger.info("Created key: " + keyAlias + "\n in key store: \n" +
                    "   Location: " + getKeyStoreParameters().getLocation() + "\n" +
                    "   Provider: " + getKeyStoreParameters().getProvider() + "\n" +
                    "   Type:     " + getKeyStoreParameters().getType());
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to create key: " + keyAlias + "\n in key store: \n" +
                    "   Location: " + getKeyStoreParameters().getLocation() + "\n" +
                    "   Provider: " + getKeyStoreParameters().getProvider() + "\n" +
                    "   Type:     " + getKeyStoreParameters().getType(),
                    e);
        }
        finally
        {
            if(keyInfoManager != null)
            {
                keyInfoManager.clear();
            }
        }
    }
    
    protected void createKeyStore(KeyStoreParameters keyStoreParameters, KeyMap keys)
    {
        KeyInfoManager keyInfoManager = null;

        try
        {
            if(!keyStoreExists(keyStoreParameters.getLocation()))
            {
                keyInfoManager = getKeyInfoManager(keyStoreParameters.getKeyMetaDataFileLocation());
                KeyStore ks = initialiseKeyStore(keyStoreParameters.getType(), keyStoreParameters.getProvider());
    
                String keyStorePassword = keyInfoManager.getKeyStorePassword();
                if(keyStorePassword == null)
                {
                    throw new AlfrescoRuntimeException("Key store password is null for keystore at location "
                            + getKeyStoreParameters().getLocation()
                            + ", key store meta data location" + getKeyMetaDataFileLocation());
                }

                for(String keyAlias : keys.getKeyAliases())
                {
                    KeyInformation keyInfo = keyInfoManager.getKeyInformation(keyAlias);

                    Key key = keys.getKey(keyAlias);
                    if(key == null)
                    {
                        logger.warn("Key with alias " + keyAlias + " is null when creating keystore at location " + keyStoreParameters.getLocation());
                    }
                    else
                    {
                        ks.setKeyEntry(keyAlias, key, keyInfo.getPassword().toCharArray(), null);
                    }
                }

//                try
//                {
//                    throw new Exception("Keystore creation: " + );
//                }
//                catch(Throwable e)
//                {
//                    logger.debug(e.getMessage());
//                    e.printStackTrace();
//                }

                OutputStream keyStoreOutStream = getKeyStoreOutStream();
                ks.store(keyStoreOutStream, keyStorePassword.toCharArray());
                // Workaround for MNT-15005
                keyStoreOutStream.close();
            }
            else
            {
                logger.warn("Can't create key store " + keyStoreParameters.getLocation() + ", already exists.");
            }
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to create keystore: \n" +
                    "   Location: " + keyStoreParameters.getLocation() + "\n" +
                    "   Provider: " + keyStoreParameters.getProvider() + "\n" +
                    "   Type:     " + keyStoreParameters.getType(),
                    e);
        }
        finally
        {
            if(keyInfoManager != null)
            {
                keyInfoManager.clear();
            }
        }
    }

    /*
     * For testing
     */
//    void createBackup()
//    {
//        createKeyStore(backupKeyStoreParameters, backupKeys);
//    }

    private byte[] generateKeyData()
    {
        try
        {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte bytes[] = new byte[DESedeKeySpec.DES_EDE_KEY_LEN];
            random.nextBytes(bytes);
            return bytes;
        }
        catch(Exception e)
        {
            throw new RuntimeException("Unable to generate secret key", e);
        }
    }

    protected Key getSecretKey(KeyInformation keyInformation) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException
    {
        byte[] keyData = keyInformation.getKeyData();

        if(keyData == null)
        {
            if(keyInformation.getKeyAlgorithm().equals("DESede"))
            {
                // no key data provided, generate key data automatically
                keyData = generateKeyData();
            }
            else
            {
                throw new AlfrescoRuntimeException("Unable to generate secret key: key algorithm is not DESede and no keyData provided");
            }
        }

        DESedeKeySpec keySpec = new DESedeKeySpec(keyData);
        SecretKeyFactory kf = SecretKeyFactory.getInstance(keyInformation.getKeyAlgorithm());
        SecretKey secretKey = kf.generateSecret(keySpec);
        return secretKey;
    }
    
    void importPrivateKey(String keyAlias, String keyPassword, InputStream fl, InputStream certstream)
    throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, KeyStoreException
    {
        KeyInfoManager keyInfoManager = null;

        writeLock.lock();
        try
        {
            keyInfoManager = getKeyInfoManager(getKeyMetaDataFileLocation());
            KeyStore ks = loadKeyStore(getKeyStoreParameters(), keyInfoManager);

            // loading Key
            byte[] keyBytes = new byte[fl.available()];
            KeyFactory kf = KeyFactory.getInstance("RSA");
            fl.read(keyBytes, 0, fl.available());
            fl.close();
            PKCS8EncodedKeySpec keysp = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey key = kf.generatePrivate(keysp);
    
            // loading CertificateChain
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
    
            @SuppressWarnings("rawtypes")
            Collection c = cf.generateCertificates(certstream) ;
            Certificate[] certs = new Certificate[c.toArray().length];
    
            certs = (Certificate[])c.toArray(new Certificate[0]);
    
            // storing keystore
            ks.setKeyEntry(keyAlias, key, keyPassword.toCharArray(), certs);

            if(logger.isDebugEnabled())
            {
                logger.debug("Key and certificate stored.");
                logger.debug("Alias:"+ keyAlias);
            }
            OutputStream keyStoreOutStream = getKeyStoreOutStream();
            ks.store(keyStoreOutStream, keyPassword.toCharArray());
            // Workaround for MNT-15005
            keyStoreOutStream.close();
        }
        finally
        {
            if(keyInfoManager != null)
            {
                keyInfoManager.clear();
            }

            writeLock.unlock();
        }
    }
    
    public boolean backupExists()
    {
        return keyStoreExists(getBackupKeyStoreParameters().getLocation());
    }
    
    protected boolean keyStoreExists(String location)
    {
        try
        {
            InputStream is = getKeyStoreStream(location);
            if (is == null)
            {
                return false;
            }
            else
            {
                try { is.close(); } catch (Throwable e) {}
                return true;
            }
        }
        catch(FileNotFoundException e)
        {
            return false;
        }
    }

    /*
     * Validates the keystore keys against the key registry, throwing exceptions if the keys have been unintentionally changed.
     * 
     * For each key to validate:
     * 
     * (i) no main key, no backup key, the key is registered for the main keystore -> error, must re-instate the keystore
     * (ii) no main key, no backup key, the key is not registered -> create the main key store and register the key
     * (iii) main key exists but is not registered -> register the key
     * (iv) main key exists, no backup key, the key is registered -> check that the key has not changed - if it has, throw an exception
     * (v) main key exists, backup key exists, the key is registered -> check in the registry that the backup key has not changed and then re-register main key
     */
    protected void validateKeys(KeyMap keys, KeyMap backupKeys) throws InvalidKeystoreException, MissingKeyException
    {
        if(!validateKeyChanges)
        {
            return;
        }

        writeLock.lock();
        try
        {
            // check for the existence of a key store first
            for(String keyAlias : keysToValidate)
            {
                if(keys.getKey(keyAlias) == null)
                {
                    if(backupKeys.getKey(keyAlias) == null)
                    {
                        if(encryptionKeysRegistry.isKeyRegistered(keyAlias))
                        {
                            // The key is registered and neither key nor backup key exist -> throw
                            // an exception indicating that the key is missing and the keystore should
                            // be re-instated.
                            throw new MissingKeyException(keyAlias, getKeyStoreParameters().getLocation());
                        }
                        else
                        {
                            // Neither the key nor the backup key exist, so create the key
                            createKey(keyAlias);
                        }
                    }
                }
                else
                {
                    if(!encryptionKeysRegistry.isKeyRegistered(keyAlias))
                    {
                        // The key is not registered, so register it
                        encryptionKeysRegistry.registerKey(keyAlias, keys.getKey(keyAlias));
                    }
                    else if(backupKeys.getKey(keyAlias) == null && encryptionKeysRegistry.checkKey(keyAlias, keys.getKey(keyAlias)) == KEY_STATUS.CHANGED)
                    {
                        // A key has been changed, indicating that the keystore has been un-intentionally changed.
                        // Note: this will halt the application bootstrap.
                        throw new InvalidKeystoreException("The key with alias " + keyAlias + " has been changed, re-instate the previous keystore");
                    }
                    else if(backupKeys.getKey(keyAlias) != null && encryptionKeysRegistry.isKeyRegistered(keyAlias))
                    {
                        // Both key and backup key exist and the key is registered.
                        if(encryptionKeysRegistry.checkKey(keyAlias, backupKeys.getKey(keyAlias)) == KEY_STATUS.OK)
                        {
                            // The registered key is the backup key so lets re-register the key in the main key store.
                            // Unregister the existing (now backup) key and re-register the main key.
                            encryptionKeysRegistry.unregisterKey(keyAlias);
                            encryptionKeysRegistry.registerKey(keyAlias, keys.getKey(keyAlias));
                        }
                    }
                }
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    public static class KeyInformation
    {
        protected String alias;
        protected byte[] keyData;
        protected String password;
        protected String keyAlgorithm;

        public KeyInformation(String alias, byte[] keyData, String password, String keyAlgorithm)
        {
            super();
            this.alias = alias;
            this.keyData = keyData;
            this.password = password;
            this.keyAlgorithm = keyAlgorithm;
        }

        public String getAlias()
        {
            return alias;
        }
        
        public byte[] getKeyData()
        {
            return keyData;
        }

        public String getPassword()
        {
            return password;
        }

        public String getKeyAlgorithm()
        {
            return keyAlgorithm;
        }
    }

    /*
     * Caches key meta data information such as password, seed.
     *
     */
    public static class KeyInfoManager
    {
        private KeyResourceLoader keyResourceLoader;
        private String metadataFileLocation;
        private Properties keyProps;
        private String keyStorePassword = null;
        private Map<String, KeyInformation> keyInfo;

        /**
         * For testing.
         * 
         * @param passwords
         */
        KeyInfoManager(Map<String, String> passwords, KeyResourceLoader keyResourceLoader)
        {
            this.keyResourceLoader = keyResourceLoader;
            keyInfo = new HashMap<String, KeyInformation>(2);
            for(Map.Entry<String, String> password : passwords.entrySet())
            {
                keyInfo.put(password.getKey(), new KeyInformation(password.getKey(), null, password.getValue(), null));
            }
        }

        KeyInfoManager(String metadataFileLocation, KeyResourceLoader keyResourceLoader) throws IOException, FileNotFoundException
        {
            this.keyResourceLoader = keyResourceLoader;
            this.metadataFileLocation = metadataFileLocation;
            keyInfo = new HashMap<String, KeyInformation>(2);
            loadKeyMetaData();
        }

        public Map<String, KeyInformation> getKeyInfo()
        {
            // TODO defensively copy
            return keyInfo;
        }

        /**
         * Set the map of key meta data (including passwords to access the keystore).
         * <p/>
         * Where required, <tt>null</tt> values must be inserted into the map to indicate the presence
         * of a key that is not protected by a password.  They entry for {@link #KEY_KEYSTORE_PASSWORD}
         * is required if the keystore is password protected.
         */
        protected void loadKeyMetaData() throws IOException, FileNotFoundException
        {
            keyProps = keyResourceLoader.loadKeyMetaData(metadataFileLocation);
            if(keyProps != null)
            {
                String aliases = keyProps.getProperty("aliases");
                if(aliases == null)
                {
                    throw new AlfrescoRuntimeException("Passwords file must contain an aliases key");
                }
    
                this.keyStorePassword = keyProps.getProperty(KEY_KEYSTORE_PASSWORD);
                
                StringTokenizer st = new StringTokenizer(aliases, ",");
                while(st.hasMoreTokens())
                {
                    String keyAlias = st.nextToken();
                    keyInfo.put(keyAlias, loadKeyInformation(keyAlias));
                }
            }
            else
            {
                // TODO
                //throw new FileNotFoundException("Cannot find key metadata file " + getKeyMetaDataFileLocation());
            }
        }
        
        public void clear()
        {
            this.keyStorePassword = null;
            if(this.keyProps != null)
            {
                this.keyProps.clear();
            }
        }

        public void removeKeyInformation(String keyAlias)
        {
            this.keyProps.remove(keyAlias);
        }

        protected KeyInformation loadKeyInformation(String keyAlias)
        {
            String keyPassword = keyProps.getProperty(keyAlias + ".password");
            String keyData = keyProps.getProperty(keyAlias + ".keyData");
            String keyAlgorithm = keyProps.getProperty(keyAlias + ".algorithm");

            byte[] keyDataBytes = null;
            if(keyData != null && !keyData.equals(""))
            {
                keyDataBytes = Base64.decodeBase64(keyData);
            }
            KeyInformation keyInfo = new KeyInformation(keyAlias, keyDataBytes, keyPassword, keyAlgorithm);
            return keyInfo;
        }

        public String getKeyStorePassword()
        {
            return keyStorePassword;
        }
        
        public void clearKeyStorePassword()
        {
            this.keyStorePassword = null;
        }

        public KeyInformation getKeyInformation(String keyAlias)
        {
            return keyInfo.get(keyAlias);
        }
    }
}
