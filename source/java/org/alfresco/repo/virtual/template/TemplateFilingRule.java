/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.template;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.config.NodeRefPathExpression;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link FilingRule} created with the criteria given in the applied virtual
 * folder template.
 *
 * @author Bogdan Horje
 */
public class TemplateFilingRule implements FilingRule
{
    private static Log logger = LogFactory.getLog(TemplateFilingRule.class);

    private ActualEnvironment env;

    private String path;

    private String type;

    private Set<String> aspects;

    private Map<String, String> stringProperties;


    public TemplateFilingRule(ActualEnvironment environment, String path, String type, Set<String> aspects,
                Map<String, String> properties)
    {
        this.env = environment;
        this.path = path;
        this.type = type;
        this.aspects = aspects;
        this.stringProperties = properties;
    }

    @Override
    public FilingData createFilingData(FilingParameters parameters) throws VirtualizationException
    {
        return createFilingData(parameters.getParentRef(),
                                parameters.getAssocTypeQName(),
                                parameters.getAssocQName(),
                                parameters.getNodeTypeQName(),
                                parameters.getProperties());
    }

    private FilingData createFilingData(Reference parentRef, QName assocTypeQName, QName assocQName,
                QName nodeTypeQName, Map<QName, Serializable> properties) throws VirtualizationException
    {

        NodeRef fParentRef = null;
        QName fType = null;
        Set<QName> fAspects = null;
        Map<QName, Serializable> fProperties = null;

        NamespacePrefixResolver nsPrefixResolver = env.getNamespacePrefixResolver();

        if (type == null || type.length() == 0)
        {
            fType = nodeTypeQName;
        }
        else
        {
            fType = QName.createQName(type,
                                      nsPrefixResolver);

            // CM-528 acceptance criteria 3 :
            // Given that the current user can upload new content into a
            // specific virtual folder (filing rule)
            // when a filing rule specifies a type or sub type of cm:folder
            // (which is a non supported configuration)
            // uploading content will create a document, not a folder

            if (env.isSubClass(fType,
                               ContentModel.TYPE_FOLDER))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("CM-528 acceptance criteria 3 : we deny the creation of folders subtype " + fType
                                + " and force cm:content instead.");
                }
                fType = ContentModel.TYPE_CONTENT;
            }

            // Explicit type matching follows.
            // It might cause non-transactional behavior.
            // To avoid it we rely on folder creation exclusion in
            // VirtualNodeServiceExtension#createNode
            // See CM-533 Suppress options to create folders in a virtual folder

            if (env.isSubClass(nodeTypeQName,
                               fType))
            {
                fType = nodeTypeQName;
            }
        }

        fParentRef = parentNodeRefFor(parentRef,
                                      true);

        fProperties = new HashMap<QName, Serializable>(properties);

        Set<Entry<String, String>> propertyEntries = stringProperties.entrySet();

        for (Entry<String, String> propertyEntry : propertyEntries)
        {
            String name = propertyEntry.getKey();
            QName qName = QName.createQName(name,
                                            nsPrefixResolver);
            if (!fProperties.containsKey(qName))
            {
                fProperties.put(qName,
                                stringProperties.get(name));
            }
        }

        fAspects = new HashSet<>();

        for (String aspect : aspects)
        {
            fAspects.add(QName.createQName(aspect,
                                           env.getNamespacePrefixResolver()));
        }

        return new FilingData(fParentRef,
                              assocTypeQName,
                              assocQName,
                              fType,
                              fAspects,
                              fProperties);

    }

    private NodeRef parentNodeRefFor(Reference parentReference, boolean failIfNotFound)
    {
        NodeRef fParentRef;
        if (path == null || path.length() == 0)
        {
            fParentRef = parentReference.execute(new GetActualNodeRefMethod(env));
        }
        else
        {

            String[] pathElements = NodeRefPathExpression.splitAndNormalizePath(path);
            for (int i = 0; i < pathElements.length; i++)
            {
                pathElements[i]=ISO9075.decode(pathElements[i]);
            }
            fParentRef = env.findQNamePath(pathElements);
        }

        if (failIfNotFound && fParentRef == null)
        {
            throw new VirtualizationException("The filing path " + path + " could not be resolved.");
        }

        return fParentRef;
    }

    @Override
    public boolean isNullFilingRule()
    {
        return false;
    }

    @Override
    public NodeRef filingNodeRefFor(FilingParameters parameters) throws VirtualizationException
    {
        return parentNodeRefFor(parameters.getParentRef(),
                                false);
    }

}