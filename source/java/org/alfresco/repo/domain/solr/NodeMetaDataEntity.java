package org.alfresco.repo.domain.solr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;

/**
 * 
 * @since 4.0
 *
 */
public class NodeMetaDataEntity implements NodeMetaData
{
    private Long nodeId;
    private NodeRef nodeRef;
    private String owner;
    private QName nodeType;
    private Long aclId;
    private Map<QName, Serializable> properties;
    private Set<QName> aspects;
    private List<Path> paths;
    private List<ChildAssociationRef> childAssocs;
    
    public String getOwner()
    {
        return owner;
    }
    public void setOwner(String owner)
    {
        this.owner = owner;
    }
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    public List<Path> getPaths()
    {
        return paths;
    }
    public void setPaths(List<Path> paths)
    {
        this.paths = paths;
    }
    public QName getNodeType()
    {
        return nodeType;
    }
    public void setNodeType(QName nodeType)
    {
        this.nodeType = nodeType;
    }
    public Long getNodeId()
    {
        return nodeId;
    }
    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }
    public Long getAclId()
    {
        return aclId;
    }
    public void setAclId(Long aclId)
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
    public List<ChildAssociationRef> getChildAssocs()
    {
        return childAssocs;
    }
    public void setChildAssocs(List<ChildAssociationRef> childAssocs)
    {
        this.childAssocs = childAssocs;
    }
    
    
}
