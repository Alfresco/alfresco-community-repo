/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * ContentServiceHelper interface.
 * 
 * Allows us to switch between the zip creation process updating content using a local content service and updating the content through a remote alfresco node.
 * 
 * @author amiller
 */
public interface ContentServiceHelper
{
    /**
     * Implementations should update the content of downlaodNode with contents of archiveFile.
     * 
     * @param downloadNode
     *            NodeRef
     * @param archiveFile
     *            File
     * @throws ContentIOException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void updateContent(NodeRef downloadNode, File archiveFile) throws ContentIOException, FileNotFoundException, IOException;
}
