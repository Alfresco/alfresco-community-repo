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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidChildAssociationRefException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The light weight version store node service implementation.
 *
 * @author Roy Wetherall
 */
public class NodeServiceImpl implements NodeService, VersionModel
{
    private static Log logger = LogFactory.getLog(NodeServiceImpl.class);
    
    /**
     * Error messages
     */
    protected final static String MSG_UNSUPPORTED =
        "This operation is not supported by a version store implementation of the node service.";

    /**
     * The name of the spoofed root association
     */
    private static final QName rootAssocName = QName.createQName(VersionModel.NAMESPACE_URI, "versionedState");

    protected NodeService dbNodeService;
    @SuppressWarnings("unused")
    private SearchService searcher;
    protected DictionaryService dicitionaryService;
    protected ContentDataDAO contentDataDAO;

    /**
     * Sets the db node service, used as the version store implementation
     *
     * @param nodeService  the node service
     */
    public void setDbNodeService(NodeService nodeService)
    {
        this.dbNodeService = nodeService;
    }

    /**
     * Sets the searcher
     */
    public void setSearcher(SearchService searcher)
    {
        this.searcher = searcher;
    }

    /**
     * Sets the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dicitionaryService = dictionaryService;
    }

    /**
     * Set the DAO to resolved content data reference IDs into ContentData instances
     */
    public void setContentDataDAO(ContentDataDAO contentDataDAO)
    {
        this.contentDataDAO = contentDataDAO;
    }

    /**
     * No-op
     */
    public List<String> cleanup()
    {
        return Collections.emptyList();
    }

    /**
     * Delegates to the <code>NodeService</code> used as the version store implementation
     */
    public List<StoreRef> getStores()
    {
        return dbNodeService.getStores();
    }

    /**
     * Delegates to the <code>NodeService</code> used as the version store implementation
     */
    public StoreRef createStore(String protocol, String identifier)
    {
        return dbNodeService.createStore(protocol, identifier);
    }
    
    /**
     * @throws UnsupportedOperationException always
     */
    public void deleteStore(StoreRef storeRef)
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * Delegates to the <code>NodeService</code> used as the version store implementation
     */
    public boolean exists(StoreRef storeRef)
    {
        return dbNodeService.exists(VersionUtil.convertStoreRef(storeRef));
    }

    /**
     * Delegates to the <code>NodeService</code> used as the version store implementation
     */
    public boolean exists(NodeRef nodeRef)
    {
        return dbNodeService.exists(VersionUtil.convertNodeRef(nodeRef));
    }

    /**
     * Delegates to the <code>NodeService</code> used as the version store implementation
     */
    public Status getNodeStatus(NodeRef nodeRef)
    {
        return dbNodeService.getNodeStatus(nodeRef);
    }

    /**
     * Delegates to the <code>NodeService</code> used as the version store implementation
     */
    @Override
    public NodeRef getNodeRef(Long nodeId)
    {
        return dbNodeService.getNodeRef(nodeId);
    }

    /**
     * Delegates to the <code>NodeService</code> used as the version store implementation
     */
    public NodeRef getRootNode(StoreRef storeRef)
    {
        return dbNodeService.getRootNode(storeRef);
    }
    

    /**
     * Delegates to the <code>NodeService</code> used as the version store implementation
     */
    @Override
    public Set<NodeRef> getAllRootNodes(StoreRef storeRef)
    {
        return dbNodeService.getAllRootNodes(storeRef);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public ChildAssociationRef createNode(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName) throws InvalidNodeRefException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public ChildAssociationRef createNode(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName,
            Map<QName, Serializable> properties) throws InvalidNodeRefException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void deleteNode(NodeRef nodeRef) throws InvalidNodeRefException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public ChildAssociationRef addChild(NodeRef parentRef,
            NodeRef childRef,
            QName assocTypeQName,
            QName qname) throws InvalidNodeRefException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public List<ChildAssociationRef> addChild(Collection<NodeRef> parentRefs,
            NodeRef childRef,
            QName assocTypeQName,
            QName qname) throws InvalidNodeRefException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void removeChild(NodeRef parentRef, NodeRef childRef) throws InvalidNodeRefException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public boolean removeChildAssociation(ChildAssociationRef childAssocRef)
    {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean removeSeconaryChildAssociation(ChildAssociationRef childAssocRef)
    {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean removeSecondaryChildAssociation(ChildAssociationRef childAssocRef)
    {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public ChildAssociationRef moveNode(NodeRef nodeToMoveRef, NodeRef newParentRef, QName assocTypeQName, QName assocQName) throws InvalidNodeRefException
    {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void setChildAssociationIndex(ChildAssociationRef childAssocRef, int index) throws InvalidChildAssociationRefException
    {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * Type translation for version store
     */
    @SuppressWarnings("deprecation")
    public QName getType(NodeRef nodeRef) throws InvalidNodeRefException
    {
        return (QName)this.dbNodeService.getProperty(VersionUtil.convertNodeRef(nodeRef), PROP_QNAME_FROZEN_NODE_TYPE);
    }

    /**
     * @see org.alfresco.service.cmr.repository.NodeService#setType(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void setType(NodeRef nodeRef, QName typeQName) throws InvalidNodeRefException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void addAspect(NodeRef nodeRef, QName aspectRef, Map<QName, Serializable> aspectProperties) throws InvalidNodeRefException, InvalidAspectException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * Translation for version store
     */
    public boolean hasAspect(NodeRef nodeRef, QName aspectRef) throws InvalidNodeRefException, InvalidAspectException
    {
        return getAspects(nodeRef).contains(aspectRef);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void removeAspect(NodeRef nodeRef, QName aspectRef) throws InvalidNodeRefException, InvalidAspectException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * Translation for version store
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    public Set<QName> getAspects(NodeRef nodeRef) throws InvalidNodeRefException
    {
        return new HashSet<QName>(
                (ArrayList<QName>)this.dbNodeService.getProperty(VersionUtil.convertNodeRef(nodeRef), PROP_QNAME_FROZEN_ASPECTS));
    }

    /**
     * Property translation for version store
     */
    public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Map<QName, Serializable> result = new HashMap<QName, Serializable>();

        Collection<ChildAssociationRef> children = this.dbNodeService.getChildAssocs(VersionUtil.convertNodeRef(nodeRef), CHILD_QNAME_VERSIONED_ATTRIBUTES, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef child : children)
        {
            NodeRef versionedAttribute = child.getChildRef();

            // Get the QName and the value
            Serializable value = null;
            QName qName = (QName)this.dbNodeService.getProperty(versionedAttribute, PROP_QNAME_QNAME);
            PropertyDefinition propDef = this.dicitionaryService.getProperty(qName);

            Boolean isMultiValue = (Boolean)this.dbNodeService.getProperty(versionedAttribute, PROP_QNAME_IS_MULTI_VALUE);
            if (isMultiValue.booleanValue() == false)
            {
                value = this.dbNodeService.getProperty(versionedAttribute, PROP_QNAME_VALUE);
                
                if (propDef != null)
                {
                    DataTypeDefinition dataTypeDef = propDef.getDataType();
                    if (dataTypeDef != null)
                    {
                        if (dataTypeDef.getName().equals(DataTypeDefinition.CONTENT) && value instanceof Long)
                        {
                            try
                            {
                                value = contentDataDAO.getContentData((Long)value).getSecond();
                            }
                            catch (AlfrescoRuntimeException e)
                            {
                                logger.warn("ContentData with ID " + value + " no longer exists for versioned node " + nodeRef);
                            }
                        }
                        else
                        {
                            value = (Serializable)DefaultTypeConverter.INSTANCE.convert(dataTypeDef, value);
                        }
                    }
                    else
                    {
                        logger.warn("Null dataTypeDefinition for: " + propDef);
                    }
                }
                else
                {
                    logger.warn("Null propertyDefinition for: " + qName);
                }
            }
            else
            {
                value = this.dbNodeService.getProperty(versionedAttribute, PROP_QNAME_MULTI_VALUE);
            }

            result.put(qName, value);
        }

        return result;
    }

    /**
     * Property translation for version store
     */
    public Serializable getProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException
    {
        // TODO should be doing this with a search ...

        Map<QName, Serializable> properties = getProperties(VersionUtil.convertNodeRef(nodeRef));
        return properties.get(qname);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void setProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws InvalidNodeRefException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void addProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws InvalidNodeRefException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void setProperty(NodeRef nodeRef, QName qame, Serializable value) throws InvalidNodeRefException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void removeProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * The node will appear to be attached to the root of the version store
     *
     * @see NodeService#getParentAssocs(NodeRef)
     */
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef)
    {
        return getParentAssocs(nodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL);
    }

    /**
     * The node will apprear to be attached to the root of the version store
     *
     * @see NodeService#getParentAssocs(NodeRef, QNamePattern, QNamePattern)
     */
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern, QNamePattern qnamePattern)
    {
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>();
        if (qnamePattern.isMatch(rootAssocName) == true)
        {
            result.add(new ChildAssociationRef(
                    ContentModel.ASSOC_CHILDREN,
                    dbNodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, STORE_ID)),
                    rootAssocName,
                    nodeRef));
        }
        return result;
    }

    /**
     * @see RegexQNamePattern#MATCH_ALL
     * @see #getChildAssocs(NodeRef, QNamePattern, QNamePattern)
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef) throws InvalidNodeRefException
    {
        return getChildAssocs(VersionUtil.convertNodeRef(nodeRef), RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL);
    }

    
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
            QNamePattern qnamePattern, boolean preload) throws InvalidNodeRefException
    {
        return getChildAssocs(nodeRef, typeQNamePattern, qnamePattern);
    }

    /**
     * Performs conversion from version store properties to <i>real</i> associations
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern, QNamePattern qnamePattern) throws InvalidNodeRefException
    {
        // Get the child assocs from the version store
        List<ChildAssociationRef> childAssocRefs = this.dbNodeService.getChildAssocs(
                VersionUtil.convertNodeRef(nodeRef),
                RegexQNamePattern.MATCH_ALL, CHILD_QNAME_VERSIONED_CHILD_ASSOCS);
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>(childAssocRefs.size());
        for (ChildAssociationRef childAssocRef : childAssocRefs)
        {
            // Get the child reference
            NodeRef childRef = childAssocRef.getChildRef();
            NodeRef referencedNode = (NodeRef)this.dbNodeService.getProperty(childRef, ContentModel.PROP_REFERENCE);

            if (this.dbNodeService.exists(referencedNode) == true)
            {
                // get the qualified name of the frozen child association and filter out unwanted names
                QName qName = (QName)this.dbNodeService.getProperty(childRef, PROP_QNAME_ASSOC_QNAME);

                if (qnamePattern.isMatch(qName) == true)
                {
                    // Retrieve the isPrimary and nthSibling values of the forzen child association
                    QName assocType = (QName)this.dbNodeService.getProperty(childRef, PROP_QNAME_ASSOC_TYPE_QNAME);
                    boolean isPrimary = ((Boolean)this.dbNodeService.getProperty(childRef, PROP_QNAME_IS_PRIMARY)).booleanValue();
                    int nthSibling = ((Integer)this.dbNodeService.getProperty(childRef, PROP_QNAME_NTH_SIBLING)).intValue();

                    // Build a child assoc ref to add to the returned list
                    ChildAssociationRef newChildAssocRef = new ChildAssociationRef(
                            assocType,
                            nodeRef,
                            qName,
                            referencedNode,
                            isPrimary,
                            nthSibling);
                    result.add(newChildAssocRef);
                }
            }
        }

        // sort the results so that the order appears to be exactly as it was originally
        Collections.sort(result);

        return result;
    }
    
    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QName typeQName, QName qname, int maxResults,
            boolean preload) throws InvalidNodeRefException
    {
        List<ChildAssociationRef> result = getChildAssocs(nodeRef, typeQName, qname);
        if (result.size() > maxResults)
        {
            return result.subList(0, maxResults);
        }
        return result;
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, Set<QName> childNodeTypes)
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public List<ChildAssociationRef> getChildrenByName(NodeRef nodeRef, QName assocTypeQName, Collection<String> childNames)
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName)
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * Simulates the node begin attached ot the root node of the version store.
     */
    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef) throws InvalidNodeRefException
    {
        return new ChildAssociationRef(
                ContentModel.ASSOC_CHILDREN,
                dbNodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, STORE_ID)),
                rootAssocName,
                nodeRef);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException, AssociationExistsException
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setAssociations(NodeRef sourceRef, QName assocTypeQName, List<NodeRef> targetRefs)
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * Gets an association by ID.
     * 
     * @param assocId
     *            the association id
     * @return the association, or <code>null</code> if it does not exist
     */
    public AssociationRef getAssoc(Long id)
    {
        return null;
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
    {
        // Get the child assocs from the version store
        List<ChildAssociationRef> childAssocRefs = this.dbNodeService.getChildAssocs(
                VersionUtil.convertNodeRef(sourceRef),
                RegexQNamePattern.MATCH_ALL, CHILD_QNAME_VERSIONED_ASSOCS);
        List<AssociationRef> result = new ArrayList<AssociationRef>(childAssocRefs.size());
        for (ChildAssociationRef childAssocRef : childAssocRefs)
        {
            // Get the assoc reference
            NodeRef childRef = childAssocRef.getChildRef();
            NodeRef referencedNode = (NodeRef)this.dbNodeService.getProperty(childRef, ContentModel.PROP_REFERENCE);

            if (this.dbNodeService.exists(referencedNode) == true)
            {
                // get the qualified type name of the frozen child association and filter out unwanted names
                QName qName = (QName)this.dbNodeService.getProperty(childRef, PROP_QNAME_ASSOC_TYPE_QNAME);

                if (qnamePattern.isMatch(qName) == true)
                {
                    AssociationRef newAssocRef = new AssociationRef(null, sourceRef, qName, referencedNode);
                    result.add(newAssocRef);
                }
            }
        }

        return result;
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public List<AssociationRef> getSourceAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public Path getPath(NodeRef nodeRef) throws InvalidNodeRefException
    {
        ChildAssociationRef childAssocRef = getPrimaryParent(nodeRef);
        Path path = new Path();
        path.append(new Path.ChildAssocElement(childAssocRef));
        return path;
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public List<Path> getPaths(NodeRef nodeRef, boolean primaryOnly) throws InvalidNodeRefException
    {
        List<Path> paths = new ArrayList<Path>(1);
        paths.add(getPath(nodeRef));
        return paths;
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public NodeRef getStoreArchiveNode(StoreRef storeRef)
    {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public NodeRef restoreNode(NodeRef archivedNodeRef, NodeRef targetParentNodeRef, QName assocTypeQName, QName assocQName)
    {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public Collection<ChildAssociationRef> getChildAssocsWithoutParentAssocsOfType(NodeRef parent, QName assocTypeQName)
    {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }        

    /**
     * Gets, converts and adds the intrinsic properties to the current node's properties
     */
    public Long getNodeAclId(NodeRef nodeRef) throws InvalidNodeRefException
    {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    @Override
    public List<ChildAssociationRef> getChildAssocsByPropertyValue(
            NodeRef nodeRef, QName propertyQName, Serializable value)
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }

    @Override
    public List<NodeRef> findNodes(FindNodeParameters params)
    {
        // This operation is not supported for a version store
        throw new UnsupportedOperationException(MSG_UNSUPPORTED);
    }
}
