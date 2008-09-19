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
