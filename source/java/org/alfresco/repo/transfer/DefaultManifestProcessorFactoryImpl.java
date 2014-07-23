/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

package org.alfresco.repo.transfer;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.requisite.TransferRequsiteWriter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.cmr.transfer.TransferReceiver;

/**
 * @author brian
 */
public class DefaultManifestProcessorFactoryImpl implements ManifestProcessorFactory
{
    private NodeService nodeService;
    private ContentService contentService;
    private DictionaryService dictionaryService;
    private PermissionService permissionService;
    private CorrespondingNodeResolverFactory nodeResolverFactory;
    private AlienProcessor alienProcessor;
    private CategoryService categoryService;
    private TaggingService taggingService;

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.transfer.ManifestProcessorFactory#getPrimaryCommitProcessor()
     */
    public List<TransferManifestProcessor> getCommitProcessors(TransferReceiver receiver, String transferId)
    {
        List<TransferManifestProcessor> processors = new ArrayList<TransferManifestProcessor>();
        CorrespondingNodeResolver nodeResolver = nodeResolverFactory.getResolver();
        
        RepoPrimaryManifestProcessorImpl primaryProcessor = new RepoPrimaryManifestProcessorImpl(receiver, transferId);
        primaryProcessor.setContentService(contentService);
        primaryProcessor.setNodeResolver(nodeResolver);
        primaryProcessor.setNodeService(nodeService);
        primaryProcessor.setDictionaryService(dictionaryService);
        primaryProcessor.setPermissionService(getPermissionService());
        primaryProcessor.setAlienProcessor(getAlienProcessor());
        primaryProcessor.setCategoryService(categoryService);
        primaryProcessor.setTaggingService(getTaggingService());
        processors.add(primaryProcessor);
        
        RepoSecondaryManifestProcessorImpl secondaryProcessor = new RepoSecondaryManifestProcessorImpl(receiver, transferId);
        secondaryProcessor.setNodeResolver(nodeResolver);
        secondaryProcessor.setNodeService(nodeService);
        processors.add(secondaryProcessor);
        
        RepoTertiaryManifestProcessorImpl tertiaryProcessor = new RepoTertiaryManifestProcessorImpl(receiver, transferId);
        tertiaryProcessor.setNodeService(nodeService);
        tertiaryProcessor.setAlienProcessor(getAlienProcessor());
        tertiaryProcessor.setNodeResolver(nodeResolver);
        processors.add(tertiaryProcessor);
        
        return processors;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService the contentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * @param dictionaryService
     *            the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param nodeResolverFactory the nodeResolverFactory to set
     */
    public void setNodeResolverFactory(CorrespondingNodeResolverFactory nodeResolverFactory)
    {
        this.nodeResolverFactory = nodeResolverFactory;
    }

    /**
     * 
     */
    public TransferManifestProcessor getRequsiteProcessor(
            TransferReceiver receiver, String transferId, TransferRequsiteWriter out)
    {
        RepoRequisiteManifestProcessorImpl processor = new RepoRequisiteManifestProcessorImpl(receiver, transferId, out);
       
        CorrespondingNodeResolver nodeResolver = nodeResolverFactory.getResolver();       
        processor.setNodeResolver(nodeResolver);
        processor.setNodeService(nodeService);
       
        return processor;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public PermissionService getPermissionService()
    {
        return permissionService;
    }

    public void setAlienProcessor(AlienProcessor alienProcessor)
    {
        this.alienProcessor = alienProcessor;
    }

    public AlienProcessor getAlienProcessor()
    {
        return alienProcessor;
    }

	public CategoryService getCategoryService()
    {
	    return categoryService;
    }

	public void setCategoryService(CategoryService categoryService)
    {
	    this.categoryService = categoryService;
    }

	public TaggingService getTaggingService()
    {
	    return taggingService;
    }

	public void setTaggingService(TaggingService taggingService)
    {
	    this.taggingService = taggingService;
    }

}
