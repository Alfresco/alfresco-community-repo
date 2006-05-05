/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
