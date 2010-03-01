/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor.node;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FormProcessor implementation that can generate and persist Form objects for
 * repository nodes.
 * 
 * @author Gavin Cornwell
 */
public class NodeFormProcessor extends ContentModelFormProcessor<NodeRef, NodeRef>
{
    /** Logger */
    private static Log logger = LogFactory.getLog(NodeFormProcessor.class);

    /*
     * @see
     * org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getLogger
     * ()
     */
    @Override
    protected Log getLogger()
    {
        return logger;
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.FilteredFormProcessor#getTypedItem(
     * org.alfresco.repo.forms.Item)
     */
    @Override
    protected NodeRef getTypedItem(Item item)
    {
        // create NodeRef representation, the id could already be in a valid
        // NodeRef format or it may be in a URL friendly format
        NodeRef nodeRef = null;
        if (NodeRef.isNodeRef(item.getId()))
        {
            nodeRef = new NodeRef(item.getId());
        }
        else
        {
            // split the string into the 3 required parts
            String[] parts = item.getId().split("/");
            if (parts.length == 3)
            {
                try
                {
                    nodeRef = new NodeRef(parts[0], parts[1], parts[2]);
                }
                catch (IllegalArgumentException iae)
                {
                    // ignored for now, dealt with below

                    if (logger.isDebugEnabled()) logger.debug("NodeRef creation failed for: " + item.getId(), iae);
                }
            }
        }

        // check we have a valid node ref
        if (nodeRef == null) { throw new FormNotFoundException(item, new IllegalArgumentException(item.getId())); }

        // check the node itself exists
        if (this.nodeService.exists(nodeRef) == false)
        {
            throw new FormNotFoundException(item, new InvalidNodeRefException("Node does not exist: " + nodeRef,
                        nodeRef));
        }
        else
        {
            // all Node based filters can expect to get a NodeRef
            return nodeRef;
        }
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.FilteredFormProcessor#internalGenerate
     * (java.lang.Object, java.util.List, java.util.List,
     * org.alfresco.repo.forms.Form, java.util.Map)
     */
    @Override
    protected void internalGenerate(NodeRef item, List<String> fields, List<String> forcedFields, Form form,
                Map<String, Object> context)
    {
        if (logger.isDebugEnabled()) logger.debug("Generating form for: " + item);

        // generate the form for the node
        generateNode(item, fields, forcedFields, form);

        if (logger.isDebugEnabled()) logger.debug("Generated form: " + form);
    }

    /**
     * Sets up the Form object for the given NodeRef
     * 
     * @param nodeRef The NodeRef to generate a Form for
     * @param fields Restricted list of fields to include
     * @param forcedFields List of fields to forcibly include
     * @param form The Form instance to populate
     */
    protected void generateNode(NodeRef nodeRef, List<String> fields, List<String> forcedFields, Form form)
    {
        // set the type and URL of the item
        QName type = this.nodeService.getType(nodeRef);
        setFormItemType(form, type.toPrefixString(this.namespaceService));

        StringBuilder builder = new StringBuilder("/api/node/");
        builder.append(nodeRef.getStoreRef().getProtocol()).append("/");
        builder.append(nodeRef.getStoreRef().getIdentifier()).append("/");
        builder.append(nodeRef.getId());
        setFormItemUrl(form, builder.toString());

        if (fields != null && fields.size() > 0)
        {
            generateSelectedFields(nodeRef, null, fields, forcedFields, form);
        }
        else
        {
            // setup field definitions and data
            generateAllPropertyFields(nodeRef, form);
            generateAllAssociationFields(nodeRef, form);
            generateTransientFields(nodeRef, form);
        }
        
        // process working copy nodes, just returns if it's not
        processWorkingCopy(nodeRef, form);
    }

    /**
     * Sets up the field definitions for all the node's properties.
     * 
     * @param nodeRef The NodeRef of the node being setup
     * @param form The Form instance to populate
     */
    protected void generateAllPropertyFields(NodeRef nodeRef, Form form)
    {
        // get data dictionary definition for node
        QName type = this.nodeService.getType(nodeRef);
        TypeDefinition typeDef = this.dictionaryService.getAnonymousType(type, this.nodeService.getAspects(nodeRef));

        // iterate round the property definitions for the node and create
        // the equivalent field definition and setup the data for the property
        Map<QName, PropertyDefinition> propDefs = typeDef.getProperties();
        Map<QName, Serializable> propValues = this.nodeService.getProperties(nodeRef);
        for (PropertyDefinition propDef : propDefs.values())
        {
            generatePropertyField(propDef, form, propValues.get(propDef.getName()), this.namespaceService);
        }
    }

    /**
     * Sets up the field definitions for all the node's associations.
     * 
     * @param nodeRef The NodeRef of the node being setup
     * @param form The Form instance to populate
     */
    protected void generateAllAssociationFields(NodeRef nodeRef, Form form)
    {
        // get data dictionary definition for the node
        QName type = this.nodeService.getType(nodeRef);
        TypeDefinition typeDef = this.dictionaryService.getAnonymousType(type, this.nodeService.getAspects(nodeRef));

        // iterate round the association defintions and setup field definition
        Map<QName, AssociationDefinition> assocDefs = typeDef.getAssociations();
        for (AssociationDefinition assocDef : assocDefs.values())
        {
            generateAssociationField(assocDef, form, retrieveAssociationValues(nodeRef, assocDef),
                        this.namespaceService);
        }
    }

    /**
     * Sets up the field definitions for any transient fields that may be
     * useful, for example, 'mimetype', 'size' and 'encoding'.
     * 
     * @param nodeRef The NodeRef of the node being setup
     * @param form The Form instance to populate
     */
    protected void generateTransientFields(NodeRef nodeRef, Form form)
    {
        // if the node is content add the 'mimetype', 'size' and 'encoding'
        // fields.
        QName type = this.nodeService.getType(nodeRef);
        if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT))
        {
            ContentData content = (ContentData) this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            if (content != null)
            {
                // setup mimetype field
                generateMimetypePropertyField(content, form);

                // setup encoding field
                generateEncodingPropertyField(content, form);

                // setup size field
                generateSizePropertyField(content, form);
            }
        }
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.FilteredFormProcessor#internalPersist
     * (java.lang.Object, org.alfresco.repo.forms.FormData)
     */
    @Override
    protected NodeRef internalPersist(NodeRef item, FormData data)
    {
        if (logger.isDebugEnabled()) logger.debug("Persisting form for: " + item);

        // persist the node
        persistNode(item, data);

        return item;
    }
}
