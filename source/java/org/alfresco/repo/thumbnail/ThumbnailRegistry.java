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
package org.alfresco.repo.thumbnail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.thumbnail.ThumbnailException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Registry of all the thumbnail details available
 * 
 * @author Roy Wetherall
 * @author Neil McErlean
 */
public class ThumbnailRegistry
{   
    /** Content service */
    private ContentService contentService;
    
    /** Rendition service */
    private RenditionService renditionService;
    
    private List<String> thumbnails;
    
    /** This flag indicates whether the thumbnail definitions have been lazily loaded or not. */
    private boolean thumbnailDefinitionsInited = false;
    
    /** Map of thumbnail definition */
    private Map<String, ThumbnailDefinition> thumbnailDefinitions = new HashMap<String, ThumbnailDefinition>();
    
    /** Cache to store mimetype to thumbnailDefinition mapping */
    private Map<String, List<ThumbnailDefinition>> mimetypeMap = new HashMap<String, List<ThumbnailDefinition>>(17);

    private ThumbnailRenditionConvertor thumbnailRenditionConvertor;
    
    public void setThumbnailRenditionConvertor(
            ThumbnailRenditionConvertor thumbnailRenditionConvertor)
    {
        this.thumbnailRenditionConvertor = thumbnailRenditionConvertor;
    }

    public ThumbnailRenditionConvertor getThumbnailRenditionConvertor()
    {
        return thumbnailRenditionConvertor;
    }
    
    /**
     * Content service
     * 
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Rendition service
     * 
     * @param renditionService    rendition service
     */
    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }

    public void setThumbnails(final List<String> thumbnails)
    {
    	this.thumbnails = thumbnails;
            
        // We'll not populate the data fields in the ThumbnailRegistry here, instead preferring
        // to do it lazily later.
    }
    
    /**
     * Get a list of all the thumbnail definitions
     * 
     * @return Collection<ThumbnailDefinition>  collection of thumbnail definitions
     */
    public List<ThumbnailDefinition> getThumbnailDefinitions()
    {
        if (thumbnailDefinitionsInited == false)
        {
            this.initThumbnailDefinitions();
            thumbnailDefinitionsInited = true;
        }
        return new ArrayList<ThumbnailDefinition>(this.thumbnailDefinitions.values());
    }
    
    private void initThumbnailDefinitions()
    {
        for (String thumbnailDefinitionName : this.thumbnails)
        {
            QName qName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, thumbnailDefinitionName);
            RenditionDefinition rAction = renditionService
                    .loadRenditionDefinition(qName);
            
            ThumbnailDefinition thDefn = thumbnailRenditionConvertor.convert(rAction);
            
            thumbnailDefinitions.put(thumbnailDefinitionName, thDefn);
        }
    }
    
    public List<ThumbnailDefinition> getThumbnailDefinitions(String mimetype)
    {
        if (thumbnailDefinitionsInited == false)
        {
            this.initThumbnailDefinitions();
            thumbnailDefinitionsInited = true;
        }

        List<ThumbnailDefinition> result = this.mimetypeMap.get(mimetype);
        
        if (result == null)
        {
            result = new ArrayList<ThumbnailDefinition>(7);
            
            for (ThumbnailDefinition thumbnailDefinition : this.thumbnailDefinitions.values())
            {
                if (this.contentService.getTransformer(
                        mimetype, 
                        thumbnailDefinition.getMimetype(), 
                        thumbnailDefinition.getTransformationOptions()) != null)
                {
                    result.add(thumbnailDefinition);
                }
            }
            
            this.mimetypeMap.put(mimetype, result);
        }
        
        return result;
    }
    
    /**
     * 
     * @param mimetype
     * @return
     * @deprecated Use {@link #getThumbnailDefinitions(String)} instead.
     */
    public List<ThumbnailDefinition> getThumnailDefintions(String mimetype)
    {
        return this.getThumbnailDefinitions(mimetype);
    }
    
    /**
     * Add a thumbnail details
     * 
     * @param thumbnailDetails  thumbnail details
     */
    public void addThumbnailDefinition(ThumbnailDefinition thumbnailDetails)
    {
        if (thumbnailDefinitionsInited == false)
        {
            this.initThumbnailDefinitions();
            thumbnailDefinitionsInited = true;
        }
        String thumbnailName = thumbnailDetails.getName();
        if (thumbnailName == null)
        {
            throw new ThumbnailException("When adding a thumbnail details object make sure the name is set.");
        }
        
        this.thumbnailDefinitions.put(thumbnailName, thumbnailDetails);
    }
    
    /**
     * Get the definition of a named thumbnail
     * 
     * @param  thumbnailNam         the thumbnail name
     * @return ThumbnailDetails     the details of the thumbnail
     */
    public ThumbnailDefinition getThumbnailDefinition(String thumbnailName)
    {
        if (thumbnailDefinitionsInited == false)
        {
            this.initThumbnailDefinitions();
            thumbnailDefinitionsInited = true;
        }
        return this.thumbnailDefinitions.get(thumbnailName);
    }
}
