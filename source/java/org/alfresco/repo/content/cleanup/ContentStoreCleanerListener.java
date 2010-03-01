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
package org.alfresco.repo.content.cleanup;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;

/**
 * A listener that can be plugged into a
 * {@link org.alfresco.repo.content.cleanup.ContentStoreCleaner cleaner} to
 * move pre-process any content that is about to be deleted from a store.
 * <p>
 * Implementations may backup the content or even perform scrubbing or obfuscation
 * tasks on the content.  In either case, this interface is called when the content
 * really will disappear i.e. there is no potential rollback of this operation.
 * 
 * @author Derek Hulley
 */
public interface ContentStoreCleanerListener
{
    /**
     * Handle the notification that a store is about to be deleted
     * 
     * @param sourceStore       the store from which the content will be deleted
     * @param contentUrl        the URL of the content to be deleted
     * 
     * @since 3.2
     */
    public void beforeDelete(ContentStore sourceStore, String contentUrl) throws ContentIOException;
}
