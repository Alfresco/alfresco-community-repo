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

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Listens for content that is about to be deleted and moves it into the store
 * configured as the backup store.
 * 
 * @author Derek Hulley
 */
public class DeletedContentBackupCleanerListener implements ContentStoreCleanerListener
{
    private static Log logger = LogFactory.getLog(DeletedContentBackupCleanerListener.class);
    
    private ContentStore store;
    
    public DeletedContentBackupCleanerListener()
    {
    }

    /**
     * Set the store to copy soon-to-be-deleted content into
     *  
     * @param store the deleted content backup store
     */
    public void setStore(ContentStore store)
    {
        this.store = store;
    }

    public void beforeDelete(ContentStore sourceStore, String contentUrl) throws ContentIOException
    {
        if (store.isContentUrlSupported(contentUrl))
        {
            ContentContext context = new ContentContext(null, contentUrl);
            ContentReader reader = sourceStore.getReader(contentUrl);
            if (!reader.exists())
            {
                // Nothing to copy over
                return;
            }
            // write the content into the target store
            ContentWriter writer = store.getWriter(context);
            // copy across
            writer.putContent(reader);
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Moved content before deletion: \n" +
                        "   URL:    " + contentUrl + "\n" +
                        "   Source: " + sourceStore + "\n" +
                        "   Target: " + store);
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Content cannot be moved during deletion.  A backup will not be made: \n" +
                        "   URL:    " + contentUrl + "\n" +
                        "   Source: " + sourceStore + "\n" +
                        "   Target: " + store);
            }
        }
    }
}
