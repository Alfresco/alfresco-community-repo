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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Propertydefinitions for a given classname eg. =>cm_person
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public abstract class AbstractPropertiesGet extends DictionaryWebServiceBase
{
    private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";
    private static final String PARAM_NAME = "name";
    private static final String REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX = "nsp";
    
    /**
     * This request parameter can be passed to filter the propertes based on 
     * type. More than one type can be supplied.
     */
    private static final String REQ_PARM_ALLOWED_TYPE = "type";

    /**
     * @Override method from DeclarativeWebScript
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        QName classQName = getClassQName(req);

        String[] names = req.getParameterValues(PARAM_NAME);

        String namespacePrefix = req.getParameter(REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX);
        String namespaceURI = null;
        if (namespacePrefix != null)
        {
            namespaceURI = this.namespaceService.getNamespaceURI(namespacePrefix);
        }

        Map<QName, PropertyDefinition> propMap = null;
        if (classQName == null)
        {
            if (names != null)
            {
                propMap = new HashMap<QName, PropertyDefinition>(names.length);
                for (String name : names)
                {
                    QName propQName = QName.createQName(name, namespaceService);
                    PropertyDefinition propDef = dictionaryservice.getProperty(propQName);
                    if (propDef != null)
                    {
                        propMap.put(propQName, propDef);
                    }
                }
            }
            else
            {
                Collection<QName> propQNames = dictionaryservice.getAllProperties(null);
                propMap = new HashMap<QName, PropertyDefinition>(propQNames.size());
                for (QName propQName : propQNames)
                {
                    propMap.put(propQName, dictionaryservice.getProperty(propQName));
                }
            }

        }
        else
        {
            // Get all the property definitions for the class
            propMap = dictionaryservice.getClass(classQName).getProperties();
        }

        // Filter the properties by URI
        List<PropertyDefinition> props = new ArrayList<PropertyDefinition>(propMap.size());
        for (Map.Entry<QName, PropertyDefinition> entry : propMap.entrySet())
        {
            if ((namespaceURI != null && namespaceURI.equals(entry.getKey().getNamespaceURI()) == true) || namespaceURI == null)
            {
                props.add(entry.getValue());
            }
        }

        // Filter the properties by the allowed types...
        String[] filterTypes = req.getParameterValues(REQ_PARM_ALLOWED_TYPE);
        if (filterTypes != null && filterTypes.length > 0)
        {
            List<PropertyDefinition> typeFilteredProps = new ArrayList<PropertyDefinition>(props.size());
            for (PropertyDefinition prop: props)
            {
                for (String type: filterTypes)
                {
                    DataTypeDefinition dtd = prop.getDataType();
                    if (dtd.getName().getPrefixString().equals(type))
                    {
                        typeFilteredProps.add(prop);
                        break;
                    }
                }
            }
            
            // Important to change the props variable to reference the type filtered properties...
            props = typeFilteredProps;
        }

        // Order property definitions by title
        Collections.sort(props, new DictionaryComparators.PropertyDefinitionComparator(dictionaryservice));

        // Pass list of property definitions to template
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, props);
        model.put(MODEL_PROP_KEY_MESSAGE_LOOKUP, dictionaryservice);
        return model;
    }

    /**
     * @param req - webscript request
     * @return  qualified name for class
     */
    protected abstract QName getClassQName(WebScriptRequest req);

}