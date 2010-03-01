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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.thumbnail.ThumbnailException;

/**
 * Registry of all the thumbnail details available
 * 
 * @author Roy Wetherall
 */
public class ThumbnailRegistry
{   
    /** Content service */
    private ContentService contentService;
    
    /** Map of thumbnail defintion */
    private Map<String, ThumbnailDefinition> thumbnailDefinitions = new HashMap<String, ThumbnailDefinition>(7);
    
    /** Cache to store mimetype to thumbnailDefinition mapping */
    private Map<String, List<ThumbnailDefinition>> mimetypeMap = new HashMap<String, List<ThumbnailDefinition>>(17);
     
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
     * Add a number of thumbnail defintions
     * 
     * @param thumbnailDefinitions  list of thumbnail details
     */
    public void setThumbnailDefinitions(List<ThumbnailDefinition> thumbnailDefinitions)
    {
        for (ThumbnailDefinition value : thumbnailDefinitions)
        {
            addThumbnailDefinition(value);
        }
    }
    
    /**
     * Get a list of all the thumbnail defintions
     * 
     * @return Collection<ThumbnailDefinition>  colleciton of thumbnail defintions
     */
    public List<ThumbnailDefinition> getThumbnailDefinitions()
    {
        return new ArrayList<ThumbnailDefinition>(this.thumbnailDefinitions.values());
    }
    
    /**
     * 
     * @param mimetype
     * @return
     */
    public List<ThumbnailDefinition> getThumnailDefintions(String mimetype)
    {
        List<ThumbnailDefinition> result = this.mimetypeMap.get(mimetype);;
        
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
     * Add a thumnail details
     * 
     * @param thumbnailDetails  thumbnail details
     */
    public void addThumbnailDefinition(ThumbnailDefinition thumbnailDetails)
    {
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
        return this.thumbnailDefinitions.get(thumbnailName);
    }
}
