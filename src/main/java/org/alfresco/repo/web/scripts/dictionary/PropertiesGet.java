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

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Propertydefinitions for a given classname eg. =>cm_person
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class PropertiesGet extends AbstractPropertiesGet
{
    private static final String DICTIONARY_CLASS_NAME = "classname";
	
    @Override
    protected QName getClassQName(WebScriptRequest req)
    {
        QName classQName = null;
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        if (className != null && className.length() != 0)
        {            
            classQName = createClassQName(className);
            if (classQName == null)
            {
                // Error 
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the className - " + className + " - parameter in the URL");
            }
        }
        return classQName;
    }
            
}
