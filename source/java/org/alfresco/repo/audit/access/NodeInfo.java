package org.alfresco.repo.audit.access;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Wrapper for a {@link NodeRef} to provide path and type values using namespace prefixes.
 * Use the {@link NodeInfoFactory#newNodeInfo(org.alfresco.service.cmr.repository.NodeRef)} to create new instances.
 * 
 * @author Alan Davis
 */
/*package*/ class NodeInfo
{
    private final NodeRef nodeRef;
    private final String path;
    private final String type;
    
    /*package*/ NodeInfo(NodeRef nodeRef, String path, String type)
    {
        this.nodeRef = nodeRef;
        this.path = path;
        this.type = type;
    }

   public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public String getPrefixPath()
    {
        return path;
    }

    public String getPrefixType()
    {
        return type;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        NodeInfo other = (NodeInfo) obj;
        if (path == null)
        {
            if (other.path != null)
            {
                return false;
            }
        }
        else if (!path.equals(other.path))
        {
            return false;
        }
        if (type == null)
        {
            if (other.type != null)
            {
                return false;
            }
        }
        else if (!type.equals(other.type))
        {
            return false;
        }
        return true;
    }
}