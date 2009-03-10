/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cache.jgroups;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.alfresco.repo.jgroups.AlfrescoJGroupsChannelFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @see JGroupsRMICacheManagerPeerProvider
 * 
 * @author Derek Hulley
 */
public class JGroupsEhCacheTest extends TestCase
{
    private static final String CACHE_INVALIDATION = "org.alresco.test.cache.invalidation";
    private static final String CACHE_REPLICATION = "org.alresco.test.cache.replication";
    private static final String CACHE_NOT_CLUSTERED = "org.alresco.test.cache.not-clustered";
    private static final String BEAN_CACHE_MANAGER = "ehCacheManager";
    
    private static final String KEY_A = "A";
    private static final String KEY_B = "B";
    private static final String KEY_C = "C";
    private static final String VALUE_A = "AAA";
    private static final String VALUE_B = "BBB";
    private static final String VALUE_C = "CCC";
    
    private static ApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[] {
                    "classpath:jgroups/ehcache-jgroups-cluster-test-context.xml",}
            );
    private CacheManager cacheManager;
    private Cache cacheInvalidation;
    private Cache cacheReplication;
    private Cache cacheNotClustered;
    
    @Override
    public void setUp() throws Exception
    {
        cacheManager = (CacheManager) ctx.getBean(BEAN_CACHE_MANAGER);
        cacheInvalidation = cacheManager.getCache(CACHE_INVALIDATION);
        cacheReplication = cacheManager.getCache(CACHE_REPLICATION);
        cacheNotClustered = cacheManager.getCache(CACHE_NOT_CLUSTERED);
    }
    
    @Override
    public void tearDown()
    {
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull(cacheManager);
        CacheManager cacheManagerCheck = (CacheManager) ctx.getBean(BEAN_CACHE_MANAGER);
        assertTrue(cacheManager == cacheManagerCheck);
        
        // Check that the cache manager is active
        assertTrue("Cache manager is not alive", cacheManager.getStatus() == Status.STATUS_ALIVE);
        
        // Check that the caches are available
        assertNotNull("Cache not found: " + CACHE_INVALIDATION, cacheInvalidation);
        assertNotNull("Cache not found: " + CACHE_REPLICATION, cacheReplication);
        assertNotNull("Cache not found: " + CACHE_NOT_CLUSTERED, cacheReplication);
        
        // Make sure that the cache manager is cluster-enabled
        assertNotNull("CacheManagerPeerProvider is not present", cacheManager.getCacheManagerPeerProvider());
    }
    
    /**
     * Loops, checking the names of the caches present in the URLs being sent to the heartbeat.  The test will
     * loop for 6s (maximum heartbeat duration) or until the correct cache names are present. 
     */
    private synchronized void checkHeartbeatCacheNamesPresent(String ... cacheNames)
    {
        Set<String> lookingFor = new HashSet<String>(Arrays.asList(cacheNames));
        
        long startTimeMs = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTimeMs < 6000)
        {
            String urls = JGroupsKeepAliveHeartbeatSender.getLastHeartbeatSendUrls();
            for (String cacheName : cacheNames)
            {
                if (urls.contains(cacheName))
                {
                    lookingFor.remove(cacheName);
                }
            }
            // Did we eliminate all the caches?
            if (lookingFor.size() == 0)
            {
                // All of the given caches are present
                return;
            }
            try { wait(100); } catch (InterruptedException e) {}
        }
        // Some of the caches are STILL NOT in the heartbeat
        fail("Caches did not appear in the heartbeat: " + lookingFor);
    }
    
    /**
     * Loops, checking the names of the caches are <b>not</b> present in the URLs being sent to the heartbeat.
     * The test will loop for 6s (maximum heartbeat duration) or until the caches are no longer present. 
     */
    private synchronized void checkHeartbeatCacheNamesNotPresent(String ... cacheNames)
    {
        Set<String> notLookingFor = new HashSet<String>(Arrays.asList(cacheNames));
        
        long startTimeMs = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTimeMs < 6000)
        {
            String urls = JGroupsKeepAliveHeartbeatSender.getLastHeartbeatSendUrls();
            for (String cacheName : cacheNames)
            {
                if (!urls.contains(cacheName))
                {
                    notLookingFor.remove(cacheName);
                }
            }
            // Did we eliminate all the caches?
            if (notLookingFor.size() == 0)
            {
                // None of the given caches are present
                return;
            }
            try { wait(100); } catch (InterruptedException e) {}
        }
        // Some of the caches are STILL in the heartbeat
        fail("Caches were not removed from the heartbeat: " + notLookingFor);
    }
    
    /**
     * Check that the default heartbeat is as expected
     */
    public void testDefaultHeartbeat() throws Exception
    {
        checkHeartbeatCacheNamesPresent(CACHE_INVALIDATION, CACHE_REPLICATION);
        checkHeartbeatCacheNamesNotPresent(CACHE_NOT_CLUSTERED);
    }
    
    /**
     * Manipulate the invalidating cache
     */
    public void testInvalidatingCache() throws Exception
    {
        cacheInvalidation.put(new Element(KEY_A, VALUE_A));
        cacheInvalidation.put(new Element(KEY_B, VALUE_B));
        cacheInvalidation.put(new Element(KEY_C, VALUE_C));

        cacheInvalidation.put(new Element(KEY_A, VALUE_C));
        cacheInvalidation.put(new Element(KEY_B, VALUE_A));
        cacheInvalidation.put(new Element(KEY_C, VALUE_B));

        cacheInvalidation.remove(KEY_A);
        cacheInvalidation.remove(KEY_B);
        cacheInvalidation.remove(KEY_C);
    }
    
    /**
     * Manipulate the replicating cache
     */
    public void testReplicatingCache() throws Exception
    {
        cacheReplication.put(new Element(KEY_A, VALUE_A));
        cacheReplication.put(new Element(KEY_B, VALUE_B));
        cacheReplication.put(new Element(KEY_C, VALUE_C));

        cacheReplication.put(new Element(KEY_A, VALUE_C));
        cacheReplication.put(new Element(KEY_B, VALUE_A));
        cacheReplication.put(new Element(KEY_C, VALUE_B));

        cacheReplication.remove(KEY_A);
        cacheReplication.remove(KEY_B);
        cacheReplication.remove(KEY_C);
    }
    
    /**
     * Manipulate the non-clustered cache
     */
    public void testNonClusteredCache() throws Exception
    {
        cacheNotClustered.put(new Element(KEY_A, VALUE_A));
        cacheNotClustered.put(new Element(KEY_B, VALUE_B));
        cacheNotClustered.put(new Element(KEY_C, VALUE_C));

        cacheNotClustered.put(new Element(KEY_A, VALUE_C));
        cacheNotClustered.put(new Element(KEY_B, VALUE_A));
        cacheNotClustered.put(new Element(KEY_C, VALUE_B));

        cacheNotClustered.remove(KEY_A);
        cacheNotClustered.remove(KEY_B);
        cacheNotClustered.remove(KEY_C);
    }
    
    /**
     * Starts up a second VM and manipulates the cache
     */
    public static void main(String ... args)
    {
        CacheManager cacheManager = (CacheManager) ctx.getBean(BEAN_CACHE_MANAGER);
        Ehcache cacheInvalidation = cacheManager.getCache(CACHE_INVALIDATION);
        Ehcache cacheReplication = cacheManager.getCache(CACHE_REPLICATION);
        Ehcache cacheNotClustered = cacheManager.getCache(CACHE_NOT_CLUSTERED);
        
        Element[] elements = new Element[] {
                new Element(KEY_A, VALUE_A),
                new Element(KEY_B, VALUE_B),
                new Element(KEY_C, VALUE_C),
        };
        
        synchronized (cacheManager)
        {
            try { cacheManager.wait(0); } catch (Throwable e) {} 
        }
    }
}
