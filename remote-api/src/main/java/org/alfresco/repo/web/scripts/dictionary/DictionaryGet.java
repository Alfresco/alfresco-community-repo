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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 * WebScript implementation to retrieve a complete dictionary required to implement a lightweight remote web-tier dictionary.
 * 
 * @author Kevin Roast
 */
public class DictionaryGet extends DictionaryWebServiceBase
{
    private static final String MODEL_CLASS_DEFS = "classdefs";
    private static final String MODEL_PROPERTY_DEFS = "propertydefs";
    private static final String MODEL_ASSOCIATION_DEFS = "assocdefs";

    /** Set of model namespaces to ignore when outputing dictionary classes and aspects */
    private Set<String> ignoreNamespaces = Collections.<String> emptySet();

    /**
     * Set of model namespaces to ignore when outputing dictionary classes and aspects
     * 
     * @param namespaces
     *            Set of model namespaces to ignore
     */
    public void setIgnoreNamespaces(Set<String> namespaces)
    {
        this.ignoreNamespaces = namespaces;
    }

    /**
     * Execute the webscript
     * 
     * @param req
     *            WebScriptRequest
     * @param status
     *            Status
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        List<QName> qnames = new ArrayList<QName>(256);
        Set<String> namespaces = new HashSet<String>();
        Map<QName, ClassDefinition> classdef = new HashMap<QName, ClassDefinition>();
        Map<QName, Collection<PropertyDefinition>> propdef = new HashMap<QName, Collection<PropertyDefinition>>();
        Map<QName, Collection<AssociationDefinition>> assocdef = new HashMap<QName, Collection<AssociationDefinition>>();

        // check configured list of model namespaces to ignore when retrieving all models
        for (String ns : this.namespaceService.getURIs())
        {
            if (!ignoreNamespaces.contains(ns))
            {
                namespaces.add(ns);
            }
        }
        // specific model qname provided or will process all available models
        String strModel = req.getParameter("model");
        if (strModel != null && strModel.length() != 0)
        {
            // handle full QName and prefixed shortname of a model
            QName modelQName = (strModel.charAt(0) == QName.NAMESPACE_BEGIN ? QName.createQName(strModel) : QName.createQName(strModel, this.namespaceService));
            ModelDefinition modelDef = this.dictionaryservice.getModel(modelQName);
            if (modelDef != null)
            {
                qnames.addAll(this.dictionaryservice.getAspects(modelQName));
                qnames.addAll(this.dictionaryservice.getTypes(modelQName));
            }
        }
        else
        {
            // walk all models and extract the aspects and types
            for (QName qname : this.dictionaryservice.getAllModels())
            {
                if (namespaces.contains(qname.getNamespaceURI()))
                {
                    qnames.addAll(this.dictionaryservice.getAspects(qname));
                    qnames.addAll(this.dictionaryservice.getTypes(qname));
                }
            }
        }
        // get the class definitions and the properties and associations
        for (QName qname : qnames)
        {
            ClassDefinition classDef = this.dictionaryservice.getClass(qname);
            classdef.put(qname, classDef);
            propdef.put(qname, classDef.getProperties().values());
            assocdef.put(qname, classDef.getAssociations().values());
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_CLASS_DEFS, classdef.values());
        model.put(MODEL_PROPERTY_DEFS, propdef.values());
        model.put(MODEL_ASSOCIATION_DEFS, assocdef.values());
        model.put(MODEL_PROP_KEY_MESSAGE_LOOKUP, this.dictionaryservice);
        return model;
    }
}
