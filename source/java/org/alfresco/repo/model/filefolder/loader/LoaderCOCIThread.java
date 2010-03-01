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

import java.util.*;
import java.io.Serializable;
import java.io.File;

/**
 * Loader thread that coci documents to the remote repository.
 */

public class LoaderCOCIThread extends AbstractLoaderThread
{
    private int filesPerIteration;
    private FileInfo[] filesInfo;

    public LoaderCOCIThread(LoaderSession session, String loaderName, long testPeriod, long testTotal, long testLoadDepth, boolean verbose, long filesPerIteration)
    {
        super(session, loaderName, testPeriod, testTotal, testLoadDepth, verbose);
        this.filesPerIteration = (int) filesPerIteration;
    }

    @Override
    protected String doLoading(LoaderServerProxy serverProxy, NodeRef workingRootNodeRef) throws Exception
    {

        List<HashMap<String, Serializable>> arrVersionProp = new ArrayList<HashMap<String, Serializable>>();

        byte[][] bytes = new byte[filesPerIteration][];
        for (int i = 0; i < filesPerIteration; i++)
        {
            File file = getFile();
            bytes[i] = FileCopyUtils.copyToByteArray(file);
            arrVersionProp.add(new HashMap<String, Serializable>());
        }

        serverProxy.loaderRemote.coci(serverProxy.ticket, getNodesRef(filesInfo), bytes, arrVersionProp);

        return String.format("update version %d files in folder: %s", filesPerIteration, workingRootNodeRef.toString());
    }

    @Override
    protected void doBefore(LoaderServerProxy loaderServerProxy, NodeRef nodeRef) throws Exception
    {
        // Get a random folder
        List<String> folderPath = super.chooseFolderPath();

        //makeFolders
        NodeRef folderNodeRef = makeFolders(loaderServerProxy.ticket, loaderServerProxy, nodeRef, folderPath);

        String[] fileNames = new String[filesPerIteration];
        NodeRef[] parentNodeRefs = new NodeRef[filesPerIteration];
        QName[] types = new QName[filesPerIteration];

        // Build a set of files to coci
        for (int i = 0; i < filesPerIteration; i++)
        {
            fileNames[i] = GUID.generate();
            parentNodeRefs[i] = folderNodeRef;
            types[i] = ContentModel.TYPE_CONTENT;
        }

        filesInfo = loaderServerProxy.fileFolderRemote.create(loaderServerProxy.ticket, parentNodeRefs, fileNames, types);
    }


    public String getSummary()
    {
        String summary = super.getSummary();
        summary += String.format("%d files per iteration", filesPerIteration);
        return summary;
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
}
