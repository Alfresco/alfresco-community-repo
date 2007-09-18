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

import java.util.List;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A loader thread that retrieves the folders beneath each directory from
 * the root.  This is an expensive process but should reach a stable execution time
 * once the folders in the profile have all been created.
 * @since 2.2
 * 
 * @author Derek Hulley
 */
public class LoaderListFoldersThread extends AbstractLoaderThread
{
    public LoaderListFoldersThread(
            LoaderSession session,
            String loaderName,
            long testPeriod,
            long testTotal,
            long testLoadDepth)
    {
        super(session, loaderName, testPeriod, testTotal, testLoadDepth);
    }

    /**
     * Go to a directory and get a listing of the folders beneath it.
     */
    @Override
    protected String doLoading(LoaderServerProxy serverProxy, NodeRef workingRootNodeRef) throws Exception
    {
        int count = listFoldersRecursive(serverProxy, workingRootNodeRef, 0);
        
        // Done
        String msg = String.format("Found %d folders below node %s", count, workingRootNodeRef.toString());
        return msg;
    }

    /**
     * Recursive method to list all folders in the hierarchy.
     * @return          Returns the number of folders listed
     */
    private int listFoldersRecursive(LoaderServerProxy serverProxy, NodeRef parentNodeRef, int count)
    {
        List<FileInfo> fileInfos = serverProxy.fileFolderRemote.listFolders(
                serverProxy.ticket,
                parentNodeRef);
        for (FileInfo info : fileInfos)
        {
            if (!info.isFolder())
            {
                // Ooops
                continue;
            }
            count += listFoldersRecursive(serverProxy, info.getNodeRef(), count);
        }
        return count;
    }
}
