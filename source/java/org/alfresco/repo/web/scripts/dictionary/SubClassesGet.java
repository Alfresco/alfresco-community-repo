/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.dictionary;

import java.util.Collection;

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Sub-Classdefinitions using classfilter , namespacePrefix and name
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class SubClassesGet extends AbstractSubClassesGet
{
    private static final String DICTIONARY_CLASS_NAME = "classname";
    	
    @Override
    protected Collection<QName> getQNameCollection(WebScriptRequest req, boolean recursive)
    {
        QName classQName = null;
        boolean isAspect = false;
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        	
        //validate the className
        if(isValidClassname(className) == true)
        {
            classQName = QName.createQName(getFullNamespaceURI(className));
            if(isValidTypeorAspect(className) == true) 
            {
                isAspect = true;
            }
        }
        else
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the className - " + className + " parameter in the URL");
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
        if(isValidClassname(namespacePrefix + "_" + name) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the namespacePrefix - " + namespacePrefix + " and name - "+ name  + " - parameter in the URL");
        }
    }
   
}