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
package org.alfresco.repo.processor;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.Processor;
import org.alfresco.service.cmr.repository.ProcessorExtension;
import org.alfresco.service.cmr.repository.ScriptProcessor;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.TemplateProcessor;
import org.alfresco.service.cmr.repository.TemplateService;

/**
 * Base class of a processor, encapsulates the implementation reguarding the registration of the processor
 * with the relevant services and the handling of processor extensions.
 * 
 * @author Roy Wetherall
 */
public abstract class BaseProcessor implements Processor
{
    /** The name of the processor */
    protected String name;
    
    /** The file extension that this processor understands */
    protected String extension;
    
    /** The script service */
    protected ScriptService scriptService;
    
    /** The template service */
    protected TemplateService templateService;
    
    /** The service registry */
    protected ServiceRegistry services;
    
    /** A map containing all the processor extenstions */
    protected Map<String, ProcessorExtension> processExtensions = new HashMap<String, ProcessorExtension>(10);
    
    /**
     * Registers this processor with the relevant services
     */
    public void register()
    {
        if (this instanceof ScriptProcessor)
        {
            scriptService.registerScriptProcessor((ScriptProcessor)this);
        }
        if (this instanceof TemplateProcessor)
        {
            templateService.registerTemplateProcessor((TemplateProcessor)this);
        }
    }
    
    /**
     * Sets the script service
     * 
     * @param scriptService the script service
     */
    public void setScriptService(ScriptService scriptService)
    {
        this.scriptService = scriptService;
    }
    
    /**
     * Sets the template service
     * 
     * @param templateService   the template service
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }
    
    /**
     * Sets the service registry
     * 
     * @param serviceRegistry   the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.services = serviceRegistry;
    }
    
    /**
     * Get the name of the processor
     * 
     * @return  String  the name of the processor
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Sets the name of the processor
     * 
     * @param name  the name of the processor
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Gets the extension that the processor understands
     * 
     * @return  String  the extension 
     */
    public String getExtension()
    {
        return extension;
    }
    
    /**
     * Sets the extenstion that the processor understands
     * 
     * @param extension     the extension
     */
    public void setExtension(String extension)
    {
        this.extension = extension;
    }
    
    /**
     * Registers a processor extension with the processor
     * 
     * @param processorExtension    the processor extension
     */
    public void registerProcessorExtension(ProcessorExtension processorExtension)
    {
        this.processExtensions.put(processorExtension.getExtensionName(), processorExtension);
    }
}
