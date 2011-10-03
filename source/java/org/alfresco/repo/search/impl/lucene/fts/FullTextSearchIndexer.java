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
package org.alfresco.repo.search.impl.lucene.fts;

import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * API for full text search indexing in the background
 * 
 * @author andyh
 */
public interface FullTextSearchIndexer extends BeanFactoryAware
{
    /**
     * Mark a store as dirty, requiring a background index update to fix it up.
     * 
     * @param storeRef
     */
    public abstract void requiresIndex(StoreRef storeRef);

    /**
     * Call back to report state back to the indexer
     * 
     * @param storeRef
     * @param remaining
     * @param e
     */
    public abstract void indexCompleted(StoreRef storeRef, int remaining, Throwable t);

    /**
     * Pause indexing 9no back ground indexing until a resume is called)
     * @throws InterruptedException
     */
    public abstract void pause() throws InterruptedException;

    /**
     * Resume after a pause
     * 
     * @throws InterruptedException
     */
    public abstract void resume() throws InterruptedException;

    /**
     * Do a chunk of outstanding indexing work
     *
     */
    public abstract void index();

}