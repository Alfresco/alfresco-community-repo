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

package org.alfresco.module.org_alfresco_module_rm.test.integration.dod;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;


/**
 * Integration test for RM1147 - A user can create a 'vanilla' or DOD compliant records management site.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RM1147DODRMSiteTest extends BaseRMTestCase implements DOD5015Model
{
	/**
	 * Don't create a RM test site in setup
	 */
	@Override
	protected boolean isRMSiteTest() 
	{
		return false;
	}
	
	/**
	 * Test the creation of a DOD file plan via the file plan service
	 */
	public void testCreateDODFilePlan()
	{
		doTestInTransaction(new Test<NodeRef>()
	    {
			@Override
	        public NodeRef run() throws Exception
	        {
				return filePlanService.createFilePlan(folder, "myDODfileplan", TYPE_DOD_5015_FILE_PLAN);
	        }
			
			@Override
			public void test(NodeRef filePlan) throws Exception 
			{
				assertNotNull(filePlan);
				assertEquals(TYPE_DOD_5015_FILE_PLAN, nodeService.getType(filePlan));				
				assertTrue(filePlanService.isFilePlanComponent(filePlan));
				assertTrue(filePlanService.isFilePlan(filePlan));			
				assertEquals(FilePlanComponentKind.FILE_PLAN, filePlanService.getFilePlanComponentKind(filePlan));				
				assertNotNull(filePlanService.getUnfiledContainer(filePlan));				
				assertNotNull(filePlanService.getHoldContainer(filePlan));
				assertNotNull(filePlanService.getTransferContainer(filePlan));				
				assertTrue(filePlanService.getFilePlans().contains(filePlan));								
				assertFalse(filePlanRoleService.getRoles(filePlan).isEmpty());	
			}
	    });		
	}
	
	/**
	 * Test the creation of a DOD site via the site service
	 */
	public void testCreateDODRMSite()
	{
		doTestInTransaction(new Test<SiteInfo>()
	    {
			String siteId = GUID.generate();
			
			@Override
	        public SiteInfo run() throws Exception
	        {
				return siteService.createSite("dodrmsite", siteId, "title", "description", SiteVisibility.PUBLIC, TYPE_DOD_5015_SITE);
	        }
			
			@Override
			public void test(SiteInfo siteInfo) throws Exception 
			{
				assertNotNull(siteInfo);
				assertEquals(TYPE_DOD_5015_SITE, nodeService.getType(siteInfo.getNodeRef()));
				
				NodeRef filePlan = siteService.getContainer(siteId, "documentLibrary");
				assertNotNull(filePlan);
				assertEquals(TYPE_DOD_5015_FILE_PLAN, nodeService.getType(filePlan));				
				assertTrue(filePlanService.isFilePlanComponent(filePlan));
				assertTrue(filePlanService.isFilePlan(filePlan));			
				assertEquals(FilePlanComponentKind.FILE_PLAN, filePlanService.getFilePlanComponentKind(filePlan));				
				assertNotNull(filePlanService.getUnfiledContainer(filePlan));				
				assertNotNull(filePlanService.getHoldContainer(filePlan));
				assertNotNull(filePlanService.getTransferContainer(filePlan));				
				assertTrue(filePlanService.getFilePlans().contains(filePlan));								
				assertFalse(filePlanRoleService.getRoles(filePlan).isEmpty());				
			}
	    });	
	}
	
	/**
	 * Test to ensure that a record created in the a DOD site does have the DOD meta-data attached
	 */
	public void testDODRecord()
	{
        doTestInTransaction(new Test<NodeRef>()
        {
            String siteId = GUID.generate();
            
            @Override
            public NodeRef run() throws Exception
            {
                siteService.createSite("dodrmsite", siteId, "title", "description", SiteVisibility.PUBLIC, TYPE_DOD_5015_SITE);
                NodeRef filePlan = siteService.getContainer(siteId, "documentlibrary");
                assertNotNull(filePlan);
                
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, "testOne");
                NodeRef recordFolder = recordFolderService.createRecordFolder(recordCategory, "testOne");                
                NodeRef record = utils.createRecord(recordFolder, "testOne.txt", "Test One");
                
                return record;
            }
            
            @Override
            public void test(NodeRef record) throws Exception 
            {
                assertNotNull(record);
                assertTrue(nodeService.hasAspect(record, ASPECT_DOD_5015_RECORD));
            }
        }); 
	}
	
	/**
	 * Test to ensure a record created in a vanilla site does not have the DOD meta-data attached
	 */
	public void testVanillaRecord()
	{
        doTestInTransaction(new Test<NodeRef>()
        {
            String siteId = GUID.generate();
            
            @Override
            public NodeRef run() throws Exception
            {
                siteService.createSite("rmsite", siteId, "title", "description", SiteVisibility.PUBLIC, TYPE_RM_SITE);
                NodeRef filePlan = siteService.getContainer(siteId, "documentlibrary");
                assertNotNull(filePlan);
                
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, "testOne");
                NodeRef recordFolder = recordFolderService.createRecordFolder(recordCategory, "testOne");                
                NodeRef record = utils.createRecord(recordFolder, "testOne.txt", "Test One");
                
                return record;
            }
            
            @Override
            public void test(NodeRef record) throws Exception 
            {
                assertNotNull(record);
                assertFalse(nodeService.hasAspect(record, ASPECT_DOD_5015_RECORD));
            }
        }); 
	}
}
