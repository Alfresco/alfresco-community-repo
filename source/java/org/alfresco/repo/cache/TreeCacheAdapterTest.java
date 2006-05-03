/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Alfresco Network License. You may obtain a
 * copy of the License at
 *
 *   http://www.alfrescosoftware.com/legal/
 *
 * Please view the license relevant to your network subscription.
 *
 * BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 * READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 * YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 * ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 * THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 * AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 * TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 * BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 * HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 * SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 * TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 * CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
 */
package org.alfresco.repo.cache;

import java.io.Serializable;

import org.jboss.cache.DummyTransactionManagerLookup;
import org.jboss.cache.Fqn;
import org.jboss.cache.TreeCache;

import junit.framework.TestCase;

/**
 * @see org.alfresco.repo.cache.TreeCacheAdapter
 * 
 * @author Derek Hulley
 */
public class TreeCacheAdapterTest extends TestCase
{
    private static final String KEY_A = "A";
    private static final String VALUE_A = "AAA";
    private static final String KEY_B = "B";
    private static final String VALUE_B = "BBB";
    
    private TreeCache treeCache;
    private TreeCacheAdapter<Serializable, Serializable> cache;
    
    @Override
    public void setUp() throws Exception
    {
        treeCache = new TreeCache();
        treeCache.setTransactionManagerLookupClass(DummyTransactionManagerLookup.class.getName());
        treeCache.start();
        
        cache = new TreeCacheAdapter<Serializable, Serializable>();
        cache.setCache(treeCache);
        cache.setRegionName(getName());
    }
    
    public void testSimplePutGet() throws Exception
    {
        cache.put(KEY_A, VALUE_A);
        cache.put(KEY_B, VALUE_B);
        
        // check that this is present in the underlying cache
        Serializable checkValueA = (Serializable) treeCache.get(new Fqn(getName()), KEY_A);
        assertNotNull("Value A is not present in underlying cache", checkValueA);
        assertEquals("Value A is incorrect in underlying cache", VALUE_A, checkValueA);
        
        Serializable checkValueB = cache.get(KEY_B);
        assertNotNull("Value B is not present in cache", checkValueB);
        assertEquals("Value B is incorrect in cache", VALUE_B, checkValueB);
    }
}
