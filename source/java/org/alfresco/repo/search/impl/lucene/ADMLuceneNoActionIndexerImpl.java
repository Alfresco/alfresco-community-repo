/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.search.impl.lucene;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

public class ADMLuceneNoActionIndexerImpl extends ADMLuceneIndexerImpl
{

    public ADMLuceneNoActionIndexerImpl()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void createChildRelationship(ChildAssociationRef relationshipRef) throws LuceneIndexException
    {
        return;
    }

    @Override
    public void createNode(ChildAssociationRef relationshipRef) throws LuceneIndexException
    {
        NodeRef childRef = relationshipRef.getChildRef();
        // If we have the root node we delete all other root nodes first
        if ((relationshipRef.getParentRef() == null) && childRef.equals(nodeService.getRootNode(childRef.getStoreRef())))
        {
            // do the root node only
            super.createNode(relationshipRef);
        }
        else
        {
            // Nothing
        }
    }

    @Override
    public void deleteChildRelationship(ChildAssociationRef relationshipRef) throws LuceneIndexException
    {
        return;
    }

    @Override
    public void deleteNode(ChildAssociationRef relationshipRef) throws LuceneIndexException
    {
        NodeRef childRef = relationshipRef.getChildRef();
        // If we have the root node we delete all other root nodes first
        if ((relationshipRef.getParentRef() == null) && childRef.equals(nodeService.getRootNode(childRef.getStoreRef())))
        {
            // do the root node only
            super.deleteNode(relationshipRef);
        }
        else
        {
            // Nothing
        }
    }

    @Override
    public void updateChildRelationship(ChildAssociationRef relationshipBeforeRef, ChildAssociationRef relationshipAfterRef) throws LuceneIndexException
    {
        return;
    }

    @Override
    public void updateNode(NodeRef nodeRef) throws LuceneIndexException
    {
        if((nodeService.hasAspect(nodeRef, ContentModel.ASPECT_ROOT) && nodeRef.equals(nodeService.getRootNode(nodeRef.getStoreRef()))))
        {
            super.updateNode(nodeRef);
        }
    }
    
    

}
