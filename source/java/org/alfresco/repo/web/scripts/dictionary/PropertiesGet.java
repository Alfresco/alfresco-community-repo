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

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.dictionary.DictionaryService;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Webscript to get the Propertydefinitions for a given classname eg. =>cm_person
 * 
 * @author Saravanan Sellathurai
 */

public class PropertiesGet extends DeclarativeWebScript
{
	private DictionaryService dictionaryservice;
	private DictionaryHelper dictionaryhelper;
	
	private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";
	private static final String MODEL_PROP_KEY_INDIVIDUAL_PROPERTY_DEFS = "individualproperty";
	private static final String DICTIONARY_CLASS_NAME = "classname";
	private static final String REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX = "nsp";
    private static final String REQ_URL_TEMPL_VAR_NAME = "n";
    
	/**
     * Set the dictionaryService property.
     * 
     * @param dictionaryService The dictionary service instance to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryservice = dictionaryService; 
    }
    
    /**
     * Set the dictionaryhelper class
     * 
     * @param dictionaryService The dictionary service instance to set
     */
    public void setDictionaryHelper(DictionaryHelper dictionaryhelper)
    {
        this.dictionaryhelper = dictionaryhelper; 
    }
    
      
    /**
     * @Override  method from DeclarativeWebScript 
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        String name = req.getParameter(REQ_URL_TEMPL_VAR_NAME);
    	String namespacePrefix = req.getParameter(REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX);
       
        Map<String, Object> model = new HashMap<String, Object>();
        QName classQname = null;
        QName propertyQname = null;
       
        //validate the className
        if(this.dictionaryhelper.isValidClassname(className) == true)
        {
        	classQname = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(className));
        }
        else
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the className - " + className + " - parameter in the URL");
        }
        
        // validate  for the presence of both name and namespaceprefix 
        if((name == null && namespacePrefix != null) || 
           (name != null && namespacePrefix == null))
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing either name or namespacePrefix parameter in the URL - both combination of name and namespacePrefix is needed");
        }
        
        // if both namespacePrefix and name parameters are given then, the combination namespacePrefix_name is used as the index to create the propertyqname
        if(name != null && namespacePrefix != null)
        {
        	if(this.dictionaryhelper.isValidPrefix(namespacePrefix) == false)
        	{
        		throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the namespacePrefix - " + namespacePrefix + " - parameter in the URL");
        	}
        	
        	// validate the class combination namespacePrefix_name
        	propertyQname = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(namespacePrefix + "_" + name));
        	if(this.dictionaryservice.getClass(classQname).getProperties().get(propertyQname) != null)
        	{
        		model.put(MODEL_PROP_KEY_INDIVIDUAL_PROPERTY_DEFS, this.dictionaryservice.getClass(classQname).getProperties().get(propertyQname));
        		model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, this.dictionaryservice.getClass(classQname).getProperties().values());
        	}
        }
        else
        {
        	model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, this.dictionaryservice.getClass(classQname).getProperties().values());
        }
        
        return model;
         
    }
   
}