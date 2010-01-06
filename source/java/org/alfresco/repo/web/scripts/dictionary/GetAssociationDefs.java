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
import org.alfresco.service.cmr.dictionary.AssociationDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Webscript to get the Associationdefinitions for a given classname 
 * @author Saravanan Sellathurai
 */

public class GetAssociationDefs extends DeclarativeWebScript
{
	private DictionaryService dictionaryservice;
	private DictionaryHelper dictionaryhelper;
	
	private static final String MODEL_PROP_KEY_ASSOCIATION_DETAILS = "assocdefs";
	private static final String MODEL_PROP_KEY_INDIVIDUAL_PROPERTY_DEFS = "individualproperty";
	private static final String DICTIONARY_CLASS_NAME = "classname";
	private static final String REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX = "nsp";
    private static final String REQ_URL_TEMPL_VAR_NAME = "n";
    private static final String REQ_URL_TEMPL_VAR_ASSOCIATION_FILTER = "af";
	
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
        String associationFilter = req.getParameter(REQ_URL_TEMPL_VAR_ASSOCIATION_FILTER);
        String namespacePrefix = req.getParameter(REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX);
        String name = req.getParameter(REQ_URL_TEMPL_VAR_NAME);
    	
        Map<String, Object> model = new HashMap<String, Object>();
        Map<QName, AssociationDefinition> assocdef = new HashMap<QName, AssociationDefinition>();
        QName associationQname = null;
        QName classQname = null;
       
        if(associationFilter == null)
        {
        	associationFilter = "all";
        }
        
        //validate association filter
        if(this.dictionaryhelper.isValidAssociationFilter(associationFilter) == false)
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the associationFilter - " + associationFilter + " - parameter in the URL");
        }
        
        //validate classname
        if(this.dictionaryhelper.isValidClassname(className) == true)
        {
        	classQname = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(className));
        }
        else
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + className + " - parameter in the URL");
        }
        
        // validate  for the presence of both name and namespaceprefix 
        if((name == null && namespacePrefix != null) || 
           (name != null && namespacePrefix == null))
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing either name or namespaceprefix parameter in the URL - both combination of name and namespaceprefix is needed");
        }
        
        // check for association filters
        if(associationFilter.equals("child"))
    	{
    		model.put(MODEL_PROP_KEY_ASSOCIATION_DETAILS, this.dictionaryservice.getClass(classQname).getChildAssociations().values());
    	}
        else if(associationFilter.equals("general"))
    	{
    		for(AssociationDefinition assocname:this.dictionaryservice.getClass(classQname).getAssociations().values())
	        {
    			if(assocname.isChild() == false)
    			{
    				assocdef.put(assocname.getName(), assocname);
    			}
	        }
    		model.put(MODEL_PROP_KEY_ASSOCIATION_DETAILS, assocdef.values());
    	}
        else if(associationFilter.equals("all"))
    	{
    		model.put(MODEL_PROP_KEY_ASSOCIATION_DETAILS, this.dictionaryservice.getClass(classQname).getAssociations().values());
        }

        // if both namespaceprefix and name parameters are given then, the combination namespaceprefix_name is used as the index to create the qname
        if(name != null && namespacePrefix != null)
        {
        	if(this.dictionaryhelper.isValidPrefix(namespacePrefix) == false)
        	{
        		throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the namespaceprefix - " + namespacePrefix + " - parameter in the URL");
        	}
        	
        	// validate the class combination namespaceprefix_name
        	associationQname = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(namespacePrefix + "_" + name));
        	
        	if(this.dictionaryservice.getClass(classQname).getAssociations().get(associationQname)== null)
        	{
        		throw new WebScriptException(Status.STATUS_NOT_FOUND, "not a Valid - namespaceprefix_name combination");
        	}
        	
        	model.put(MODEL_PROP_KEY_INDIVIDUAL_PROPERTY_DEFS, this.dictionaryservice.getClass(classQname).getAssociations().get(associationQname));
        }

        return model;
        
    }
    
}