 
package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.GUID;

/**
 * Link/Unlink Record Tests
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class LinkRecordTest extends BaseRMTestCase
{
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    @Override
    protected void initServices()
    {
        super.initServices();
    }

    /**
     * Given source and destination disposition schedules are compatible
     * When I link a record to the record folder
     * Then it is successful
     */
    public void testLinkWithCompatibleDispositionSchedules() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef sourceRecordCategory;
            private NodeRef targetRecordCategory;
            private NodeRef sourceRecordFolder;
            private NodeRef targetRecordFolder;
            private NodeRef myRecord;
            
            public void given() throws Exception
            {
                // test entities
                sourceRecordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                sourceRecordFolder = recordFolderService.createRecordFolder(sourceRecordCategory, GUID.generate());
                myRecord = utils.createRecord(sourceRecordFolder, GUID.generate());
                targetRecordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                targetRecordFolder = recordFolderService.createRecordFolder(targetRecordCategory, GUID.generate());
                
                // create disposition schedules on record folders
                utils.createBasicDispositionSchedule(
                        sourceRecordCategory, 
                        "disposition instructions", 
                        "disposition authority", 
                        false, 
                        true);
                utils.createBasicDispositionSchedule(
                        targetRecordCategory, 
                        "disposition instructions", 
                        "disposition authority", 
                        false, 
                        true);
            }

            public void when() throws Exception
            {
                // link the record into the record folder
                recordService.link(myRecord, targetRecordFolder);
            }

            public void then() throws Exception
            {
                // assert that the record now has two parents
                List<ChildAssociationRef> assocs = nodeService.getParentAssocs(myRecord);
                assertNotNull(assocs);
                assertEquals(2, assocs.size());                
            }
        });
    }
    
    /**
     * Given source and destination disposition schedules are incompatible
     * When I link a record to the record folder
     * Then it is fails
     */
    public void testLinkWithIncompatibleDispositionSchedules() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class)
        {
            private NodeRef sourceRecordCategory;
            private NodeRef targetRecordCategory;
            private NodeRef sourceRecordFolder;
            private NodeRef targetRecordFolder;
            private NodeRef myRecord;
            
            public void given() throws Exception
            {
                // test entities
                sourceRecordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                sourceRecordFolder = recordFolderService.createRecordFolder(sourceRecordCategory, GUID.generate());
                myRecord = utils.createRecord(sourceRecordFolder, GUID.generate());
                targetRecordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                targetRecordFolder = recordFolderService.createRecordFolder(targetRecordCategory, GUID.generate());
                
                // create disposition schedules on record folders
                utils.createBasicDispositionSchedule(
                        sourceRecordCategory, 
                        "disposition instructions", 
                        "disposition authority", 
                        false, 
                        true);
                utils.createBasicDispositionSchedule(
                        targetRecordCategory, 
                        "disposition instructions", 
                        "disposition authority", 
                        true, 
                        true);
            }

            public void when() throws Exception
            {
                // link the record into the record folder
                recordService.link(myRecord, targetRecordFolder);
            }
        });
    }
}
