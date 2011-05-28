package org.alfresco.repo.web.scripts.solr;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;

public class NodeMetaData
{
    private long id;
    private NodeRef nodeRef;
    private QName type;
    private long aclId;
    private Map<QName, Serializable> properties;
    private Set<QName> aspects;
    private Path paths;
    public long getId()
    {
        return id;
    }
    public void setId(long id)
    {
        this.id = id;
    }
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    public QName getType()
    {
        return type;
    }
    public void setType(QName type)
    {
        this.type = type;
    }
    public long getAclId()
    {
        return aclId;
    }
    public void setAclId(long aclId)
    {
        this.aclId = aclId;
    }
    public Map<QName, Serializable> getProperties()
    {
        return properties;
    }
    public void setProperties(Map<QName, Serializable> properties)
    {
        this.properties = properties;
    }
    public Set<QName> getAspects()
    {
        return aspects;
    }
    public void setAspects(Set<QName> aspects)
    {
        this.aspects = aspects;
    }
    public Path getPaths()
    {
        return paths;
    }
    public void setPaths(Path paths)
    {
        this.paths = paths;
    }
}
