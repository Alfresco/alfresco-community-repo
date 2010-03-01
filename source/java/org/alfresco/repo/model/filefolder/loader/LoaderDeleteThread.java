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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.util.List;

/**
 * Loader thread that deletes documents to the remote repository.
 */
public class LoaderDeleteThread extends AbstractLoaderThread
{
    private FileInfo[] filesInfo;
    private int filesPerIteration;

    public LoaderDeleteThread(
            LoaderSession loaderSession,
            String loaderName,
            long testPeriod,
            long testTotal,
            long testLoadDepth,
            boolean verbose,
            long filesPerIteration)
    {
        super(loaderSession, loaderName, testPeriod, testTotal, testLoadDepth, verbose);
        this.filesPerIteration = (int) filesPerIteration;
    }

    @Override
    protected String doLoading(LoaderServerProxy loaderServerProxy, NodeRef nodeRef) throws Exception
    {
        // Delete it
        loaderServerProxy.fileFolderRemote.delete(loaderServerProxy.ticket, getNodesRef(filesInfo));

        // Done
        String msg = String.format("Deleted %d files from folder: %s", filesInfo.length, nodeRef.toString());

        return msg;
    }

    @Override
    protected void doBefore(LoaderServerProxy loaderServerProxy, NodeRef workingRootNodeRef) throws Exception
    {
        // Get a random folder
        List<String> folderPath = super.chooseFolderPath();

        //makeFolders
        NodeRef folderNodeRef = makeFolders(loaderServerProxy.ticket, loaderServerProxy, workingRootNodeRef, folderPath);

        byte[][] bytes = new byte[filesPerIteration][];
        String[] fileNames = new String[filesPerIteration];
        NodeRef[] parentNodeRefs = new NodeRef[filesPerIteration];
        QName[] types = new QName[filesPerIteration];

        // Build a set of files to delete
        for (int i = 0; i < filesPerIteration; i++)
        {
            File file = getFile();
            bytes[i] = FileCopyUtils.copyToByteArray(file);
            fileNames[i] = GUID.generate();
            parentNodeRefs[i] = folderNodeRef;
            types[i] = ContentModel.TYPE_CONTENT;
        }

        filesInfo = loaderServerProxy.fileFolderRemote.create(loaderServerProxy.ticket, parentNodeRefs, fileNames, types);
        
        loaderServerProxy.fileFolderRemote.putContent(loaderServerProxy.ticket, getNodesRef(filesInfo), bytes, fileNames);
    }

    NodeRef[] getNodesRef(FileInfo[] filesInfoList)
    {
        NodeRef[] nr = new NodeRef[filesInfoList.length];
        for (int i = 0; i < filesInfoList.length; i++)
        {
            nr[i] = (filesInfoList[i].getNodeRef());
        }
        return nr;
    }

    public String getSummary()
    {
        String summary = super.getSummary();
        summary += (String.format("%d files per iteration", filesPerIteration));
        return summary;
    }
}