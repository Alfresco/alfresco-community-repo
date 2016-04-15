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
