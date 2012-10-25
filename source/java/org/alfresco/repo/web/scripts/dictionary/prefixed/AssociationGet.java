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

import org.alfresco.repo.web.scripts.dictionary.AbstractAssociationGet;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/*
 * Webscript to get the Associationdefinition for a given classname and association-name
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class AssociationGet extends AbstractAssociationGet
{
    private static final String DICTIONARY_PREFIX = "prefix";
    private static final String DICTIONARY_SHORT_CLASS_NAME = "shortClassName";
    private static final String DICTIONARY_ASSOCIATION_PREFIX = "assocprefix";
    private static final String DICTIONARY_ASSOCIATION_SHORTNAME = "assocname";
    
    @Override
    protected QName getClassQname(WebScriptRequest req)
    {
        String classPrefix = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PREFIX);
        String shortClassName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_SHORT_CLASS_NAME);
        
        //validate the classname
        if(isValidClassname(classPrefix, shortClassName) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + classPrefix + ":" + shortClassName + " - parameter in the URL");
        }
       
        return QName.createQName(getFullNamespaceURI(classPrefix, shortClassName));
    }

    @Override
    protected QName getAssociationQname(WebScriptRequest req)
    {
        String associationName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_ASSOCIATION_SHORTNAME);
        String associationPrefix = req.getServiceMatch().getTemplateVars().get(DICTIONARY_ASSOCIATION_PREFIX);
        
        if(associationPrefix == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing parameter association prefix in the URL");
        }
        if(associationName == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing parameter association name in the URL");
        }
        
        return QName.createQName(getFullNamespaceURI(associationPrefix, associationName));
    }
    
}