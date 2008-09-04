/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.cmis.dictionary;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

/**
 * A CMIS property type id
 * 
 * @author andyh
 *
 */
public class CMISTypeId implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -4709046883083948302L;

    private String typeId;

    private CMISScope scope;

    private QName qName;

    public CMISTypeId(CMISScope scope, QName qName, String typeId)
    {
        this.scope = scope;
        this.qName = qName;
        this.typeId = typeId;
    }

    /**
     * Get the CMIS type id string 
     * @return
     */
    public String getTypeId()
    {
        return typeId;
    }

    /**
     * Get the Alfresco model QName associated with the type
     * @return
     */
    public QName getQName()
    {
        return qName;
    }

    /**
     * Get the scope for the type (Doc, Folder, Relationship or unknown)
     * @return
     */
    public CMISScope getScope()
    {
        return scope;
    }

    /**
     * Get the root type id
     * @return
     */
    public CMISTypeId getRootTypeId()
    {
        switch (scope)
        {
        case DOCUMENT:
            return CMISMapping.DOCUMENT_TYPE_ID;
        case FOLDER:
            return CMISMapping.FOLDER_TYPE_ID;
        case RELATIONSHIP:
            return CMISMapping.RELATIONSHIP_TYPE_ID;
        case UNKNOWN:
        default:
            return null;
        }
    }

    public String toString()
    {
        return getTypeId();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final CMISTypeId other = (CMISTypeId) obj;
        if (typeId == null)
        {
            if (other.typeId != null)
                return false;
        }
        else if (!typeId.equals(other.typeId))
            return false;
        return true;
    }

}
