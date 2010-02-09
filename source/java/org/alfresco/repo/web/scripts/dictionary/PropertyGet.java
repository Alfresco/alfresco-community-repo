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
 * Webscript to get the Propertydefinition for a given classname and propname
 * 
 * @author Saravanan Sellathurai
 */

public class PropertyGet extends DeclarativeWebScript
{
	private DictionaryService dictionaryservice;
	private DictionaryHelper dictionaryhelper;
	
	private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";
	private static final String DICTIONARY_CLASS_NAME = "classname";
	private static final String DICTIONARY_PROPERTY_NAME = "propname";
    
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
        String propertyName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PROPERTY_NAME);
        Map<String, Object> model = new HashMap<String, Object>(1);
        QName classQname = null;
        QName propertyQname = null;
        
        //validate the classname
        if(this.dictionaryhelper.isValidClassname(className) == false)
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + className + " - parameter in the URL");
        }
       
        classQname = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(className));
        
        //validate the presence of property name
        if(propertyName == null)
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing parameter propertyname in the URL");
        }
        
        propertyQname = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(propertyName));
        
		if(this.dictionaryservice.getClass(classQname).getProperties().get(propertyQname) != null)
		{
			model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, this.dictionaryservice.getClass(classQname).getProperties().get(propertyQname));
		}
		
		return model;
       
    }
    
}