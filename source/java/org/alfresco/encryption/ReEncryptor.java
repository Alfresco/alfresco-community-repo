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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.crypto.SealedObject;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodePropertyEntity;
import org.alfresco.repo.domain.node.NodePropertyKey;
import org.alfresco.repo.domain.node.NodePropertyValue;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.surf.util.I18NUtil;

// TODO lock so that only one encryptor can run at a time
/**
 * Re-encrypts encryptable repository properties using a new set of encryption keys.
 * Decrypts the repository properties using the default encryptor, falling back to
 * a backup decryptor (using the old encryption keys) if necessary, and then re-encrypts
 * the properties.
 * 
 * Can run in one of two ways:
 * 
 * <ul>
 * <li> during bootstrap (used by the community edition of the software)
 * <li> by using JMX. In this case, the system can stay running while the re-encryption takes place.
 * </ul>
 * 
 * @since 4.0
 */
public class ReEncryptor implements ApplicationContextAware
{
    private static Log logger = LogFactory.getLog(ReEncryptor.class);

	private NodeDAO nodeDAO;
	private DictionaryDAO dictionaryDAO;
	private DictionaryService dictionaryService;
	private TransactionService transactionService;
	private QNameDAO qnameDAO;
	
	private MetadataEncryptor metadataEncryptor;
//	private KeyStoreParameters keyStoreParameters;
//	private KeyStoreParameters backupKeyStoreParameters;
	private AlfrescoKeyStore backupKeyStore;
	private AlfrescoKeyStore keyStore;
	private KeyProvider backupKeyProvider;
	private KeyProvider keyProvider;
//	private KeyResourceLoader keyResourceLoader;
	
	private ApplicationContext applicationContext;
	private RetryingTransactionHelper transactionHelper;
	private String cipherAlgorithm;

	private int chunkSize;
	private boolean splitTxns = true;

    /**
     * Set the transaction provider so that each execution can be performed within a transaction
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
        this.transactionHelper = transactionService.getRetryingTransactionHelper();
        this.transactionHelper.setForceWritable(true);
    }
    
	public void setMetadataEncryptor(MetadataEncryptor metadataEncryptor)
	{
		this.metadataEncryptor = metadataEncryptor;
	}

	public void setChunkSize(int chunkSize)
	{
		this.chunkSize = chunkSize;
	}

	public void setSplitTxns(boolean splitTxns)
	{
		this.splitTxns = splitTxns;
	}

	public void setNodeDAO(NodeDAO nodeDAO)
	{
		this.nodeDAO = nodeDAO;
	}

	public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
	{
		this.dictionaryDAO = dictionaryDAO;
	}

	public void setDictionaryService(DictionaryService dictionaryService)
	{
		this.dictionaryService = dictionaryService;
	}

	public void setQnameDAO(QNameDAO qnameDAO)
	{
		this.qnameDAO = qnameDAO;
	}

	public void setCipherAlgorithm(String cipherAlgorithm)
	{
		this.cipherAlgorithm = cipherAlgorithm;
	}
	
//	public void setKeyStoreParameters(KeyStoreParameters keyStoreParameters)
//	{
//		this.keyStoreParameters = keyStoreParameters;
//	}
//
//	public void setBackupKeyStoreParameters(KeyStoreParameters backupKeyStoreParameters)
//	{
//		this.backupKeyStoreParameters = backupKeyStoreParameters;
//	}
	
//	protected KeyProvider getKeyProvider(AlfrescoKeyStore keyStore)
//	{
//		KeyProvider keyProvider = new KeystoreKeyProvider(keyStore);
//		return keyProvider;
//	}
	
	public void setBackupKeyStore(AlfrescoKeyStore backupKeyStore)
	{
		this.backupKeyStore = backupKeyStore;
	}

	public void setKeyStore(AlfrescoKeyStore keyStore)
	{
		this.keyStore = keyStore;
	}

	public void setBackupKeyProvider(KeyProvider backupKeyProvider)
	{
		this.backupKeyProvider = backupKeyProvider;
	}
	
	public void setKeyProvider(KeyProvider keyProvider)
	{
		this.keyProvider = keyProvider;
	}
	
//	public void setKeyResourceLoader(KeyResourceLoader keyResourceLoader)
//	{
//		this.keyResourceLoader = keyResourceLoader;
//	}
	
//	public MetadataEncryptor getMetadataEncryptor()
//	{
//		DefaultEncryptor backupEncryptor = new DefaultEncryptor();
//		backupEncryptor.setCipherProvider(null); // TODO parameterize
//		backupEncryptor.setCipherAlgorithm(cipherAlgorithm);
//		backupEncryptor.setKeyProvider(backupKeyProvider);
//
//		DefaultEncryptor encryptor = new DefaultEncryptor();
//		encryptor.setCipherProvider(null); // TODO parameterize
//		encryptor.setCipherAlgorithm(cipherAlgorithm);
//		encryptor.setKeyProvider(keyProvider);
//
//		DefaultFallbackEncryptor fallbackEncryptor = new DefaultFallbackEncryptor(encryptor, backupEncryptor);
//		MetadataEncryptor metadataEncryptor = new MetadataEncryptor();
//		metadataEncryptor.setEncryptor(fallbackEncryptor);
//		metadataEncryptor.setDictionaryService(dictionaryService);
//		return metadataEncryptor;
//	}
//
//	public MetadataEncryptor getMetadataEncryptor(KeyProvider backupKeyProvider, KeyProvider newKeyProvider)
//	{
//		DefaultEncryptor backupEncryptor = new DefaultEncryptor();
//		backupEncryptor.setCipherProvider(null); // TODO parameterize
//		backupEncryptor.setCipherAlgorithm(cipherAlgorithm);
//		backupEncryptor.setKeyProvider(backupKeyProvider);
//
//		DefaultEncryptor encryptor = new DefaultEncryptor();
//		encryptor.setCipherProvider(null); // TODO parameterize
//		encryptor.setCipherAlgorithm(cipherAlgorithm);
//		encryptor.setKeyProvider(newKeyProvider);
//
//		DefaultFallbackEncryptor fallbackEncryptor = new DefaultFallbackEncryptor(encryptor, backupEncryptor);
//		MetadataEncryptor metadataEncryptor = new MetadataEncryptor();
//		metadataEncryptor.setEncryptor(fallbackEncryptor);
//		metadataEncryptor.setDictionaryService(dictionaryService);
//		return metadataEncryptor;
//	}

//	protected KeyProvider getKeyProvider(final Map<String, Key> keys)
//	{
//		KeyProvider keyProvider = new KeyProvider()
//		{
//			@Override
//			public Key getKey(String keyAlias)
//			{
//				return keys.get(keyAlias);
//			}
//		};
//		return keyProvider;
//	}

	protected Encryptor getEncryptor(KeyProvider keyProvider)
	{
		DefaultEncryptor encryptor = new DefaultEncryptor();
		encryptor.setCipherProvider(null); // TODO parameterize
		encryptor.setCipherAlgorithm(cipherAlgorithm);
		encryptor.setKeyProvider(keyProvider);

		return encryptor;
	}

	/**
	 * For testing use.
	 * 
	 * @param keyProvider
	 */
	void reEncrypt(KeyProvider keyProvider)
	{
		metadataEncryptor.setEncryptor(getEncryptor(keyProvider));
		reEncryptImpl();
	}

	protected void reEncrypt(final MetadataEncryptor metadataEncryptor, final List<NodePropertyEntity> properties)
	{
		final Iterator<NodePropertyEntity> it = properties.iterator();
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

            public void process(final NodePropertyEntity entity) throws Throwable
            {
    			NodePropertyValue nodePropValue = entity.getValue();
    			// TODO check that we have the correct type i.e. can be cast to Serializable
    			Serializable value = nodePropValue.getSerializableValue();
    			if(value instanceof SealedObject)
    			{
	    			SealedObject sealed = (SealedObject)value;

	    			NodePropertyKey propertyKey = entity.getKey();
	    			QName propertyQName = qnameDAO.getQName(propertyKey.getQnameId()).getSecond();

	    			// decrypt...
	    			Serializable decrypted = metadataEncryptor.decrypt(propertyQName, sealed);

	    			// ...and then re-encrypt. The new keys will be used.
	    			Serializable resealed = metadataEncryptor.encrypt(propertyQName, decrypted);

	    			// TODO update resealed using batch update?
	    			// does the node DAO do batch updating?
	    			nodeDAO.setNodeProperties(entity.getNodeId(), Collections.singletonMap(propertyQName, resealed));
    			}
    			else
    			{
	    			NodePropertyKey nodeKey = entity.getKey();
	    			QName propertyQName = qnameDAO.getQName(nodeKey.getQnameId()).getSecond();
    				logger.warn("Encountered an encrypted property that is not a SealedObject, for node id " +
    						entity.getNodeId() + ", property " + propertyQName);
    			}
            }
        };

        BatchProcessWorkProvider<NodePropertyEntity> provider = new BatchProcessWorkProvider<NodePropertyEntity>()
		{
			@Override
			public int getTotalEstimatedWorkSize()
			{
				return properties.size();
			}

			@Override
			public Collection<NodePropertyEntity> getNextWork()
			{
				int count = 0;
				List<NodePropertyEntity> sublist = new ArrayList<NodePropertyEntity>(chunkSize);
				while(it.hasNext() && count < chunkSize)
				{
					sublist.add(it.next());
					count++;
				}

				return sublist;
			}
		};

		// TODO, "propertize" these numbers
        new BatchProcessor<NodePropertyEntity>(
                I18NUtil.getMessage(""),
                transactionHelper,
                provider,
                2, 20,
                applicationContext,
                logger, 20).process(worker, splitTxns);
	}

	/**
	 * Re-encrypt using the configured backup keystore to decrypt and the main keystore to encrypt
	 */
	public int bootstrapReEncrypt() throws MissingKeyStoreException
	{
		if(backupKeyStore.getKey(KeyProvider.ALIAS_METADATA) == null)
		{
			throw new MissingKeyStoreException("Backup key store is either not present or does not contain a metadata encryption key");
		}
		return reEncrypt();
	}

	/**
	 * Re-encrypt by decrypting using the configured keystore and encrypting using a keystore configured using the provided new key store parameters.
	 * Called from e.g. JMX.
	 * 
	 * Note: it is the responsibility of the end user to ensure that the keystore configured by newKeyStoreParameters is
	 * placed in the repository keystore directory. This can be done while the repository is running and it will be picked
	 * up automatically the next time the repository restarts.
	 */
	public int reEncrypt() throws MissingKeyStoreException
	{
		backupKeyStore.reload();
		keyStore.reload();
		if(keyStore.getKey(KeyProvider.ALIAS_METADATA) == null)
		{
			throw new MissingKeyStoreException("Main key store is either not present or does not contain a metadata encryption key");
		}
		if(backupKeyStore.getKey(KeyProvider.ALIAS_METADATA) == null)
		{
			throw new MissingKeyStoreException("Backup key store is either not present or does not contain a metadata encryption key");
		}
		return reEncryptImpl();
	}

	protected int reEncryptImpl()
	{
		// get properties that are encrypted
		Collection<PropertyDefinition> propertyDefs = dictionaryDAO.getPropertiesOfDataType(DataTypeDefinition.ENCRYPTED);
		// TODO use callback mechanism
		List<NodePropertyEntity> properties = nodeDAO.selectProperties(propertyDefs);
		
		if(logger.isDebugEnabled())
		{
			logger.debug("Found " + properties.size() + " properties to re-encrypt...");
		}

		// reencrypt these properties
		reEncrypt(metadataEncryptor, properties);

		if(logger.isDebugEnabled())
		{
			logger.debug("...done re-encrypting.");
		}

		return properties.size();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException
	{
		this.applicationContext = applicationContext;
	}
}
