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
package org.alfresco.cmis;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;


/**
 * CMIS Property Id
 * 
 * @author davidc
 */
public class CMISPropertyId implements Serializable
{
    private static final long serialVersionUID = 4094778633095367606L;

    // Id properties
    private QName propertyQName;
    private String propertyId;

    
    /**
     * Construct
     * 
     * @param propertyName
     * @param propertyId
     * @param internalQName
     */
    public CMISPropertyId(QName propertyQName, String propertyId)
    {
        this.propertyQName = propertyQName;
        this.propertyId = propertyId;
    }

    /**
     * Get property id
     * 
     * @return
     */
    public String getId()
    {
        return propertyId;
    }

    /**
     * Get property local name
     * @return
     */
    public String getLocalName()
    {
        return propertyQName.getLocalName();
    }
    
    /**
     * Get property local namespace
     * @return
     */
    public String getLocalNamespace()
    {
        return propertyQName.getNamespaceURI();
    }
    
    /**
     * Get the Alfresco model QName associated with the property
     * 
     * @return  alfresco QName
     */
    public QName getQName()
    {
        return propertyQName;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getId();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((propertyQName == null) ? 0 : propertyQName.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final CMISPropertyId other = (CMISPropertyId) obj;
        if (propertyQName == null)
        {
            if (other.propertyQName != null)
                return false;
        }
        else if (!propertyQName.equals(other.propertyQName))
            return false;
        return true;
    }

}
