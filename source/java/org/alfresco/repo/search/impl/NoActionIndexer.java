package org.alfresco.repo.search.impl;

import java.util.Collection;

import org.alfresco.repo.search.Indexer;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;

/**
 * A no action indexer - the indexing is done automatically along with
 * persistence
 * 
 * TODO: Rename to Adaptor?
 * 
 * @author andyh
 * 
 */
public class NoActionIndexer implements Indexer
{
    public void setReadThrough(boolean isReadThrough)
    {
        return;
    }

    public void createNode(ChildAssociationRef relationshipRef)
    {
        return;
    }

    public void updateNode(NodeRef nodeRef)
    {
        return;
    }

    public void deleteNode(ChildAssociationRef relationshipRef)
    {
        return;
    }

    public void createChildRelationship(ChildAssociationRef relationshipRef)
    {
        return;
    }

    public void updateChildRelationship(ChildAssociationRef relationshipBeforeRef, ChildAssociationRef relationshipAfterRef)
    {
        return;
    }

    public void deleteChildRelationship(ChildAssociationRef relationshipRef)
    {
        return;
    }
    
    public void detectNodeChanges(NodeRef nodeRef, SearchService searcher,
            Collection<ChildAssociationRef> addedParents, Collection<ChildAssociationRef> deletedParents,
            Collection<ChildAssociationRef> createdNodes, Collection<NodeRef> updatedNodes)
    {
        return;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.Indexer#deleteIndex(org.alfresco.service.cmr.repository.StoreRef)
     */
    public void deleteIndex(StoreRef storeRef)
    {
        return;
    }

    public void flushPending()
    {
        return;
    }    
}
