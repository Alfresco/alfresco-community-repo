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
import java.util.ArrayList;
import java.util.List;

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
    private Encryptor encryptor;

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

	public void registerKey(String keyAlias)
	{
		// register the key by creating an attribute that stores a guid and its encrypted value
		String guid = GUID.generate();
		Serializable encrypted = encryptor.sealObject(keyAlias, null, guid);
		KeyCheck keyCheck = new KeyCheck(guid, encrypted);
		attributeService.createAttribute(keyCheck, TOP_LEVEL_KEY, keyAlias);
		logger.info("Registered key " + keyAlias);
	}
	
	public KeysReport getKeysReport()
	{
		final List<String> registeredKeys = new ArrayList<String>();

		if(attributeService.exists(TOP_LEVEL_KEY))
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
			TOP_LEVEL_KEY);
		}

		List<String> keyAliasesChanged = new ArrayList<String>();
		List<String> keyAliasesUnchanged = new ArrayList<String>();

		for(String keyAlias : registeredKeys)
		{
			KEY_STATUS keyStatus = checkKey(keyAlias);
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
	
	public KEY_STATUS checkKey(String keyAlias)
	{
		if(attributeService.exists(TOP_LEVEL_KEY, keyAlias))
		{
			try
			{
				// check that the key has not changed by decrypting the encrypted guid attribute
				// comparing against the guid
				KeyCheck keyCheck = (KeyCheck)attributeService.getAttribute(TOP_LEVEL_KEY, keyAlias);
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
	public void removeRegisteredKeys(final AlfrescoKeyStore keyStore)
	{
    	RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
    	final RetryingTransactionCallback<Void> removeKeysCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
            	for(String keyAlias : keyStore.getKeyAliases())
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
}
