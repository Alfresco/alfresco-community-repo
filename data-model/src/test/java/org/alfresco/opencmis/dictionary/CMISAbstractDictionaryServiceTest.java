/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.opencmis.dictionary;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.alfresco.repo.cache.MemoryCache;
import org.alfresco.repo.cache.SimpleCache;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link CMISAbstractDictionaryServiceTest} class.
 * 
 * @author Matt Ward
 */
public class CMISAbstractDictionaryServiceTest
{
    // Class under test
    private CMISAbstractDictionaryService dictService;
    private SimpleCache<String, CMISDictionaryRegistry> cache;
    private boolean initCalled;
    private CMISDictionaryRegistry dictRegistry;
    
    @Before
    public void setUp() throws Exception
    {
        dictService = new CMISAbstractDictionaryService()
        {
            @Override
            protected CMISDictionaryRegistry getRegistry()
            {
                initCalled = true;
                cache.put("cache_key", dictRegistry);
                return dictRegistry;
            }

			@Override
			protected DictionaryInitializer getCoreDictionaryInitializer()
			{
				return null;
			}

			@Override
			protected DictionaryInitializer getTenantDictionaryInitializer()
			{
				return null;
			}
        };

        dictRegistry = new CMISDictionaryRegistryImpl();
        cache = new MemoryCache<String, CMISDictionaryRegistry>();
        dictService.setSingletonCache(cache);
        initCalled = false;
    }

    @Test
    public void canGetRegistryWhenInitNotYetCalled()
    {
        // Pre-conditions of test
//        dictService.key_opencmis_dictionary_registry = null;
//        assertNull(dictService.key_opencmis_dictionary_registry);
        assertFalse(initCalled);

        CMISDictionaryRegistry registry = dictService.getRegistry();

        assertTrue("init() should have been called.", initCalled);
        assertSame(dictRegistry, registry);
    }
    
    @Test
    public void canGetRegistryWhenInitAlreadyCalled()
    {
        // Pre-conditions of test
        dictService.getRegistry();
//        assertNotNull(dictService.key_opencmis_dictionary_registry);
        assertTrue(initCalled);

        // Perform test
        CMISDictionaryRegistry registry = dictService.getRegistry();

        assertTrue("init() should have been called.", initCalled);
        assertSame(dictRegistry, registry);
    }
}
