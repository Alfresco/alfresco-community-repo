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
package org.alfresco.repo.search;

import java.util.List;

import org.alfresco.repo.search.impl.lucene.AVMLuceneIndexer;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Andy
 *
 */
public interface AVMSnapShotTriggeredIndexingMethodInterceptor extends MethodInterceptor
{

    public final static QName PROP_SANDBOX_STAGING_MAIN = QName.createQName(null, ".sandbox.staging.main");

    public final static QName PROP_SANDBOX_STAGING_PREVIEW = QName.createQName(null, ".sandbox.staging.preview");

    public final static QName PROP_SANDBOX_AUTHOR_MAIN = QName.createQName(null, ".sandbox.author.main");

    public final static QName PROP_SANDBOX_AUTHOR_PREVIEW = QName.createQName(null, ".sandbox.author.preview");

    public final static QName PROP_SANDBOX_WORKFLOW_MAIN = QName.createQName(null, ".sandbox.workflow.main");

    public final static QName PROP_SANDBOX_WORKFLOW_PREVIEW = QName.createQName(null, ".sandbox.workflow.preview");

    public final static QName PROP_SANDBOX_AUTHOR_WORKFLOW_MAIN = QName.createQName(null, ".sandbox.author.workflow.main");

    public final static QName PROP_SANDBOX_AUTHOR_WORKFLOW_PREVIEW = QName.createQName(null, ".sandbox.author.workflow.preview");

    @SuppressWarnings("unchecked")
    public abstract Object invoke(MethodInvocation mi) throws Throwable;

    /**
     * Set the AVM service
     * 
     * @param avmService
     */
    public abstract void setAvmService(AVMService avmService);

    /**
     * Set the AVM indexer and searcher
     * 
     * @param indexerAndSearcher
     */
    public abstract void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher);

    /**
     * Enable or disable indexing
     * 
     * @param enableIndexing
     */
    public abstract void setEnableIndexing(boolean enableIndexing);

    /**
     * Set the index modes.... Strings of the form ... (ASYNCHRONOUS | SYNCHRONOUS | UNINDEXED):(NAME | TYPE):regexp
     * 
     * @param definitions
     */
    public abstract void setIndexingDefinitions(List<String> definitions);

    /**
     * Set the default index mode = used when there are no matches
     * 
     * @param defaultMode
     */
    public abstract void setDefaultMode(IndexMode defaultMode);

    /**
     * Is snapshot triggered indexing enabled
     * 
     * @return true if indexing is enabled for AVM
     */
    public abstract boolean isIndexingEnabled();

    /**
     * @param store
     * @param before
     * @param after
     */
    public abstract void indexSnapshot(String store, int before, int after);

    /**
     * @param store
     * @param after
     */
    public abstract void indexSnapshot(String store, int after);

    /**
     * @param store
     * @return - the last indexed snapshot
     */
    public abstract int getLastIndexedSnapshot(String store);

    /**
     * Is the snapshot applied to the index? Is there an entry for any node that was added OR have all the nodes in the
     * transaction been deleted as expected?
     * 
     * @param store
     * @param id
     * @return - true if applied, false if not
     */
    public abstract boolean isSnapshotIndexed(String store, int id);

    /**
     * Check if the index is up to date according to its index defintion and that all asynchronous work is done.
     * 
     * @param store
     * @return
     */
    public abstract boolean isIndexUpToDateAndSearchable(String store);

    /**
     * Check if the index is up to date according to its index defintion i it does not check that all asynchronous work
     * is done.
     * 
     * @param store
     * @return
     */
    public abstract boolean isIndexUpToDate(String store);

    /**
     * Given an avm store name determine if it is indexed and if so how.
     * 
     * @param store
     * @return
     */
    public abstract IndexMode getIndexMode(String store);

    public abstract boolean hasIndexBeenCreated(String store);

    public abstract void createIndex(String store);

    public abstract AVMLuceneIndexer getIndexer(String store);

    public abstract void deleteIndex(String store);

}