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

import org.alfresco.repo.web.scripts.dictionary.AbstractClassesGet;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Webscript to get the Classdefinitions using classfilter , namespaceprefix and name
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class ClassesGet extends AbstractClassesGet
{
    @Override
    protected QName getQNameForModel(String namespacePrefix, String name)
    {
        return QName.createQName(getFullNamespaceURI(namespacePrefix, name));
    }
        
    @Override
    protected QName getClassQname(String namespacePrefix, String name)
    {
        if(isValidClassname(namespacePrefix, name) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the name - " + name + "parameter in the URL");
        }
        return QName.createQName(getFullNamespaceURI(namespacePrefix, name));
    }

}