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

import org.alfresco.web.scripts.Cache;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript to get the Classdefinitions using classfilter , namespaceprefix and name
 * @author Saravanan Sellathurai
 */

public class GetClassDetail extends DeclarativeWebScript
{
	private DictionaryService dictionaryservice;
	private DictionaryHelper dictionaryhelper;
	
	private static final String MODEL_PROP_KEY_CLASS_DEFS = "classdefs";
	private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";
	private static final String MODEL_PROP_KEY_ASSOCIATION_DETAILS = "assocdefs";
	
    private static final String CLASS_FILTER_OPTION_TYPE1 = "all";
    private static final String CLASS_FILTER_OPTION_TYPE2 = "aspect";
    private static final String CLASS_FILTER_OPTION_TYPE3 = "type";
    
    private static final String REQ_URL_TEMPL_VAR_CLASS_FILTER = "cf";
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
    	String classfilter = this.dictionaryhelper.getValidInput(req.getParameter(REQ_URL_TEMPL_VAR_CLASS_FILTER));
        String namespaceprefix = this.dictionaryhelper.getValidInput(req.getParameter(REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX));
        String name = this.dictionaryhelper.getValidInput(req.getParameter(REQ_URL_TEMPL_VAR_NAME));
        String classname = null;
        
        Map<QName, ClassDefinition> classdef = new HashMap<QName, ClassDefinition>();
        Map<QName, Collection<PropertyDefinition>> propdef = new HashMap<QName, Collection<PropertyDefinition>>();
        Map<QName, Collection<AssociationDefinition>> assocdef = new HashMap<QName, Collection<AssociationDefinition>>();
        Map<String, Object> model = new HashMap<String, Object>();
        
        Collection <QName> qnames = null; 
        QName class_qname = null;
        QName myModel = null;
		
        //if classfilter is not given, then it defaults to all
        if(classfilter == null)
        {
        	classfilter = "all";
        }
        
        //validate classfilter
        if(this.dictionaryhelper.isValidClassFilter(classfilter) == false)
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classfilter - " + classfilter + " provided in the URL");
        }
        
        //validate the namespaceprefix and name parameters => if namespaceprefix is given, then name has to be validated along with it
        if(namespaceprefix != null)
        {
        	if(this.dictionaryhelper.isValidPrefix(namespaceprefix) == false)
	        {
	        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the namespaceprefix - " + namespaceprefix + " parameter in the URL");
	        }
        	
        	//validate name parameter if present along with the namespaceprefix
        	if(name != null)
        	{
        		classname = namespaceprefix + "_" + name;
        		if(this.dictionaryhelper.isValidClassname(classname) == false)
        		{
        			throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the name - " + name + "parameter in the URL");
        		}
        		class_qname = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(classname));
        		classdef.put(class_qname, this.dictionaryservice.getClass(class_qname));
        		propdef.put(class_qname, this.dictionaryservice.getClass(class_qname).getProperties().values());
        		assocdef.put(class_qname, this.dictionaryservice.getClass(class_qname).getAssociations().values());
        	}
        	else
        	{	
        		//if name is not given then the model is extracted from the namespaceprefix, there can be more than one model associated with one namespaceprefix
        		String namespaceQname = this.dictionaryhelper.getNamespaceURIfromPrefix(namespaceprefix);
        		for(QName qnameObj:this.dictionaryservice.getAllModels())
		        {
		             if(qnameObj.getNamespaceURI().equals(namespaceQname))
		             {
		                 name = qnameObj.getLocalName();
		                 myModel = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(namespaceprefix + "_" + name));
		                 
		                 // check the classfilter to pull out either all or tyype or aspects
		                 if (classfilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE1)) 
		                 {
		             		qnames = this.dictionaryservice.getAspects(myModel);
		             		qnames.addAll(this.dictionaryservice.getTypes(myModel));
		             	 }
		                 else if (classfilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE3))
		             	 {
		             		qnames = this.dictionaryservice.getTypes(myModel);
		             	 }
		             	else if (classfilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE2))
		               	{
		               		qnames = this.dictionaryservice.getAspects(myModel);
		               	}
		             }
		        } 
        	}
        }
        
        // if namespaceprefix is null, then check the classfilter to pull out either all or type or aspects
        if(myModel == null)
        {
	        if (classfilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE1)) 
	        {
	    		qnames = this.dictionaryservice.getAllAspects();
	        	qnames.addAll(this.dictionaryservice.getAllTypes());
	        }
	    	else if (classfilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE3))
	    	{
	    		qnames = this.dictionaryservice.getAllTypes();
	    	}
	       	else if (classfilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE2))
	       	{
	       		qnames = this.dictionaryservice.getAllAspects();
	       	}
        }
        
		if(classdef.isEmpty() == true)
		{
			for(QName qnameObj: qnames)
	        {	
	    		classdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj));
	        	propdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj).getProperties().values());
	        	assocdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj).getAssociations().values());
			}
		}
    	model.put(MODEL_PROP_KEY_CLASS_DEFS, classdef.values());
	    model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, propdef.values());
	    model.put(MODEL_PROP_KEY_ASSOCIATION_DETAILS, assocdef.values());
	    return model;
    }
   
 }