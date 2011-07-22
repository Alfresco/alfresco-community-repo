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

import org.alfresco.cmis.CMISRelationshipReference;
import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Association ID relationship reference. An association ID can be resolved to an {@link AssociationRef}.
 * 
 * @author dward
 */
public class AssociationIdRelationshipReference implements CMISRelationshipReference
{
    private CMISServices cmisServices;
    private String assocId;

    /**
     * Construct
     * 
     * @param cmisServices
     * @param assocId
     */
    public AssociationIdRelationshipReference(CMISServices cmisServices, String assocId)
    {
        this.cmisServices = cmisServices;
        this.assocId = assocId;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISRelationshipReference#getAssocRef()
     */
    public AssociationRef getAssocRef()
    {
        try
        {
            return cmisServices.getReadableObject(this.assocId, AssociationRef.class);
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
                return getAssocRef().getSourceRef().getStoreRef();
            }
        };
    }

    @Override
    public String toString()
    {
        return "AssociationIdRelationshipReference[assocId=" + this.assocId + "]";
    }
}
