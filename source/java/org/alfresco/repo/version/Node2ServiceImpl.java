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
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

/**
 * The version2 store node service implementation
 */
public class Node2ServiceImpl extends NodeServiceImpl implements NodeService, Version2Model
{
    /**
     * The name of the spoofed root association
     */
    private static final QName rootAssocName = QName.createQName(Version2Model.NAMESPACE_URI, "versionedState");
    
    
    /**
     * Type translation for version store
     */
    public QName getType(NodeRef nodeRef) throws InvalidNodeRefException
    {
        if (nodeRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID))
        {
            return super.getType(nodeRef);
        }
        
        // frozen node type -> replaced by actual node type of the version node
        return (QName)this.dbNodeService.getType(VersionUtil.convertNodeRef(nodeRef));
    }
    
    /**
     * Aspects translation for version store
     */
    public Set<QName> getAspects(NodeRef nodeRef) throws InvalidNodeRefException
    {
        if (nodeRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID))
        {
            return super.getAspects(nodeRef);
        }
        
        Set<QName> aspects = this.dbNodeService.getAspects(VersionUtil.convertNodeRef(nodeRef));
        aspects.remove(Version2Model.ASPECT_VERSION);
        return aspects;
    }
    
    /**
     * Properties translation for version store
     */
    public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException
    {
        if (nodeRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID))
        {
            return super.getProperties(nodeRef);
        }
        
        Map<QName, Serializable> props = dbNodeService.getProperties(VersionUtil.convertNodeRef(nodeRef));
        VersionUtil.convertFrozenToOriginalProps(props);
        
        return props;
    }
    
    /**
     * Property translation for version store
     */
    public Serializable getProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException
    {
        if (nodeRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID))
        {
            return super.getProperty(nodeRef, qname);
        }
        
        // TODO optimise - get property directly and convert if needed
        Map<QName, Serializable> properties = getProperties(VersionUtil.convertNodeRef(nodeRef));
        return properties.get(qname);
    }
    
    /**
     * The node will appear to be attached to the root of the version store
     *
     * @see NodeService#getParentAssocs(NodeRef, QNamePattern, QNamePattern)
     */
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern, QNamePattern qnamePattern)
    {
        if (nodeRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID))
        {
            return super.getParentAssocs(nodeRef, typeQNamePattern, qnamePattern);
        }
        
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>();
        if (qnamePattern.isMatch(rootAssocName) == true)
        {
            result.add(new ChildAssociationRef(
                    ContentModel.ASSOC_CHILDREN,
                    dbNodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID)),
                    rootAssocName,
                    nodeRef));
        }
        return result;
    }
    
    /**
     * Child Assocs translation for version store
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern, QNamePattern qnamePattern) throws InvalidNodeRefException
    {
        if (nodeRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID))
        {
            return super.getChildAssocs(nodeRef, typeQNamePattern, qnamePattern);
        }
        
        // Get the child assoc references from the version store
        List<ChildAssociationRef> childAssocRefs = this.dbNodeService.getChildAssocs(
                VersionUtil.convertNodeRef(nodeRef),
                typeQNamePattern, qnamePattern);
        
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>(childAssocRefs.size());
        
        for (ChildAssociationRef childAssocRef : childAssocRefs)
        {
            if (! childAssocRef.getTypeQName().equals(Version2Model.CHILD_QNAME_VERSIONED_ASSOCS))
            {
                // Get the child reference
                NodeRef childRef = childAssocRef.getChildRef();
                NodeRef referencedNode = (NodeRef)this.dbNodeService.getProperty(childRef, ContentModel.PROP_REFERENCE);
                
                if (this.dbNodeService.exists(referencedNode))
                {
                    // Build a child assoc ref to add to the returned list
                    ChildAssociationRef newChildAssocRef = new ChildAssociationRef(
                            childAssocRef.getTypeQName(),
                            childAssocRef.getParentRef(),
                            childAssocRef.getQName(),
                            referencedNode,
                            childAssocRef.isPrimary(),
                            childAssocRef.getNthSibling());
                    
                    result.add(newChildAssocRef);
                }
            }
        }
        
        // sort the results so that the order appears to be exactly as it was originally
        Collections.sort(result);
        
        return result;
    }
    
    /**
     * Simulates the node begin attached to the root node of the version store.
     */
    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef) throws InvalidNodeRefException
    {
        if (nodeRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID))
        {
            return super.getPrimaryParent(nodeRef);
        }
        
        return new ChildAssociationRef(
                ContentModel.ASSOC_CHILDREN,
                dbNodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID)),
                rootAssocName,
                nodeRef);
    }
    
    /**
     * Assocs translation for version store
     * 
     * @since 3.3 (Ent)
     */
    @Override
    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
    {
        if (sourceRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID))
        {
            return super.getTargetAssocs(sourceRef, qnamePattern);
        }
        
        // Get the assoc references from the version store
        List<ChildAssociationRef> childAssocRefs = this.dbNodeService.getChildAssocs(
                VersionUtil.convertNodeRef(sourceRef),
                Version2Model.CHILD_QNAME_VERSIONED_ASSOCS, qnamePattern);
        
        List<AssociationRef> result = new ArrayList<AssociationRef>(childAssocRefs.size());
        
        for (ChildAssociationRef childAssocRef : childAssocRefs)
        {
            // Get the assoc reference
            NodeRef childRef = childAssocRef.getChildRef();
            NodeRef referencedNode = (NodeRef)this.dbNodeService.getProperty(childRef, ContentModel.PROP_REFERENCE);
            
            if (this.dbNodeService.exists(referencedNode))
            {
                Long assocDbId = (Long)this.dbNodeService.getProperty(childRef, Version2Model.PROP_QNAME_ASSOC_DBID);
                
                // Build an assoc ref to add to the returned list
                AssociationRef newAssocRef = new AssociationRef(
                        assocDbId,
                        sourceRef,
                        childAssocRef.getQName(),
                        referencedNode);
                
                result.add(newAssocRef);
                }
        }
        
        return result;
    }
}
