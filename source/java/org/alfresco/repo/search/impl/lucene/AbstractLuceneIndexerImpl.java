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
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.xa.XAResource;

import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.impl.lucene.index.TransactionStatus;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Common support for indexing across implementations
 * 
 * @author andyh
 * @param <T> -
 *            the type used to generate the key in the index file
 */
public abstract class AbstractLuceneIndexerImpl<T> extends AbstractLuceneBase
{
    /**
     * Enum for indexing actions against a node
     */
    protected enum Action
    {
        /**
         * An index
         */
        INDEX,
        /**
         * A reindex
         */
        REINDEX,
        /**
         * A delete
         */
        DELETE,
        MOVE,
        /**
         * A cascaded reindex (ensures directory structre is ok)
         */
        CASCADEREINDEX
    }

    protected enum IndexUpdateStatus
    {
        /**
         * Inde is unchanged
         */
        UNMODIFIED,
        /**
         * Index is being changein in TX
         */
        SYNCRONOUS,
        /**
         * Index is eiong changed by a background upate
         */
        ASYNCHRONOUS;
    }

    protected enum IndexDeleteMode {REINDEX, DELETE, MOVE};
    
    protected enum FTSStatus {New, Dirty, Clean};

    protected long docs;
    
    // An indexer with read through activated can only see already-committed documents in the database. Useful when
    // reindexing lots of old documents and not wanting to pollute the caches with stale versions of nodes.
    private boolean isReadThrough;
    
    protected TransactionService transactionService;

    public void setReadThrough(boolean isReadThrough)
    {
        this.isReadThrough = isReadThrough;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    protected static class Command<S>
    {
        S ref;

        Action action;

        Command(S ref, Action action)
        {
            this.ref = ref;
            this.action = action;
        }

        public String toString()
        {
            StringBuffer buffer = new StringBuffer();
            if (action == Action.INDEX)
            {
                buffer.append("Index ");
            }
            else if (action == Action.DELETE)
            {
                buffer.append("Delete ");
            }
            else if (action == Action.REINDEX)
            {
                buffer.append("Reindex ");
            }
            else
            {
                buffer.append("Unknown ... ");
            }
            buffer.append(ref);
            return buffer.toString();
        }

    }

    /**
     * No transform available
     */
    public static final String NOT_INDEXED_NO_TRANSFORMATION = "nint";

    /**
     * Tranfrom failed
     */
    public static final String NOT_INDEXED_TRANSFORMATION_FAILED = "nitf";

    /**
     * No content
     */
    public static final String NOT_INDEXED_CONTENT_MISSING = "nicm";

    /**
     * No type conversion
     */
    public static final String NOT_INDEXED_NO_TYPE_CONVERSION = "nintc";

    /**
     * Logger
     */
    private static Log    s_logger = LogFactory.getLog(AbstractLuceneIndexerImpl.class);

    protected static Set<String> deletePrimary(Collection<String> nodeRefs, IndexReader reader, boolean delete)
            throws LuceneIndexException
    {

        Set<String> refs = new LinkedHashSet<String>();

        for (String nodeRef : nodeRefs)
        {

            try
            {
                TermDocs td = reader.termDocs(new Term("PRIMARYPARENT", nodeRef));
                while (td.next())
                {
                    int doc = td.doc();
                    Document document = reader.document(doc);
                    String[] ids = document.getValues("ID");
                    refs.add(ids[ids.length - 1]);
                    if (delete)
                    {
                        reader.deleteDocument(doc);
                    }
                }
                td.close();
            }
            catch (IOException e)
            {
                throw new LuceneIndexException("Failed to delete node by primary parent for " + nodeRef, e);
            }
        }

        return refs;

    }

    protected static Set<String> deleteReference(Collection<String> nodeRefs, IndexReader reader, boolean delete)
            throws LuceneIndexException
    {

        Set<String> refs = new LinkedHashSet<String>();

        for (String nodeRef : nodeRefs)
        {

            try
            {
                TermDocs td = reader.termDocs(new Term("PARENT", nodeRef));
                while (td.next())
                {
                    int doc = td.doc();
                    Document document = reader.document(doc);
                    String[] ids = document.getValues("ID");
                    refs.add(ids[ids.length - 1]);
                    if (delete)
                    {
                        reader.deleteDocument(doc);
                    }
                }
                td.close();
            }
            catch (IOException e)
            {
                throw new LuceneIndexException("Failed to delete node by parent for " + nodeRef, e);
            }
        }

        return refs;

    }

    protected static Set<String> deleteContainerAndBelow(String nodeRef, IndexReader reader, boolean delete,
            boolean cascade) throws LuceneIndexException
    {
        Set<String> refs = new LinkedHashSet<String>();

        try
        {
            if (delete)
            {
                reader.deleteDocuments(new Term("ID", nodeRef));
            }
            refs.add(nodeRef);
            if (cascade)
            {
                TermDocs td = reader.termDocs(new Term("ANCESTOR", nodeRef));
                while (td.next())
                {
                    int doc = td.doc();
                    Document document = reader.document(doc);
                    String[] ids = document.getValues("ID");
                    refs.add(ids[ids.length - 1]);
                    if (delete)
                    {
                        reader.deleteDocument(doc);
                    }
                }
                td.close();
            }
        }
        catch (IOException e)
        {
            throw new LuceneIndexException("Failed to delete container and below for " + nodeRef, e);
        }
        return refs;
    }

    /** the maximum transformation time to allow atomically, defaulting to 20ms */
    protected long maxAtomicTransformationTime = 20;

    /**
     * A list of all deletions we have made - at merge these deletions need to be made against the main index. TODO:
     * Consider if this information needs to be persisted for recovery
     */
    protected Set<String> deletions = new LinkedHashSet<String>();
    
    /**
     * A list of deletions associated with the changes to nodes in the current flush
     */
    protected Set<String> deletionsSinceFlush = new HashSet<String>(); 

    /**
     * List of pending indexing commands.
     */
    protected List<Command<T>> commandList = new ArrayList<Command<T>>(10000);

    /**
     * Flag to indicte if we are doing an in transactional delta or a batch update to the index. If true, we are just
     * fixing up non atomically indexed things from one or more other updates.
     */
    protected IndexUpdateStatus indexUpdateStatus = IndexUpdateStatus.UNMODIFIED;

    /**
     * Set the max time allowed to transform content atomically
     * 
     * @param maxAtomicTransformationTime
     */
    public void setMaxAtomicTransformationTime(long maxAtomicTransformationTime)
    {
        this.maxAtomicTransformationTime = maxAtomicTransformationTime;
    }

    /**
     * Utility method to check we are in the correct state to do work Also keeps track of the dirty flag.
     * 
     * @throws IOException
     */

    protected void checkAbleToDoWork(IndexUpdateStatus indexUpdateStatus)
    {
        if (this.indexUpdateStatus == IndexUpdateStatus.UNMODIFIED)
        {
            this.indexUpdateStatus = indexUpdateStatus;
        }
        else if (this.indexUpdateStatus == indexUpdateStatus)
        {
            return;
        }
        else
        {
            throw new IndexerException("Can not mix FTS and transactional updates");
        }

        switch (getStatus())
        {
        case UNKNOWN:
            try
            {
                setStatus(TransactionStatus.ACTIVE);
            }
            catch (IOException e)
            {
                throw new LuceneIndexException("Failed to set TX active", e);
            }
            break;
        case ACTIVE:
            // OK
            break;
        default:
            // All other states are a problem
            throw new IndexerException(buildErrorString());
        }
    }

    /**
     * Utility method to report errors about invalid state.
     * 
     * @return - an error based on status
     */
    private String buildErrorString()
    {
        StringBuilder buffer = new StringBuilder(128);
        buffer.append("The indexer is unable to accept more work: ");
        switch (getStatus().getStatus())
        {
        case Status.STATUS_COMMITTED:
            buffer.append("The indexer has been committed");
            break;
        case Status.STATUS_COMMITTING:
            buffer.append("The indexer is committing");
            break;
        case Status.STATUS_MARKED_ROLLBACK:
            buffer.append("The indexer is marked for rollback");
            break;
        case Status.STATUS_PREPARED:
            buffer.append("The indexer is prepared to commit");
            break;
        case Status.STATUS_PREPARING:
            buffer.append("The indexer is preparing to commit");
            break;
        case Status.STATUS_ROLLEDBACK:
            buffer.append("The indexer has been rolled back");
            break;
        case Status.STATUS_ROLLING_BACK:
            buffer.append("The indexer is rolling back");
            break;
        case Status.STATUS_UNKNOWN:
            buffer.append("The indexer is in an unknown state");
            break;
        default:
            break;
        }
        return buffer.toString();
    }

    /**
     * Commit this index
     * 
     * @throws LuceneIndexException
     */
    public void commit() throws LuceneIndexException
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug(Thread.currentThread().getName() + " Starting Commit");
        }
        switch (getStatus().getStatus())
        {
        case Status.STATUS_COMMITTING:
            throw new LuceneIndexException("Unable to commit: Transaction is committing");
        case Status.STATUS_COMMITTED:
            throw new LuceneIndexException("Unable to commit: Transaction is commited ");
        case Status.STATUS_ROLLING_BACK:
            throw new LuceneIndexException("Unable to commit: Transaction is rolling back");
        case Status.STATUS_ROLLEDBACK:
            throw new LuceneIndexException("Unable to commit: Transaction is aleady rolled back");
        case Status.STATUS_MARKED_ROLLBACK:
            throw new LuceneIndexException("Unable to commit: Transaction is marked for roll back");
        case Status.STATUS_PREPARING:
            throw new LuceneIndexException("Unable to commit: Transaction is preparing");
        case Status.STATUS_ACTIVE:
            // special case - commit from active
            prepare();
            // drop through to do the commit;
        default:
            if (getStatus().getStatus() != Status.STATUS_PREPARED)
            {
                throw new LuceneIndexException("Index must be prepared to commit");
            }
            try
            {
                setStatus(TransactionStatus.COMMITTING);
                if (isModified())
                {
                    doCommit();
                }
                setStatus(TransactionStatus.COMMITTED);
            }
            catch (LuceneIndexException e)
            {
                // If anything goes wrong we try and do a roll back
                rollback();
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName() + " Commit Failed", e);
                }
                throw new LuceneIndexException("Commit failed", e);
            }
            catch (Throwable t)
            {
                // If anything goes wrong we try and do a roll back
                rollback();
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName() + " Commit Failed", t);
                }
                throw new LuceneIndexException("Commit failed", t);                
            }
            finally
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName() + " Ending Commit");
                }
                
                // Make sure we tidy up
                // deleteDelta();
            }
            break;
        }
    }

    /**
     * Prepare to commit At the moment this makes sure we have all the locks TODO: This is not doing proper
     * serialisation against the index as would a data base transaction.
     * 
     * @return the tx state
     * @throws LuceneIndexException
     */
    public int prepare() throws LuceneIndexException
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug(Thread.currentThread().getName() + " Starting Prepare");
        }
        switch (getStatus().getStatus())
        {
        case Status.STATUS_COMMITTING:
            throw new IndexerException("Unable to prepare: Transaction is committing");
        case Status.STATUS_COMMITTED:
            throw new IndexerException("Unable to prepare: Transaction is commited ");
        case Status.STATUS_ROLLING_BACK:
            throw new IndexerException("Unable to prepare: Transaction is rolling back");
        case Status.STATUS_ROLLEDBACK:
            throw new IndexerException("Unable to prepare: Transaction is aleady rolled back");
        case Status.STATUS_MARKED_ROLLBACK:
            throw new IndexerException("Unable to prepare: Transaction is marked for roll back");
        case Status.STATUS_PREPARING:
            throw new IndexerException("Unable to prepare: Transaction is already preparing");
        case Status.STATUS_PREPARED:
            throw new IndexerException("Unable to prepare: Transaction is already prepared");
        default:
            try
            {
                setStatus(TransactionStatus.PREPARING);
                if (isModified())
                {
                    doPrepare();
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug(Thread.currentThread().getName() + " Waiting to Finish Preparing");
                    }                    
                }
                setStatus(TransactionStatus.PREPARED);
                return isModified() ? XAResource.XA_OK : XAResource.XA_RDONLY;
            }
            catch (LuceneIndexException e)
            {
                setRollbackOnly();
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName() + " Prepare Failed", e);
                }
                throw new LuceneIndexException("Index failed to prepare", e);
            }
            catch (Throwable t)
            {
                // If anything goes wrong we try and do a roll back
                rollback();
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName() + " Prepare Failed", t);
                }
                throw new LuceneIndexException("Prepared failed", t);                
            }
            finally
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName() + " Ending Prepare");
                }                
            }
        }
    }

    /**
     * Has this index been modified?
     * 
     * @return true if modified
     */
    public boolean isModified()
    {
        return indexUpdateStatus != IndexUpdateStatus.UNMODIFIED;
    }

    /**
     * Roll back the index changes (this just means they are never added)
     * 
     * @throws LuceneIndexException
     */
    public void rollback() throws LuceneIndexException
    {
        switch (getStatus().getStatus())
        {

        case Status.STATUS_COMMITTED:
            throw new IndexerException("Unable to roll back: Transaction is committed ");
        case Status.STATUS_ROLLING_BACK:
            throw new IndexerException("Unable to roll back: Transaction is rolling back");
        case Status.STATUS_ROLLEDBACK:
            throw new IndexerException("Unable to roll back: Transaction is already rolled back");
        case Status.STATUS_COMMITTING:
            // Can roll back during commit
        default:
            try
            {
                setStatus(TransactionStatus.ROLLINGBACK);
                doRollBack();
                setStatus(TransactionStatus.ROLLEDBACK);
            }
            catch (IOException e)
            {
                throw new LuceneIndexException("rollback failed ", e);
            }
            break;
        }
    }

    /**
     * Mark this index for roll back only. This action can not be reversed. It will reject all other work and only allow
     * roll back.
     */
    public void setRollbackOnly()
    {
        switch (getStatus().getStatus())
        {
        case Status.STATUS_COMMITTING:
            throw new IndexerException("Unable to mark for rollback: Transaction is committing");
        case Status.STATUS_COMMITTED:
            throw new IndexerException("Unable to mark for rollback: Transaction is committed");
        default:
            try
            {
                doSetRollbackOnly();
                setStatus(TransactionStatus.MARKED_ROLLBACK);
            }
            catch (IOException e)
            {
                throw new LuceneIndexException("Set rollback only failed ", e);
            }
            break;
        }
    }

    protected abstract void doPrepare() throws IOException;

    protected abstract void doCommit() throws IOException;

    protected abstract void doRollBack() throws IOException;

    protected abstract void doSetRollbackOnly() throws IOException;

    protected abstract List<Document> createDocuments(String stringNodeRef, FTSStatus ftsStatus, boolean indexAllProperties,
            boolean includeDirectoryDocuments);
    
    protected List<Document> readDocuments(final String stringNodeRef, final FTSStatus ftsStatus,
            final boolean indexAllProperties, final boolean includeDirectoryDocuments)
    {
        if (isReadThrough)
        {
            return transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionCallback<List<Document>>()
                    {
                        @Override
                        public List<Document> execute() throws Throwable
                        {
                            try
                            {
                                return createDocuments(stringNodeRef, ftsStatus, indexAllProperties,
                                        includeDirectoryDocuments);
                            }
                            catch (InvalidNodeRefException e)
                            {
                                // Turn InvalidNodeRefExceptions into retryable exceptions.
                                throw new ConcurrencyFailureException(
                                        "Possible cache integrity issue during reindexing", e);
                            }

                        }
                    }, true, true);
        }
        else
        {
            return createDocuments(stringNodeRef, ftsStatus, indexAllProperties, includeDirectoryDocuments);
        }
    }

    protected Set<String> deleteImpl(String nodeRef, IndexDeleteMode mode, boolean cascade,  IndexReader mainReader)
            throws LuceneIndexException, IOException

    {
        Set<String> leafrefs = new LinkedHashSet<String>();
        IndexReader deltaReader = null;

    	// startTimer();
        getDeltaReader();
        // outputTime("Delete "+nodeRef+" size = "+getDeltaWriter().docCount());
        Set<String> refs = new LinkedHashSet<String>();
        Set<String> containerRefs = new LinkedHashSet<String>();
        Set<String> temp = null;

        switch(mode)
        {
        case MOVE:
        	temp = deleteContainerAndBelow(nodeRef, getDeltaReader(), true, cascade);
            closeDeltaReader();
            containerRefs.addAll(temp);
            temp = deleteContainerAndBelow(nodeRef, mainReader, false, cascade);
            containerRefs.addAll(temp);
            
            temp = deletePrimary(containerRefs, getDeltaReader(), true);
            leafrefs.addAll(temp);
            closeDeltaReader();
            // May not have to delete references
            temp = deleteReference(containerRefs, getDeltaReader(), true);
            leafrefs.addAll(temp);
            closeDeltaReader();
            
            refs.addAll(containerRefs);
            refs.addAll(leafrefs);
            deletions.addAll(refs);
            // should not be included as a delete for optimisation in deletionsSinceFlush
            // should be optimised out
            // defensive against any issue with optimisation of events
            // the node has only moved - it still requires a real delete
            
            // make sure leaves are also removed from the delta before reindexing
            
            deltaReader = getDeltaReader();
            for(String id : leafrefs)
            {
                deltaReader.deleteDocuments(new Term("ID", id));
            }
            closeDeltaReader();
        	break;
        case REINDEX:
            temp = deleteContainerAndBelow(nodeRef, getDeltaReader(), true, cascade);
            closeDeltaReader();
            refs.addAll(temp);
            deletions.addAll(temp);
            // should not be included as a delete for optimisation in deletionsSinceFlush
            // should be optimised out
            // defensive against any issue with optimisation of events
            // the nodes have not been deleted and would require a real delete 
            temp = deleteContainerAndBelow(nodeRef, mainReader, false, cascade);
            refs.addAll(temp);
            deletions.addAll(temp);
            // should not be included as a delete for optimisation
            // should be optimised out
            // defensive agaainst any issue with optimisation of events
            // the nodes have not been deleted and would require a real delete 
            break;
        case DELETE:
            // if already deleted don't do it again ...
            if(deletionsSinceFlush.contains(nodeRef))
            {
                // nothing to do
                break;
            }
            else
            {
                // Delete all and reindex as they could be secondary links we have deleted and they need to be updated.
                // Most will skip any indexing as they will really have gone.
                temp = deleteContainerAndBelow(nodeRef, getDeltaReader(), true, cascade);
                closeDeltaReader();
                containerRefs.addAll(temp);
                refs.addAll(temp);
                temp = deleteContainerAndBelow(nodeRef, mainReader, false, cascade);
                containerRefs.addAll(temp);

                temp = deletePrimary(containerRefs, getDeltaReader(), true);
                leafrefs.addAll(temp);
                closeDeltaReader();
                temp = deletePrimary(containerRefs, mainReader, false);
                leafrefs.addAll(temp);
                
                // May not have to delete references
                temp = deleteReference(containerRefs, getDeltaReader(), true);
                leafrefs.addAll(temp);
                closeDeltaReader();
                temp = deleteReference(containerRefs, mainReader, false);
                leafrefs.addAll(temp);
               
                refs.addAll(containerRefs);
                refs.addAll(leafrefs);
                deletions.addAll(refs);
                // do not delete anything we have deleted before in this flush
                // probably OK to cache for the TX as a whole but done per flush => See ALF-8007 
                deletionsSinceFlush.addAll(refs);

                // make sure leaves are also removed from the delta before reindexing

                deltaReader = getDeltaReader();
                for(String id : leafrefs)
                {
                    deltaReader.deleteDocuments(new Term("ID", id));
                }
                closeDeltaReader();
                break;
            }
        }

        return refs;

    }

    protected void indexImpl(String nodeRef, boolean isNew) throws LuceneIndexException, IOException
    {
        IndexWriter writer = getDeltaWriter();

        // avoid attempting to index nodes that don't exist

        try
        {
            List<Document> docs = readDocuments(nodeRef, isNew ? FTSStatus.New : FTSStatus.Dirty, false, true);
            for (Document doc : docs)
            {
                try
                {
                    writer.addDocument(doc);
                }
                catch (IOException e)
                {
                    throw new LuceneIndexException("Failed to add document to index", e);
                }
            }
        }
        catch (InvalidNodeRefException e)
        {
            // The node does not exist
            return;
        }

    }

    void indexImpl(Set<String> refs, boolean isNew) throws LuceneIndexException, IOException
    {
        for (String ref : refs)
        {
            indexImpl(ref, isNew);
        }
    }

    protected void index(T ref) throws LuceneIndexException
    {
        addCommand(new Command<T>(ref, Action.INDEX));
    }

    protected void reindex(T ref, boolean cascadeReindexDirectories) throws LuceneIndexException
    {
        addCommand(new Command<T>(ref, cascadeReindexDirectories ? Action.CASCADEREINDEX : Action.REINDEX));
    }

    protected void delete(T ref) throws LuceneIndexException
    {
        addCommand(new Command<T>(ref, Action.DELETE));
    }

    protected void move(T ref) throws LuceneIndexException
    {
        addCommand(new Command<T>(ref, Action.MOVE));
    }
    
    private void addCommand(Command<T> command)
    {
        if (commandList.size() > 0)
        {
            Command<T> last = commandList.get(commandList.size() - 1);
            if ((last.action == command.action) && (last.ref.equals(command.ref)))
            {
                return;
            }
        }
        purgeCommandList(command);
        commandList.add(command);

        if (commandList.size() > getLuceneConfig().getIndexerBatchSize())
        {
            flushPending();
        }
    }

    private void purgeCommandList(Command<T> command)
    {
        if (command.action == Action.DELETE)
        {
            removeFromCommandList(command, false);
        }
        else if (command.action == Action.REINDEX)
        {
            removeFromCommandList(command, true);
        }
        else if (command.action == Action.INDEX)
        {
            removeFromCommandList(command, true);
        }
        else if (command.action == Action.CASCADEREINDEX)
        {
            removeFromCommandList(command, true);
        }
    }

    private void removeFromCommandList(Command<T> command, boolean matchExact)
    {
        for (ListIterator<Command<T>> it = commandList.listIterator(commandList.size()); it.hasPrevious(); /**/)
        {
            Command<T> current = it.previous();
            if (matchExact)
            {
                if (current.ref.equals(command.ref))
                {
                    if ((current.action == command.action))
                    {
                        it.remove();
                        return;
                    }
                    // If there is an INDEX in this same transaction and the current command is a reindex, remove it and
                    // replace the current command with it
                    else if (command.action != Action.DELETE && current.action == Action.INDEX)
                    {
                        it.remove();
                        command.action = Action.INDEX;
                    }
                }
            }
            else
            {
                if (current.ref.equals(command.ref))
                {
                    it.remove();
                }
            }
        }
    }

    /**
     * @throws LuceneIndexException
     */
    public void flushPending() throws LuceneIndexException
    {
        IndexReader mainReader = null;
        try
        {
            saveDelta();
            
            // Make sure the in flush deletion list is clear at the start
            deletionsSinceFlush.clear();
            if (commandList.isEmpty())
            {
                return;
            }
            
            mainReader = getReader();
            Set<String> forIndex = new LinkedHashSet<String>();

            for (Command<T> command : commandList)
            {
                if (command.action == Action.INDEX)
                {
                    // Indexing just requires the node to be added to the list
                    forIndex.add(command.ref.toString());
                }
                else if (command.action == Action.REINDEX)
                {
                    // Reindex is a delete and then and index
                    Set<String> set = deleteImpl(command.ref.toString(), IndexDeleteMode.REINDEX, false, mainReader);

                    // Deleting any pending index actions
                    // - make sure we only do at most one index
                    forIndex.removeAll(set);
                    // Add the nodes for index
                    forIndex.addAll(set);
                }
                else if (command.action == Action.CASCADEREINDEX)
                {
                    // Reindex is a delete and then and index
                    Set<String> set = deleteImpl(command.ref.toString(), IndexDeleteMode.REINDEX, true, mainReader);

                    // Deleting any pending index actions
                    // - make sure we only do at most one index
                    forIndex.removeAll(set);
                    // Add the nodes for index
                    forIndex.addAll(set);
                }
                else if (command.action == Action.DELETE)
                {
                    // Delete the nodes
                    Set<String> set = deleteImpl(command.ref.toString(), IndexDeleteMode.DELETE, true, mainReader);
                    // Remove any pending indexes
                    forIndex.removeAll(set);
                    // Add the leaf nodes for reindex
                    forIndex.addAll(set);
                }
                else if (command.action == Action.MOVE)
                {
                    // Delete the nodes
                    Set<String> set = deleteImpl(command.ref.toString(), IndexDeleteMode.MOVE, true, mainReader);
                    // Remove any pending indexes
                    forIndex.removeAll(set);
                    // Add the leaf nodes for reindex
                    forIndex.addAll(set);
                }
            }
            commandList.clear();
            indexImpl(forIndex, false);
            docs = getDeltaWriter().docCount();
            deletionsSinceFlush.clear();
        }
        catch (IOException e)
        {
            // If anything goes wrong we try and do a roll back
            throw new LuceneIndexException("Failed to flush index", e);
        }
        finally
        {
            if (mainReader != null)
            {
                try
                {
                    mainReader.close();
                }
                catch (IOException e)
                {
                    throw new LuceneIndexException("Filed to close main reader", e);
                }
            }
            // Make sure deletes are sent
            try
            {
                closeDeltaReader();
            }
            catch (IOException e)
            {

            }
            // Make sure writes and updates are sent.
            try
            {
                closeDeltaWriter();
            }
            catch (IOException e)
            {

            }
        }
    }

    /**
     * Are we deleting leaves only (not meta data)
     * 
     * @return - deleting only nodes.
     */
    public boolean getDeleteOnlyNodes()
    {
        return indexUpdateStatus == IndexUpdateStatus.ASYNCHRONOUS;
    }

    /**
     * Get the deletions
     * 
     * @return - the ids to delete
     */
    public Set<String> getDeletions()
    {
        return Collections.unmodifiableSet(deletions);
    }

    /**
     * Delete all entries from the index.
     */
    public void deleteAll()
    {
        deleteAll(null);
    }

    /**
     * Delete all index entries which do not start with the given prefix
     * 
     * @param prefix
     */
    public void deleteAll(String prefix)
    {
        IndexReader mainReader = null;
        try
        {
            mainReader = getReader();
            for (int doc = 0; doc < mainReader.maxDoc(); doc++)
            {
                if (!mainReader.isDeleted(doc))
                {
                    Document document = mainReader.document(doc);
                    String[] ids = document.getValues("ID");
                    if ((prefix == null) || nonStartwWith(ids, prefix))
                    {
                        deletions.add(ids[ids.length - 1]);
                        // should be included in the deletion cache if we move back to caching at the TX level and not the flush level
                        // Entries here will currently be ignored as the list is cleared at the start and end of a flush.
                        deletionsSinceFlush.add(ids[ids.length - 1]);
                    }
                }
            }

        }
        catch (IOException e)
        {
            // If anything goes wrong we try and do a roll back
            throw new LuceneIndexException("Failed to delete all entries from the index", e);
        }
        finally
        {
            if (mainReader != null)
            {
                try
                {
                    mainReader.close();
                }
                catch (IOException e)
                {
                    throw new LuceneIndexException("Filed to close main reader", e);
                }
            }
        }
    }

    private boolean nonStartwWith(String[] values, String prefix)
    {
        for (String value : values)
        {
            if (value.startsWith(prefix))
            {
                return false;
            }
        }
        return true;
    }

}
