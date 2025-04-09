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
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.service.namespace.QName;

/**
 * Webscript to get the Classdefinitions for a classname eg. =>cm_author
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class ClassGet extends AbstractClassGet
{
    private static final String DICTIONARY_CLASS_NAME = "className";

    /**
     * Override method from AbstractClassGet
     */
    @Override
    protected QName getClassQname(WebScriptRequest req)
    {
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        // validate the classname and throw appropriate error message
        if (isValidClassname(className) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + className + " - parameter in the URL");
        }
        return QName.createQName(getFullNamespaceURI(className));
    }

}
