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
package org.alfresco.repo.cmis.reference;

import org.alfresco.cmis.CMISRelationshipReference;
import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Source Node / Rel Type / Target Node Relationship Reference
 * 
 * @author davidc
 */
public class SourceTypeTargetRelationshipReference implements CMISRelationshipReference
{
    protected CMISServices cmisServices;
    protected StoreRepositoryReference repo;
    protected CMISTypeDefinition type;
    protected NodeRef source;
    protected NodeRef target;

    
    /**
     * Construct
     * 
     * @param cmisServices
     * @param type
     * @param srcStore
     * @param srcId
     * @param tgtStore
     * @param tgtId
     */
    public SourceTypeTargetRelationshipReference(CMISServices cmisServices, CMISTypeDefinition type, String srcStore, String srcId, String tgtStore, String tgtId)
    {
        this.cmisServices = cmisServices;
        this.repo = new StoreRepositoryReference(cmisServices, srcStore);
        this.type = type;
        this.source = new NodeRef(repo.getStoreRef(), srcId);
        StoreRepositoryReference tgtStoreRef = new StoreRepositoryReference(cmisServices, tgtStore);
        this.target = new NodeRef(tgtStoreRef.getStoreRef(), tgtId);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISRelationshipReference#getRepositoryReference()
     */
    public CMISRepositoryReference getRepositoryReference()
    {
        return repo;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISRelationshipReference#getAssocRef()
     */
    public AssociationRef getAssocRef()
    {
        return cmisServices.getRelationship(type, source, target);
    }
    
    @Override
    public String toString()
    {
        return "SourceTypeTargetRelationshipReference[type=" + type.getTypeId().getId() + ",source=" + source.toString() + ",target=" + target.toString() + "]";
    }

}
