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
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
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

/**
 * Re-encrypts encryptable repository properties using a new set of encryption keys.
 * Decrypts the repository properties using the default encryptor, falling back to
 * a backup decryptor (using the old encryption keys) if necessary, and then re-encrypts
 * the properties.
 * 
 * Can run in one of two ways:
 * 
 * <ul>
 * <li> during bootstrap.
 * <li> by using JMX (available only to Enterprise). In this case, the system can stay running while the re-encryption takes place.
 * </ul>
 * 
 * @since 4.0
 */
public class ReEncryptor implements ApplicationContextAware
{
    private static Log logger = LogFactory.getLog(ReEncryptor.class);

	private NodeDAO nodeDAO;
	private DictionaryDAO dictionaryDAO;
	private QNameDAO qnameDAO;
	
	private MetadataEncryptor metadataEncryptor;
	
	private ApplicationContext applicationContext;
	private TransactionService transactionService;
	private RetryingTransactionHelper transactionHelper;

	private int numThreads;
	private int chunkSize;
	private boolean splitTxns = true;
	
    private static final QName LOCK = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "OrphanReaper");
    private JobLockService jobLockService;

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

	public MetadataEncryptor getMetadataEncryptor()
	{
		return metadataEncryptor;
	}
	
	public void setJobLockService(JobLockService jobLockService)
	{
		this.jobLockService = jobLockService;
	}
	
	public void setNumThreads(int numThreads)
	{
		this.numThreads = numThreads;
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

	public void setQnameDAO(QNameDAO qnameDAO)
	{
		this.qnameDAO = qnameDAO;
	}

    /**
     * Attempts to get the lock. If the lock couldn't be taken, then <tt>null</tt> is returned.
     * 
     * @return Returns the lock token or <tt>null</tt>
     */
    private String getLock(long time)
    {
        try
        {
            return jobLockService.getLock(LOCK, time);
        }
        catch (LockAcquisitionException e)
        {
            return null;
        }
    }

    /**
     * Attempts to get the lock. If it fails, the current transaction is marked for rollback.
     * 
     * @return Returns the lock token
     */
    private void refreshLock(String lockToken, long time)
    {
        if (lockToken == null)
        {
            throw new IllegalArgumentException("Must provide existing lockToken");
        }
        jobLockService.refreshLock(lockToken, LOCK, time);
    }

	protected void reEncryptProperties(final List<NodePropertyEntity> properties, final String lockToken)
	{
		final Iterator<NodePropertyEntity> it = properties.iterator();
		
		// TODO use BatchProcessWorkerAdaptor?
		
        BatchProcessor.BatchProcessWorker<NodePropertyEntity> worker = new BatchProcessor.BatchProcessWorker<NodePropertyEntity>()
        {
            public String getIdentifier(NodePropertyEntity entity)
            {
                return String.valueOf(entity.getNodeId());
            }

            public void beforeProcess() throws Throwable
            {
                refreshLock(lockToken, chunkSize * 100L);
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

	    			// ...and then re-encrypt. The new key will be used.
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
				List<NodePropertyEntity> sublist = new ArrayList<NodePropertyEntity>(chunkSize);

				synchronized(it)
				{
					int count = 0;
					while(it.hasNext() && count < chunkSize)
					{
						sublist.add(it.next());
						count++;
					}
				}

				return sublist;
			}
		};

        new BatchProcessor<NodePropertyEntity>(
                "Reencryptor",
                transactionHelper,
                provider,
                numThreads, chunkSize,
                applicationContext,
                logger, 100).process(worker, splitTxns);
	}

	/**
	 * Re-encrypt using the configured backup keystore to decrypt and the main keystore to encrypt
	 */
	public int bootstrapReEncrypt() throws MissingKeyException
	{
		if(!metadataEncryptor.backupKeyAvailable(KeyProvider.ALIAS_METADATA))
		{
			throw new MissingKeyException("Backup key store is either not present or does not contain a metadata encryption key");
		}
		return reEncrypt();
	}

	/**
	 * Re-encrypt by decrypting using the configured keystore and encrypting using a keystore configured using the provided new key store parameters.
	 * Called from e.g. JMX.
	 * 
	 * Assumes that the main key store has been already been reloaded.
	 * 
	 * Note: it is the responsibility of the end user to ensure that the underlying keystores have been set up appropriately
	 * i.e. the old key store is backed up to the location defined by the property '${dir.keystore}/backup-keystore' and the new
	 * key store replaces it. This can be done while the repository is running.
	 */
	public int reEncrypt() throws MissingKeyException
	{
		if(!metadataEncryptor.keyAvailable(KeyProvider.ALIAS_METADATA))
		{
			throw new MissingKeyException("Main key store is either not present or does not contain a metadata encryption key");
		}
		if(!metadataEncryptor.backupKeyAvailable(KeyProvider.ALIAS_METADATA))
		{
			throw new MissingKeyException("Backup key store is either not present or does not contain a metadata encryption key");
		}
		
    	int numProps = reEncryptImpl();
    	return numProps;
	}

	protected int reEncryptImpl()
	{
		// Take out a re-encryptor lock
		RetryingTransactionCallback<String> txnWork = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Exception
            {
		        String lockToken = getLock(20000L);
		        return lockToken;
            }
        };

        String lockToken = transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);
        if(lockToken == null)
        {
            logger.warn("Can't get lock. Assume multiple re-encryptors ...");
            return 0;
        }

		// get encrypted properties
		Collection<PropertyDefinition> propertyDefs = dictionaryDAO.getPropertiesOfDataType(DataTypeDefinition.ENCRYPTED);
		// TODO use callback mechanism, or select based on set of nodes?
		List<NodePropertyEntity> properties = nodeDAO.selectProperties(propertyDefs);

		if(logger.isDebugEnabled())
		{
			logger.debug("Found " + properties.size() + " properties to re-encrypt...");
		}

		// reencrypt these properties TODO don't call if num props == 0
		reEncryptProperties(properties, lockToken);

		if(logger.isDebugEnabled())
		{
			logger.debug("...done re-encrypting.");
		}

		return properties.size();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		this.applicationContext = applicationContext;
	}
}
