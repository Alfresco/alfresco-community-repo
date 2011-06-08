/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.node.locator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class AncestorNodeLocator extends AbstractNodeLocator
{
    public static final String NAME = "AncestorNodeLocator";
    public static final String TYPE_KEY = "type";
    public static final String ASPECT_KEY = "aspect";
    
    private NamespaceService namespaceService;
    private NodeService nodeService;
    
    /**
    * {@inheritDoc}
    */
    @Override
    public NodeRef getNode(NodeRef source, Map<String, Serializable> params)
    {
        QName type = getQNameParam(TYPE_KEY, params);
        QName aspect = getQNameParam(ASPECT_KEY, params);
        NodeRef child = source;
        while(true)
        {
            ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(child);
            if(parentAssoc == null)
            {
                break; // No matching ancestor found.
            }
            NodeRef parent = parentAssoc.getParentRef();
            if(parent == null)
            {
                break; // No matching ancestor found.
            }
            if(typeMatches(type, parent) && aspectMatches(aspect, parent))
            {
                return parent; // Matching ancestor was found.
            }
            child = parent;
        }
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public List<ParameterDefinition> getParameterDefinitions()
    {
        List<ParameterDefinition> paramDefs = new ArrayList<ParameterDefinition>(2);
        paramDefs.add(new ParameterDefinitionImpl(TYPE_KEY, DataTypeDefinition.QNAME, false, "Type"));
        paramDefs.add(new ParameterDefinitionImpl(ASPECT_KEY, DataTypeDefinition.QNAME, false, "Aspect"));
        return paramDefs;
    }
    
    private boolean typeMatches(QName type, NodeRef parent)
    {
        return type==null || type.equals(nodeService.getType(parent));
    }
    
    private boolean aspectMatches(QName aspect, NodeRef parent)
    {
        return aspect==null || nodeService.getAspects(parent).contains(aspect);
    }

    private QName getQNameParam(String key, Map<String, Serializable> params)
    {
        String value = (String) params.get(key);
        if(value!=null)
        {
            return QName.createQName(value, namespaceService);
        }
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getName()
    {
        return NAME;
    }
    
    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

}
