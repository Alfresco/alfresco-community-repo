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
            long testPeriod,
            long testTotal,
            long testLoadDepth,
            boolean verbose)
    {
        super(session, loaderName, testPeriod, testTotal, testLoadDepth, verbose);
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
