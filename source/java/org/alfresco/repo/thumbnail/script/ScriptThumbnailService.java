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
package org.alfresco.repo.thumbnail.script;

import java.text.MessageFormat;
import java.util.Map;

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

    /**
     * Gets the resource path for the place holder thumbnail for the given named thumbnail and the given mime type.
     * If there is no icon available for the specified MIME type, a generic icon will be used instead.
     * The generic icon is that returned by {@link getPlaceHolderResourcePath(String)}
     * If neither a MIME-specific icon nor a generic icon is available, <code>null</code> is returned.
     * 
     * @param thumbnailName     the thumbnail name
     * @param mimetype          the mimetype of the piece of content.
     * @return String           the place holder thumbnail resource path
     * @see #getPlaceHolderResourcePath(String)
     */
    public String getMimeAwarePlaceHolderResourcePath(String thumbnailName, String mimetype)
    {
        String result = null;
        
        // Sanity check
        if (mimetype == null || mimetype.trim().length() == 0)
        {
            return getPlaceHolderResourcePath(thumbnailName);
        }
        
        Map<String, String> extensionsByMimetype = serviceRegistry.getMimetypeService().getExtensionsByMimetype();
        // We get the extension for the mime type directly from the Map as we need
        // to know if a mimetype is not recognised by the system. (The MimetypeService gives us ".bin"
        // for unrecognised mimetypes.)

        // The mime fragment is the part of the resource path that refers to the mime type
        // e.g. "_pdf" in alfresco/thumbnail/thumbnail_placeholder_doclib_pdf.png
        
        String extension = extensionsByMimetype.get(mimetype);
        
        if (extension != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("_").append(String.valueOf(extension));
            String mimeFragment = sb.toString();
            
            ThumbnailDefinition details = serviceRegistry.getThumbnailService().getThumbnailRegistry().getThumbnailDefinition(thumbnailName);
            if (details != null)
            {
                String resourcePath = details.getMimeAwarePlaceHolderResourcePath();
                if (resourcePath != null)
                {
                    // It's possible that the mimetype service recognises mime types for which we have no icon.
                    String formattedResourcePath = MessageFormat.format(resourcePath, mimeFragment);
                    boolean iconResourceExists = classpathResourceExists("/" + formattedResourcePath);
                    
                    if (iconResourceExists)
                    {
                        result = formattedResourcePath;
                    }
                }
            }
        }
        
        return result == null ? getPlaceHolderResourcePath(thumbnailName) : result;
    }
    
    /**
     * This method returns <code>true</code> if the specified classpath resource exists,
     * else <code>false</code>.
     */
    private boolean classpathResourceExists(String resourcePath)
    {
        boolean result = this.getClass().getResource(resourcePath) != null;
        
        return result;
    }
}
