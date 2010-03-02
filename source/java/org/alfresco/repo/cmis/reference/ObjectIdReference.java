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
package org.alfresco.repo.cmis.reference;

import org.alfresco.cmis.CMISObjectReference;
import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * CMIS Object ID reference. A CMIS object ID encapsulates both a store and a node and identifies a specific version.
 * 
 * @author dward
 */
public class ObjectIdReference implements CMISObjectReference
{
    private CMISServices cmisServices;
    private String objectId;

    /**
     * Construct
     * 
     * @param cmisServices
     * @param storeRef
     */
    public ObjectIdReference(CMISServices cmisServices, String objectId)
    {
        this.cmisServices = cmisServices;
        this.objectId = objectId;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISObjectReference#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        try
        {
            return cmisServices.getReadableObject(this.objectId, NodeRef.class);
        }
        catch (CMISServiceException e)
        {
            throw new WebScriptException(e.getStatusCode(), e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISObjectReference#getRepositoryReference()
     */
    public CMISRepositoryReference getRepositoryReference()
    {
        return new AbstractRepositoryReference(cmisServices)
        {
            public StoreRef getStoreRef()
            {
                return getNodeRef().getStoreRef();
            }
        };
    }

    @Override
    public String toString()
    {
        return "ObjectIdReference[objectId=" + this.objectId + "]";
    }

}
