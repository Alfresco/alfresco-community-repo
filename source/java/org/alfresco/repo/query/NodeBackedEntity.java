package org.alfresco.repo.query;

import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Parent class of Canned Query Entities which are a
 *  {@link NodeEntity} with additional properties
 *
 * @author Nick Burch
 * @since 4.0
 */
public class NodeBackedEntity implements PermissionCheckValue
{
    private Long id; // node id
    private String name;
    private NodeEntity node;
    
    // Supplemental query-related parameters
    private Long parentNodeId;
    private Long nameQNameId;
    private Long contentTypeQNameId;
    
    /**
     * Default constructor
     */
    public NodeBackedEntity()
    {
    }
    
    /**
     * Query constructor
     */
    public NodeBackedEntity(Long parentNodeId, Long nameQNameId, Long contentTypeQNameId)
    {
       this.parentNodeId = parentNodeId;
       this.nameQNameId = nameQNameId;
       this.contentTypeQNameId = contentTypeQNameId;
    }
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    // helper
    public NodeRef getNodeRef()
    {
        return (node != null ? node.getNodeRef() : null);
    }
    
    // helper (ISO 8061)
    public String getCreatedDate()
    {
        return ((node != null && node.getAuditableProperties() != null) ? node.getAuditableProperties().getAuditCreated() : null);
    }
    
    // helper
    public String getCreator()
    {
        return ((node != null && node.getAuditableProperties() != null) ? node.getAuditableProperties().getAuditCreator() : null);
    }
    
    // helper (ISO 8061)
    public String getModifiedDate()
    {
        return ((node != null && node.getAuditableProperties() != null) ? node.getAuditableProperties().getAuditModified() : null);
    }
    
    // helper
    public String getModifier()
    {
        return ((node != null && node.getAuditableProperties() != null) ? node.getAuditableProperties().getAuditModifier() : null);
    }
    
    public NodeEntity getNode()
    {
        return node;
    }
    
    public void setNode(NodeEntity childNode)
    {
        this.node = childNode;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    
    // Supplemental query-related parameters
    
    public Long getParentNodeId()
    {
        return parentNodeId;
    }
    
    public Long getNameQNameId()
    {
        return nameQNameId;
    }

    public Long getContentTypeQNameId() 
    {
       return contentTypeQNameId;
    }
}
