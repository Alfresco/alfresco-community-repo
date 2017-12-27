package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.GUID;

public class RM5225Test extends BaseRMTestCase
{
    /**
     * Given the RM site, a record category created in the fileplan, a record foler containing a record
     * When we create a copy from the existing record
     * Then the created record name contains both the name of the record from which it was created and the unique identifier of the current record.
     */
    public void testCopyToRecord()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private NodeRef copiedRecord;

            public void given()
            {

                /** Create record category. */
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());

                /** Create record folder. */
                NodeRef recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());

                /** File record. */
                record = utils.createRecord(recordFolder, GUID.generate());
            }

            public void when()
            {
                /** Create a copy of the original record */
                copiedRecord = recordService.createRecordFromCopy(filePlan, record);
            }

            public void then()
            {
                /** Check if the copied record contains the name of the record from which is copied. */
                assertTrue(nodeService.getProperty(copiedRecord, PROP_NAME).toString().contains(nodeService.getProperty(record, PROP_NAME).toString()));
                /** Check if the copied record name contains its unique id. */
                assertTrue(nodeService.getProperty(copiedRecord, PROP_NAME).toString().contains(nodeService.getProperty(copiedRecord, PROP_IDENTIFIER).toString()));
            }
        });
    }
}
