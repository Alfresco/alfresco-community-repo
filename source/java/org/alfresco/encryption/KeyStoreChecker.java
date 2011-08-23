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

import java.io.FileOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.attributes.AttributeService.AttributeQueryCallback;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Validates an Alfresco keystore by ensuring that it's registered keys have not changed.
 * 
 * @since 4.0
 *
 */
public class KeyStoreChecker
{
    public static String TOP_LEVEL_KEY = "keyCheck";
    private static final Log logger = LogFactory.getLog(KeyStoreChecker.class);
    private static enum KEY_STATUS
    {
    	OK, CHANGED, MISSING;
    };

    private AttributeService attributeService;
    private Encryptor encryptor;
    private TransactionService transactionService;

    public KeyStoreChecker()
    {
    }

    public void setAttributeService(AttributeService attributeService)
	{
		this.attributeService = attributeService;
	}

	public void setEncryptor(Encryptor encryptor)
	{
		this.encryptor = encryptor;
	}

	public void setTransactionService(TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}

	public KeyStoreChecker(AttributeService attributeService, Encryptor encryptor, TransactionService transactionService)
    {
    	this.attributeService = attributeService;
    	this.transactionService = transactionService;
    	this.encryptor = encryptor;
    }

	private KEY_STATUS checkKey(AlfrescoKeyStore keyStore, String keyAlias)
	{
		if(attributeService.exists(TOP_LEVEL_KEY, keyStore.getLocation(), keyAlias))
		{
			try
			{
				// check that the key has not changed by decrypting the encrypted guid attribute
				// comparing against the guid
				KeyCheck keyCheck = (KeyCheck)attributeService.getAttribute(TOP_LEVEL_KEY, keyStore.getLocation(), keyAlias);
				Serializable storedGUID = encryptor.unsealObject(keyAlias, keyCheck.getEncrypted());
				return EqualsHelper.nullSafeEquals(storedGUID, keyCheck.getGuid()) ? KEY_STATUS.OK : KEY_STATUS.CHANGED;
			}
			catch(InvalidKeyException e)
			{
				// key exception indicates that the key has changed - it can't decrypt the
				// previously-encrypted data
				return KEY_STATUS.CHANGED;
			}
		}
		else
		{
			return KEY_STATUS.MISSING;
		}		
	}
	
	private void registerKey(AlfrescoKeyStore keyStore, String keyAlias)
	{
		// register the key by creating an attribute that stores a guid and its encrypted value
		String guid = GUID.generate();
		Serializable encrypted = encryptor.sealObject(keyAlias, null, guid);
		KeyCheck keyCheck = new KeyCheck(guid, encrypted);
		attributeService.createAttribute(keyCheck, TOP_LEVEL_KEY, keyStore.getLocation(), keyAlias);
		logger.info("Registered key " + keyAlias);
	}

	protected KeysReport getKeysReport(AlfrescoKeyStore keyStore)
	{
		final List<String> registeredKeys = new ArrayList<String>();

		if(attributeService.exists(TOP_LEVEL_KEY, keyStore.getLocation()))
		{
			attributeService.getAttributes(new AttributeQueryCallback()
			{
				public boolean handleAttribute(Long id, Serializable value,
						Serializable[] keys)
				{
					registeredKeys.add((String)value);
					return true;
				}
				
			},
			TOP_LEVEL_KEY, keyStore.getLocation());
		}

		List<String> keyAliasesChanged = new ArrayList<String>();
		List<String> keyAliasesUnchanged = new ArrayList<String>();

		for(String keyAlias : registeredKeys)
		{
			KEY_STATUS keyStatus = checkKey(keyStore, keyAlias);
			if(keyStatus == KEY_STATUS.CHANGED)
			{
				keyAliasesChanged.add(keyAlias);
			}
			else
			{
				keyAliasesUnchanged.add(keyAlias);
			}
		}

		return new KeysReport(keyAliasesChanged, keyAliasesUnchanged);
	}
		
	public void checkKeyStore(final AlfrescoKeyStore keyStore)
	{
    	RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
    	final RetryingTransactionCallback<Void> checkKeysCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
				KeysReport keysReport = getKeysReport(keyStore);

		    	// Check for the existence of a key store first
		    	if(keyStore.exists())
		    	{
		    		// The keystore exists - check whether any keys have been changed
		    		if(keysReport.getKeysChanged().size() > 0)
		    		{
						// Note: this will halt the application bootstrap.
						throw new AlfrescoRuntimeException("The keys with aliases " + keysReport.getKeysChanged() + " have been changed, re-instate the previous keystore");
		    		}
		    	}
		    	else
		    	{
		    		// keystore not found, check whether any keys have been registered
		    		if(keysReport.getKeysChanged().size() + keysReport.getKeysUnchanged().size() > 0)
		    		{
						// Note: this will halt the application bootstrap.
						throw new AlfrescoRuntimeException("Keys have already been registered, re-instate the previous keystore");
		    		}
		    		
		    		// no keys found, create a new keystore
		    		// TODO
		    		if(logger.isDebugEnabled())
		    		{
		    			logger.debug("Keystore not found, creating...");
		    		}
		    		createKeyStore(keyStore);
		    	}

		    	return null;
            }
        };
        retryingTransactionHelper.doInTransaction(checkKeysCallback, false);
	}
	
	protected void createKeyStore(AlfrescoKeyStore keyStore)
	{
		new CreatingKeyStore(keyStore).create();
	}

	public static class KeysReport
	{
		private List<String> keysChanged;
		private List<String> keysUnchanged;
		
		public KeysReport(List<String> keysChanged, List<String> keysUnchanged)
		{
			super();
			this.keysChanged = keysChanged;
			this.keysUnchanged = keysUnchanged;
		}

		public List<String> getKeysChanged()
		{
			return keysChanged;
		}

		public List<String> getKeysUnchanged()
		{
			return keysUnchanged;
		}
	}
	
	/**
	 * A KeyCheck object stores a well-known guid and it's encrypted value.
	 * 
	 * @since 4.0
	 *
	 */
	private static class KeyCheck implements Serializable
	{
		private static final long serialVersionUID = 4514315444977162903L;

		private String guid;
		private Serializable encrypted;
		
		public KeyCheck(String guid, Serializable encrypted)
		{
			super();
			this.guid = guid;
			this.encrypted = encrypted;
		}

		public String getGuid()
		{
			return guid;
		}

		public Serializable getEncrypted()
		{
			return encrypted;
		}
		
		public boolean equals(Object other)
		{
			if(this == other)
			{
				return true;
			}

			if(!(other instanceof KeyCheck))
			{
				return false;
			}
			KeyCheck keyCheck = (KeyCheck)other;
			return EqualsHelper.nullSafeEquals(keyCheck.getGuid(), getGuid()) &&
					EqualsHelper.nullSafeEquals(keyCheck.getEncrypted(), getEncrypted());
		}
	}

	public void removeRegisteredKeys(final AlfrescoKeyStore keyStore)
	{
    	RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
    	final RetryingTransactionCallback<Void> removeKeysCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
        		attributeService.removeAttributes(TOP_LEVEL_KEY, keyStore.getLocation());

        		return null;
            }
        };
        retryingTransactionHelper.doInTransaction(removeKeysCallback, false);
	}
	
	private class CreatingKeyStore extends CachingKeyStore
	{
		CreatingKeyStore(AlfrescoKeyStore keyStore)
		{
			super(keyStore.getkeyStoreParameters(), keyStore.getKeyResourceLoader());
		}

		public void create()
		{
			KeyInfoManager keyInfoManager = null;

			try
			{
		        keyInfoManager = getKeyInfoManager();
		
		        ks.load(null, null);
		        String keyStorePassword = keyInfoManager.getKeyStorePassword();
		        if(keyStorePassword == null)
		        {
		        	throw new AlfrescoRuntimeException("Key store password is null for keystore at location " + getLocation()
		        			+ ", key store meta data location" + getKeyMetaDataFileLocation());
		        }
		
				// Add keys from the passwords file to the keystore
				for(Map.Entry<String, CachingKeyStore.KeyInformation> keyEntry : keyInfoManager.getKeyInfo().entrySet())
				{
					KeyInformation keyInfo = keyInfoManager.getKeyInformation(keyEntry.getKey());
			        String keyPassword = keyInfo.getPassword();
			        if(keyPassword == null)
			        {
			        	throw new AlfrescoRuntimeException("No password found for encryption key " + keyEntry.getKey());
			        }
		        	Key key = generateSecretKey(keyEntry.getValue());
		        	ks.setKeyEntry(keyInfo.getAlias(), key, keyInfo.getPassword().toCharArray(), null);
				}
		
		        ks.store(new FileOutputStream(getLocation()), keyStorePassword.toCharArray());

		        // Register the key store keys
				for(Map.Entry<String, CachingKeyStore.KeyInformation> keyEntry : keyInfoManager.getKeyInfo().entrySet())
				{
					registerKey(this, keyEntry.getKey());
				}
			}
			catch(Throwable e)
			{
	            throw new AlfrescoRuntimeException(
	                    "Failed to create keystore: \n" +
	                    "   Location: " + getLocation() + "\n" +
	                    "   Provider: " + getProvider() + "\n" +
	                    "   Type:     " + getType(),
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
	}
}
