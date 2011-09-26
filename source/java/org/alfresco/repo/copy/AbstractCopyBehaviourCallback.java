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
package org.alfresco.repo.copy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.copy.CopyBehaviourCallback.AssocCopySourceAction;
import org.alfresco.repo.copy.CopyBehaviourCallback.AssocCopyTargetAction;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Abstract implementation to allow for easier migration if the interface changes.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public abstract class AbstractCopyBehaviourCallback implements CopyBehaviourCallback
{
    private static final String KEY_NODEREF_REPOINTING_PREFIX = "recordNodeRefPropertiesForRepointing-";
    
    /**
     * @return          Returns
     *                  {@link AssocCopySourceAction#COPY_REMOVE_EXISTING} and
     *                  {@link AssocCopyTargetAction#USE_COPIED_TARGET}
     */
    @Override
    public Pair<AssocCopySourceAction, AssocCopyTargetAction> getAssociationCopyAction(
                QName classQName,
                CopyDetails copyDetails,
                CopyAssociationDetails assocCopyDetails)
    {
        return new Pair<AssocCopySourceAction, AssocCopyTargetAction>(
                AssocCopySourceAction.COPY_REMOVE_EXISTING,
                AssocCopyTargetAction.USE_COPIED_TARGET);
    }

    /**
     * @return      Returns {@link ChildAssocRecurseAction#RESPECT_RECURSE_FLAG}
     */
    public ChildAssocRecurseAction getChildAssociationRecurseAction(
            QName classQName,
            CopyDetails copyDetails,
            CopyChildAssociationDetails childAssocCopyDetails)
    {
        return ChildAssocRecurseAction.RESPECT_RECURSE_FLAG;
    }
    
    /**
     * @throws      IllegalStateException  always
     */
    protected void throwExceptionForUnexpectedBehaviour(CopyDetails copyDetails, String ... otherDetails)
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("Behaviour should have been invoked: \n" +
                "   Aspect: " + this.getClass().getName() + "\n" +
                "   " + copyDetails + "\n");
        for (String otherDetail : otherDetails)
        {
            sb.append("   ").append(otherDetail).append("\n");
        }
        throw new IllegalStateException(sb.toString());
    }
    
    /**
     * Helper method to transactionally record <code>NodeRef</code> properties so that they
     * can later be fixed up to point to the relative, after-copy locations.
     * <p>
     * When the copy has been completed, the second stage of the process can be applied.
     * 
     * @param sourceNodeRef             the node that is being copied
     * @param properties                the node properties being copied
     * @param propertyQName             the qualified name of the property to check
     * 
     * @see #repointNodeRefs(NodeRef, QName, Map, NodeService)
     */
    public void recordNodeRefsForRepointing(
            NodeRef sourceNodeRef,
            Map<QName, Serializable> properties,
            QName propertyQName)
    {
        Serializable parameterValue = properties.get(propertyQName);
        if (parameterValue != null &&
                (parameterValue instanceof Collection<?> || parameterValue instanceof NodeRef))
        {
            String key = KEY_NODEREF_REPOINTING_PREFIX + propertyQName.toString();
            // Store it for later
            Map<NodeRef, Serializable> map = TransactionalResourceHelper.getMap(key);
            map.put(sourceNodeRef, parameterValue);
        }
    }
    
    /**
     * The second stage of the <code>NodeRef</code> repointing.  Call this method to have
     * any <code>NodeRef</code> properties readjusted to reflect the copied node hierarchy.
     * Only use this method if it a requirement for the particular type or aspect that you
     * are coding for.
     * 
     * @param sourceNodeRef         the source node
     * @param propertyQName         the target node i.e. the copy of the source node
     * @param copyMap               the full hierarchy copy map of source to copies
     * 
     * @see #recordNodeRefsForRepointing(NodeRef, Map, QName)
     */
    @SuppressWarnings("unchecked")
    public void repointNodeRefs(
            NodeRef sourceNodeRef,
            NodeRef targetNodeRef,
            QName propertyQName,
            Map<NodeRef, NodeRef> copyMap,
            NodeService nodeService)
    {
        String key = KEY_NODEREF_REPOINTING_PREFIX + propertyQName.toString();
        Map<NodeRef, Serializable> map = TransactionalResourceHelper.getMap(key);
        Serializable value = map.get(sourceNodeRef);
        if (value == null)
        {
            // Don't bother.  The source node did not have a NodeRef property
            return;
        }
        Serializable newValue = null;
        if (value instanceof Collection)
        {
            Collection<Serializable> oldList = (Collection<Serializable>) value;
            List<Serializable> newList = new ArrayList<Serializable>(oldList.size());
            for (Serializable oldListValue : oldList)
            {
                Serializable newListValue = oldListValue;
                if (oldListValue instanceof NodeRef)
                {
                    newListValue = repointNodeRef(copyMap, (NodeRef) oldListValue);
                }
                // Put the value in the new list even though the new list might be discarded
                newList.add(newListValue);
                // Check if the value changed
                if (!newListValue.equals(oldListValue))
                {
                    // The value changed, so the new list will have to be set onto the target node
                    newValue = (Serializable) newList;
                }
            }
        }
        else if (value instanceof NodeRef)
        {
            NodeRef newNodeRef = repointNodeRef(copyMap, (NodeRef) value);
            if (!newNodeRef.equals(value))
            {
                // The value changed, so the new list will have to be set onto the target node
                newValue = newNodeRef;
            }
        }
        else
        {
            throw new IllegalStateException("Should only have Collections and NodeRef values");
        }
        // Fix the node property on the target, if necessary
        if (newValue != null)
        {
            nodeService.setProperty(targetNodeRef, propertyQName, newValue);
        }
    }
    
    private NodeRef repointNodeRef(Map<NodeRef, NodeRef> copyMap, NodeRef pointerNodeRef)
    {
        NodeRef copiedPointerNodeRef = copyMap.get(pointerNodeRef);
        if (copiedPointerNodeRef == null)
        {
            return pointerNodeRef;
        }
        else
        {
            return copiedPointerNodeRef;
        }
    }
}
