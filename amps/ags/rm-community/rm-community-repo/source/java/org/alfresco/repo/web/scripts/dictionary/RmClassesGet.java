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
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Classdefinitions using classfilter , namespaceprefix and name
 *
 * This class makes it possible to get only RM related class definitions
 * @see ClassesGet for the original implementation
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RmClassesGet extends DictionaryWebServiceBase implements RecordsManagementModel
{
    private static final String MODEL_PROP_KEY_CLASS_DEFS = "classdefs";
    private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";
    private static final String MODEL_PROP_KEY_ASSOCIATION_DETAILS = "assocdefs";

    private static final String CLASS_FILTER_OPTION_TYPE1 = "all";
    private static final String CLASS_FILTER_OPTION_TYPE2 = "aspect";
    private static final String CLASS_FILTER_OPTION_TYPE3 = "type";

    private static final String REQ_URL_TEMPL_VAR_CLASS_FILTER = "cf";
    private static final String REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX = "nsp";
    private static final String REQ_URL_TEMPL_VAR_NAME = "n";

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
        String classFilter = getValidInput(req.getParameter(REQ_URL_TEMPL_VAR_CLASS_FILTER));
        String namespacePrefix = getValidInput(req.getParameter(REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX));
        String name = getValidInput(req.getParameter(REQ_URL_TEMPL_VAR_NAME));
        String className = null;

        Map<QName, ClassDefinition> classdef = new HashMap<>();
        Map<QName, Collection<PropertyDefinition>> propdef = new HashMap<>();
        Map<QName, Collection<AssociationDefinition>> assocdef = new HashMap<>();
        Map<String, Object> model = new HashMap<>();

        List<QName> qnames = new ArrayList<>();
        QName classQname = null;
        QName myModel = null;

        //if classfilter is not given, then it defaults to all
        if (classFilter == null)
        {
            classFilter = "all";
        }

        //validate classfilter
        if (!isValidClassFilter(classFilter))
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classfilter - " + classFilter + " provided in the URL");
        }

        //name alone has no meaning without namespaceprefix
        if (namespacePrefix == null && name != null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing namespaceprefix parameter in the URL - both combination of name and namespaceprefix is needed");
        }

        //validate the namespaceprefix and name parameters => if namespaceprefix is given, then name has to be validated along with it
        if (namespacePrefix != null)
        {
            //validate name parameter if present along with the namespaceprefix
            if (name != null)
            {
                className = namespacePrefix + "_" + name;
                if (!isValidClassname(className))
                {
                    throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the name - " + name + "parameter in the URL");
                }
                classQname = QName.createQName(getFullNamespaceURI(className));
                classdef.put(classQname, this.dictionaryservice.getClass(classQname));
                propdef.put(classQname, this.dictionaryservice.getClass(classQname).getProperties().values());
                assocdef.put(classQname, this.dictionaryservice.getClass(classQname).getAssociations().values());
            }
            else
            {
                //if name is not given then the model is extracted from the namespaceprefix, there can be more than one model associated with one namespaceprefix
                String namespaceUri = namespaceService.getNamespaceURI(namespacePrefix);
                for (QName qnameObj : this.dictionaryservice.getAllModels())
                {
                     if (qnameObj.getNamespaceURI().equals(namespaceUri))
                     {
                         name = qnameObj.getLocalName();
                         myModel = QName.createQName(getFullNamespaceURI(namespacePrefix + "_" + name));

                         // check the classfilter to pull out either all or type or aspects
                         if (classFilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE1))
                         {
                            qnames.addAll(this.dictionaryservice.getAspects(myModel));
                            qnames.addAll(this.dictionaryservice.getTypes(myModel));
                         }
                         else if (classFilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE3))
                         {
                            qnames.addAll(this.dictionaryservice.getTypes(myModel));
                         }
                         else if (classFilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE2))
                         {
                            qnames.addAll(this.dictionaryservice.getAspects(myModel));
                         }
                     }
                }
            }
        }

        // if namespacePrefix is null, then check the class filter to pull out either all, type or aspects
        if (myModel == null)
        {
            if (classFilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE1))
            {
                qnames.addAll(getAspects(isRM));
                qnames.addAll(getTypes(isRM));
            }
            else if (classFilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE3))
            {
                qnames.addAll(getTypes(isRM));
            }
            else if (classFilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE2))
            {
                qnames.addAll(getAspects(isRM));
            }
        }

        if (classdef.isEmpty())
        {
            for (QName qnameObj : qnames)
            {
                classdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj));
                propdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj).getProperties().values());
                assocdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj).getAssociations().values());
            }
        }

        List<ClassDefinition> classDefinitions = new ArrayList<>(classdef.values());
        Collections.sort(classDefinitions, new DictionaryComparators.ClassDefinitionComparator(dictionaryservice));
        model.put(MODEL_PROP_KEY_CLASS_DEFS, classDefinitions);
        model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, propdef.values());
        model.put(MODEL_PROP_KEY_ASSOCIATION_DETAILS, assocdef.values());
        model.put(MODEL_PROP_KEY_MESSAGE_LOOKUP, dictionaryservice);

        return model;
    }

    /**
     * Returns the names of the types depending on {@link isRM} parameter
     *
     * @param isRM if true only RM related types will be retrieved
     * @return The names of the types defined within the specified model or all of them depending on {@link isRM} parameter
     */
    private Collection<QName> getTypes(boolean isRM)
    {
        if (isRM)
        {
            return this.dictionaryservice.getTypes(RM_MODEL);
        }
        else
        {
            return this.dictionaryservice.getAllTypes();
        }
    }

    /**
     * Returns the names of the aspects depending on {@link isRM} parameter
     *
     * @param isRM if true only RM related aspects will be retrieved
     * @return The names of the aspects defined within the specified model or all of them depending on {@link isRM} parameter
     */
    private Collection<QName> getAspects(boolean isRM)
    {
        if (isRM)
        {
            return this.dictionaryservice.getAspects(RM_MODEL);
        }
        else
        {
            return this.dictionaryservice.getAllAspects();
        }
    }
}
