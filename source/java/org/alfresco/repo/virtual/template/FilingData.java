
package org.alfresco.repo.virtual.template;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class FilingData
{
    private NodeRef filingNodeRef;

    private QName assocTypeQName;

    private QName assocQName;

    private QName nodeTypeQName;

    private Set<QName> aspects;

    private Map<QName, Serializable> properties;

    public FilingData(NodeRef filingNodeRef, QName assocTypeQName, QName assocQName, QName nodeTypeQName,
                Set<QName> aspects, Map<QName, Serializable> properties)
    {
        super();
        this.filingNodeRef = filingNodeRef;
        this.assocTypeQName = assocTypeQName;
        this.assocQName = assocQName;
        this.nodeTypeQName = nodeTypeQName;
        this.aspects = aspects;
        this.properties = properties;
    }

    public Set<QName> getAspects()
    {
        return aspects;
    }

    public NodeRef getFilingNodeRef()
    {
        return filingNodeRef;
    }

    public QName getAssocTypeQName()
    {
        return assocTypeQName;
    }

    public QName getAssocQName()
    {
        return assocQName;
    }

    public QName getNodeTypeQName()
    {
        return nodeTypeQName;
    }

    public Map<QName, Serializable> getProperties()
    {
        return properties;
    }

}
