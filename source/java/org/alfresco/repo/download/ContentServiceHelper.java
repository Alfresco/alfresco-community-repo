package org.alfresco.repo.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * ContentServiceHelper interface.
 * 
 * Allows us to switch between the zip creation process updating content using a local content service 
 * and updating the content through a remote alfresco node.
 * 
 * @author amiller
 */
public interface ContentServiceHelper
{
    /**
     * Implementations should update the content of downlaodNode with contents of archiveFile.
     * 
     * @param downloadNode  NodeRef
     * @param archiveFile File
     * @throws ContentIOException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void updateContent(NodeRef downloadNode, File archiveFile) throws ContentIOException, FileNotFoundException, IOException;
}
