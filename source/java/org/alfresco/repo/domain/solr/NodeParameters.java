package org.alfresco.repo.domain.solr;

import java.util.List;
import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * Stores node parameters for use in SOLR DAO queries
 * 
 * @since 4.0
 */
public class NodeParameters
{
    private List<Long> transactionIds;
    private Long fromNodeId;
    private Long toNodeId;
    
    private String storeProtocol;
    private String storeIdentifier;
    
    private Set<QName> includeNodeTypes;
    private Set<QName> excludeNodeTypes;
    private List<Long> includeTypeIds;
    private List<Long> excludeTypeIds;
    
    private Set<QName> includeAspects;
    private Set<QName> excludeAspects;
    private List<Long> includeAspectIds;
    private List<Long> excludeAspectIds;
    
    public boolean getStoreFilter()
    {
        return (storeProtocol != null || storeIdentifier != null);
    }
    
    public void setStoreProtocol(String storeProtocol)
    {
        this.storeProtocol = storeProtocol;
    }

    public String getStoreProtocol()
    {
        return storeProtocol;
    }

    public void setStoreIdentifier(String storeIdentifier)
    {
        this.storeIdentifier = storeIdentifier;
    }

    public String getStoreIdentifier()
    {
        return storeIdentifier;
    }
    
    public void setTransactionIds(List<Long> txnIds)
    {
        this.transactionIds = txnIds;
    }

    public List<Long> getTransactionIds()
    {
        return transactionIds;
    }

    public Long getFromNodeId()
    {
        return fromNodeId;
    }

    public void setFromNodeId(Long fromNodeId)
    {
        this.fromNodeId = fromNodeId;
    }

    public Long getToNodeId()
    {
        return toNodeId;
    }

    public void setToNodeId(Long toNodeId)
    {
        this.toNodeId = toNodeId;
    }

    public Set<QName> getIncludeNodeTypes()
    {
        return includeNodeTypes;
    }

    public Set<QName> getExcludeNodeTypes()
    {
        return excludeNodeTypes;
    }

    public Set<QName> getIncludeAspects()
    {
        return includeAspects;
    }

    public Set<QName> getExcludeAspects()
    {
        return excludeAspects;
    }

    public void setIncludeNodeTypes(Set<QName> includeNodeTypes)
    {
        this.includeNodeTypes = includeNodeTypes;
    }

    public void setExcludeNodeTypes(Set<QName> excludeNodeTypes)
    {
        this.excludeNodeTypes = excludeNodeTypes;
    }

    public void setIncludeAspects(Set<QName> includeAspects)
    {
        this.includeAspects = includeAspects;
    }

    public void setExcludeAspects(Set<QName> excludeAspects)
    {
        this.excludeAspects = excludeAspects;
    }

    public List<Long> getIncludeAspectIds()
    {
        return includeAspectIds;
    }

    public void setIncludeAspectIds(List<Long> includeAspectIds)
    {
        this.includeAspectIds = includeAspectIds;
    }

    public List<Long> getExcludeAspectIds()
    {
        return excludeAspectIds;
    }

    public void setExcludeAspectIds(List<Long> excludeAspectIds)
    {
        this.excludeAspectIds = excludeAspectIds;
    }

    public List<Long> getIncludeTypeIds()
    {
        return includeTypeIds;
    }

    public void setIncludeTypeIds(List<Long> includeTypeIds)
    {
        this.includeTypeIds = includeTypeIds;
    }

    public List<Long> getExcludeTypeIds()
    {
        return excludeTypeIds;
    }

    public void setExcludeTypeIds(List<Long> excludeTypeIds)
    {
        this.excludeTypeIds = excludeTypeIds;
    }

}
