/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
