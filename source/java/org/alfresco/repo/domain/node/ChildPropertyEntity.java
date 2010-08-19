package org.alfresco.repo.domain.node;

/**
 * Bean to convey the query parameters for select child assocs by property value. 
 * @author mrogers
 */
public class ChildPropertyEntity
{
    private Long parentNodeId;
    private Long propertyQNameId;
    private NodePropertyValue value;
    
    public void setParentNodeId(Long nodeId)
    {
        this.parentNodeId = nodeId;
    }
    public Long getParentNodeId()
    {
        return parentNodeId;
    }
    public void setPropertyQNameId(Long propertyQNameId)
    {
        this.propertyQNameId = propertyQNameId;
    }
    public Long getPropertyQNameId()
    {
        return propertyQNameId;
    }
    public void setValue(NodePropertyValue value)
    {
        this.value = value;
    }
    public NodePropertyValue getValue()
    {
        return value;
    }
}
