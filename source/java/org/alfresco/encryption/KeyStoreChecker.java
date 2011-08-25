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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Validates an Alfresco keystore by ensuring that it's registered keys have not changed.
 * 
 * The keys are registered using the AttributeService and are stored in one level under TOP_LEVEL_KEY
 * (so key aliases must be unique across however many keystores are being used).
 * 
 * @since 4.0
 *
 */
public class KeyStoreChecker
{
    private static final Log logger = LogFactory.getLog(KeyStoreChecker.class);

    private TransactionService transactionService;
    private EncryptionKeysRegistryImpl encryptionKeysRegistry;

    public KeyStoreChecker()
    {
    }
    
    public void setEncryptionKeysRegistry(EncryptionKeysRegistryImpl encryptionKeysRegistry)
	{
		this.encryptionKeysRegistry = encryptionKeysRegistry;
	}

	public void setTransactionService(TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}

	public KeyStoreChecker(EncryptionKeysRegistryImpl encryptionKeysRegistry)
    {
    	this.encryptionKeysRegistry = encryptionKeysRegistry;
    }
	
	protected void createKeyStore(AlfrescoKeyStore keyStore)
	{
		keyStore.create();

        // Register the key store keys
		for(String keyAlias : keyStore.getKeyAliases())
		{
			encryptionKeysRegistry.registerKey(keyAlias);
		}
	}

	public void checkKeyStore(final AlfrescoKeyStore keyStore)
	{
		// TODO check that, if keystore exists, keys are registered
    	RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
    	final RetryingTransactionCallback<Void> checkKeysCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
				KeysReport keysReport = encryptionKeysRegistry.getKeysReport();

		    	// Check for the existence of a key store first
		    	if(keyStore.exists())
		    	{
		    		// The keystore exists - check whether any keys have been changed
	            	// find out which registered keys have changed
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
}
