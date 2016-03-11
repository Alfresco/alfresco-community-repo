package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Integration test for RM-1887
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class RM1887Test extends BaseRMTestCase 
{        
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }
   
    /**
     * Given that a record is unfiled
     * And an unfiled folder has been created
     * When I move the unfiled record into the unfiled folder
     * Then the filed date of the unfiled record remains unset
     */
    public void testMoveUnfiledRecord() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {         
            private NodeRef unfiledRecordFolder;
            private NodeRef unfiledRecord;
            
            public void given() throws Exception
            {
                // create unfiled folder
                unfiledRecordFolder = fileFolderService.create(filePlanService.getUnfiledContainer(filePlan), "my test folder", TYPE_UNFILED_RECORD_FOLDER).getNodeRef();
                
                // crate unfiled record
                unfiledRecord = recordService.createRecordFromContent(filePlan, "test.txt", TYPE_CONTENT, null, null);
                
                // check the record
                assertTrue(recordService.isRecord(unfiledRecord));
                assertFalse(recordService.isFiled(unfiledRecord));
            }
            
            public void when() throws Exception
            {   
                // move the record into the unfiled folder
                fileFolderService.move(unfiledRecord, unfiledRecordFolder, null);
            }            
            
            public void then()
            {
                // check the record
                assertTrue(recordService.isRecord(unfiledRecord));
                assertFalse(recordService.isFiled(unfiledRecord));
            }
        });  

    }
    

}
