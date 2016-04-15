/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.forms.processor.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FormProcessor implementation that can generate and persist Form objects for
 * repository nodes.
 * 
 * @author Gavin Cornwell
 * @author Nick Smith
 */
public class NodeFormProcessor extends ContentModelFormProcessor<NodeRef, NodeRef>
{
    /** Logger */
    private static Log logger = LogFactory.getLog(NodeFormProcessor.class);

    /*
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getLogger()
     */
    @Override
    protected Log getLogger()
    {
        return logger;
    }

    /*
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getTypedItem(org.alfresco.repo.forms.Item)
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

                    if (logger.isDebugEnabled()) 
                        logger.debug("NodeRef creation failed for: " + item.getId(), iae);
                }
            }
        }

        // check we have a valid node ref
        if (nodeRef == null) 
        { 
            throw new FormNotFoundException(item, new IllegalArgumentException(item.getId())); 
        }

        // check the node itself exists
        if (this.nodeService.exists(nodeRef) == false)
        {
            throw new FormNotFoundException(item, 
                        new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef));
        }
        else
        {
            // all Node based filters can expect to get a NodeRef
            return nodeRef;
        }
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemType(java.lang.Object)
     */
    @Override
    protected String getItemType(NodeRef item)
    {
        QName type = this.nodeService.getType(item);
        return type.toPrefixString(this.namespaceService);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemURI(java.lang.Object)
     */
    @Override
    protected String getItemURI(NodeRef item)
    {
        StringBuilder builder = new StringBuilder("/api/node/");
        builder.append(item.getStoreRef().getProtocol()).append("/");
        builder.append(item.getStoreRef().getIdentifier()).append("/");
        builder.append(item.getId());
        return builder.toString();
    }
    
    @Override
    protected Map<QName, Serializable> getPropertyValues(NodeRef nodeRef) 
    {
        return nodeService.getProperties(nodeRef);
    }

    @Override
    protected Map<QName, Serializable> getAssociationValues(NodeRef item)
    {
        HashMap<QName, Serializable> assocs = new HashMap<QName, Serializable>();
        List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(item, RegexQNamePattern.MATCH_ALL);
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(item);
        for (ChildAssociationRef childAssoc : childAssocs) 
        {
            QName name = childAssoc.getTypeQName();
            NodeRef target = childAssoc.getChildRef();
            addAssocToMap(name, target, assocs);
        }
        for (AssociationRef associationRef : targetAssocs) 
        {
            QName name = associationRef.getTypeQName();
            NodeRef target = associationRef.getTargetRef();
            if (nodeService.exists(target) && (permissionService.hasPermission(target, PermissionService.READ) == AccessStatus.ALLOWED))
            {
                addAssocToMap(name, target, assocs);
            }
        }
        return assocs;
    }

    @SuppressWarnings("unchecked")
    private void addAssocToMap(QName name, NodeRef target, HashMap<QName, Serializable> assocs)
    {
        Serializable value = assocs.get(name);
        if (value == null)
        {
            LinkedHashSet<NodeRef> values = new LinkedHashSet<NodeRef>();
            values.add(target);
            assocs.put(name, values);
        }
        else
        {
            if (value instanceof Set<?>)
            {
                ((Set<NodeRef>)value).add(target);
            }
        }
    }
    
    @Override
    protected Map<String, Object> getTransientValues(NodeRef item)
    {
        Map<String, Object> values = new HashMap<String, Object>(3);
        ContentData contentData = getContentData(item);
        if (contentData != null)
        {
            values.put(EncodingFieldProcessor.KEY, contentData.getEncoding());
            values.put(MimetypeFieldProcessor.KEY, contentData.getMimetype());
            values.put(SizeFieldProcessor.KEY, contentData.getSize());
        }
        return values;
    }

    @Override
    protected Set<QName> getAspectNames(NodeRef nodeRef) 
    {
        return nodeService.getAspects(nodeRef);
    }

    @Override
    protected TypeDefinition getBaseType(NodeRef nodeRef) 
    {
        QName typeName = nodeService.getType(nodeRef);
        return dictionaryService.getType(typeName);
    }

    private ContentData getContentData(NodeRef nodeRef)
    {
        // Checks if the node is content and if so gets the ContentData
        QName type = this.nodeService.getType(nodeRef);
        ContentData content = null;
        if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT))
        {
            content = (ContentData) this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        }
        return content;
    }

    /**
     * Determines whether the given node represents a working copy, if it does
     * the name field is searched for and set to protected as the name field
     * should not be edited for a working copy.
     * 
     * If the node is not a working copy this method has no effect.
     * 
     * @param nodeRef NodeRef of node to check and potentially process
     * @param form The generated form
     */
    protected void processWorkingCopy(NodeRef nodeRef, Form form)
    {
        // if the node is a working copy ensure that the name field (id present)
        // is set to be protected as it can not be edited
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            // go through fields looking for name field
            for (FieldDefinition fieldDef : form.getFieldDefinitions())
            {
                if (fieldDef.getName().equals(ContentModel.PROP_NAME.toPrefixString(this.namespaceService)))
                {
                    fieldDef.setProtectedField(true);
                    
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("Set " + ContentModel.PROP_NAME.toPrefixString(this.namespaceService) +
                                    "field to protected as it is a working copy");
                    }
                    
                    break;
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#internalGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
     */
    @Override
    protected void internalGenerate(NodeRef item, List<String> fields, List<String> forcedFields, Form form,
            Map<String, Object> context)
    {
        super.internalGenerate(item, fields, forcedFields, form, context);
        processWorkingCopy(item, form);
    }

    /*
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#internalPersist(java.lang.Object, org.alfresco.repo.forms.FormData)
     */
    @Override
    protected NodeRef internalPersist(NodeRef item, FormData data)
    {
        if (logger.isDebugEnabled()) 
            logger.debug("Persisting form for: " + item);

        // persist the node
        persistNode(item, data);

        return item;
    }
}
