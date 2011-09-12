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
import org.alfresco.util.Pair;
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
// TODO instead of persisting the Pair when registering a key, create two attributes per key (one for the
// guid and one for the encrypted value of the guid). This means a custom class does not need to be bound to
// the attribute service.
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
		Pair<String, Serializable> keyCheck = new Pair<String, Serializable>(guid, encrypted);
		attributeService.createAttribute(keyCheck, TOP_LEVEL_KEY, keyAlias);
		logger.info("Registered key " + keyAlias);
	}
	
	public void unregisterKey(String keyAlias)
	{
		attributeService.removeAttribute(TOP_LEVEL_KEY, keyAlias);
	}
	
	public boolean isKeyRegistered(String keyAlias)
	{
		try
		{
			return (attributeService.getAttribute(TOP_LEVEL_KEY, keyAlias) != null);
		}
		catch(Throwable e)
		{
			// there is an issue getting the attribute. Remove it.
			attributeService.removeAttribute(TOP_LEVEL_KEY, keyAlias);
			return (attributeService.getAttribute(TOP_LEVEL_KEY, keyAlias) != null);
		}
	}
	
	public List<String> getRegisteredKeys(final Set<String> keyStoreKeys)
	{
		final List<String> registeredKeys = new ArrayList<String>();

		attributeService.getAttributes(new AttributeQueryCallback()
		{
			public boolean handleAttribute(Long id, Serializable value,
					Serializable[] keys)
			{
				// Add as a registered key if the keystore contains the key
				String keyAlias = (String)keys[1];
				if(keyStoreKeys.contains(keyAlias))
				{
					registeredKeys.add(keyAlias);
				}
				return true;
			}

		},
		TOP_LEVEL_KEY);

		return registeredKeys;
	}

	@SuppressWarnings("unchecked")
	public KEY_STATUS checkKey(String keyAlias, Key key)
	{
		Pair<String, Serializable> keyCheck = null;

		if(attributeService.exists(TOP_LEVEL_KEY, keyAlias))
		{
			try
			{
				// check that the key has not changed by decrypting the encrypted guid attribute
				// comparing against the guid
				try
				{
					keyCheck = (Pair<String, Serializable>)attributeService.getAttribute(TOP_LEVEL_KEY, keyAlias);
				}
				catch(Throwable e)
				{
					// there is an issue getting the attribute. Remove it.
					attributeService.removeAttribute(TOP_LEVEL_KEY, keyAlias);
					return KEY_STATUS.MISSING;
				}
				
				if(keyCheck == null)
				{
					return KEY_STATUS.MISSING;
				}

				KeyMap keys = new KeyMap();
				keys.setKey(keyAlias, key);
				Encryptor encryptor = getEncryptor(keys);
				Serializable storedGUID = encryptor.unsealObject(keyAlias, keyCheck.getSecond());
				return EqualsHelper.nullSafeEquals(storedGUID, keyCheck.getFirst()) ? KEY_STATUS.OK : KEY_STATUS.CHANGED;
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
}
