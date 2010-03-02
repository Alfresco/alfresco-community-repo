/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Classdefinitions for a classname eg. =>cm_author
 * @author Saravanan Sellathurai
 */

public class ClassGet extends DictionaryWebServiceBase
{	
	private static final String MODEL_PROP_KEY_CLASS_DETAILS = "classdefs";
	private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";
	private static final String MODEL_PROP_KEY_ASSOCIATION_DETAILS = "assocdefs";
	private static final String DICTIONARY_CLASS_NAME = "className";
    
    /**
     * @Override  method from DeclarativeWebScript 
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        
        Map<String, Object> model = new HashMap<String, Object>(3);
        QName classQname = null;
        Map<QName, ClassDefinition> classdef = new HashMap<QName, ClassDefinition>();
        Map<QName, Collection<PropertyDefinition>> propdef = new HashMap<QName, Collection<PropertyDefinition>>();
        Map<QName, Collection<AssociationDefinition>> assocdef = new HashMap<QName, Collection<AssociationDefinition>>();
        
        //validate the classname and throw appropriate error message
        if(isValidClassname(className) == true)
        {
        	classQname = QName.createQName(getFullNamespaceURI(className));
        	classdef.put(classQname, this.dictionaryservice.getClass(classQname));
        	propdef.put(classQname, this.dictionaryservice.getClass(classQname).getProperties().values());
    		assocdef.put(classQname, this.dictionaryservice.getClass(classQname).getAssociations().values());
        }
        else
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + className + " - parameter in the URL");
        }
        
        model.put(MODEL_PROP_KEY_CLASS_DETAILS, classdef.values());
        model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, propdef.values());
        model.put(MODEL_PROP_KEY_ASSOCIATION_DETAILS, assocdef.values());
        
        return model;
         
    }
    
}