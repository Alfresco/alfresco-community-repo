package org.alfresco.repo.query;

import java.util.List;

import org.alfresco.repo.domain.node.NodeEntity;

/**
 * Parent class of Canned Query Entities which are a
 *  {@link NodeEntity} with additional properties
 *
 * @author Nick Burch
 * @since 4.0
 */
public class NodeWithTargetsEntity extends NodeBackedEntity
{
    private List<TargetAndTypeId> targets;
    
    // Supplemental query-related parameters
    private Long assocTypeId;
    
    /**
     * Default constructor
     */
    public NodeWithTargetsEntity()
    {
    }
    
    /**
     * Query constructor
     */
    public NodeWithTargetsEntity(Long parentNodeId, Long nameQNameId, Long contentTypeQNameId, Long assocTypeId)
    {
       super(parentNodeId, nameQNameId, contentTypeQNameId);
       this.assocTypeId = assocTypeId;
    }

    /**
     * @return Pairs of (Target Node, Assoc Type)
     */
    public List<TargetAndTypeId> getTargetIds() 
    {
       return targets;
    }

    public void setTargets(List<TargetAndTypeId> targets) 
    {
       this.targets = targets;
    }

    /**
     * If set, the ID of the assocation type to limit
     *  the target assocs to.
     */
    public Long getAssocTypeId() 
    {
       return assocTypeId;
    }
    
    public static class TargetAndTypeId
    {
       private final Long targetId;
       private final Long assocTypeId;
       
       public TargetAndTypeId(Long targetId, Long assocTypeId)
       {
          this.targetId = targetId;
          this.assocTypeId = assocTypeId;
       }

       public Long getTargetId() 
       {
          return targetId;
       }

       public Long getAssocTypeId() 
       {
          return assocTypeId;
       }
    }
}
