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

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
 * Registered Encryption Keys are stored in the AttributeService directly under a top level key defined by
 * TOP_LEVEL_KEY (which means that all key aliases must be unique across however many keystores are being used).
 * 
 * @since 4.0
 *
 */
// TODO caching? This will probably not be used extensively.
public class EncryptionKeysRegistryImpl implements EncryptionKeysRegistry
{
    public static String TOP_LEVEL_KEY = "keyCheck";
    private static final Log logger = LogFactory.getLog(EncryptionKeysRegistryImpl.class);

    private TransactionService transactionService;
    private AttributeService attributeService;
    private String cipherAlgorithm;
    private String cipherProvider;

	public void setAttributeService(AttributeService attributeService)
	{
		this.attributeService = attributeService;
	}

	public void setCipherAlgorithm(String cipherAlgorithm)
	{
		this.cipherAlgorithm = cipherAlgorithm;
	}

	public void setCipherProvider(String cipherProvider)
	{
		this.cipherProvider = cipherProvider;
	}

	public void setTransactionService(TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}

	protected Encryptor getEncryptor(final KeyMap keys)
	{
		DefaultEncryptor encryptor = new DefaultEncryptor();
		encryptor.setCipherAlgorithm(cipherAlgorithm);
		encryptor.setCipherProvider(cipherProvider);
		encryptor.setKeyProvider(new KeyProvider()
		{
			@Override
			public Key getKey(String keyAlias)
			{
				return keys.getCachedKey(keyAlias).getKey();
			}
		});
		return encryptor;
	}

	public void init()
	{
	}

	public void registerKey(String keyAlias, Key key)
	{
		if(isKeyRegistered(keyAlias))
		{
			throw new IllegalArgumentException("Key " + keyAlias + " is already registered");
		}

		// register the key by creating an attribute that stores a guid and its encrypted value
		String guid = GUID.generate();

		KeyMap keys = new KeyMap();
		keys.setKey(keyAlias, key);
		Encryptor encryptor = getEncryptor(keys);
		Serializable encrypted = encryptor.sealObject(keyAlias, null, guid);
		KeyCheck keyCheck = new KeyCheck(guid, encrypted);
		attributeService.createAttribute(keyCheck, TOP_LEVEL_KEY, keyAlias);
		logger.info("Registered key " + keyAlias);
	}
	
	public void unregisterKey(String keyAlias)
	{
		attributeService.removeAttribute(TOP_LEVEL_KEY, keyAlias);
	}
	
	public boolean isKeyRegistered(String keyAlias)
	{
		return (attributeService.getAttribute(TOP_LEVEL_KEY, keyAlias) != null);
	}
	
	public List<String> getRegisteredKeys(final Set<String> keyStoreKeys)
	{
		final List<String> registeredKeys = new ArrayList<String>();

		attributeService.getAttributes(new AttributeQueryCallback()
		{
			public boolean handleAttribute(Long id, Serializable value,
					Serializable[] keys)
			{
				if(value instanceof KeyCheck)
				{
					// Add as a registered key if the keystore contains the key
					String keyAlias = (String)keys[1];
					if(keyStoreKeys.contains(keyAlias))
					{
						registeredKeys.add(keyAlias);
					}
				}
				else
				{
					logger.warn("Unexpected value class in keys registry: " + value.getClass());
				}
				return true;
			}

		},
		TOP_LEVEL_KEY);

		return registeredKeys;
	}

	public KEY_STATUS checkKey(String keyAlias, Key key)
	{
		if(attributeService.exists(TOP_LEVEL_KEY, keyAlias))
		{
			try
			{
				// check that the key has not changed by decrypting the encrypted guid attribute
				// comparing against the guid
				KeyCheck keyCheck = (KeyCheck)attributeService.getAttribute(TOP_LEVEL_KEY, keyAlias);
				
				KeyMap keys = new KeyMap();
				keys.setKey(keyAlias, key);
				Encryptor encryptor = getEncryptor(keys);
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
	
	// note that this removes _all_ keys in the keystore. Use with care.
	public void removeRegisteredKeys(final Set<String> keys)
	{
    	RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
    	final RetryingTransactionCallback<Void> removeKeysCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
            	for(String keyAlias : keys)
            	{
            		attributeService.removeAttribute(TOP_LEVEL_KEY, keyAlias);
            	}

        		return null;
            }
        };
        retryingTransactionHelper.doInTransaction(removeKeysCallback, false);
	}
	
	/**
	 * A KeyCheck object stores a well-known guid and it's encrypted value.
	 * 
	 * @since 4.0
	 *
	 */
	public static class KeyCheck implements Serializable
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

}
