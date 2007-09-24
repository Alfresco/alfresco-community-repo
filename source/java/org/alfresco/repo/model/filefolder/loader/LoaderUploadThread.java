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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
        System.setProperty(CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY, "TRUE");
        URL url = LoaderUploadThread.class.getResource("/org/alfresco/repo/model/filefolder/loader/loader-ehcache.xml");
        CacheManager cacheManager = new CacheManager(url);
        Cache cache = cacheManager.getCache("org.alfresco.LoaderUploadThread.PathCache");
        
        pathCache = new EhCacheAdapter<String, NodeRef>();
        pathCache.setCache(cache);
    }
    
    private int filesPerUpload;
    
    public LoaderUploadThread(
            LoaderSession session,
            String loaderName,
            long testPeriod,
            long testTotal,
            long testLoadDepth,
            boolean verbose,
            long filesPerUpload)
    {
        super(session, loaderName, testPeriod, testTotal, testLoadDepth, verbose);
        this.filesPerUpload = (int) filesPerUpload;
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
        String currentKey = workingRootNodeRef.toString();
        for (String pathElement : folderPath)
        {
            currentPath.add(pathElement);
            currentKey += ("/" + pathElement);
            // Is this there?
            NodeRef nodeRef = pathCache.get(currentKey);
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
                    Collections.singletonList(pathElement),
                    ContentModel.TYPE_FOLDER);
            currentParentNodeRef = folderInfo.getNodeRef();
            // Cache the new node
            pathCache.put(currentKey, currentParentNodeRef);
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

        // Build a set of files to upload
        byte[][] bytes = new byte[filesPerUpload][];
        String[] filenames = new String[filesPerUpload];
        for (int i = 0; i < filesPerUpload; i++)
        {
            // Get a random file
            File file = getFile();
            bytes[i] = FileCopyUtils.copyToByteArray(file);
            // Get the extension
            filenames[i] = GUID.generate();
            int index = file.getName().lastIndexOf('.');
            if (index > 0)
            {
                String ext = file.getName().substring(index + 1, file.getName().length());
                filenames[i] += ("." + ext);
            }
        }
        
        // Upload it
        FileInfo[] fileInfos = serverProxy.loaderRemote.uploadContent(
                serverProxy.ticket,
                folderNodeRef,
                filenames,
                bytes);
        
        // Done
        String msg = String.format("Uploaded %d files to folder: %s", fileInfos.length, folderPath.toString());
        return msg;
    }

    @Override
    public String getSummary()
    {
        String summary = super.getSummary();
        summary += (String.format("%d files per iteration", filesPerUpload));
        return summary;
    }
}
