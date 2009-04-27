/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
