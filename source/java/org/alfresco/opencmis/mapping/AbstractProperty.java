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
package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.dictionary.CMISPropertyAccessor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Base class for all property accessors
 * 
 * @author andyh
 *
 */
public abstract class AbstractProperty implements CMISPropertyAccessor
{
    private ServiceRegistry serviceRegistry;
    private String propertyName;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param propertyName
     */
    protected AbstractProperty(ServiceRegistry serviceRegistry, String propertyName)
    {
        this.serviceRegistry = serviceRegistry;
        this.propertyName = propertyName;
    }

    /**
     * @return  service registry
     */
    protected ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyAccessor#getName()
     */
    public String getName()
    {
        return propertyName;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyAccessor#getMappedProperty()
     */
    public QName getMappedProperty()
    {
        return null;
    }

    @Override
    public Serializable getValue(NodeRef nodeRef)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setValue(NodeRef nodeRef, Serializable value)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Serializable getValue(AssociationRef assocRef)
    {
        throw new UnsupportedOperationException();
    }
}
