/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.thumbnail.script;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.ServiceRegistry;


/**
 * Script object representing the site service.
 * 
 * @author Roy Wetherall
 */
public class ScriptThumbnailService extends BaseScopableProcessorExtension
{
	/** Service Registry */
	private ServiceRegistry serviceRegistry;
	
    /**
     * Sets the Service Registry
     * 
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.serviceRegistry = serviceRegistry;
    }

    /**
     * Indicates whether a given thumbnail name has been registered.
     * 
     * @param  thumbnailName    thumbnail name
     * @return boolean          true if the thumbnail name is registered, false otherwise
     */
    public boolean isThumbnailNameRegistered(String thumbnailName)
    {
        return (this.serviceRegistry.getThumbnailService().getThumbnailRegistry().getThumbnailDefinition(thumbnailName) != null);
    }
    
    /**
     * Gets the resource path for the place holder thumbnail for the given named thumbnail.
     * 
     * Returns null if none set.
     * 
     * @param thumbnailName     the thumbnail name
     * @return String           the place holder thumbnail resource path, null if none set     
     */
    public String getPlaceHolderResourcePath(String thumbnailName)
    {
        String result = null;
        ThumbnailDefinition details = this.serviceRegistry.getThumbnailService().getThumbnailRegistry().getThumbnailDefinition(thumbnailName);
        if (details != null)
        {
            result = details.getPlaceHolderResourcePath();
        }
        return result;
    }

}
