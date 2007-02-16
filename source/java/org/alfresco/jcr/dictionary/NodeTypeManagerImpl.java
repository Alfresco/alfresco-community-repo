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
package org.alfresco.jcr.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Alfresco implementation of JCR Node Type Manager
 * 
 * @author David Caruana
 */
public class NodeTypeManagerImpl implements NodeTypeManager
{
    private SessionImpl session;
    private NamespaceService namespaceService;
    
    /**
     * Construct
     * 
     * @param dictionaryService  dictionary service
     * @param namespaceService  namespace service (global repository registry)
     */
    public NodeTypeManagerImpl(SessionImpl session, NamespaceService namespaceService)
    {
        this.session = session;
        this.namespaceService = namespaceService;
    }
    
    /**
     * Get Dictionary Service
     * 
     * @return  the dictionary service
     */    
    public SessionImpl getSession()
    {
        return session;
    }

    /**
     * Get Namespace Service
     * 
     * @return  the namespace service
     */
    public NamespaceService getNamespaceService()
    {
        return namespaceService;
    }

    /**
     * Get Node Type Implementation for given Class Name
     * 
     * @param nodeTypeName  alfresco class name 
     * @return  the node type
     */
    public NodeTypeImpl getNodeTypeImpl(QName nodeTypeName)
    {
        // TODO: Might be worth caching here... wait and see
        NodeTypeImpl nodeType = null;
        ClassDefinition definition = session.getRepositoryImpl().getServiceRegistry().getDictionaryService().getClass(nodeTypeName);
        if (definition != null)
        {
            nodeType = new NodeTypeImpl(this, definition);
        }
        return nodeType;
    }

    /**
     * Get Property Definition Implementation for given Property Name
     * 
     * @param propertyName  alfresco property name 
     * @return  the property
     */
    public PropertyDefinitionImpl getPropertyDefinitionImpl(QName propertyName)
    {
        // TODO: Might be worth caching here... wait and see
        PropertyDefinitionImpl propDef = null;
        PropertyDefinition definition = session.getRepositoryImpl().getServiceRegistry().getDictionaryService().getProperty(propertyName);
        if (definition != null)
        {
            propDef = new PropertyDefinitionImpl(this, definition);
        }
        return propDef;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeTypeManager#getNodeType(java.lang.String)
     */
    public NodeType getNodeType(String nodeTypeName) throws NoSuchNodeTypeException, RepositoryException
    {
        QName name = QName.createQName(nodeTypeName, namespaceService);
        NodeTypeImpl nodeTypeImpl = getNodeTypeImpl(name);
        if (nodeTypeImpl == null)
        {
            throw new NoSuchNodeTypeException("Node type " + nodeTypeName + " does not exist");
        }
        return nodeTypeImpl;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeTypeManager#getAllNodeTypes()
     */
    public NodeTypeIterator getAllNodeTypes() throws RepositoryException
    {
        Collection<QName> typeNames = session.getRepositoryImpl().getServiceRegistry().getDictionaryService().getAllTypes();
        Collection<QName> aspectNames = session.getRepositoryImpl().getServiceRegistry().getDictionaryService().getAllAspects();
        List<QName> typesList = new ArrayList<QName>(typeNames.size() + aspectNames.size());
        typesList.addAll(typeNames);
        typesList.addAll(aspectNames);
        return new NodeTypeNameIterator(this, typesList);
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeTypeManager#getPrimaryNodeTypes()
     */
    public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException
    {
        Collection<QName> typeNames = session.getRepositoryImpl().getServiceRegistry().getDictionaryService().getAllTypes();
        List<QName> typesList = new ArrayList<QName>(typeNames.size());
        typesList.addAll(typeNames);
        return new NodeTypeNameIterator(this, typesList);
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeTypeManager#getMixinNodeTypes()
     */
    public NodeTypeIterator getMixinNodeTypes() throws RepositoryException
    {
        Collection<QName> typeNames = session.getRepositoryImpl().getServiceRegistry().getDictionaryService().getAllAspects();
        List<QName> typesList = new ArrayList<QName>(typeNames.size());
        typesList.addAll(typeNames);
        return new NodeTypeNameIterator(this, typesList);
    }
    
}
