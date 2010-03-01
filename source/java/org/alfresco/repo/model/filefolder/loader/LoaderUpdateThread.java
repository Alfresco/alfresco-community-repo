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
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.model.ContentModel;
import org.springframework.util.FileCopyUtils;

import java.util.List;
import java.io.File;

/**
 * Loader thread that updates documents to the remote repository.
 */

public class LoaderUpdateThread extends AbstractLoaderThread
{
    private FileInfo[] filesInfo;
    private int filesPerIteration;

    public LoaderUpdateThread(
            LoaderSession session,
            String loaderName,
            long testPeriod,
            long testTotal,
            long testLoadDepth,
            boolean verbose,
            long filesPerIteration)
    {
        super(session, loaderName, testPeriod, testTotal, testLoadDepth, verbose);
        this.filesPerIteration = (int)filesPerIteration;
    }

    @Override
    protected void doBefore(LoaderServerProxy loaderServerProxy, NodeRef nodeRef) throws Exception
    {
        // Get a random folder
        List<String> folderPath = super.chooseFolderPath();

        //makeFolders
        NodeRef folderNodeRef = makeFolders(loaderServerProxy.ticket, loaderServerProxy, nodeRef, folderPath);

        String[] fileNames = new String[filesPerIteration];
        NodeRef[] pareNodeRefs = new NodeRef[filesPerIteration];
        QName[] types = new QName[filesPerIteration];

        // Build a set of files to update
        for (int i = 0; i < filesPerIteration; i++)
        {
            fileNames[i] = GUID.generate();
            pareNodeRefs[i] = folderNodeRef;
            types[i] = ContentModel.TYPE_CONTENT;
        }
        filesInfo = loaderServerProxy.fileFolderRemote.create(loaderServerProxy.ticket, pareNodeRefs, fileNames, types);
    }

    @Override
    protected String doLoading(LoaderServerProxy loaderServerProxy, NodeRef nodeRef) throws Exception
    {
        byte[][] bytes = new byte[filesPerIteration][];
        String[] fileNames = new String[filesPerIteration];
        NodeRef[] nodeRefs = new NodeRef[filesPerIteration];

        for (int i = 0; i < filesPerIteration; i++)
        {
            File file = getFile();
            bytes[i] = FileCopyUtils.copyToByteArray(file);
            fileNames[i] = filesInfo[i].getName();
            nodeRefs[i] = filesInfo[i].getNodeRef();
        }

        //Update it
        loaderServerProxy.fileFolderRemote.putContent(loaderServerProxy.ticket, nodeRefs, bytes, fileNames);

        // Done
        return String.format("Updated %d files in folder: %s", filesInfo.length, nodeRef.toString());
    }


    public String getSummary()
    {
        String summary = super.getSummary();
        summary += (String.format("%d files per iteration", filesPerIteration));
        return summary;
    }
}