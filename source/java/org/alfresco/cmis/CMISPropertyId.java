/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
    private String propertyName;
    private String propertyId;
    private QName qName;

    
    /**
     * Construct
     * 
     * @param propertyName
     * @param propertyId
     * @param qName
     */
    public CMISPropertyId(String propertyName, String propertyId, QName qName)
    {
        this.propertyName = propertyName;
        this.propertyId = propertyId;
        this.qName = qName;
    }

    /**
     * Get property name
     *  
     * @return
     */
    public String getName()
    {
        return propertyName;
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
     * Get the Alfresco model QName associated with the property
     * 
     * @return  alfresco QName
     */
    public QName getQName()
    {
        return qName;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName();
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
        result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
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
        if (propertyName == null)
        {
            if (other.propertyName != null)
                return false;
        }
        else if (!propertyName.equals(other.propertyName))
            return false;
        return true;
    }

}
