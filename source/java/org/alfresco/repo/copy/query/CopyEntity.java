package org.alfresco.repo.copy.query;

import org.alfresco.repo.domain.node.NodeEntity;

/**
 * Bean class to data about copied nodes
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class CopyEntity
{
    private NodeEntity copy;
    private String copyName;
    
    public NodeEntity getCopy()
    {
        return copy;
    }
    public void setCopy(NodeEntity copy)
    {
        this.copy = copy;
    }
    public String getCopyName()
    {
        return copyName;
    }
    public void setCopyName(String copyName)
    {
        this.copyName = copyName;
    }
}