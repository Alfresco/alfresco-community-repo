package org.alfresco.repo.solr;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * 
 * @since 4.0
 */
public class NodeMetaData
{
    private Long nodeId;
    private NodeRef nodeRef;
    private String owner;
    private QName nodeType;
    private Long aclId;
    private Map<QName, Serializable> properties;
    private Set<QName> aspects;
    private Collection<Pair<Path, QName>> paths;
    private Collection<Collection<String>> namePaths;
    private List<ChildAssociationRef> childAssocs;
    private List<ChildAssociationRef> parentAssocs;
    private Long parentAssocsCrc;
    private List<Long> childIds;
    private Long txnId;
    private String tenantDomain;
    
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
    public Collection<Pair<Path, QName>> getPaths()
    {
        return paths;
    }
    public void setPaths(Collection<Pair<Path, QName>> paths)
    {
        this.paths = paths;
    }
    public Collection<Collection<String>> getNamePaths()
    {
        return namePaths;
    }
    public void setNamePaths(Collection<Collection<String>> namePaths)
    {
        this.namePaths = namePaths;
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
    /**
     * @param parentAssocs List<ChildAssociationRef>
     * @param parentAssocsCrc Long
     */
    public void setParentAssocs(List<ChildAssociationRef> parentAssocs, Long parentAssocsCrc)
    {
        this.parentAssocs = parentAssocs;
        this.parentAssocsCrc = parentAssocsCrc;
        
    }
    public List<ChildAssociationRef> getParentAssocs()
    {
        return parentAssocs;
    }
    public Long getParentAssocsCrc()
    {
        return parentAssocsCrc;
    }
    public List<Long> getChildIds()
    {
        return childIds;
    }
    public void setChildIds(List<Long> childIds)
    {
        this.childIds = childIds;
    }
    public Long getTxnId()
    {
        return txnId;
    }
    public void setTxnId(Long txnId)
    {
        this.txnId = txnId;
    }
    public String getTenantDomain()
    {
        return tenantDomain;
    }
    public void setTenantDomain(String tenantDomain)
    {
        this.tenantDomain = tenantDomain;
    }
}
