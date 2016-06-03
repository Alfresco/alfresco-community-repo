package org.alfresco.repo.bulkimport.impl;

import org.alfresco.repo.bulkimport.MetadataLoader;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;

/**
 * 
 * @since 4.0
 *
 */
public class AbstractNodeImporterFactory
{
    protected FileFolderService fileFolderService;
    protected NodeService nodeService;
    protected MetadataLoader metadataLoader = null;
    protected BulkImportStatusImpl importStatus;
    protected VersionService versionService;
    protected BehaviourFilter behaviourFilter;
    
	public void setFileFolderService(FileFolderService fileFolderService)
	{
		this.fileFolderService = fileFolderService;
	}

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	public void setMetadataLoader(MetadataLoader metadataLoader)
	{
		this.metadataLoader = metadataLoader;
	}

	public void setImportStatus(BulkImportStatusImpl importStatus)
	{
		this.importStatus = importStatus;
	}

	public void setVersionService(VersionService versionService)
	{
		this.versionService = versionService;
	}

	public void setBehaviourFilter(BehaviourFilter behaviourFilter)
	{
		this.behaviourFilter = behaviourFilter;
	}
}
