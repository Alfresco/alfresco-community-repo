package org.alfresco.repo.cache;

import java.util.Properties;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CacheManagerPeerProviderFactory;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Alfresco's <tt>CacheManagerPeerProviderFactory</tt> that defers to the community or
 * enterprise factories.
 * 
 * @author Derek Hulley
 * @since 3.1
 */
public class AlfrescoCacheManagerPeerProviderFactory extends CacheManagerPeerProviderFactory
{
    private static Log logger = LogFactory.getLog(AlfrescoCacheManagerPeerProviderFactory.class);
    
    @Override
    public CacheManagerPeerProvider createCachePeerProvider(CacheManager cacheManager, Properties properties)
    {
        CacheManagerPeerProviderFactory factory = null;
        try
        {
            @SuppressWarnings("unchecked")
            Class clazz = Class.forName("org.alfresco.enterprise.repo.cache.jgroups.JGroupsRMICacheManagerPeerProvider$Factory");
            factory = (CacheManagerPeerProviderFactory) clazz.newInstance();
        }
        catch (ClassNotFoundException e)
        {
            // Entirely expected if the Enterprise-level code is not present
        }
        catch (Throwable e)
        {
            logger.error("Failed to instantiate JGroupsRMICacheManagerPeerProvider factory.", e);
        }
        finally
        {
            if (factory == null)
            {
                // Use EHCache's default implementation
                factory = new RMICacheManagerPeerProviderFactory();
            }
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Using peer provider factory: " + factory.getClass().getName());
        }
        
        return factory.createCachePeerProvider(cacheManager, properties);
    }

}
