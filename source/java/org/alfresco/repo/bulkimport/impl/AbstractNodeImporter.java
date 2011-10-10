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
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.bulkimport.BulkFilesystemImporter;
import org.alfresco.repo.bulkimport.DirectoryAnalyser;
import org.alfresco.repo.bulkimport.ImportableItem;
import org.alfresco.repo.bulkimport.MetadataLoader;
import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.repo.bulkimport.impl.BulkImportStatusImpl.NodeState;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Triple;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for the node importer, containing helper methods for use by subclasses.
 * 
 * @since 4.0
 *
 */
public abstract class AbstractNodeImporter implements NodeImporter
{
    protected final static Log logger = LogFactory.getLog(BulkFilesystemImporter.class);

    protected FileFolderService fileFolderService;
    protected NodeService nodeService;
    protected MetadataLoader metadataLoader = null;
    protected BulkImportStatusImpl importStatus;
    protected VersionService versionService;
    protected BehaviourFilter behaviourFilter;

    public void setVersionService(VersionService versionService)
	{
		this.versionService = versionService;
	}

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

	public void setBehaviourFilter(BehaviourFilter behaviourFilter)
	{
		this.behaviourFilter = behaviourFilter;
	}

    protected abstract NodeRef importImportableItemImpl(ImportableItem importableItem, boolean replaceExisting);
    protected abstract void importContentAndMetadata(NodeRef nodeRef, ImportableItem.ContentAndMetadata contentAndMetadata, MetadataLoader.Metadata metadata);

	/*
     * Because commons-lang ToStringBuilder doesn't seem to like unmodifiable Maps
     */
    protected final String mapToString(Map<?, ?> map)
    {
        StringBuffer result = new StringBuffer();
        
        if (map != null)
        {
            result.append('[');

            if (map.size() > 0)
            {
                for (Object key : map.keySet())
                {
                    result.append(String.valueOf(key));
                    result.append(" = ");
                    result.append(String.valueOf(map.get(key)));
                    result.append(",\n");
                }
                
                // Delete final dangling ", " value
                result.delete(result.length() - 2, result.length());
            }
            
            result.append(']');
        }
        else
        {
            result.append("(null)");
        }
        
        return(result.toString());
    }
    
    /**
     * Returns the name of the given importable item.  This is the final name of the item, as it would appear in the repository,
     * after metadata renames are taken into account.
     * 
     * @param importableItem The importableItem with which to 
     * @param metadata 
     * @return the name of the given importable item
     */
    protected final String getImportableItemName(ImportableItem importableItem, MetadataLoader.Metadata metadata)
    {
        String result = null;

        // Step 1: attempt to get name from metadata
        if (metadata != null)
        {
            result = (String)metadata.getProperties().get(ContentModel.PROP_NAME);
        }
        
        // Step 2: attempt to get name from metadata file
        if (result         == null &&
            importableItem != null &&
            importableItem.getHeadRevision() != null)
        {
            File metadataFile = importableItem.getHeadRevision().getMetadataFile();
            
            if (metadataFile != null)
            {
                final String metadataFileName = metadataFile.getName();
                
                result = metadataFileName.substring(0, metadataFileName.length() -
                                                       (MetadataLoader.METADATA_SUFFIX.length() + metadataLoader.getMetadataFileExtension().length()));
            }
        }
                
        return(result);
    }
    
    protected final int importImportableItemFile(NodeRef nodeRef, ImportableItem importableItem, MetadataLoader.Metadata metadata)
    {
    	int result = 0;

    	if (importableItem.hasVersionEntries())
    	{
    		// If cm:versionable isn't listed as one of the aspects for this node, add it - cm:versionable is required for nodes that have versions
    		if (!metadata.getAspects().contains(ContentModel.ASPECT_VERSIONABLE))
    		{
    			if (logger.isWarnEnabled()) logger.warn("Metadata for file '" + getFileName(importableItem.getHeadRevision().getContentFile()) + "' was missing the cm:versionable aspect, yet it has " + importableItem.getVersionEntries().size() + " versions.  Adding cm:versionable.");
    			metadata.addAspect(ContentModel.ASPECT_VERSIONABLE);
    		}

    		result = importContentVersions(nodeRef, importableItem);
    	}

    	if (logger.isDebugEnabled()) logger.debug("Creating head revision of node " + nodeRef.toString());

    	importContentAndMetadata(nodeRef, importableItem.getHeadRevision(), metadata);

    	return(result);
    }

    protected final int importContentVersions(NodeRef nodeRef, ImportableItem importableItem)
    {
    	int result = 0;

    	for (final ImportableItem.VersionedContentAndMetadata versionEntry : importableItem.getVersionEntries())
    	{
    		Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
    		MetadataLoader.Metadata   metadata          = loadMetadata(versionEntry);

    		importContentAndMetadata(nodeRef, versionEntry, metadata);

    		if (logger.isDebugEnabled()) logger.debug("Creating v" + String.valueOf(versionEntry.getVersion()) + " of node '" + nodeRef.toString() + "' (note: version label in Alfresco will not be the same - it is not currently possible to explicitly force a particular version label).");

    		// Note: PROP_VERSION_LABEL is a "reserved" property, and cannot be modified by custom code.
    		// In other words, we can't use the version label on disk as the version label in Alfresco.  :-(
    		// See: http://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=85
    		//versionProperties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), String.valueOf(versionEntry.getVersion()));
    		versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);   // Load every version as a major version for now - see http://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=84
    		versionService.createVersion(nodeRef, versionProperties);

    		result += metadata.getProperties().size() + 4;  // Add 4 for "standard" metadata properties read from filesystem
    	}

    	return(result);
    }

    protected final Triple<NodeRef, Boolean, NodeState> createOrFindNode(NodeRef target, ImportableItem importableItem,
    		boolean replaceExisting, MetadataLoader.Metadata metadata)
	{
    	Triple<NodeRef, Boolean, NodeState> result      = null;
    	boolean                             isDirectory = false;
    	NodeState                           nodeState   = replaceExisting ? NodeState.REPLACED : NodeState.SKIPPED;
    	String                              nodeName    = getImportableItemName(importableItem, metadata);
    	NodeRef                             nodeRef     = null;

    	//####TODO: handle this more elegantly
    	if (nodeName == null)
    	{
    		throw new IllegalStateException("Unable to determine node name for " + String.valueOf(importableItem));
    	}

    	if (logger.isDebugEnabled())
    	{
    		logger.debug("Searching for node with name '" + nodeName + "' within node '" + target.toString() + "'.");
    	}

    	nodeRef = fileFolderService.searchSimple(target, nodeName);

    	// If we didn't find an existing item, create a new node in the repo. 
    	if (nodeRef == null)
    	{
    		// But only if the content file exists - we don't create new nodes based on metadata-only importableItems
    		if (importableItem.getHeadRevision().contentFileExists())
    		{
    			isDirectory = ImportableItem.FileType.DIRECTORY.equals(importableItem.getHeadRevision().getContentFileType());

    			try
    			{
    				if (logger.isDebugEnabled()) logger.debug("Creating new node of type '" + metadata.getType().toString() + "' with name '" + nodeName + "' within node '" + target.toString() + "'.");
    				nodeRef   = fileFolderService.create(target, nodeName, metadata.getType()).getNodeRef();
    				nodeState = NodeState.CREATED;
    			}
    			catch (final FileExistsException fee)
    			{
    				if (logger.isWarnEnabled()) logger.warn("Node with name '" + nodeName + "' within node '" + target.toString() + "' was created concurrently to the bulk import.  Skipping importing it.", fee);
    				nodeRef   = null;
    				nodeState = NodeState.SKIPPED;
    			}
    		}
    		else
    		{
    			if (logger.isDebugEnabled()) logger.debug("Skipping creation of new node '" + nodeName + "' within node '" + target.toString() + "' since it doesn't have a content file.");
    			nodeRef   = null;
    			nodeState = NodeState.SKIPPED;
    		}
    	}
    	// We found the node in the repository.  Make sure we return the NodeRef, so that recursive loading works (we need the NodeRef of all sub-spaces, even if we didn't create them).
    	else
    	{
    		if (replaceExisting)
    		{
    			boolean targetNodeIsSpace = fileFolderService.getFileInfo(nodeRef).isFolder();

    			if (importableItem.getHeadRevision().contentFileExists())
    			{
    				// If the source file exists, ensure that the target node is of the same type (i.e. file or folder) as it. 
    				isDirectory = ImportableItem.FileType.DIRECTORY.equals(importableItem.getHeadRevision().getContentFileType());

    				if (isDirectory != targetNodeIsSpace)
    				{
    					if (logger.isWarnEnabled()) logger.warn("Skipping replacement of " + (isDirectory ? "Directory " : "File ") +
    							"'" + getFileName(importableItem.getHeadRevision().getContentFile()) + "'. " +
    							"The target node in the repository is a " + (targetNodeIsSpace ? "space node" : "content node") + ".");
    					nodeState = NodeState.SKIPPED;
    				}
    			}
    			else
    			{
    				isDirectory = targetNodeIsSpace;
    			}

    			if (nodeRef != null)
    			{
    				if (metadata.getType() != null)
    				{
    					// Finally, specialise the type.
    					if (logger.isDebugEnabled()) logger.debug("Specialising type of node '" + nodeRef.toString() + "' to '" + String.valueOf(metadata.getType()) + "'.");
    					nodeService.setType(nodeRef, metadata.getType());
    				}

    				nodeState = NodeState.REPLACED;
    			}
    		}
    		else
    		{
    			if (logger.isDebugEnabled()) logger.debug("Found content node '" + nodeRef.toString() + "', but replaceExisting=false, so skipping it.");
    			nodeState = NodeState.SKIPPED;
    		}
    	}

    	result = new Triple<NodeRef, Boolean, NodeState>(nodeRef, isDirectory, nodeState);

    	return(result);
	}
    
    protected String getFileName(File file)
    {
    	return FileUtils.getFileName(file);
    }
    
    protected final void importImportableItemMetadata(NodeRef nodeRef, File parentFile, MetadataLoader.Metadata metadata)
    {
    	// Attach aspects
    	if (metadata.getAspects() != null)
    	{
    		for (final QName aspect : metadata.getAspects())
    		{
    			if (logger.isDebugEnabled()) logger.debug("Attaching aspect '" + aspect.toString() + "' to node '" + nodeRef.toString() + "'.");

    			nodeService.addAspect(nodeRef, aspect, null);  // Note: we set the aspect's properties separately, hence null for the third parameter
    		}
    	}

    	// Set property values for both the type and any aspect(s)
    	if (metadata.getProperties() != null)
    	{
    		if (logger.isDebugEnabled()) logger.debug("Adding properties to node '" + nodeRef.toString() + "':\n" + mapToString(metadata.getProperties()));

    		try
    		{
    			nodeService.addProperties(nodeRef, metadata.getProperties());
    		}
    		catch (final InvalidNodeRefException inre)
    		{
    			if (!nodeRef.equals(inre.getNodeRef()))
    			{
    				// Caused by an invalid NodeRef in the metadata (e.g. in an association)
    				throw new IllegalStateException("Invalid nodeRef found in metadata for '" + getFileName(parentFile) + "'.  " +
    						"Probable cause: an association is being populated via metadata, but the " +
    						"NodeRef for the target of that association ('" + inre.getNodeRef() + "') is invalid.  " +
    						"Please double check your metadata file and try again.", inre);
    			}
    			else
    			{
    				// Logic bug in the BFSIT.  :-(
    				throw inre;
    			}
    		}
    	}
    }

    protected final void importImportableItemDirectory(NodeRef nodeRef, ImportableItem importableItem, MetadataLoader.Metadata metadata)
    {
    	if (importableItem.hasVersionEntries())
    	{
    		logger.warn("Skipping versions for directory '" + getFileName(importableItem.getHeadRevision().getContentFile()) + "' - Alfresco does not support versioned spaces.");
    	}

    	// Attach aspects and set all properties
    	importImportableItemMetadata(nodeRef, importableItem.getHeadRevision().getContentFile(), metadata);
    }

    protected final MetadataLoader.Metadata loadMetadata(ImportableItem.ContentAndMetadata contentAndMetadata)
    {
        MetadataLoader.Metadata result = new MetadataLoader.Metadata();
        
        // Load "standard" metadata from the filesystem
        if (contentAndMetadata != null && contentAndMetadata.contentFileExists())
        {
            final String filename = contentAndMetadata.getContentFile().getName().trim().replaceFirst(DirectoryAnalyser.VERSION_SUFFIX_REGEX, "");  // Strip off the version suffix (if any)
            final Date   modified = new Date(contentAndMetadata.getContentFile().lastModified());
            final Date   created  = modified;    //TODO: determine proper file creation time (awaiting JDK 1.7 NIO2 library)
            
            result.setType(ImportableItem.FileType.FILE.equals(contentAndMetadata.getContentFileType()) ? ContentModel.TYPE_CONTENT : ContentModel.TYPE_FOLDER);
            result.addProperty(ContentModel.PROP_NAME,     filename);
            result.addProperty(ContentModel.PROP_TITLE,    filename);
            result.addProperty(ContentModel.PROP_CREATED,  created);
            result.addProperty(ContentModel.PROP_MODIFIED, modified);
        }
            
        if (metadataLoader != null)
        {
            metadataLoader.loadMetadata(contentAndMetadata, result);
        }
        
        return(result);
    }

    public NodeRef importImportableItem(ImportableItem importableItem, boolean replaceExisting)
    {
        if(logger.isDebugEnabled())
        {
        	logger.debug("Importing " + String.valueOf(importableItem));
        }

        NodeRef nodeRef = importImportableItemImpl(importableItem, replaceExisting);

        // allow parent to be garbage collected
        //importableItem.setParent(null);
//        importableItem.clearParent();

    	importableItem.setNodeRef(nodeRef);

        return nodeRef;
    }
    
    protected void skipImportableDirectory(ImportableItem importableItem)
    {
        if (logger.isInfoEnabled())
    	{
        	logger.info("Skipping '" + getFileName(importableItem.getHeadRevision().getContentFile()) + "' as it already exists in the repository and 'replace existing' is false.");
    	}
        importStatus.incrementImportableItemsSkipped(importableItem, true);
    }

    protected void skipImportableFile(ImportableItem importableItem)
    {
        if (logger.isInfoEnabled())
    	{
        	logger.info("Skipping '" + getFileName(importableItem.getHeadRevision().getContentFile()) + "' as it already exists in the repository and 'replace existing' is false.");
    	}
        importStatus.incrementImportableItemsSkipped(importableItem, false);
    }
}
