package org.alfresco.repo.cache;

/**
 * A generic event with the cache id and affected tenant
 * 
 * @author Andy
 */
public abstract class AbstractRefreshableCacheEvent implements RefreshableCacheEvent
{
    private static final long serialVersionUID = 1324638640132648062L;

    private String cacheId;
    private String tenantId;

    AbstractRefreshableCacheEvent(String cacheId, String tenantId)
    {
        this.cacheId = cacheId;
        this.tenantId = tenantId;
    }

    @Override
    public String getCacheId()
    {
        return cacheId;
    }

    @Override
    public String getTenantId()
    {
        return tenantId;
    }

    @Override
    public String toString()
    {
        return "AbstractRefreshableCacheEvent [cacheId=" + cacheId + ", tenantId=" + tenantId + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cacheId == null) ? 0 : cacheId.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AbstractRefreshableCacheEvent other = (AbstractRefreshableCacheEvent) obj;
        if (cacheId == null)
        {
            if (other.cacheId != null) return false;
        }
        else if (!cacheId.equals(other.cacheId)) return false;
        if (tenantId == null)
        {
            if (other.tenantId != null) return false;
        }
        else if (!tenantId.equals(other.tenantId)) return false;
        return true;
    }
}
