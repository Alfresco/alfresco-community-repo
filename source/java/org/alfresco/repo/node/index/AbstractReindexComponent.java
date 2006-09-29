/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.node.index;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract helper for reindexing.
 * 
 * @see #reindexImpl()
 * @see #getIndexerWriteLock()
 * @see #isShuttingDown()
 * 
 * @author Derek Hulley
 */
public abstract class AbstractReindexComponent implements IndexRecovery
{
    private static Log logger = LogFactory.getLog(AbstractReindexComponent.class);
    
    /** kept to notify the thread that it should quit */
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener("MissingContentReindexComponent");
    
    private AuthenticationComponent authenticationComponent;
    /** provides transactions to atomically index each missed transaction */
    protected TransactionComponent transactionService;
    /** the component to index the node hierarchy */
    protected Indexer indexer;
    /** the FTS indexer that we will prompt to pick up on any un-indexed text */
    protected FullTextSearchIndexer ftsIndexer;
    /** the component providing searches of the indexed nodes */
    protected SearchService searcher;
    /** the component giving direct access to <b>store</b> instances */
    protected NodeService nodeService;
    /** the component giving direct access to <b>transaction</b> instances */
    protected NodeDaoService nodeDaoService;
    
    private boolean shutdown;
    private final WriteLock indexerWriteLock;
    
    public AbstractReindexComponent()
    {
        shutdown = false;
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        indexerWriteLock = readWriteLock.writeLock();
    }
    
    /**
     * Convenience method to get a common write lock.  This can be used to avoid
     * concurrent access to the work methods.
     */
    protected WriteLock getIndexerWriteLock()
    {
        return indexerWriteLock;
    }
    
    /**
     * Programmatically notify a reindex thread to terminate
     * 
     * @param shutdown true to shutdown, false to reset
     */
    public void setShutdown(boolean shutdown)
    {
        this.shutdown = shutdown;
    }
    
    /**
     * 
     * @return Returns true if the VM shutdown hook has been triggered, or the instance
     *      was programmatically {@link #shutdown shut down}
     */
    protected boolean isShuttingDown()
    {
        return shutdown || vmShutdownListener.isVmShuttingDown();
    }

    /**
     * @param authenticationComponent ensures that reindexing operates as system user
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * Set the low-level transaction component to use
     * 
     * @param transactionComponent provide transactions to index each missed transaction
     */
    public void setTransactionComponent(TransactionComponent transactionComponent)
    {
        this.transactionService = transactionComponent;
    }

    /**
     * @param indexer the indexer that will be index
     */
    public void setIndexer(Indexer indexer)
    {
        this.indexer = indexer;
    }
    
    /**
     * @param ftsIndexer the FTS background indexer
     */
    public void setFtsIndexer(FullTextSearchIndexer ftsIndexer)
    {
        this.ftsIndexer = ftsIndexer;
    }

    /**
     * @param searcher component providing index searches
     */
    public void setSearcher(SearchService searcher)
    {
        this.searcher = searcher;
    }

    /**
     * @param nodeService provides information about nodes for indexing
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param nodeDaoService provides access to transaction-related queries
     */
    public void setNodeDaoService(NodeDaoService nodeDaoService)
    {
        this.nodeDaoService = nodeDaoService;
    }

    /**
     * Perform the actual work.  This method will be called as the system user
     * and within an existing transaction.  This thread will only ever be accessed
     * by a single thread per instance.
     *
     */
    protected abstract void reindexImpl();
    
    /**
     * If this object is currently busy, then it just nothing
     */
    public final void reindex()
    {
        PropertyCheck.mandatory(this, "authenticationComponent", this.authenticationComponent);
        PropertyCheck.mandatory(this, "ftsIndexer", this.ftsIndexer);
        PropertyCheck.mandatory(this, "indexer", this.indexer);
        PropertyCheck.mandatory(this, "searcher", this.searcher);
        PropertyCheck.mandatory(this, "nodeService", this.nodeService);
        PropertyCheck.mandatory(this, "nodeDaoService", this.nodeDaoService);
        PropertyCheck.mandatory(this, "transactionComponent", this.transactionService);
        
        if (indexerWriteLock.tryLock())
        {
            Authentication auth = null;
            try
            {
                auth = AuthenticationUtil.getCurrentAuthentication();
                // authenticate as the system user
                authenticationComponent.setSystemUserAsCurrentUser();
                TransactionWork<Object> reindexWork = new TransactionWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        reindexImpl();
                        return null;
                    }
                };
                TransactionUtil.executeInUserTransaction(transactionService, reindexWork);
            }
            finally
            {
                try { indexerWriteLock.unlock(); } catch (Throwable e) {}
                if (auth != null)
                {
                    authenticationComponent.setCurrentAuthentication(auth);
                }
            }
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Reindex work completed: " + this);
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Bypassed reindex work - already busy: " + this);
            }
        }
    }
}