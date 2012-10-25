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
package org.alfresco.repo.web.scripts.dictionary.prefixed;

import org.alfresco.repo.web.scripts.dictionary.AbstractPropertyGet;
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
    private static final String DICTIONARY_PREFIX = "prefix";
    private static final String DICTIONARY_SHORT_CLASS_NAME = "shortClassName";
    private static final String DICTIONARY_SHORTPROPERTY_NAME = "propname";
    private static final String DICTIONARY_PROPERTY_FREFIX = "proppref";
    
    @Override
    protected QName getPropertyQname(WebScriptRequest req)
    {
        String propertyName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_SHORTPROPERTY_NAME);
        String propertyPrefix = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PROPERTY_FREFIX);
        
        //validate the presence of property name
        if(propertyName == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing parameter short propertyname in the URL");
        }
        //validate the presence of property prefix
        if(propertyPrefix == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing parameter propertyprefix in the URL");
        }
        
        return QName.createQName(getFullNamespaceURI(propertyPrefix, propertyName));
    }

    @Override
    protected QName getClassQname(WebScriptRequest req)
    {
        String prefix = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PREFIX);
        String shortClassName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_SHORT_CLASS_NAME);
        
        // validate the classname
        if (isValidClassname(prefix, shortClassName) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + prefix + ":" + shortClassName + " - parameter in the URL");
        }
        
        return QName.createQName(getFullNamespaceURI(prefix, shortClassName));
    }

}