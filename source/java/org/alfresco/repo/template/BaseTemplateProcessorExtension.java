/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.template;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.cmr.repository.TemplateProcessorExtension;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

/**
 * Abstract base class for a template extension implementation
 * 
 * @author Kevin Roast
 */
@AlfrescoPublicApi
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
