package org.alfresco.repo.domain.node;

import java.util.List;

/**
 * Bean to convey <b>alf_node_aspects</b> data.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodeAspectsEntity
{
    private Long nodeId;
	private Long nodeVersion;
    private List<Long> aspectQNameIds;

    /** Carries data for queries */
    private List<Long> nodeIds;

    /**
     * Required default constructor
     */
    public NodeAspectsEntity()
    {
    }
        
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("NodeAspectsEntity")
          .append(", nodeId=").append(nodeId)
          .append(", nodeVersion=").append(nodeVersion)
          .append(", aspects=").append(aspectQNameIds)
          .append("]");
        return sb.toString();
    }

    public Long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }
    
    public Long getNodeVersion()
    {
        return nodeVersion;
    }

    public void setNodeVersion(Long nodeVersion)
    {
        this.nodeVersion = nodeVersion;
    }

    public List<Long> getAspectQNameIds()
    {
        return aspectQNameIds;
    }

    public void setAspectQNameIds(List<Long> aspectQNameIds)
    {
        this.aspectQNameIds = aspectQNameIds;
    }
    
    public List<Long> getNodeIds()
    {
        return nodeIds;
    }

    public void setNodeIds(List<Long> nodeIds)
    {
        this.nodeIds = nodeIds;
    }
}
