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
package org.alfresco.repo.node.index;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component to check and recover the indexes.
 * 
 * @deprecated    Deprecated as of 1.4.5.  Use {@linkplain IndexTransactionTracker}
 * 
 * @author Derek Hulley
 */
public class IndexRemoteTransactionTracker extends AbstractReindexComponent
{
    private static Log logger = LogFactory.getLog(IndexRemoteTransactionTracker.class);
    
    /**
     * Dumps an error message.
     */
    public IndexRemoteTransactionTracker()
    {
        logger.warn(
                "The component 'org.alfresco.repo.node.index.IndexRemoteTransactionTracker' " +
                "has been replaced by 'org.alfresco.repo.node.index.IndexTransactionTracker' \n" +
                "See the extension sample file 'index-tracking-context.xml.sample'. \n" +
                "See http://wiki.alfresco.com/wiki/High_Availability_Configuration_V1.4_to_V2.1#Lucene_Index_Synchronization.");
    }

    /**
     * As of release 1.4.5, 2.0.5 and 2.1.1, this property is no longer is use.
     */
    public void setRemoteOnly(boolean remoteOnly)
    {
    }

    @Override
    protected void reindexImpl()
    {
    }
}