package org.alfresco.repo.domain.node.ibatis;

import java.util.List;

/**
 * Bean to convey carry query information of node batch loading.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodeBatchLoadEntity
{
    private Long storeId;
    private List<String> uuids;
    private List<Long> ids;
    
    public Long getStoreId()
    {
        return storeId;
    }
    public void setStoreId(Long storeId)
    {
        this.storeId = storeId;
    }
    public List<String> getUuids()
    {
        return uuids;
    }
    public void setUuids(List<String> uuids)
    {
        this.uuids = uuids;
    }
    public List<Long> getIds()
    {
        return ids;
    }
    public void setIds(List<Long> ids)
    {
        this.ids = ids;
    }
}
