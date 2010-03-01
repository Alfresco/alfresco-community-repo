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
package org.alfresco.jcr.item.property;

import javax.jcr.RepositoryException;

import org.alfresco.jcr.dictionary.JCRNamespace;
import org.alfresco.jcr.item.NodeImpl;
import org.alfresco.jcr.item.PropertyImpl;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Implementation for mix:lockable lockOwner property
 * 
 * @author David Caruana
 */
public class JCRLockOwnerProperty extends PropertyImpl
{
    public static QName PROPERTY_NAME = QName.createQName(JCRNamespace.JCR_URI, "lockOwner");
    

    /**
     * Construct
     * 
     * @param node
     */
    public JCRLockOwnerProperty(NodeImpl node)
    {
        super(node, PROPERTY_NAME);
    }

    @Override
    protected Object getPropertyValue() throws RepositoryException
    {
        NodeImpl nodeImpl = getNodeImpl();
        NodeService nodeService = nodeImpl.getSessionImpl().getRepositoryImpl().getServiceRegistry().getNodeService();
        String lockOwner = (String)nodeService.getProperty(nodeImpl.getNodeRef(), ContentModel.PROP_LOCK_OWNER);
        return lockOwner;
    }
    
}
