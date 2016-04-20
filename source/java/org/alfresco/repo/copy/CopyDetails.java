package org.alfresco.repo.copy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Simple Java bean that contains the details of a copy process underway.
 * 
 * @see CopyServicePolicies
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class CopyDetails
{
    private final NodeRef sourceNodeRef;
    private final QName sourceNodeTypeQName;
    private final Set<QName> sourceNodeAspectQNames;
    private final Map<QName, Serializable> sourceNodeProperties;
    private final NodeRef targetParentNodeRef;
    private final NodeRef targetNodeRef;
    private final boolean targetNodeIsNew;
    private final QName assocTypeQName;
    private final QName assocQName;
    
    public CopyDetails(
            NodeRef sourceNodeRef,
            QName sourceNodeTypeQName,
            Set<QName> sourceNodeAspectQNames,
            Map<QName, Serializable> sourceNodeProperties,
            NodeRef targetParentNodeRef,
            NodeRef targetNodeRef,
            boolean targetNodeIsNew,
            QName assocTypeQName,
            QName assocQName)
    {
        this.sourceNodeRef = sourceNodeRef;
        this.sourceNodeTypeQName = sourceNodeTypeQName;
        this.sourceNodeAspectQNames = sourceNodeAspectQNames;
        this.sourceNodeProperties = sourceNodeProperties;
        this.targetParentNodeRef = targetParentNodeRef;
        this.targetNodeRef = targetNodeRef;
        this.targetNodeIsNew = targetNodeIsNew;
        this.assocTypeQName = assocTypeQName;
        this.assocQName = assocQName;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("CopyDetails")
          .append(" [source=").append(sourceNodeRef)
          .append(", targetParent=").append(targetParentNodeRef)
          .append(", targetNode=").append(targetNodeRef)
          .append(", targetNodeIsNew=").append(targetNodeIsNew)
          .append(", assocTypeQName=").append(assocTypeQName)
          .append(", assocQName=").append(assocQName)
          .append("]");
        return sb.toString();
    }
    
    /**
     * Get the source node
     */
    public final NodeRef getSourceNodeRef()
    {
        return sourceNodeRef;
    }

    /**
     * Get the type of the source node
     */
    public final QName getSourceNodeTypeQName()
    {
        return this.sourceNodeTypeQName;
    }

    /**
     * Get the aspects associated with the source node
     */
    public final Set<QName> getSourceNodeAspectQNames()
    {
        return sourceNodeAspectQNames;
    }

    /**
     * Get the properties associated with the source node
     */
    public final Map<QName, Serializable> getSourceNodeProperties()
    {
        return sourceNodeProperties;
    }

    /**
     * Get the node under which the new/existing copy will be placed
     */
    public final NodeRef getTargetParentNodeRef()
    {
        return targetParentNodeRef;
    }

    /**
     * Get the node to which the copy will occur.  The node may not
     * <i>yet</i> exist.
     */
    public final NodeRef getTargetNodeRef()
    {
        return targetNodeRef;
    }

    /**
     * Determine if the {@link #getTargetNodeRef() target node} was newly-created
     * for the copy or if it pre-existed.
     * 
     * @return      <tt>true</tt> if the node was created by the copy
     */
    public final boolean isTargetNodeIsNew()
    {
        return targetNodeIsNew;
    }

    /**
     * Get the new association type qualified name
     */
    public final QName getAssocTypeQName()
    {
        return assocTypeQName;
    }

    /**
     * Get the association path qualified name
     */
    public final QName getAssocQName()
    {
        return assocQName;
    }
}