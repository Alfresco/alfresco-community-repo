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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.jcr.item.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.alfresco.jcr.dictionary.JCRNamespace;
import org.alfresco.jcr.dictionary.NodeTypeImpl;
import org.alfresco.jcr.item.NodeImpl;
import org.alfresco.jcr.item.PropertyImpl;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Implementation for nt:base primaryType property
 * 
 * @author David Caruana
 */
public class JCRMixinTypesProperty extends PropertyImpl
{
    public static QName PROPERTY_NAME = QName.createQName(JCRNamespace.JCR_URI, "mixinTypes");
    

    /**
     * Construct
     * 
     * @param node
     */
    public JCRMixinTypesProperty(NodeImpl node)
    {
        super(node, PROPERTY_NAME);
    }

    @Override
    protected Object getPropertyValue() throws RepositoryException
    {
        // get aspects from node
        NodeImpl nodeImpl = getNodeImpl();
        NodeService nodeService = nodeImpl.getSessionImpl().getRepositoryImpl().getServiceRegistry().getNodeService();
        Set<QName> aspects = nodeService.getAspects(nodeImpl.getNodeRef());

        // resolve against session namespace prefix resolver
        List<String> aspectNames = new ArrayList<String>(aspects.size() + 1);
        for (QName aspect : aspects)
        {
            aspectNames.add(aspect.toPrefixString(nodeImpl.getSessionImpl().getNamespaceResolver()));
        }
        
        // add JCR referenceable
        aspectNames.add(NodeTypeImpl.MIX_REFERENCEABLE.toPrefixString(nodeImpl.getSessionImpl().getNamespaceResolver()));
        
        return aspectNames; 
    }
    
}
