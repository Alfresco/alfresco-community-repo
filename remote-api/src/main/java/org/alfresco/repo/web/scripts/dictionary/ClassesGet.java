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

import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.service.namespace.QName;

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
        return QName.createQName(getFullNamespaceURI(namespacePrefix + "_" + name));
    }

    @Override
    protected QName getClassQname(String namespacePrefix, String name)
    {
        String className = namespacePrefix + "_" + name;
        if (isValidClassname(className) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the name - " + name + "parameter in the URL");
        }
        return QName.createQName(getFullNamespaceURI(className));
    }

}
