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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * 
 * @since 4.0
 *
 */
public class KeyStoreTests
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

	private TransactionService transactionService;
	private KeyStoreChecker keyStoreChecker;
	private EncryptionKeysRegistry encryptionKeysRegistry;
	private UserTransaction txn = null;
	private KeyResourceLoader keyResourceLoader;
	private List<String> toDelete;
	private DefaultEncryptor backupEncryptor;
	
    @Before
	public void setup() throws SystemException, NotSupportedException
	{
    	transactionService = (TransactionService)ctx.getBean("transactionService");
    	keyStoreChecker = (KeyStoreChecker)ctx.getBean("keyStoreChecker");
    	encryptionKeysRegistry = (EncryptionKeysRegistry)ctx.getBean("encryptionKeysRegistry");
    	keyResourceLoader = (KeyResourceLoader)ctx.getBean("springKeyResourceLoader");
        backupEncryptor = (DefaultEncryptor)ctx.getBean("backupEncryptor");

    	toDelete = new ArrayList<String>(10);

        AuthenticationUtil.setRunAsUserSystem();
		UserTransaction txn = transactionService.getUserTransaction();
		txn.begin();
	}

    @After
	public void teardown() throws IllegalStateException, SecurityException, SystemException
	{
		if(txn != null)
		{
			txn.rollback();
		}

		for(String guid : toDelete)
		{
			File file = new File(guid);
			if(file.exists())
			{
				file.delete();
			}
		}
	}

    public String generateEncodedKey()
    {
		try
		{
	    	return Base64.encodeBase64String(generateKeyData());
		}
		catch(Throwable e)
		{
			fail("Unexpected exception: " + e.getMessage());
			return null;
		}
    }

	public byte[] generateKeyData() throws NoSuchAlgorithmException
	{
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		random.setSeed(System.currentTimeMillis());
		byte bytes[] = new byte[DESedeKeySpec.DES_EDE_KEY_LEN];
		random.nextBytes(bytes);
		return bytes;
	}

	protected String generateKeystoreName()
	{
		String guid = GUID.generate();	
		toDelete.add(guid);
		return guid;
	}

	protected Key generateSecretKey(String keyAlgorithm)
	{
		try
		{
			DESedeKeySpec keySpec = new DESedeKeySpec(generateKeyData());
			SecretKeyFactory kf = SecretKeyFactory.getInstance(keyAlgorithm);
	    	SecretKey secretKey = kf.generateSecret(keySpec);
	    	return secretKey;
		}
		catch(Throwable e)
		{
			fail("Unexpected exception: " + e.getMessage());
			return null;
		}
	}

	protected TestAlfrescoKeyStore getKeyStore(String name, String type, final Map<String, String> passwords, final Map<String, String> encodedKeyData,
			String keyStoreLocation, String backupKeyStoreLocation)
	{
		KeyResourceLoader testKeyResourceLoader = new KeyResourceLoader()
		{
			@Override
			public InputStream getKeyStore(String keyStoreLocation)
					throws FileNotFoundException
			{
				return keyResourceLoader.getKeyStore(keyStoreLocation);
			}

			@Override
			public Properties loadKeyMetaData(String keyMetaDataFileLocation)
					throws IOException, FileNotFoundException
			{
				Properties p = new Properties();
				p.put("keystore.password", "password");
				StringBuilder aliases = new StringBuilder();
				for(String keyAlias : passwords.keySet())
				{
					p.put(keyAlias + ".password", passwords.get(keyAlias));
					if(encodedKeyData != null && encodedKeyData.get(keyAlias) != null)
					{
						p.put(keyAlias + ".keyData", encodedKeyData.get(keyAlias));
					}
					p.put(keyAlias + ".algorithm", "DESede");
					aliases.append(keyAlias);
					aliases.append(",");
				}
				if(aliases.length() > 0)
				{
					// remove trailing comma
					aliases.delete(aliases.length() - 1, aliases.length());
				}
				p.put("aliases", aliases.toString());
				return p;
			}
		};
		
		KeyStoreParameters keyStoreParameters = new KeyStoreParameters(name, type, null, "", keyStoreLocation);
		KeyStoreParameters backupKeyStoreParameters = new KeyStoreParameters(name + ".backup", type, null, "", backupKeyStoreLocation);
		TestAlfrescoKeyStore keyStore = new TestAlfrescoKeyStore();
		keyStore.setKeyStoreParameters(keyStoreParameters);
		keyStore.setBackupKeyStoreParameters(backupKeyStoreParameters);
		keyStore.setKeyResourceLoader(testKeyResourceLoader);
		keyStore.setValidateKeyChanges(true);
		keyStore.setEncryptionKeysRegistry(encryptionKeysRegistry);
		return keyStore;
	}
	
	@Test
	public void test1()
	{
		// missing keystore, missing backup keystore, no registered keys -> create key store with metadata key and register key

		TestAlfrescoKeyStore missingMainKeyStore = getKeyStore("main", "JCEKS", Collections.singletonMap(KeyProvider.ALIAS_METADATA, "metadata"),
				Collections.singletonMap(KeyProvider.ALIAS_METADATA, generateEncodedKey()), generateKeystoreName(), generateKeystoreName());

		encryptionKeysRegistry.unregisterKey(KeyProvider.ALIAS_METADATA);
		keyStoreChecker.setMainKeyStore(missingMainKeyStore);

		try
		{
			keyStoreChecker.validateKeyStores();
		}
		catch(InvalidKeystoreException e)
		{
			fail("Unexpected exception: " + e.getMessage());
		}
		catch (MissingKeyException e)
		{
			fail("Unexpected exception : " + e.getMessage());
		}

		assertTrue("", encryptionKeysRegistry.getRegisteredKeys(missingMainKeyStore.getKeyAliases()).contains(KeyProvider.ALIAS_METADATA));
		assertTrue("", missingMainKeyStore.exists());
		assertTrue("", missingMainKeyStore.getKey(KeyProvider.ALIAS_METADATA) != null);
	}
	
	@Test
	public void test2()
	{
		// missing main keystore, missing backup keystore, metadata registered key -> error, re-instate the keystore
		TestAlfrescoKeyStore missingMainKeyStore = getKeyStore("main", "JCEKS", Collections.singletonMap(KeyProvider.ALIAS_METADATA, "metadata"),
				null, generateKeystoreName(), generateKeystoreName());
		
		assertTrue("", encryptionKeysRegistry.isKeyRegistered("metadata"));

		keyStoreChecker.setMainKeyStore(missingMainKeyStore);

		try
		{
			keyStoreChecker.validateKeyStores();
			fail("Should have caught missing main keystore");
		}
		catch(InvalidKeystoreException e)
		{
			fail("Unexpected exception : " + e.getMessage());
		}
		catch (MissingKeyException e)
		{
			// ok, expected
		}
	}

	@Test
	public void test3()
	{
		// main keystore exists, no registered metadata key -> register key
		
		// create main keystore
		TestAlfrescoKeyStore mainKeyStore = getKeyStore("main", "JCEKS", Collections.singletonMap(KeyProvider.ALIAS_METADATA, "metadata"),
				null, generateKeystoreName(), generateKeystoreName());
		createAndPopulateKeyStore(mainKeyStore);

		// de-register metadata key
		encryptionKeysRegistry.unregisterKey(KeyProvider.ALIAS_METADATA);

		// check keys
		keyStoreChecker.setMainKeyStore(mainKeyStore);

		try
		{
			keyStoreChecker.validateKeyStores();
		}
		catch(InvalidKeystoreException e)
		{
			fail("Unexpected exception: " + e.getMessage());
		}
		catch (MissingKeyException e)
		{
			fail("Unexpected exception : " + e.getMessage());
		}
		
		assertTrue("", encryptionKeysRegistry.isKeyRegistered(KeyProvider.ALIAS_METADATA));
	}

	@Test
	public void test4()
	{
		// create keystore, change key -> check for exception InvalidKey

		// Firstly, create main keystore to register a well-known key

		encryptionKeysRegistry.unregisterKey(KeyProvider.ALIAS_METADATA);

		TestAlfrescoKeyStore keyStore = getKeyStore("main", "JCEKS", Collections.singletonMap(KeyProvider.ALIAS_METADATA, "metadata"),
				null, generateKeystoreName(), generateKeystoreName());
		createAndPopulateKeyStore(keyStore);

		keyStoreChecker.setMainKeyStore(keyStore);

		// check keys
		try
		{
			// should register the metadata key
			keyStoreChecker.validateKeyStores();
		}
		catch(InvalidKeystoreException e)
		{
			fail("Unexpected exception: " + e.getMessage());
		}
		catch (MissingKeyException e)
		{
			fail("Unexpected exception : " + e.getMessage());
		}
		
		// check that the metadata key has been registered
		assertTrue("", encryptionKeysRegistry.isKeyRegistered("metadata"));

		// a changed main keystore with a different metadata key
//		TestAlfrescoKeyStore changedMainKeyStore = getKeyStore("main", "JCEKS", Collections.singletonMap(KeyProvider.ALIAS_METADATA, "metadata"),
//				null, generateKeystoreName(), generateKeystoreName());
//		createAndPopulateKeyStore(changedMainKeyStore);
		keyStore.changeKey(KeyProvider.ALIAS_METADATA, generateSecretKey("DESede"));
		
//		keyStoreChecker.setMainKeyStore(changedMainKeyStore);
		try
		{
			keyStoreChecker.validateKeyStores();
			fail("Expected key store checker to detect changed metadata key");
		}
		catch(InvalidKeystoreException e)
		{
			// ok, expected
		}
		catch (MissingKeyException e)
		{
			fail("Unexpected exception : " + e.getMessage());
		}
	}

	@Test
	public void test5()
	{
		// create main keystore, backup main keystore, change main keystore -> check that backup keystore key is ok, re-register new main key
		// check that the new main keystore key has been re-registered

		// Firstly, re-install main keystore to register a well-known key
		encryptionKeysRegistry.unregisterKey(KeyProvider.ALIAS_METADATA);

		TestAlfrescoKeyStore keyStore = getKeyStore("main", "JCEKS", Collections.singletonMap(KeyProvider.ALIAS_METADATA, "metadata"),
				null, generateKeystoreName(), generateKeystoreName());
		createAndPopulateKeyStore(keyStore);

		try
		{
			keyStoreChecker.setMainKeyStore(keyStore);
			keyStoreChecker.validateKeyStores();
		}
		catch(InvalidKeystoreException e)
		{
			fail("Unexpected exception: " + e.getMessage());
		}
		catch (MissingKeyException e)
		{
			fail("Unexpected exception : " + e.getMessage());
		}

		keyStore.backup();
		
		// check that the metadata key has been registered
		assertTrue("", encryptionKeysRegistry.isKeyRegistered("metadata"));

		// change the metadata key
		keyStore.changeKey(KeyProvider.ALIAS_METADATA, generateSecretKey("DESede"));

		try
		{
			// should detect changed metadata key and re-register it
			keyStoreChecker.validateKeyStores();
		}
		catch(InvalidKeystoreException e)
		{
			fail("Unexpected exception: " + e.getMessage());
		}
		catch (MissingKeyException e)
		{
			fail("Unexpected exception : " + e.getMessage());
		}

		// check that the new metadata key has been successfully re-registered by encrypting and decrypting some content with it
		assertTrue("", EncryptionKeysRegistry.KEY_STATUS.OK == encryptionKeysRegistry.checkKey(KeyProvider.ALIAS_METADATA,
				keyStore.getKey(KeyProvider.ALIAS_METADATA)));
	}
	
	private void createAndPopulateKeyStore(TestAlfrescoKeyStore keyStore)
	{
		KeyMap keyMap = new KeyMap();
		keyMap.setKey(KeyProvider.ALIAS_METADATA, generateSecretKey("DESede"));
		keyStore.create(keyMap, null);	
	}
	
	private static class TestAlfrescoKeyStore extends AlfrescoKeyStoreImpl
	{
		public void create(KeyMap keys, KeyMap backupKeys)
		{
			this.keys = (keys != null ? keys : new KeyMap());
			this.backupKeys = (backupKeys != null ? backupKeys : new KeyMap());
			super.create();
		}
		
		void changeKey(String keyAlias, Key key)
		{
			keys.setKey(KeyProvider.ALIAS_METADATA, key);
		}
	}
}
