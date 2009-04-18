/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.forms;

import java.util.List;

import org.alfresco.repo.forms.processor.FormProcessor;
import org.alfresco.repo.forms.processor.FormProcessorRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Form Service Implementation.
 * 
 * @author Gavin Cornwell
 */
public class FormServiceImpl implements FormService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(FormServiceImpl.class);
    
    /** Services */
    private FormProcessorRegistry processorRegistry;
    
    /**
     * Sets the FormProcessorRegistry
     * 
     * @param registry The FormProcessorRegistry instance to use
     */
    public void setProcessorRegistry(FormProcessorRegistry registry)
    {
        this.processorRegistry = registry;
    }

    /*
     * @see org.alfresco.repo.forms.FormService#getForm(org.alfresco.repo.forms.Item)
     */
    public Form getForm(Item item)
    {
        return getForm(item, null, null);
    }

    /*
     * @see org.alfresco.repo.forms.FormService#getForm(org.alfresco.repo.forms.Item, java.util.List)
     */
    public Form getForm(Item item, List<String> fields)
    {
        return getForm(item, fields, null);
    }

    /*
     * @see org.alfresco.repo.forms.FormService#getForm(org.alfresco.repo.forms.Item, java.util.List, java.util.List)
     */
    public Form getForm(Item item, List<String> fields, List<String> forcedFields)
    {
        if (this.processorRegistry == null)
        {
            throw new FormException("Property 'processorRegistry' has not been set.");
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Retrieving form for item: " + item);
        
        FormProcessor processor = this.processorRegistry.getApplicableFormProcessor(item);
        
        if (processor == null)
        {
            throw new FormException("Failed to find appropriate FormProcessor to generate Form for item: " + item);
        }
        else
        {
            return processor.generate(item, fields, forcedFields);
        }
    }
    
    /*
     * @see org.alfresco.repo.forms.FormService#saveForm(org.alfresco.repo.forms.Item, org.alfresco.repo.forms.FormData)
     */
    public void saveForm(Item item, FormData data)
    {
        if (this.processorRegistry == null)
        {
            throw new FormException("FormProcessorRegistry has not been setup");
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Saving form for item '" + item + "': " + data);
        
        FormProcessor processor = this.processorRegistry.getApplicableFormProcessor(item);
        
        if (processor == null)
        {
            throw new FormException("Failed to find appropriate FormProcessor to persist Form for item: " + item);
        }
        else
        {
            processor.persist(item, data);
        }
    }
}
