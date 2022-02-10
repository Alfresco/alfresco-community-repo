/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.web.scripts.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Propertydefinitions for a given classname eg. =&gt;cm_person
 *
 * This class makes it possible to get only RM related property definitions
 * @see PropertiesGet for the original implementation
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RmPropertiesGet extends DictionaryWebServiceBase implements RecordsManagementModel
{
    private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";
    private static final String DICTIONARY_CLASS_NAME = "classname";
    private static final String PARAM_NAME = "name";
    private static final String REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX = "nsp";

    /** Site service*/
    private SiteService siteService;

    /**
     * @param siteService the site service to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        return executeImpl(req, RmDictionaryWebServiceUtils.isRmSite(req, siteService));
    }

    /**
     * Execute custom Java logic
     *
     * @param req  Web Script request
     * @param isRM  indicates whether the request comes from an RM site or not
     * @return custom service model
     */
    private Map<String, Object> executeImpl(WebScriptRequest req, boolean isRM)
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
                propMap = new HashMap<>(names.length);
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
                Collection<QName> propQNames = getProperties(isRM);
                propMap = new HashMap<>(propQNames.size());
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
        List<PropertyDefinition> props = new ArrayList<>(propMap.size());
        for (Map.Entry<QName, PropertyDefinition> entry : propMap.entrySet())
        {
            if ((namespaceURI != null &&
                 namespaceURI.equals(entry.getKey().getNamespaceURI())) ||
                namespaceURI == null)
            {
                props.add(entry.getValue());
            }
        }

        // Order property definitions by title
        Collections.sort(props, new DictionaryComparators.PropertyDefinitionComparator(dictionaryservice));

        // Pass list of property definitions to template
        Map<String, Object> model = new HashMap<>();
        model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, props);
        model.put(MODEL_PROP_KEY_MESSAGE_LOOKUP, dictionaryservice);

        return model;
    }

    /**
     * Returns the names of the properties depending on {@link isRM} parameter
     *
     * @param isRM if true only RM related properties will be retrieved
     * @return The names of the properties defined within the specified model or all of them depending on {@link isRM} parameter
     */
    private Collection<QName> getProperties(boolean isRM)
    {
        if (isRM)
        {
            return dictionaryservice.getProperties(RM_MODEL);
        }
        else
        {
            return dictionaryservice.getAllProperties(null);
        }
    }
}
