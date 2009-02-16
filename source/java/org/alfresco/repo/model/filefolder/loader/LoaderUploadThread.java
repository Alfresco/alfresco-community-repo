/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.util.List;

/**
 * Loader thread that puts documents to the remote repository.
 * 
 * @author Derek Hulley
 * @since 2.2
 */
public class LoaderUploadThread extends AbstractLoaderThread
{
    
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
