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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.template;

import org.alfresco.service.cmr.repository.TemplateExtensionImplementation;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateService;

/**
 * Abstract base class for a template extension implementation
 * 
 * @author Kevin Roast
 */
public abstract class BaseTemplateExtensionImplementation implements TemplateExtensionImplementation
{
    /** The template service instance */
    private TemplateService templateService;
    
    /** The name of the template extension */
    private String extensionName;
    
    /** The TemplateImageResolver for the current template execution thread */
    private ThreadLocal<TemplateImageResolver> resolver = new ThreadLocal<TemplateImageResolver>();
    
    
    /**
     * @param templateService   The TemplateService to set.
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.TemplateExtensionImplementation#setTemplateImageResolver(org.alfresco.service.cmr.repository.TemplateImageResolver)
     */
    public void setTemplateImageResolver(TemplateImageResolver resolver)
    {
        this.resolver.set(resolver);
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.TemplateExtensionImplementation#getTemplateImageResolver()
     */
    public TemplateImageResolver getTemplateImageResolver()
    {
        return this.resolver.get();
    }

    /**
     * Registers this template extension with the Template Service
     */
    public void register()
    {
        this.templateService.registerExtension(this);
    }
    
    /**
     * Returns the name of the template extension 
     * 
     * @return the name of the template extension
     */
    public String getExtensionName()
    {
        return extensionName;
    }

    /**
     * @param extensionName     The template extension name.
     */
    public void setExtensionName(String extensionName)
    {
        this.extensionName = extensionName;
    }
}
