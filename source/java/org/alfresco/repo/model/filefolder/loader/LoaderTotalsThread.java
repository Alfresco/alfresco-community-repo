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
package org.alfresco.repo.model.filefolder.loader;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * A loader thread that merely reports the size of the remote repository.
 * @since 2.2
 * 
 * @author Derek Hulley
 */
public class LoaderTotalsThread extends AbstractLoaderThread
{
    public LoaderTotalsThread(
            LoaderSession session,
            String loaderName,
            int testPeriod,
            int testTotal,
            int testLoadDepth)
    {
        super(session, loaderName, testPeriod, testTotal, testLoadDepth);
    }

    /**
     * Gets the remote repository sizes and dumps those.
     */
    @Override
    protected String doLoading(LoaderServerProxy serverProxy, NodeRef workingRootNodeRef) throws Exception
    {
        return getTotalsMessage();
    }

    @Override
    public String getSummary()
    {
        return super.getSummary() + getTotalsMessage();
    }
    
    private String getTotalsMessage()
    {
        LoaderServerProxy serverProxy = session.getRemoteServers().get(0);
        StringBuilder sb = new StringBuilder();
        // Get total
        int totalNodeCount = serverProxy.loaderRemote.getNodeCount(
                serverProxy.ticket);
        sb.append(String.format("Total=%d", totalNodeCount));
        // Get totals for each store
        for (NodeRef nodeRef : session.getWorkingRootNodeRefs())
        {
            StoreRef storeRef = nodeRef.getStoreRef();
            int storeNodeCount = serverProxy.loaderRemote.getNodeCount(
                    serverProxy.ticket,
                    storeRef);
            sb.append(", ").append(storeRef.getIdentifier()).append("=").append(storeNodeCount);
        }
        // Done
        return sb.toString();
    }
}
