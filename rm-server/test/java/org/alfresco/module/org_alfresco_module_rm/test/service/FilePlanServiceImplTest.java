/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.test.service;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;

/**
 * File plan service unit test
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class FilePlanServiceImplTest extends BaseRMTestCase
{
    /** 
     * Pull in collaboration test data 
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }
    
    /**
     * {@link FilePlanService#isFilePlan(org.alfresco.service.cmr.repository.NodeRef)}
     */
    public void testIsFilePlan()
    {
        doTestInTransaction(new VoidTest()
        {
            public void runImpl() throws Exception
            {             
                assertTrue(filePlanService.isFilePlan(filePlan));
                assertFalse(filePlanService.isFilePlan(rmContainer));
                assertFalse(filePlanService.isFilePlan(dmDocument));
            }
        });         
    }
    
    /**
     * {@link FilePlanService#getFilePlan(org.alfresco.service.cmr.repository.NodeRef)}
     */
    public void testGetFilePlans()
    {
        doTestInTransaction(new VoidTest()
        {
            public void runImpl() throws Exception
            {             
                assertEquals(filePlan, filePlanService.getFilePlan(filePlan));
                assertEquals(filePlan, filePlanService.getFilePlan(rmContainer));
                assertEquals(filePlan, filePlanService.getFilePlan(rmFolder));
                assertNull(filePlanService.getFilePlan(dmDocument));
            }
        });        
    }
    
    /**
     * {@link FilePlanService#getFilePlanBySiteId(String)}
     */
    public void testGetFilePlanBySiteId()
    {
        doTestInTransaction(new VoidTest()
        {
            public void runImpl() throws Exception
            {             
                assertEquals(filePlan, filePlanService.getFilePlanBySiteId(SITE_ID));
                assertNull(filePlanService.getFilePlanBySiteId("rubbish"));
                
                String siteId = GUID.generate();
                siteService.createSite("anything", siteId, "title", "descrition", SiteVisibility.PUBLIC);
                assertNull(filePlanService.getFilePlanBySiteId(siteId));
            }
        }); 
        
    }
}
