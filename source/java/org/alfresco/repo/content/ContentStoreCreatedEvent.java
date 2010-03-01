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
package org.alfresco.repo.content;

import org.alfresco.repo.content.filestore.FileContentStore;
import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of the existence of a {@link FileContentStore}. Useful for Monitoring
 * purposes.
 * 
 * @author dward
 * @since 3.1
 */
public class ContentStoreCreatedEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 7090069096441126707L;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source content store
     * @param rootDirectory
     *            the root directory
     */
    public ContentStoreCreatedEvent(ContentStore source)
    {
        super(source);
    }
    
    /**
     * @return      Returns the source {@link ContentStore}
     */
    public ContentStore getContentStore()
    {
        return (ContentStore) getSource();
    }
}
