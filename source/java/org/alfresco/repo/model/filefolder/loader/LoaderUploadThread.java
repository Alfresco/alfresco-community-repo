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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.EhCacheAdapter;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.springframework.util.FileCopyUtils;

/**
 * Loader thread that puts documents to the remote repository.
 * 
 * @author Derek Hulley
 * @since 2.2
 */
public class LoaderUploadThread extends AbstractLoaderThread
{
    private static EhCacheAdapter<String, NodeRef> pathCache;
    
    static
    {
        CacheManager cacheManager = new CacheManager();
        Cache cache = new Cache("LoaderUploadThread-PathCache", 10000, false, false, 300, 300);
        cacheManager.addCache(cache);
        
        pathCache = new EhCacheAdapter<String, NodeRef>();
        pathCache.setCache(cache);
    }
    
    public LoaderUploadThread(
            LoaderSession session,
            String loaderName,
            long testPeriod,
            long testTotal,
            long testLoadDepth)
    {
        super(session, loaderName, testPeriod, testTotal, testLoadDepth);
    }

    /**
     * Creates or find the folders based on caching.
     */
    private NodeRef makeFolders(
            String ticket,
            LoaderServerProxy serverProxy,
            NodeRef workingRootNodeRef,
            List<String> folderPath) throws Exception
    {
        // Iterate down the path, checking the cache and populating it as necessary
        List<String> currentPath = new ArrayList<String>();
        NodeRef currentParentNodeRef = workingRootNodeRef;
        for (String pathElement : currentPath)
        {
            currentPath.add(pathElement);
            String key = currentPath.toString();
            // Is this there?
            NodeRef nodeRef = pathCache.get(key);
            if (nodeRef != null)
            {
                // Found it
                currentParentNodeRef = nodeRef;
                // Step into the next level
                continue;
            }
            // It is not there, so create it
            FileInfo folderInfo = serverProxy.fileFolderRemote.makeFolders(
                    serverProxy.ticket,
                    currentParentNodeRef,
                    currentPath,
                    ContentModel.TYPE_FOLDER);
            currentParentNodeRef = folderInfo.getNodeRef();
            // Cache the new node
            pathCache.put(key, currentParentNodeRef);
        }
        // Done
        return currentParentNodeRef;
    }
    
    @Override
    protected String doLoading(LoaderServerProxy serverProxy, NodeRef workingRootNodeRef) throws Exception
    {
        // Get a random folder
        List<String> folderPath = super.chooseFolderPath();
        // Make sure the folder exists
        NodeRef folderNodeRef = makeFolders(serverProxy.ticket, serverProxy, workingRootNodeRef, folderPath);

        // Get a random file
        File file = getFile();
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        // Get the extension
        String filename = GUID.generate();
        int index = file.getName().lastIndexOf('.');
        if (index > 0)
        {
            String ext = file.getName().substring(index + 1, file.getName().length());
            filename += ("." + ext);
        }
        
        // Upload it
        FileInfo fileInfo = serverProxy.fileFolderRemote.create(
                serverProxy.ticket,
                folderNodeRef,
                filename,
                ContentModel.TYPE_CONTENT);
        NodeRef fileNodeRef = fileInfo.getNodeRef();
        serverProxy.fileFolderRemote.putContent(serverProxy.ticket, fileNodeRef, bytes, filename);
        
        // Done
        String msg = String.format("Uploaded %s to folder: %s", filename, folderPath.toString());
        return msg;
    }
}
