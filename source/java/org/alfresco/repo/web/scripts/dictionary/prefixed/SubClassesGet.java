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

import java.util.Collection;

import org.alfresco.repo.web.scripts.dictionary.AbstractSubClassesGet;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Sub-Classdefinitions using classfilter , namespacePrefix and name
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class SubClassesGet extends AbstractSubClassesGet
{
    private static final String DICTIONARY_PREFIX = "prefix";
    private static final String DICTIONARY_CLASS_SHORTNAME = "shortClassName";

    @Override
    protected Collection<QName> getQNameCollection(WebScriptRequest req, boolean recursive)
    {
        String prefix = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PREFIX);
        String shortClassName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_SHORTNAME);
        QName classQName = null;
        boolean isAspect = false;
            
        //validate the className
        if(isValidClassname(prefix, shortClassName) == true)
        {
            classQName = QName.createQName(getFullNamespaceURI(prefix, shortClassName));
            if(isValidTypeorAspect(prefix, shortClassName) == true) 
            {
                isAspect = true;
            }
        }
        else
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the className - " + prefix + ":" + shortClassName + " parameter in the URL");
        }
        
        // collect the subaspects or subtypes of the class
        if(isAspect == true) 
        {
            return this.dictionaryservice.getSubAspects(classQName, recursive);
        }
        else
        {
            return this.dictionaryservice.getSubTypes(classQName, recursive);
        }
    }
        
    @Override
    protected void validateClassname(String namespacePrefix, String name)
    {
        if(isValidClassname(namespacePrefix, name) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the namespacePrefix - " + namespacePrefix + " and name - "+ name  + " - parameter in the URL");
        }
    }

}