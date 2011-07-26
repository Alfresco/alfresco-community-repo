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
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * The EncryptionChecker checks the state of the repository's encryption system.
 * In particular it checks:
 * <ul>
 *    <li> that the encryption keys have not been changed. If so, the bootstrap will be halted.
 * </ul>
 * 
 * @since 4.0
 *
 */
public class EncryptionChecker extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.	getLog(EncryptionChecker.class);
    public static String TOP_LEVEL_KEY = "keyCheck";
    private static enum KEY_STATUS
    {
    	OK, CHANGED, MISSING;
    };

    private TransactionService transactionService;
    private AttributeService attributeService;
    private Encryptor encryptor;
    private List<String> keyAliases;
    
	public void setTransactionService(TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}

	public void setAttributeService(AttributeService attributeService)
	{
		this.attributeService = attributeService;
	}

	public void setKeyAliases(List<String> keyAliases)
	{
		this.keyAliases = keyAliases;
	}

	public void setEncryptor(Encryptor encryptor)
	{
		this.encryptor = encryptor;
	}

	private void removeKey(String keyAlias)
	{
		attributeService.removeAttributes(TOP_LEVEL_KEY);
		logger.info("Removed registered key " + keyAlias);
	}

	private KEY_STATUS checkKey(String keyAlias)
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
			// register the key by creating an attribute that stores a guid and its encrypted value
			String guid = GUID.generate();
			Serializable encrypted = encryptor.sealObject(keyAlias, null, guid);
			KeyCheck keyCheck = new KeyCheck(guid, encrypted);
			attributeService.createAttribute(keyCheck, TOP_LEVEL_KEY, keyAlias);
			logger.info("Registered key " + keyAlias);
			return KEY_STATUS.MISSING;
		}		
	}

	@Override
	protected void onBootstrap(ApplicationEvent event)
	{
    	RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
    	final RetryingTransactionCallback<Void> checkKeysCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
        		for(String keyAlias : keyAliases)
        		{
        			KEY_STATUS keyStatus = checkKey(keyAlias);
        			if(keyStatus == KEY_STATUS.CHANGED)
        			{
        				// Note: this will halt the application bootstrap.
        				throw new AlfrescoRuntimeException("The key with alias " + keyAlias + " has been changed, re-instate the previous keystore");
        			}
        		}

        		return null;
            }
        };
        retryingTransactionHelper.doInTransaction(checkKeysCallback, false);
	}

	@Override
	protected void onShutdown(ApplicationEvent event)
	{
		
	}
	
	public void removeRegisteredKeys()
	{
    	RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
    	final RetryingTransactionCallback<Void> removeKeysCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
        		for(String keyAlias : keyAliases)
        		{
        			removeKey(keyAlias);
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
