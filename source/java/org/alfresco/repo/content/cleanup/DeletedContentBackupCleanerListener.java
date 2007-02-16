/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.cleanup;

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

    public void beforeDelete(ContentReader reader) throws ContentIOException
    {
        // write the content into the target store
        ContentWriter writer = store.getWriter(null, reader.getContentUrl());
        // copy across
        writer.putContent(reader);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Moved content before deletion: \n" +
                    "   URL: " + reader.getContentUrl() + "\n" +
                    "   Store: " + store);
        }
    }
}
