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

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Classdefinitions using classfilter , namespaceprefix and name
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public abstract class AbstractClassesGet extends DictionaryWebServiceBase
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

    /**
     * @Override method from DeclarativeWebScript
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String classFilter = getValidInput(req.getParameter(REQ_URL_TEMPL_VAR_CLASS_FILTER));
        String namespacePrefix = getValidInput(req.getParameter(REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX));
        String name = getValidInput(req.getParameter(REQ_URL_TEMPL_VAR_NAME));

        Map<QName, ClassDefinition> classdef = new HashMap<QName, ClassDefinition>();
        Map<QName, Collection<PropertyDefinition>> propdef = new HashMap<QName, Collection<PropertyDefinition>>();
        Map<QName, Collection<AssociationDefinition>> assocdef = new HashMap<QName, Collection<AssociationDefinition>>();
        Map<String, Object> model = new HashMap<String, Object>();

        List<QName> qnames = new ArrayList<QName>();
        QName classQname = null;
        QName myModel = null;

        // if classfilter is not given, then it defaults to all
        if (classFilter == null)
        {
            classFilter = "all";
        }

        // validate classfilter
        if (isValidClassFilter(classFilter) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classfilter - " + classFilter + " provided in the URL");
        }

        // name alone has no meaning without namespaceprefix
        if (namespacePrefix == null && name != null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing namespaceprefix parameter in the URL - both combination of name and namespaceprefix is needed");
        }

        // validate the namespaceprefix and name parameters => if namespaceprefix is given, then name has to be validated along with it
        if (namespacePrefix != null)
        {
            // validate name parameter if present along with the namespaceprefix
            if (name != null)
            {
                classQname = getClassQname(namespacePrefix, name);
                classdef.put(classQname, this.dictionaryservice.getClass(classQname));
                propdef.put(classQname, this.dictionaryservice.getClass(classQname).getProperties().values());
                assocdef.put(classQname, this.dictionaryservice.getClass(classQname).getAssociations().values());
            }
            else
            {
                // if name is not given then the model is extracted from the namespaceprefix, there can be more than one model associated with one namespaceprefix
                String namespaceUri = namespaceService.getNamespaceURI(namespacePrefix);
                for (QName qnameObj : this.dictionaryservice.getAllModels())
                {
                    if (qnameObj.getNamespaceURI().equals(namespaceUri))
                    {
                        name = qnameObj.getLocalName();
                        myModel = getQNameForModel(namespacePrefix, name);

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

        // if namespacePrefix is null, then check the classfilter to pull out either all or type or aspects
        if (myModel == null)
        {
            if (classFilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE1))
            {
                qnames.addAll(this.dictionaryservice.getAllAspects());
                qnames.addAll(this.dictionaryservice.getAllTypes());
            }
            else if (classFilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE3))
            {
                qnames.addAll(this.dictionaryservice.getAllTypes());
            }
            else if (classFilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE2))
            {
                qnames.addAll(this.dictionaryservice.getAllAspects());
            }
        }

        if (classdef.isEmpty() == true)
        {
            for (QName qnameObj : qnames)
            {
                classdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj));
                propdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj).getProperties().values());
                assocdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj).getAssociations().values());
            }
        }

        List<ClassDefinition> classDefinitions = new ArrayList<ClassDefinition>(classdef.values());
        Collections.sort(classDefinitions, new DictionaryComparators.ClassDefinitionComparator(dictionaryservice));
        model.put(MODEL_PROP_KEY_CLASS_DEFS, classDefinitions);
        model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, reorderedValues(classDefinitions, propdef));
        model.put(MODEL_PROP_KEY_ASSOCIATION_DETAILS, reorderedValues(classDefinitions, assocdef));
        model.put(MODEL_PROP_KEY_MESSAGE_LOOKUP, dictionaryservice);

        return model;
    }

    /**
     * @param namespacePrefix - namespace prefix of the class
     * @param name - local name of the class
     * @return qualified name for model
     */
    protected abstract QName getQNameForModel(String namespacePrefix, String name);

    /**
     * @param req - webscript request
     * @return  qualified name for class
     */
    protected abstract QName getClassQname(String namespacePrefix, String name);

}