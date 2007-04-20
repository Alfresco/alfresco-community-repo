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

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.cmr.repository.TemplateProcessorExtension;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

/**
 * Abstract base class for a template extension implementation
 * 
 * @author Kevin Roast
 */
public abstract class BaseTemplateProcessorExtension extends BaseProcessorExtension implements TemplateProcessorExtension
{   
    /** The TemplateImageResolver for the current template execution thread */
    private ThreadLocal<TemplateImageResolver> resolver = new ThreadLocal<TemplateImageResolver>();
    
    /**
     * @see org.alfresco.service.cmr.repository.TemplateProcessorExtension#setTemplateImageResolver(org.alfresco.service.cmr.repository.TemplateImageResolver)
     */
    public void setTemplateImageResolver(TemplateImageResolver resolver)
    {
        this.resolver.set(resolver);
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.TemplateProcessorExtension#getTemplateImageResolver()
     */
    public TemplateImageResolver getTemplateImageResolver()
    {
        return this.resolver.get();
    }
}
