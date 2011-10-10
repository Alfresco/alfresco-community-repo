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

import java.io.File;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.bulkimport.ImportableItem;
import org.alfresco.repo.bulkimport.MetadataLoader;
import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.repo.bulkimport.impl.BulkImportStatusImpl.NodeState;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Triple;

/**
 * 
 * @since 4.0
 *
 */
public class StreamingNodeImporterFactory extends AbstractNodeImporterFactory
{
	public NodeImporter getNodeImporter(File sourceFolder)
	{
		StreamingNodeImporter nodeImporter = new StreamingNodeImporter();
		nodeImporter.setNodeService(nodeService);
		nodeImporter.setBehaviourFilter(behaviourFilter);
		nodeImporter.setFileFolderService(fileFolderService);
		nodeImporter.setMetadataLoader(metadataLoader);
		nodeImporter.setVersionService(versionService);
		nodeImporter.setImportStatus(importStatus);

		nodeImporter.setSourceFolder(sourceFolder);

		return nodeImporter;
	}
	
	/**
	 * 
	 * @since 4.0
	 *
	 */
	private static class StreamingNodeImporter extends AbstractNodeImporter
	{
	    private File sourceFolder;

		public void setSourceFolder(File sourceFolder)
		{
			this.sourceFolder = sourceFolder;
		}
		
	    protected final void importContentAndMetadata(NodeRef nodeRef, ImportableItem.ContentAndMetadata contentAndMetadata, MetadataLoader.Metadata metadata)
	    {
	    	// Write the content of the file
	    	if (contentAndMetadata.contentFileExists())
	    	{
	    		String filename = getFileName(contentAndMetadata.getContentFile());

	    		if (logger.isDebugEnabled())
				{
	    			logger.debug("Streaming contents of file '" + filename + "' into node '" + nodeRef.toString() + "'.");
				}

	    		ContentWriter writer = fileFolderService.getWriter(nodeRef);
	    		writer.putContent(contentAndMetadata.getContentFile());
	    	}
	    	else
	    	{
	    		if (logger.isDebugEnabled()) logger.debug("No content to stream into node '" + nodeRef.toString() + "' - importing metadata only.");
	    	}

	    	// Attach aspects and set all properties
	    	importImportableItemMetadata(nodeRef, contentAndMetadata.getContentFile(), metadata);
	    }

	    protected NodeRef importImportableItemImpl(ImportableItem importableItem, boolean replaceExisting)
	    {
	        NodeRef target = importableItem.getParent().getNodeRef();
	        if(target == null)
	        {
	        	// the parent has not been created yet, retry
	        	throw new AlfrescoRuntimeException("Bulk importer: target is not known for importable item: " + importableItem.getParent());
	        }
	        NodeRef result = null;
	        MetadataLoader.Metadata metadata = loadMetadata(importableItem.getHeadRevision());

	        Triple<NodeRef, Boolean, NodeState> node = createOrFindNode(target, importableItem, replaceExisting, metadata);
	        boolean isDirectory = node.getSecond() == null ? false : node.getSecond();  // Watch out for NPEs during unboxing!
	        NodeState nodeState = node.getThird();
	        
	        result = node.getFirst();

	        if (result != null && nodeState != NodeState.SKIPPED)
	        {
	            int numVersionProperties = 0;

	            importStatus.incrementImportableItemsRead(importableItem, isDirectory);

	            // Load the item
	            if (isDirectory)
	            {
	                importImportableItemDirectory(result, importableItem, metadata);
	            }
	            else
	            {
	                numVersionProperties = importImportableItemFile(result, importableItem, metadata);
	            }
	            
	            importStatus.incrementNodesWritten(importableItem, isDirectory, nodeState, metadata.getProperties().size() + 4, numVersionProperties);
	            importStatus.incrementContentBytesWritten(importableItem, isDirectory, nodeState);
	        }
	        else
	        {
	        	if(isDirectory)
	        	{
	        		skipImportableDirectory(importableItem);
	        	}
	        	else
	        	{
	        		skipImportableFile(importableItem);
	        	}
	        }

	        return(result);
	    }

		@Override
		public File getSourceFolder()
		{
			return sourceFolder;
		}
	}
}
