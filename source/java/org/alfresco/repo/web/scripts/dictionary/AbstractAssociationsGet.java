/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.web.scripts.dictionary;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Associationdefinitions for a given classname 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public abstract class AbstractAssociationsGet extends DictionaryWebServiceBase
{
	private static final String MODEL_PROP_KEY_ASSOCIATION_DETAILS = "assocdefs";
	private static final String MODEL_PROP_KEY_INDIVIDUAL_PROPERTY_DEFS = "individualproperty";
	private static final String REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX = "nsp";
	private static final String REQ_URL_TEMPL_VAR_NAME = "n";
	private static final String REQ_URL_TEMPL_VAR_ASSOCIATION_FILTER = "af";
    
    /**
     * @Override  method from DeclarativeWebScript 
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String associationFilter = req.getParameter(REQ_URL_TEMPL_VAR_ASSOCIATION_FILTER);
        String namespacePrefix = req.getParameter(REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX);
        String name = req.getParameter(REQ_URL_TEMPL_VAR_NAME);
    	
        Map<String, Object> model = new HashMap<String, Object>();
        Map<QName, AssociationDefinition> assocdef = new HashMap<QName, AssociationDefinition>();
        QName associationQname = null;
        QName classQname = getClassQname(req);
       
        if(associationFilter == null)
        {
        	associationFilter = "all";
        }
        
        //validate association filter
        if(isValidAssociationFilter(associationFilter) == false)
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the associationFilter - " + associationFilter + " - parameter in the URL");
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
        	// validate the class combination namespaceprefix_name
        	associationQname = getAssociationQname(namespacePrefix, name);
        	
        	if(this.dictionaryservice.getClass(classQname).getAssociations().get(associationQname)== null)
        	{
        		throw new WebScriptException(Status.STATUS_NOT_FOUND, "not a Valid - namespaceprefix_name combination");
        	}
        	
        	model.put(MODEL_PROP_KEY_INDIVIDUAL_PROPERTY_DEFS, this.dictionaryservice.getClass(classQname).getAssociations().get(associationQname));
        }

        model.put(MODEL_PROP_KEY_MESSAGE_LOOKUP, this.dictionaryservice);
        
        return model;
    }
    
    /**
     * @param req - webscript request
     * @return  qualified name for class
     */
    protected abstract QName getClassQname(WebScriptRequest req);
    
    /**
     * @param req - webscript request
     * @return  qualified name for association
     */
    protected abstract QName getAssociationQname(String namespacePrefix, String name);
    
}