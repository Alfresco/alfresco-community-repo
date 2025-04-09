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

import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.service.namespace.QName;

/**
 * Webscript to get the Propertydefinition for a given classname and propname
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public abstract class AbstractPropertyGet extends DictionaryWebServiceBase
{
    private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";

    /**
     * Override method from DeclarativeWebScript
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(1);
        QName classQname = getClassQname(req);
        QName propertyQname = getPropertyQname(req);

        if (this.dictionaryservice.getClass(classQname).getProperties().get(propertyQname) != null)
        {
            model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, this.dictionaryservice.getClass(classQname).getProperties().get(propertyQname));
            model.put(MODEL_PROP_KEY_MESSAGE_LOOKUP, this.dictionaryservice);
        }

        return model;
    }

    /**
     * @param req
     *            - webscript request
     * @return qualified name for property
     */
    protected abstract QName getPropertyQname(WebScriptRequest req);

    /**
     * @param req
     *            - webscript request
     * @return qualified name for class
     */
    protected abstract QName getClassQname(WebScriptRequest req);

}
