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
package org.alfresco.repo.forms.script;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormService;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Script object representing the form service.
 * 
 * @author Neil McErlean
 */
public class ScriptFormService extends BaseScopableProcessorExtension
{
    private static Log logger = LogFactory.getLog(ScriptFormService.class);
    /** Service Registry */
    private ServiceRegistry serviceRegistry;

    /** The site service */
    private FormService formService;

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
     * Set the form service
     * 
     * @param formService
     *            the form service
     */
    public void setFormService(FormService formService)
    {
        this.formService = formService;
    }

    /**
     * Returns the form for the given item
     * 
     * @param item The item to retrieve a form for
     * @return The form
     */
    public ScriptForm getForm(String item)
    {
        Form result = formService.getForm(item);
        return result == null ? null : new ScriptForm(result);
    }
    
    /**
     * Persists the given data object for the item provided
     * 
     * @param item The item to persist the data for
     * @param postData The post data, this can be a Map of name value
     *                 pairs, a webscript FormData object or a JSONObject
     */
    public void saveForm(String item, Object postData)
    {
        // A note on data conversion as passed in to this method:
        // Each of the 3 submission methods (multipart/formdata, JSON Post and
        // application/x-www-form-urlencoded) pass an instance of FormData into this
        // method.
        FormData dataForFormService = null;
        if (postData instanceof FormData)
        {
            dataForFormService = (FormData)postData;
            // A note on data conversion as passed out of this method:
            // The Repo will handle conversion of String-based data into the types
            // required by the model.
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("ScriptFormService.saveForm: postData not instanceof FormData.");
            }
            return;
        }
       
        formService.saveForm(item, dataForFormService);
    }
}
