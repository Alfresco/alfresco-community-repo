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