 
package org.alfresco.module.org_alfresco_module_rm.test.integration.dod;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;


/**
 * Integration test for RM1147 - A user can create a 'vanilla' or DOD compliant records management site.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RM1194ExcludeDoDRecordTypesTest extends BaseRMTestCase implements DOD5015Model
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
	 * Ensure that the correct record metadata aspects are available for a DoD record.
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
                Set<QName> aspects = recordService.getRecordMetadataAspects(record);
                assertNotNull(aspects);
                assertEquals(5, aspects.size());                
            }
        }); 
	}
	
	/**
	 * Ensure that the correct record metadata aspects are available for a vanilla record.
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
                Set<QName> aspects = recordService.getRecordMetadataAspects(record);
                assertNotNull(aspects);
                assertEquals(2, aspects.size());
            }
        }); 
	}
}
