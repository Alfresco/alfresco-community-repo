/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
