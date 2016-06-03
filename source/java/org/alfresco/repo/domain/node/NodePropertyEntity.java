package org.alfresco.repo.domain.node;

import java.util.List;

/**
 * Bean to convey <b>alf_node_properties</b> data.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodePropertyEntity
{
    private Long nodeId;
    private Long nodeVersion;
    private NodePropertyKey key;
    private NodePropertyValue value;
    /** Carries data for queries and updates */
    private List<Long> qnameIds;
    /** Carries data for queries */
    private List<Long> nodeIds;
    
    /**
     * Required default constructor
     */
    public NodePropertyEntity()
    {
    }
        
    @Override
    public String toString()
    {
        return "NodePropertyEntity [node=" + nodeId + ", nodeVersion=" + nodeVersion + ", key=" + key + ", value=" + value + "]";
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

    public NodePropertyKey getKey()
    {
        return key;
    }

    public void setKey(NodePropertyKey key)
    {
        this.key = key;
    }

    public NodePropertyValue getValue()
    {
        return value;
    }

    public void setValue(NodePropertyValue value)
    {
        this.value = value;
    }

    public List<Long> getQnameIds()
    {
        return qnameIds;
    }

    public void setQnameIds(List<Long> qnameIds)
    {
        this.qnameIds = qnameIds;
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