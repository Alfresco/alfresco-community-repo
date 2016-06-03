package org.alfresco.repo.discussion.cannedqueries;

import java.util.List;

import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.query.NodeBackedEntity;

/**
 * An extension of a {@link NodeEntity} which has the name
 *  of all children of it, used with the discussions 
 *  canned queries.
 * As well as the name comes some auditable information, but
 *  not full nodes as we don't do permissions checking on
 *  the children.
 *
 * @author Nick Burch
 * @since 4.0
 */
public class NodeWithChildrenEntity extends NodeBackedEntity
{
    private List<NameAndCreatedAt> children;
    
    // Supplemental query-related parameters
    private Long childrenTypeQNameId;
    
    /**
     * Default constructor
     */
    public NodeWithChildrenEntity()
    {
    }
    
    /**
     * Query constructor
     */
    public NodeWithChildrenEntity(Long parentNodeId, Long nameQNameId, Long contentTypeQNameId, Long childrenTypeQNameId)
    {
       super(parentNodeId, nameQNameId, contentTypeQNameId);
       this.childrenTypeQNameId = childrenTypeQNameId;
    }

    /**
     * @return Child Node name+created at 
     */
    public List<NameAndCreatedAt> getChildren() 
    {
       return children;
    }

    public void setChildren(List<NameAndCreatedAt> children) 
    {
       this.children = children;
    }

    /**
     * If set, the ID of the children's content type to limit
     *  the children too.
     */
    public Long getChildrenTypeQNameId() 
    {
       return childrenTypeQNameId;
    }
    
    public static class NameAndCreatedAt
    {
       private final Long nodeId;
       private final String name;
       private final String createdAt;
       
       public NameAndCreatedAt(Long nodeId, String name, String createdAt)
       {
          this.nodeId = nodeId;
          this.name = name;
          this.createdAt = createdAt;
       }

       public Long getNodeId() 
       {
          return nodeId;
       }
       
       public String getName()
       {
          return name;
       }
       
       public String getCreatedAt()
       {
          return createdAt;
       }
    }
}
