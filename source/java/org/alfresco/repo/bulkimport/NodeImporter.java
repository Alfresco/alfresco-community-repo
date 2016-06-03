package org.alfresco.repo.bulkimport;

import java.io.File;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Imports an importable item in the filesystem into the repository by creating a node to represent it.
 * 
 * @since 4.0
 *
 */
public interface NodeImporter
{
    public NodeRef importImportableItem(ImportableItem importableItem, boolean replaceExisting);
    public File getSourceFolder();
}
