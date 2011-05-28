package org.alfresco.repo.domain.solr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;

public interface NodeMetaData
{
    public NodeRef getNodeRef();
    public List<Path> getPaths();
    public QName getNodeType();
    public Long getNodeId();
    public Long getAclId();
    public String getOwner();
    public Map<QName, Serializable> getProperties();
    public Set<QName> getAspects();
    public List<ChildAssociationRef> getChildAssocs();
}
