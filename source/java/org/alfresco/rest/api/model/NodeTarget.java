package org.alfresco.rest.api.model;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A target object.
 * @author Gethin James
 */
public class NodeTarget extends Target
{
    String targetParentId;
    String name;

    public NodeTarget()
    {
    }

    public String getTargetParentId()
    {
        return targetParentId;
    }

    public void setTargetParentId(String targetParentId)
    {
        this.targetParentId = targetParentId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("NodeTarget{");
        sb.append("targetParentId=").append(targetParentId);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
