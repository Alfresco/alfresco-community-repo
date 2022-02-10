/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestServiceImpl;
import org.alfresco.service.namespace.QName;

/**
 * Unit test for service base implementation.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class ServiceBaseImplTest extends BaseRMTestCase
{
    /** test service */
    private TestServiceImpl testService;
    
    /**
     * Init services
     */
    @Override
    protected void initServices()
    {
        super.initServices();
        
        testService = (TestServiceImpl)applicationContext.getBean("testService");
    }

    /**
     * test instanceOf()
     */
    public void testInstanceOf()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                assertTrue(testService.doInstanceOf(rmFolder, ContentModel.TYPE_FOLDER));
                assertTrue(testService.doInstanceOf(rmFolder, TYPE_RECORD_FOLDER));
                assertFalse(testService.doInstanceOf(rmFolder, TYPE_RECORD_CATEGORY));
                
                return null;
            }
        });
        
    }
    
    /**
     * test getNextCounter()
     */
    public void testGetNextCounter()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                assertNull(nodeService.getProperty(rmFolder, PROP_COUNT));
                assertEquals(1, testService.doGetNextCount(rmFolder));
                assertEquals(2, testService.doGetNextCount(rmFolder));
                assertEquals(3, testService.doGetNextCount(rmFolder));
                
                return null;
            }
        });
        
    }
    
    /**
     * test getTypeAndAspects()
     */
    public void testGetTypeAndAspects()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                Set<QName> result = testService.doGetTypeAndApsects(rmFolder);
                assertTrue(result.contains(TYPE_RECORD_FOLDER));
        
                return null;
            }
        });        
    }

}
