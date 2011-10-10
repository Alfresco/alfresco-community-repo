/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
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
