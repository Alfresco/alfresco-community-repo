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
package org.alfresco.repo.web.scripts.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * 
 * Webscript to get the Propertydefinitions for a given classname eg. =>cm_person
 * 
 * @author Saravanan Sellathurai
 */

public class PropertiesGet extends DictionaryWebServiceBase
{
	private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";
	private static final String DICTIONARY_CLASS_NAME = "classname";
	private static final String REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX = "nsp";
    
	/**
     * @Override  method from DeclarativeWebScript 
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        if (className == null || className.length() == 0)
        {
            // Error
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the className - " + className + " - parameter in the URL");
            
        }
        QName classQName = createClassQName(className);
        if (classQName == null)
        {
            // Error 
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the className - " + className + " - parameter in the URL");
        }
        
        String namespacePrefix = req.getParameter(REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX);
        
        String namespaceURI = null;
        if (namespacePrefix != null)
        {
            namespaceURI = this.namespaceService.getNamespaceURI(namespacePrefix);
        }
        
        Map<QName, PropertyDefinition> propMap = dictionaryservice.getClass(classQName).getProperties();
        List<PropertyDefinition> props = new ArrayList<PropertyDefinition>(propMap.size());
        for (Map.Entry<QName, PropertyDefinition> entry : propMap.entrySet())
        {
            if ((namespaceURI != null && 
                 namespaceURI.equals(entry.getKey().getNamespaceURI()) == true) ||
                namespaceURI == null)
            {
                props.add(entry.getValue());   
            }
        }
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, props);
        return model;
         
    }
   
}