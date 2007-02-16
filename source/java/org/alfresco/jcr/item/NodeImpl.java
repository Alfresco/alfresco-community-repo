/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.jcr.item;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.MergeException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jcr.api.JCRNodeRef;
import org.alfresco.jcr.dictionary.ClassMap;
import org.alfresco.jcr.dictionary.NodeDefinitionImpl;
import org.alfresco.jcr.dictionary.NodeTypeImpl;
import org.alfresco.jcr.item.property.PropertyResolver;
import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.jcr.util.JCRProxyFactory;
import org.alfresco.jcr.version.VersionHistoryImpl;
import org.alfresco.jcr.version.VersionImpl;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;


/**
 * Alfresco Implementation of a JCR Node
 * 
 * @author David Caruana
 */
public class NodeImpl extends ItemImpl implements Node
{
    /** Node Reference to wrap */
    private NodeRef nodeRef;

    /** Proxy */
    private Node proxy = null;
    
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param nodeRef  node reference to wrap
     */
    public NodeImpl(SessionImpl context, NodeRef nodeRef)
    {
        super(context);
        this.nodeRef = nodeRef;
    }

    /**
     * Get Node Proxy
     * 
     * @param nodeImpl
     * @return
     */
    @Override
    public Node getProxy()
    {
        if (proxy == null)
        {
            proxy = (Node)JCRProxyFactory.create(this, Node.class, session); 
        }
        return proxy;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.Node#addNode(java.lang.String)
     */
    public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException
    {
        return addNode(relPath, null);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addNode(java.lang.String, java.lang.String)
     */
    public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException
    {
        ParameterCheck.mandatoryString("relPath", relPath);
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        DictionaryService dictionaryService = session.getRepositoryImpl().getServiceRegistry().getDictionaryService();
        
        // Determine parent node reference and new node name
        Path path = new JCRPath(session.getNamespaceResolver(), relPath).getPath();
        QName nodeName = null;
        NodeRef parentRef = null;
        if (path.size() == 1)
        {
            parentRef = nodeRef;
            nodeName = ((JCRPath.SimpleElement)path.get(0)).getQName();
        }
        else
        {
            Path parentPath = path.subPath(path.size() -2);
            parentRef = ItemResolver.getNodeRef(session, nodeRef, parentPath.toPrefixString(session.getNamespaceResolver()));
            if (parentRef == null)
            {
                throw new PathNotFoundException("Path '" + relPath + "' does not exist from node " + nodeRef);
            }
            nodeName = ((JCRPath.SimpleElement)path.get(path.size() -1)).getQName();
        }

        // Check for invalid node name
        // TODO: Replace with proper name validation
        if (nodeName.getLocalName().indexOf('[') != -1 || nodeName.getLocalName().indexOf(']') != -1)
        {
            throw new RepositoryException("Node name '" + nodeName + "' is invalid");
        }

        // Determine child association to add node under
        ChildAssociationDefinition childAssocDef = null;
        QName nodeType = null;
        if (primaryNodeTypeName == null || primaryNodeTypeName.length() == 0)
        {
            childAssocDef = getDefaultChildAssocDefForParent(nodeService, dictionaryService, parentRef);
            nodeType = childAssocDef.getTargetClass().getName();
        }
        else
        {
            nodeType = QName.createQName(primaryNodeTypeName, session.getNamespaceResolver());  
            childAssocDef = getNodeTypeChildAssocDefForParent(nodeService, dictionaryService, parentRef, nodeType);
        }

        // Do not allow creation of sys:base (it's really an abstract type)
        // TODO: Consider adding abstract to the content model
        if (nodeType.equals(ContentModel.TYPE_BASE))
        {
            throw new RepositoryException("Node type of node to add is " + nodeType.toPrefixString(session.getNamespaceResolver()) + " which is an abstract type");
        }
        
        // Create node
        // Note: Integrity exception will be thrown when the node is saved
        ChildAssociationRef childRef = nodeService.createNode(parentRef, childAssocDef.getName(), nodeName, nodeType);
        NodeImpl nodeImpl = new NodeImpl(session, childRef.getChildRef());
        return nodeImpl.getProxy();
    }

    /**
     * Get the default child association definition for the specified node
     * 
     * @param nodeService   node service
     * @param dictionaryService  dictionary service
     * @param nodeRef  node reference
     * @return  child association definition
     */
    private ChildAssociationDefinition getDefaultChildAssocDefForParent(NodeService nodeService, DictionaryService dictionaryService, NodeRef nodeRef)
    {
        QName type = nodeService.getType(nodeRef);
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        ClassDefinition classDef = dictionaryService.getAnonymousType(type, aspects);
        Map<QName, ChildAssociationDefinition> childAssocs = classDef.getChildAssociations();
        if (childAssocs.size() != 1)
        {
            throw new AlfrescoRuntimeException("Cannot determine node type for child within parent " + nodeRef);
        }
        ChildAssociationDefinition childAssocDef = childAssocs.values().iterator().next();
        return childAssocDef;
    }
    
    /**
     * Get the child association definition whose target matches the specified node type for the specified node
     * 
     * @param nodeService  node service
     * @param dictionaryService  dictionary service
     * @param nodeRef   node reference
     * @param nodeType  node type to find child association definition for
     * @return  child association definition
     */
    private ChildAssociationDefinition getNodeTypeChildAssocDefForParent(NodeService nodeService, DictionaryService dictionaryService, NodeRef nodeRef, QName nodeType)
    {
        ChildAssociationDefinition nodeTypeChildAssocDef = null; 
        QName type = nodeService.getType(nodeRef);
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        ClassDefinition classDef = dictionaryService.getAnonymousType(type, aspects);
        Map<QName, ChildAssociationDefinition> childAssocs = classDef.getChildAssociations();
        for (ChildAssociationDefinition childAssocDef : childAssocs.values())
        {
            QName targetClass = childAssocDef.getTargetClass().getName();
            if (dictionaryService.isSubClass(nodeType, targetClass))
            {
                if (nodeTypeChildAssocDef != null)
                {
                    throw new AlfrescoRuntimeException("Cannot determine child association for node type '" + nodeType + " within parent " + nodeRef);
                }
                nodeTypeChildAssocDef = childAssocDef;
            }
        }
        if (nodeTypeChildAssocDef == null)
        {
            throw new AlfrescoRuntimeException("Cannot determine child association for node type '" + nodeType + " within parent " + nodeRef);
        }
        return nodeTypeChildAssocDef;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.Item#remove()
     */
    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        
        // Note: remove the primary child association, therefore forcing a delete of the node (including any secondary child
        //       associations)
        ChildAssociationRef assocRef = nodeService.getPrimaryParent(nodeRef);
        NodeRef parentRef = assocRef.getParentRef();
        if (parentRef == null)
        {
            throw new ConstraintViolationException("Cannot remove the root node");
        }
        nodeService.removeChild(parentRef, nodeRef);
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.Node#orderBefore(java.lang.String, java.lang.String)
     */
    public void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value)
     */
    public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(value, -1);
        return (value == null ) ? null : property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value, int)
     */
    public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(value, type);
        return (value == null ) ? null : property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value[])
     */
    public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(values, -1);
        return (values == null ) ? null : property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value[], int)
     */
    public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(values, type);
        return (values == null ) ? null : property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String[])
     */
    public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(values, -1);
        return (values == null ) ? null : property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String[], int)
     */
    public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(values, type);
        return (values == null ) ? null : property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String)
     */
    public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(value, -1);
        return (value == null ) ? null : property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String, int)
     */
    public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(value, type);
        return (value == null ) ? null : property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, java.io.InputStream)
     */
    public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(value, -1);
        return (value == null ) ? null : property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, boolean)
     */
    public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(value, -1);
        return property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, double)
     */
    public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(value, -1);
        return property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, long)
     */
    public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue(value, -1);
        return property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, java.util.Calendar)
     */
    public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue((value == null) ? null : value.getTime(), -1);
        return (value == null ) ? null : property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Node)
     */
    public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        QName propertyName = QName.createQName(name, session.getNamespaceResolver());
        PropertyImpl property = new PropertyImpl(this, propertyName);
        property.setPropertyValue((value == null) ? null : JCRNodeRef.getNodeRef(value), -1);
        return (value == null ) ? null : property;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getNode(java.lang.String)
     */
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException
    {
        NodeImpl nodeImpl = ItemResolver.findNode(session, nodeRef, relPath);
        return nodeImpl.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getNodes()
     */
    public NodeIterator getNodes() throws RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef);        
        NodeIterator iterator = new ChildAssocNodeIteratorImpl(session, childAssocs);
        return iterator;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getNodes(java.lang.String)
     */
    public NodeIterator getNodes(String namePattern) throws RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        JCRPatternMatch match = new JCRPatternMatch(namePattern, session.getNamespaceResolver());
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef, RegexQNamePattern.MATCH_ALL, match);        
        NodeIterator iterator = new ChildAssocNodeIteratorImpl(session, childAssocs);
        return iterator;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getProperty(java.lang.String)
     */
    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException
    {
        JCRPath jcrPath = new JCRPath(session.getNamespaceResolver(), relPath);
        Path path = jcrPath.getPath();
        if (path.size() == 1)
        {
            QName propertyName = ((JCRPath.SimpleElement)path.get(0)).getQName();
            return PropertyResolver.createProperty(this, propertyName).getProxy();
        }

        ItemImpl itemImpl = ItemResolver.findItem(session, nodeRef, relPath);
        if (itemImpl == null || !(itemImpl instanceof PropertyImpl))
        {
            throw new PathNotFoundException("Property path " + relPath + " not found from node " + nodeRef);
        }
        return ((PropertyImpl)itemImpl).getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getProperties()
     */
    public PropertyIterator getProperties() throws RepositoryException
    {
        List<PropertyImpl> properties = PropertyResolver.createProperties(this, null);
        PropertyIterator iterator = new PropertyListIterator(properties);
        return iterator;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getProperties(java.lang.String)
     */
    public PropertyIterator getProperties(String namePattern) throws RepositoryException
    {
        JCRPatternMatch match = new JCRPatternMatch(namePattern, session.getNamespaceResolver());
        List<PropertyImpl> properties = PropertyResolver.createProperties(this, match);
        PropertyIterator iterator = new PropertyListIterator(properties);
        return iterator;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getPrimaryItem()
     */
    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException
    {
        // Note: Alfresco does not support the notion of primary item
        throw new ItemNotFoundException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getUUID()
     */
    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException
    {
        return nodeRef.getId();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getIndex()
     */
    public int getIndex() throws RepositoryException
    {
        int index = 1;
        String name = getName();
        if (name != null)
        {
            // TODO: Look at more efficient approach
            SearchService searchService = session.getRepositoryImpl().getServiceRegistry().getSearchService();
            List<NodeRef> siblings = searchService.selectNodes(nodeRef, "../" + name, null, session.getNamespaceResolver(), false);
            for (NodeRef sibling : siblings)
            {
                if (sibling.equals(nodeRef))
                {
                    break;
                }
                index++;
            }
        }        
        return index;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getReferences()
     */
    public PropertyIterator getReferences() throws RepositoryException
    {
        // Note: Lookup for references not supported for now
        return new PropertyListIterator(new ArrayList<PropertyImpl>());
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#hasNode(java.lang.String)
     */
    public boolean hasNode(String relPath) throws RepositoryException
    {
        return ItemResolver.nodeExists(session, nodeRef, relPath);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#hasProperty(java.lang.String)
     */
    public boolean hasProperty(String relPath) throws RepositoryException
    {
        JCRPath jcrPath = new JCRPath(session.getNamespaceResolver(), relPath);
        Path path = jcrPath.getPath();
        if (path.size() == 1)
        {
            QName propertyName = ((JCRPath.SimpleElement)path.get(0)).getQName();
            return PropertyResolver.hasProperty(this, propertyName);
        }

        return ItemResolver.itemExists(session, nodeRef, relPath);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#hasNodes()
     */
    public boolean hasNodes() throws RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef);        
        return childAssocs.size() > 0;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#hasProperties()
     */
    public boolean hasProperties() throws RepositoryException
    {
        // Note: nt:base has a mandatory primaryType property for which we don't have security access control
        return true;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getPrimaryNodeType()
     */
    public NodeType getPrimaryNodeType() throws RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        QName type = nodeService.getType(nodeRef);
        return session.getTypeManager().getNodeTypeImpl(type);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getMixinNodeTypes()
     */
    public NodeType[] getMixinNodeTypes() throws RepositoryException
    {
        // Add aspects defined by node
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        NodeType[] nodeTypes = new NodeType[aspects.size() + 1];
        int i = 0;
        for (QName aspect : aspects)
        {
            nodeTypes[i++] = session.getTypeManager().getNodeTypeImpl(aspect);
            QName mixin = ClassMap.convertClassToType(aspect);
            if (mixin != null)
            {
                nodeTypes[i++] = session.getTypeManager().getNodeTypeImpl(mixin);
            }
        }
        
        return nodeTypes;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#isNodeType(java.lang.String)
     */
    public boolean isNodeType(String nodeTypeName) throws RepositoryException
    {
        QName nodeType = QName.createQName(nodeTypeName, session.getNamespaceResolver());
        
        // is it one of standard types
        if (nodeType.equals(NodeTypeImpl.MIX_REFERENCEABLE) || nodeType.equals(NodeTypeImpl.NT_BASE))
        {
            return true;
        }

        // map JCR mixins to Alfresco mixins
        QName nodeClass = ClassMap.convertTypeToClass(nodeType);
        if (nodeClass == null)
        {
            nodeClass = nodeType;
        }
        
        // determine via class hierarchy
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        DictionaryService dictionaryService = session.getRepositoryImpl().getServiceRegistry().getDictionaryService();

        // first, check the type
        QName type = nodeService.getType(nodeRef);
        if (dictionaryService.isSubClass(nodeClass, type))
        {
            return true;
        }
        
        // second, check the aspects
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        for (QName aspect : aspects)
        {
            if (dictionaryService.isSubClass(nodeClass, aspect))
            {
                return true;
            }
        }

        // no, its definitely not of the specified type
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#addMixin(java.lang.String)
     */
    public void addMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException
    {
        // map JCR mixins to Alfresco mixins
        QName mixin = QName.createQName(mixinName, session.getNamespaceResolver());
        QName aspect = ClassMap.convertTypeToClass(mixin);
        if (aspect == null)
        {
            aspect = mixin;
        }
        
        // retrieve aspect definition
        DictionaryService dictionaryService = session.getRepositoryImpl().getServiceRegistry().getDictionaryService();
        AspectDefinition aspectDef = dictionaryService.getAspect(aspect);
        if (aspectDef == null)
        {
            throw new NoSuchNodeTypeException("Unknown mixin name '" + mixinName + "'");
        }

        // apply aspect
        ClassMap.AddMixin addMixin = ClassMap.getAddMixin(aspect);
        Map<QName, Serializable> initialProperties = addMixin.preAddMixin(session, nodeRef);
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        nodeService.addAspect(nodeRef, aspect, initialProperties);
        addMixin.postAddMixin(session, nodeRef);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#removeMixin(java.lang.String)
     */
    public void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException
    {
        // map JCR mixins to Alfresco mixins
        QName mixin = QName.createQName(mixinName, session.getNamespaceResolver());
        QName aspect = ClassMap.convertTypeToClass(mixin);
        if (aspect == null)
        {
            aspect = mixin;
        }
        
        // retrieve aspect definition
        DictionaryService dictionaryService = session.getRepositoryImpl().getServiceRegistry().getDictionaryService();
        AspectDefinition aspectDef = dictionaryService.getAspect(aspect);
        if (aspectDef == null)
        {
            throw new NoSuchNodeTypeException("Unknown mixin name '" + mixinName + "'");
        }

        // check the node actually has the mixin
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        Set<QName> nodeAspects = nodeService.getAspects(nodeRef);
        if (!nodeAspects.contains(aspect))
        {
            throw new NoSuchNodeTypeException("Node " + nodeRef.getId() + " does not have the mixin " + mixin);
        }
        
        // remove aspect
        ClassMap.RemoveMixin removeMixin = ClassMap.getRemoveMixin(aspect);
        removeMixin.preRemoveMixin(session, nodeRef);
        nodeService.removeAspect(nodeRef, aspect);
        removeMixin.postRemoveMixin(session, nodeRef);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#canAddMixin(java.lang.String)
     */
    public boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException
    {
        // map JCR mixins to Alfresco mixins
        QName mixin = QName.createQName(mixinName, session.getNamespaceResolver());
        QName aspect = ClassMap.convertTypeToClass(mixin);
        if (aspect == null)
        {
            aspect = mixin;
        }
        
        // retrieve aspect definition
        DictionaryService dictionaryService = session.getRepositoryImpl().getServiceRegistry().getDictionaryService();
        AspectDefinition aspectDef = dictionaryService.getAspect(aspect);
        if (aspectDef == null)
        {
            throw new NoSuchNodeTypeException("Unknown mixin name '" + mixinName + "'");
        }

        // TODO: check for write permission

        // check for locked node
        LockService lockService = session.getRepositoryImpl().getServiceRegistry().getLockService();
        LockStatus lockStatus = lockService.getLockStatus(nodeRef);
        if (lockStatus == LockStatus.LOCKED)
        {
            return false;
        }
        
        // mixin addition is allowed
        return true;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getDefinition()
     */
    public NodeDefinition getDefinition() throws RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        DictionaryService dictionaryService = session.getRepositoryImpl().getServiceRegistry().getDictionaryService();
        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(this.nodeRef);
        ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition)dictionaryService.getAssociation(childAssocRef.getTypeQName());
        NodeDefinition nodeDef = new NodeDefinitionImpl(session.getTypeManager(), childAssocDef);
        return nodeDef;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#checkin()
     */
    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException
    {
        // check this node is versionable
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
        {
            throw new UnsupportedRepositoryOperationException("Node " + nodeRef + " is not versionable");
        }

        Version version = null;
        if (!isCheckedOut())
        {
            // return current version
            version = getBaseVersion();
        }
        else
        {
            // create a new version snapshot
            VersionService versionService = session.getRepositoryImpl().getServiceRegistry().getVersionService();
            org.alfresco.service.cmr.version.Version versionNode = versionService.createVersion(nodeRef, null);
            org.alfresco.service.cmr.version.VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
            version = new VersionImpl(new VersionHistoryImpl(session, versionHistory), versionNode).getProxy();
            
            // set to 'read only'
            LockService lockService = session.getRepositoryImpl().getServiceRegistry().getLockService();
            lockService.lock(nodeRef, LockType.READ_ONLY_LOCK);
        }
        
        return version;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#checkout()
     */
    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException
    {
        // check this node is versionable
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
        {
            throw new UnsupportedRepositoryOperationException("Node " + nodeRef + " is not versionable");
        }

        // remove 'read only' lock
        if (!isCheckedOut())
        {
            LockService lockService = session.getRepositoryImpl().getServiceRegistry().getLockService();
            lockService.unlock(nodeRef);
        }
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#doneMerge(javax.jcr.version.Version)
     */
    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#cancelMerge(javax.jcr.version.Version)
     */
    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#update(java.lang.String)
     */
    public void update(String srcWorkspaceName) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#merge(java.lang.String, boolean)
     */
    public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getCorrespondingNodePath(java.lang.String)
     */
    public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#isCheckedOut()
     */
    public boolean isCheckedOut() throws RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
        {
            // it's not versionable, therefore it's checked-out and writable
            // TODO: Do not yet take into consideration versionable ancestor
            return true;
        }

        // it's versionable, use the lock to determine if it's checked-out
        LockService lockService = session.getRepositoryImpl().getServiceRegistry().getLockService();
        LockType lockType = lockService.getLockType(nodeRef);
        if (lockType == null)
        {
            // it's not locked at all
            return true;
        }

        // it's only checked-in when a read-only locked
        return (lockType.equals(LockType.READ_ONLY_LOCK)) ? false : true;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#restore(java.lang.String, boolean)
     */
    public void restore(String versionName, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException
    {
        // check this node is versionable
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
        {
            throw new UnsupportedRepositoryOperationException("Node " + nodeRef + " is not versionable");
        }

        // retrieve version for label
        VersionService versionService = session.getRepositoryImpl().getServiceRegistry().getVersionService();
        org.alfresco.service.cmr.version.VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
        org.alfresco.service.cmr.version.Version version = versionHistory.getVersion(versionName);
        if (version == null)
        {
            throw new VersionException("Version name " + versionName + " does not exist in the version history of node " + nodeRef);
        }

        // unlock if necessary
        LockService lockService = session.getRepositoryImpl().getServiceRegistry().getLockService();
        LockType lockType = lockService.getLockType(nodeRef);
        if (lockType != null)
        {
            lockService.unlock(nodeRef);
        }
        
        // revert to version
        versionService.revert(nodeRef, version);
        lockService.lock(nodeRef, LockType.READ_ONLY_LOCK);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#restore(javax.jcr.version.Version, boolean)
     */
    public void restore(Version version, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException
    {
        restore(version.getName(), removeExisting);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#restore(javax.jcr.version.Version, java.lang.String, boolean)
     */
    public void restore(Version version, String relPath, boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#restoreByLabel(java.lang.String, boolean)
     */
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getVersionHistory()
     */
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException
    {
        // check this node is versionable
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
        {
            throw new UnsupportedRepositoryOperationException("Node " + nodeRef + " is not versionable");
        }

        // construct version history
        VersionService versionService = session.getRepositoryImpl().getServiceRegistry().getVersionService();
        org.alfresco.service.cmr.version.VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
        return new VersionHistoryImpl(session, versionHistory).getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getBaseVersion()
     */
    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException
    {
        // check this node is versionable
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
        {
            throw new UnsupportedRepositoryOperationException("Node " + nodeRef + " is not versionable");
        }

        // construct version
        VersionService versionService = session.getRepositoryImpl().getServiceRegistry().getVersionService();
        org.alfresco.service.cmr.version.VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
        org.alfresco.service.cmr.version.Version version = versionService.getCurrentVersion(nodeRef);
        return new VersionImpl(new VersionHistoryImpl(session, versionHistory), version).getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#lock(boolean, boolean)
     */
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException
    {
        // note: alfresco does not yet support session scoped locks
        if (isSessionScoped)
        {
            throw new UnsupportedRepositoryOperationException("Session scope locking is not supported.");
        }
        
        // check this node is lockable
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
        {
            throw new LockException("Node " + nodeRef + " does is not lockable.");
        }
        
        // lock the node
        LockService lockService = session.getRepositoryImpl().getServiceRegistry().getLockService();
        lockService.lock(nodeRef, LockType.WRITE_LOCK, 0, isDeep);

        // return lock
        return new LockImpl(this).getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#getLock()
     */
    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException
    {
        // check this node is lockable
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
        {
            throw new LockException("Node " + nodeRef + " does is not lockable.");
        }

        // return lock
        return new LockImpl(this).getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#unlock()
     */
    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException
    {
        // check this node is lockable
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
        {
            throw new LockException("Node " + nodeRef + " does is not lockable.");
        }

        // unlock
        LockService lockService = session.getRepositoryImpl().getServiceRegistry().getLockService();
        lockService.unlock(nodeRef, true);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#holdsLock()
     */
    public boolean holdsLock() throws RepositoryException
    {
        // note: for now, alfresco doesn't distinguish between lock holder and locked
        return isLocked();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Node#isLocked()
     */
    public boolean isLocked() throws RepositoryException
    {
        LockService lockService = session.getRepositoryImpl().getServiceRegistry().getLockService();
        LockStatus lockStatus = lockService.getLockStatus(getNodeRef());
        return lockStatus.equals(LockStatus.LOCK_OWNER) || lockStatus.equals(LockStatus.LOCKED);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Item#getName()
     */
    public String getName() throws RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
        QName childName = parentAssoc.getQName();
        return (childName == null) ? "" : childName.toPrefixString(session.getNamespaceResolver());
    }

    /* (non-Javadoc)
     * @see javax.jcr.Item#isNode()
     */
    public boolean isNode()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Item#getParent()
     */
    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
        if (parentAssoc == null || parentAssoc.getParentRef() == null)
        {
            // TODO: Distinguish between ItemNotFound and AccessDenied
            throw new ItemNotFoundException("Parent of node " + nodeRef + " does not exist.");
        }
        NodeImpl nodeImpl = new NodeImpl(session, parentAssoc.getParentRef());
        return nodeImpl.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Item#getPath()
     */
    public String getPath() throws RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        SearchService searchService = session.getRepositoryImpl().getServiceRegistry().getSearchService();
        Path path = nodeService.getPath(nodeRef);
        
        // Add indexes for same name siblings
        // TODO: Look at more efficient approach
        for (int i = path.size() - 1; i >= 0; i--)
        {
            Path.Element pathElement = path.get(i);
            if (i > 0 && pathElement instanceof Path.ChildAssocElement)
            {
                int index = 1;
                String searchPath = path.subPath(i).toPrefixString(session.getNamespaceResolver());
                List<NodeRef> siblings = searchService.selectNodes(nodeRef, searchPath, null, session.getNamespaceResolver(), false);
                if (siblings.size() > 1)
                {
                    ChildAssociationRef childAssoc = ((Path.ChildAssocElement)pathElement).getRef();
                    NodeRef childRef = childAssoc.getChildRef();
                    for (NodeRef sibling : siblings)
                    {
                        if (sibling.equals(childRef))
                        {
                            childAssoc.setNthSibling(index);
                            break;
                        }
                        index++;
                    }
                }
            }
        }
        
        return path.toPrefixString(session.getNamespaceResolver());
    }

    /* (non-Javadoc)
     * @see javax.jcr.Item#getDepth()
     */
    public int getDepth() throws RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        Path path = nodeService.getPath(nodeRef);
        // Note: Root is at depth 0
        return path.size() -1;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Item#getAncestor(int)
     */
    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException
    {
        // Retrieve primary parent path for node
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        Path path = nodeService.getPath(nodeRef);
        if (depth < 0 || depth > (path.size() - 1))
        {
            throw new ItemNotFoundException("Ancestor at depth " + depth + " not found for node " + nodeRef);
        }

        // Extract path element at requested depth
        Element element = path.get(depth);
        if (!(element instanceof Path.ChildAssocElement))
        {
            throw new RepositoryException("Path element at depth " + depth + " is not a node");
        }
        Path.ChildAssocElement childAssocElement = (Path.ChildAssocElement)element;
        
        // Create node
        NodeRef ancestorNodeRef = childAssocElement.getRef().getChildRef();
        NodeImpl nodeImpl = new NodeImpl(session, ancestorNodeRef);
        return nodeImpl.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Item#isSame(javax.jcr.Item)
     */
    public boolean isSame(Item otherItem) throws RepositoryException
    {
        return getProxy().equals(otherItem);
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.Item#accept(javax.jcr.ItemVisitor)
     */
    public void accept(ItemVisitor visitor) throws RepositoryException
    {
        visitor.visit(getProxy());
    }
    
    /**
     * Gets the Alfresco Node Reference
     * 
     * @return  the node reference
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (!(obj instanceof NodeImpl))
        {
            return false;
        }
        NodeImpl other = (NodeImpl)obj;
        return this.nodeRef.equals(other.nodeRef);
    }

    @Override
    public int hashCode()
    {
        return nodeRef.hashCode();
    }
    
}
