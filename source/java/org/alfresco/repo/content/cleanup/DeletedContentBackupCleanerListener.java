/*
 * Copyright (C) 2005 Alfresco, Inc.
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
