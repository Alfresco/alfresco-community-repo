/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transaction;

import java.util.Set;

import org.alfresco.repo.cache.TransactionalCache;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.search.impl.lucene.LuceneIndexerAndSearcher;
import org.alfresco.util.transaction.TransactionListener;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Repo Specific Helper class to manage transaction synchronization.  This provides helpers to
 * ensure that the necessary <code>TransactionSynchronization</code> instances
 * are registered on behalf of the application code.
 * <p>
 * This class remains for backward API compatibility, the majority of transaction support has been moved to
 * TransactionSupportUtil in the Core project.
 * 
 * @author Derek Hulley
 * @author mrogers
 */
public abstract class AlfrescoTransactionSupport extends TransactionSupportUtil
{
    /*
     * The registrations of services is very explicit on the interface.  This
     * is to convey the idea that the execution of these services when the
     * transaction completes is very explicit.  As we only have a finite
     * list of types of services that need registration, this is still
     * OK.
     */
	
	private static int COMMIT_ORDER_NORMAL=0;
	private static int COMMIT_ORDER_INTEGRITY=1;
	private static int COMMIT_ORDER_LUCENE=2;
	private static int COMMIT_ORDER_DAO=3;
	private static int COMMIT_ORDER_CACHE=4;
    
    /**
     * The order of synchronization set to be 100 less than the Hibernate synchronization order
     */
    public static final int SESSION_SYNCHRONIZATION_ORDER =
        SessionFactoryUtils.SESSION_SYNCHRONIZATION_ORDER - 100;

    
    private static Log logger = LogFactory.getLog(AlfrescoTransactionSupport.class);
    
    /**
     * 
     * @author Derek Hulley
     * @since 2.1.4
     */
    public static enum TxnReadState
    {
        /** No transaction is active */
        TXN_NONE,
        /** The current transaction is read-only */
        TXN_READ_ONLY,
        /** The current transaction supports writes */
        TXN_READ_WRITE
    }
    

    
    /**
     * @return      Returns the read-write state of the current transaction
     * @since 2.1.4
     */
    public static TxnReadState getTransactionReadState()
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            return TxnReadState.TXN_NONE;
        }
        // Find the read-write state of the txn
        if (getResource(RESOURCE_KEY_TXN_COMPLETING) != null)
        {
            // Transaction is completing.  For all intents and purposes, we are not in a transaction.
            return TxnReadState.TXN_NONE;
        }
        else if (TransactionSynchronizationManager.isCurrentTransactionReadOnly())
        {
            return TxnReadState.TXN_READ_ONLY;
        }
        else
        {
            return TxnReadState.TXN_READ_WRITE;
        }
    }
    
    /**
     * Checks the state of the current transaction and throws an exception if a transaction
     * is not present or if the transaction is not read-write, if required.
     * 
     * @param requireReadWrite          <tt>true</tt> if the transaction must be read-write
     * 
     * @since 3.2
     */
    public static void checkTransactionReadState(boolean requireReadWrite)
    {
        TxnReadState readState = AlfrescoTransactionSupport.getTransactionReadState();
        switch (readState)
        {
            case TXN_NONE:
                throw new IllegalStateException(
                        "The current operation requires an active " +
                        (requireReadWrite ? "read-write" : "") +
                        "transaction.");
            case TXN_READ_ONLY:
                if (requireReadWrite)
                {
                    throw new IllegalStateException("The current operation requires an active read-write transaction.");
                }
            case TXN_READ_WRITE:
                // All good
        }
    }
    
    /**
     * Are there any pending changes which must be synchronized with the store?
     * 
     * @return true => changes are pending
     * 
     * @deprecated  To be replaced by {@link DirtySessionMethodInterceptor}
     */
    public static boolean isDirty() 
    {
    	Set<TransactionListener> allListeners = getListeners();
    	for(TransactionListener listener : allListeners)
    	{
    		if(listener instanceof TransactionalDao)
    		{
    			TransactionalDao service = (TransactionalDao)listener;
    	        if (service.isDirty())
    	        {
    	            return true;
    	        }
    			
    		}
    		else if (listener instanceof DAOAdapter)
    		{
    			DAOAdapter adapter = (DAOAdapter)listener;
    			TransactionalDao service = adapter.getService();
    	        if (service.isDirty())
    	        {
    	            return true;
    	        }
    		}
    		
    	}
    	       
        return false;
    }
    
    
    /**
     * Method that registers a <tt>NodeDaoService</tt> against the transaction.
     * Setting this will ensure that the pre- and post-commit operations perform
     * the necessary cleanups against the <tt>NodeDaoService</tt>.
     * <p>
     * This method can be called repeatedly as long as the service being bound
     * implements <tt>equals</tt> and <tt>hashCode</tt>.
     * 
     * @param daoService
     */
    public static void bindDaoService(TransactionalDao daoService)
    {
        
        DAOAdapter adapter = new DAOAdapter(daoService);
        
        boolean bound = bindListener(adapter, COMMIT_ORDER_DAO);
        
        // done
        if (logger.isDebugEnabled())
        {
            logBoundService(daoService, bound); 
        }
    }

    /**
     * Method that registers an <tt>IntegrityChecker</tt> against the transaction.
     * Setting this will ensure that the pre- and post-commit operations perform
     * the necessary cleanups against the <tt>IntegrityChecker</tt>.
     * <p>
     * This method can be called repeatedly as long as the service being bound
     * implements <tt>equals</tt> and <tt>hashCode</tt>.
     * 
     * @param integrityChecker
     */
    public static void bindIntegrityChecker(IntegrityChecker integrityChecker)
    {
       
        // bind the service in
        boolean bound = bindListener((TransactionListener) integrityChecker, COMMIT_ORDER_INTEGRITY);
        
        if (logger.isDebugEnabled())
        {
            logBoundService(integrityChecker, bound); 
        }
    }

    /**
     * Method that registers a <tt>LuceneIndexerAndSearcherFactory</tt> against
     * the transaction.
     * <p>
     * Setting this will ensure that the pre- and post-commit operations perform
     * the necessary cleanups against the <tt>LuceneIndexerAndSearcherFactory</tt>.
     * <p>
     * Although bound within a <tt>Set</tt>, it would still be better for the caller
     * to only bind once per transaction, if possible.
     * 
     * @param indexerAndSearcher the Lucene indexer to perform transaction completion
     *      tasks on
     */
    public static void bindLucene(LuceneIndexerAndSearcher indexerAndSearcher)
    {
        LuceneIndexerAndSearcherAdapter adapter = new LuceneIndexerAndSearcherAdapter(indexerAndSearcher);
        
        boolean bound = bindListener(adapter, COMMIT_ORDER_LUCENE);
       
        // done
        if (logger.isDebugEnabled())
        {
            logBoundService(indexerAndSearcher, bound); 
        }
    }
    
    /**
     * Method that registers a <tt>Listener</tt> against
     * the transaction.
     * <p> will be better for the caller
     * to only bind once per transaction, if possible.
     * 
     * @param indexerAndSearcher the Lucene indexer to perform transaction completion
     *      tasks on
     */
    public static void bindListener(TransactionListener listener)
    {
    	boolean bound = false;
    	
        if (listener instanceof IntegrityChecker)
        {
            bound = bindListener(listener, COMMIT_ORDER_INTEGRITY);
        }
        else if (listener instanceof TransactionalCache)
        {
            bound = bindListener(listener, COMMIT_ORDER_CACHE);
        }
        else
        {
            bound = bindListener(listener,  COMMIT_ORDER_NORMAL);
        }

        if (logger.isDebugEnabled())
        {
            logBoundService(listener, bound); 
        }
    }
    
    /**
     * Use as part of a debug statement
     * 
     * @param service the service to report 
     * @param bound true if the service was just bound; false if it was previously bound
     */
    private static void logBoundService(Object service, boolean bound)
    {
        if (bound)
        {
            logger.debug("Bound service: \n" +
                    "   transaction: " + getTransactionId() + "\n" +
                    "   service: " + service);
        }
        else
        {
            logger.debug("Service already bound: \n" +
                    "   transaction: " + getTransactionId() + "\n" +
                    "   service: " + service);
        }
    }
    
    /**
     * No-op
     * 
     * @deprecated      No longer does anything
     */
    public static void flush()
    {
        // No-op
    }
     
}    
