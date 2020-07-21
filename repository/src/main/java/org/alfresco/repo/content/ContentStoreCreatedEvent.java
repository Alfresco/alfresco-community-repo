/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.content;

import java.io.Serializable;
import java.util.Map;

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
    protected transient Map<String, Serializable> extendedEventParams;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source content store
     */
    public ContentStoreCreatedEvent(ContentStore source, Map<String, Serializable> extendedEventParams)
    {
        super(source);
        this.extendedEventParams = extendedEventParams;
    }
    
    /**
     * @return      Returns the source {@link ContentStore}
     */
    public ContentStore getContentStore()
    {
        return (ContentStore) getSource();
    }
    
    public Map<String, Serializable> getExtendedEventParams()
    {
    	return extendedEventParams;
    }
}
