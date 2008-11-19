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

import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
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

public class GetPropertyDefs extends DeclarativeWebScript
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
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        String classname = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        String name = req.getParameter(REQ_URL_TEMPL_VAR_NAME);
    	String namespaceprefix = req.getParameter(REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX);
       
        Map<String, Object> model = new HashMap<String, Object>();
        QName class_qname = null;
        QName property_qname = null;
       
        //validate the classname
        if(this.dictionaryhelper.isValidClassname(classname) == true)
        {
        	class_qname = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(classname));
        }
        else
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + classname + " - parameter in the URL");
        }
        
        //validate namespaceprefix
        if(namespaceprefix != null)
        {
        	if(this.dictionaryhelper.isValidPrefix(namespaceprefix) == false)
        	{
        		throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the namespaceprefix - " + namespaceprefix + " - parameter in the URL");
        	}
        	
        	// validate whether the namespaceprefix is same of classname prefix
        	if(!this.dictionaryhelper.getPrefix(classname).equalsIgnoreCase(namespaceprefix))
        	{
        		throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the namespaceprefix - " + namespaceprefix + " parameter in the URL, namespaceprefix should be of type "+ classname);
        	}
        }
        
        // validate  the condition, if name is present and namespaceprefix is null
        if(name !=null && namespaceprefix == null)
        {
        	property_qname = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(this.dictionaryhelper.getPrefix(classname) + "_" + name));
        	if(this.dictionaryservice.getClass(class_qname).getProperties().get(property_qname)== null)
        	{
        		throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the parameter name - "+ name +" in the URL ");
        	}
        	model.put(MODEL_PROP_KEY_INDIVIDUAL_PROPERTY_DEFS, this.dictionaryservice.getClass(class_qname).getProperties().get(property_qname));
        }
        
        // if both namespaceprefix and name parameters are given then, the combination namespaceprefix_name is used as the index to create the propertyqname
        if(name != null && namespaceprefix != null)
        {
        	// validate the class combination namespaceprefix_name
        	property_qname = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(namespaceprefix + "_" + name));
        	if(this.dictionaryservice.getClass(class_qname).getProperties().get(property_qname)== null)
        	{
        		throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the name - " + name + " - parameter in the URL");
        	}
        	
        	model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, this.dictionaryservice.getClass(class_qname).getProperties().values());
        	model.put(MODEL_PROP_KEY_INDIVIDUAL_PROPERTY_DEFS, this.dictionaryservice.getClass(class_qname).getProperties().get(property_qname));
        }
        else
        {	// if no name and namespaceprefix parameters are given then pull all properties pertaining to the classname
        	model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, this.dictionaryservice.getClass(class_qname).getProperties().values());
        }
        
        return model;
         
    }
   
}