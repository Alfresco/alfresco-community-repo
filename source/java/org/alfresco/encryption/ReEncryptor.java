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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.crypto.SealedObject;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodePropertyEntity;
import org.alfresco.repo.domain.node.NodePropertyKey;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.surf.util.I18NUtil;

// TODO use Batch code to run in parallel
// TODO lock so that only one encryptor can run at a time
/**
 * Re-encrypts encryptable repository properties using a new set of encryption keys.
 * Decrypts the repository properties using the default encryptor, falling back to
 * a backup decryptor (using the old encryption keys) if necessary, and then re-encrypts
 * the properties.
 * 
 * The system can stay running during this operation.
 * 
 * @since 4.0
 */
public class ReEncryptor implements ApplicationContextAware
{
    private static Log logger = LogFactory.getLog(ReEncryptor.class);

	private NodeDAO nodeDAO;
	private NamespaceDAO namespaceDAO;
	private DictionaryDAO dictionaryDAO;
	private DictionaryService dictionaryService;
	private QNameDAO qnameDAO;

	private MetadataEncryptor metadataEncryptor;

	private ApplicationContext applicationContext;

	private TransactionService transactionService;
	private RetryingTransactionHelper transactionHelper;

    /**
     * Set the transaction provider so that each execution can be performed within a transaction
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
        this.transactionHelper = transactionService.getRetryingTransactionHelper();
        this.transactionHelper.setForceWritable(true);
    }

//    protected MetadataEncryptor getMetadataEncryptor(EncryptionParameters encryptionParameters)
//    {
//		DefaultEncryptor encryptor = new DefaultEncryptor();
//		encryptor.setCipherAlgorithm(encryptionParameters.getCipherAlgorithm());
//		encryptor.setCipherProvider(null);
//		KeystoreKeyProvider keyProvider = new KeystoreKeyProvider();
//		keyProvider.setLocation(encryptionParameters.getKeyStoreLocation());
//		keyProvider.setPasswordsFileLocation(encryptionParameters.getPasswordFileLocation());
//		keyProvider.setType(encryptionParameters.getKeyStoreType());
//		keyProvider.setKeyResourceLoader(new SpringKeyResourceLoader());
//		keyProvider.setProvider(encryptionParameters.getKeyStoreProvider()
//				);
//
//		encryptor.setKeyProvider(keyProvider);
//
//		MetadataEncryptor metadataEncryptor = new MetadataEncryptor();
//		metadataEncryptor.setEncryptor(encryptor);
//		metadataEncryptor.setDictionaryService(dictionaryService);
//
//		return metadataEncryptor;
//    }
    
	public void setMetadataEncryptor(MetadataEncryptor metadataEncryptor)
	{
		this.metadataEncryptor = metadataEncryptor;
	}

	public void init()
	{
	}

	public void reencrypt(final KeyStoreParameters newEncryptionParameters, final List<NodePropertyEntity> properties)
	{
        BatchProcessor.BatchProcessWorker<NodePropertyEntity> worker = new BatchProcessor.BatchProcessWorker<NodePropertyEntity>()
        {
            public String getIdentifier(NodePropertyEntity entity)
            {
                return String.valueOf(entity.getNodeId());
            }

            public void beforeProcess() throws Throwable
            {
            }

            public void afterProcess() throws Throwable
            {
            }

            public void process(NodePropertyEntity entity) throws Throwable
            {
    			Object value = entity.getValue();
    			if(value instanceof SealedObject)
    			{
	    			SealedObject sealed = (SealedObject)value;

	    			NodePropertyKey nodeKey = entity.getKey();
	    			QName propertyQName = qnameDAO.getQName(nodeKey.getQnameId()).getSecond();

	    			// metadataEncryptor uses a fallback encryptor; decryption will try the
	    			// default (new) keys first (which will fail for properties created before the
	    			// change in keys), followed by the backup keys.
	    			Serializable decrypted = metadataEncryptor.decrypt(propertyQName, sealed);

	    			// Re-encrypt. The new keys will be used.
	    			Serializable resealed = metadataEncryptor.encrypt(propertyQName, decrypted);

	    			// TODO update resealed using batch update?
	    			// does the node DAO do batch updating?
	    			nodeDAO.setNodeProperties(entity.getNodeId(), Collections.singletonMap(propertyQName, resealed));
    			}
    			else
    			{
    				// TODO
    			}
            }
        };

        BatchProcessWorkProvider<NodePropertyEntity> provider = new BatchProcessWorkProvider<NodePropertyEntity>()
		{
        	private int start = 0;

			@Override
			public int getTotalEstimatedWorkSize()
			{
				return properties.size();
			}

			@Override
			public Collection<NodePropertyEntity> getNextWork()
			{
				int end = start + 20;
				if(end > properties.size())
				{
					end = properties.size();
				}
				List<NodePropertyEntity> sublist = properties.subList(start, end);
				start += 20;
				return sublist;
			}
		};

        // Migrate using 2 threads, 20 authorities per transaction. Log every 100 entries.
		// TODO, propertize these numbers
        new BatchProcessor<NodePropertyEntity>(
                I18NUtil.getMessage(""),
                transactionHelper,
                provider,
                2, 20,
                applicationContext,
                logger, 100).process(worker, true);
	}

	public void execute(KeyStoreParameters newEncryptionParameters)
	{
		// Proceed only if fallback is available i.e. the systems has both old and new keys
		if(metadataEncryptor.isFallbackAvailable())
		{
			QName model = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, namespaceDAO);

			// get properties that are encrypted
			Collection<PropertyDefinition> propertyDefs = dictionaryDAO.getProperties(model, DataTypeDefinition.ENCRYPTED);
			List<NodePropertyEntity> properties = nodeDAO.getProperties(propertyDefs);

			// reencrypt these properties
			reencrypt(newEncryptionParameters, properties);
		}
		else
		{
			// TODO
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException
	{
		this.applicationContext = applicationContext;
	}
}
