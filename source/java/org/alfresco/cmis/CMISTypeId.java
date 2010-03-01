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

import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.service.namespace.QName;

/**
 * CMIS Type Id
 * 
 * @author andyh
 *
 */
public class CMISTypeId implements Serializable
{
    private static final long serialVersionUID = -4709046883083948302L;

    private CMISScope scope;
    private QName typeQName;
    private String typeId;
    private QName internalQName;

    /**
     * Construct
     * 
     * @param scope  type scope
     * @param typeQName  name of the CMIS type
     * @param typeId  id of the CMIS type
     * @param internalQName  alfresco class definition representing the type
     */
    public CMISTypeId(CMISScope scope, QName typeQName, String typeId, QName internalQName)
    {
        this.scope = scope;
        this.typeQName = typeQName;
        this.typeId = typeId;
        this.internalQName = internalQName;
    }

    /**
     * Get the CMIS type id string 
     * @return
     */
    public String getId()
    {
        return typeId;
    }

    /**
     * Get the CMIS local name
     * @return
     */
    public String getLocalName()
    {
        return typeQName.getLocalName();
    }
    
    /**
     * Get the CMIS local namespace
     * @return
     */
    public String getLocalNamespace()
    {
        return typeQName.getNamespaceURI();
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
     * Get the Alfresco model QName associated with the type
     * 
     * @return  alfresco QName
     */
    public QName getQName()
    {
        return internalQName;
    }
    
    /**
     * Get the base type id
     * @return
     */
    public CMISTypeId getBaseTypeId()
    {
        switch (scope)
        {
        case DOCUMENT:
            return CMISDictionaryModel.DOCUMENT_TYPE_ID;
        case FOLDER:
            return CMISDictionaryModel.FOLDER_TYPE_ID;
        case RELATIONSHIP:
            return CMISDictionaryModel.RELATIONSHIP_TYPE_ID;
        case POLICY:
            return CMISDictionaryModel.POLICY_TYPE_ID;
        case OBJECT:
            return CMISMapping.OBJECT_TYPE_ID;
        case UNKNOWN:
        default:
            return null;
        }
    }

    @Override
    public String toString()
    {
        return getId();
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
        if (typeQName == null)
        {
            if (other.typeQName != null)
                return false;
        }
        else if (!typeQName.equals(other.typeQName))
            return false;
        return true;
    }

}
