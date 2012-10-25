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

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Propertydefinition for a given classname and propname
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class PropertyGet extends AbstractPropertyGet
{
    private static final String DICTIONARY_CLASS_NAME = "classname";
    private static final String DICTIONARY_PROPERTY_NAME = "propname";
    
    @Override
    protected QName getPropertyQname(WebScriptRequest req)
    {
        String propertyName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PROPERTY_NAME);
        //validate the presence of property name
        if(propertyName == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing parameter propertyname in the URL");
        }
        
        return QName.createQName(getFullNamespaceURI(propertyName));
    }
        
    @Override
    protected QName getClassQname(WebScriptRequest req)
    {
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
		
        // validate the classname
        if (isValidClassname(className) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + className + " - parameter in the URL");
        }
       
        return QName.createQName(getFullNamespaceURI(className));
    }
    
}