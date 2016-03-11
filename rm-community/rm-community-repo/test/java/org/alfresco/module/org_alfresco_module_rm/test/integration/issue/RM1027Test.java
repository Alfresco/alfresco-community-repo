package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Unit test for RM-1027 .. can't copy a closed folder
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class RM1027Test extends BaseRMTestCase
{
    public void testCopyingAClosedFolder() throws Exception
    {
        final NodeRef recordFolder = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create a folder and close it
                NodeRef recordFolder = recordFolderService.createRecordFolder(rmContainer, "My Closed Record Folder");
                utils.closeFolder(recordFolder);

                assertTrue((Boolean)nodeService.getProperty(recordFolder, PROP_IS_CLOSED));

                return recordFolder;
            }
        });

        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                // create a destination for the copy
                NodeRef destination = filePlanService.createRecordCategory(filePlan, "Copy Destination");

                // try and copy the closed record folder
                FileInfo copyInfo = fileFolderService.copy(recordFolder, destination, null);

                return copyInfo.getNodeRef();
            }

            @Override
            public void test(NodeRef copy) throws Exception
            {
                assertNotNull(copy);

                assertNotNull(nodeService.getProperty(copy, PROP_IDENTIFIER));
                assertFalse((Boolean)nodeService.getProperty(copy, PROP_IS_CLOSED));
            }
        });
    }

    public void testCopyingAFolderWithADispositionSchedule() throws Exception
    {
        final NodeRef recordFolder = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create a folder
                NodeRef recordFolder = recordFolderService.createRecordFolder(rmContainer, "My Closed Record Folder");

                // show that the default disposition schedule has been applied
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));

                return recordFolder;
            }
        });

        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                // create a destination for the copy
                NodeRef destination = filePlanService.createRecordCategory(filePlan, "Copy Destination");

                // try and copy the closed record folder
                FileInfo copyInfo = fileFolderService.copy(recordFolder, destination, null);

                return copyInfo.getNodeRef();
            }

            @Override
            public void test(NodeRef copy) throws Exception
            {
                assertNotNull(copy);

                assertNotNull(nodeService.getProperty(copy, PROP_IDENTIFIER));
                assertFalse(nodeService.hasAspect(copy, ASPECT_DISPOSITION_LIFECYCLE));
            }
        });
    }
}
