package org.alfresco.filesys.config;

/**
 * The Class ClusterConfigBean.
 * 
 * @author mrogers
 * @since 4.0 
 */
public class ClusterConfigBean
{
    private String debugFlags;
    private int nearCacheTimeout;

    public boolean getClusterEnabled()
    {
       // No clustering support in community edition.
       return false;
    }
    
    public String getClusterName()
    {
        // No clustering support in community edition.
        return null;
    }

    public void setDebugFlags(String debugFlags)
    {
        this.debugFlags = debugFlags;
    }

    public String getDebugFlags()
    {
        return debugFlags;
    }

    public void setNearCacheTimeout(int nearCacheTimeout)
    {
        this.nearCacheTimeout = nearCacheTimeout;
    }

    public int getNearCacheTimeout()
    {
        return nearCacheTimeout;
    }
}
