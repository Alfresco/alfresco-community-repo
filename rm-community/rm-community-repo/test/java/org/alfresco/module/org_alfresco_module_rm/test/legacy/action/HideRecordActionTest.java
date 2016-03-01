 
package org.alfresco.module.org_alfresco_module_rm.test.legacy.action;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.action.dm.HideRecordAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Hide Record Action Unit Test
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class HideRecordActionTest extends BaseRMTestCase
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

    public void testHideRecordAction()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                // Create a document so that the user has the write permissions for that document
                NodeRef doc = fileFolderService.create(dmFolder, "testfile.txt", ContentModel.TYPE_CONTENT).getNodeRef();

                // Create a record from that document
                Action createAction = actionService.createAction(CreateRecordAction.NAME);
                createAction.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);
                actionService.executeAction(createAction, doc);

                // Check if the document is a record now
                assertTrue(recordService.isRecord(doc));

                // The record should have the original location information
                assertNotNull(nodeService.getProperty(doc, PROP_RECORD_ORIGINATING_LOCATION));

                // Check the parents. In this case the document should have two parents (doclib and fileplan)
                assertTrue(nodeService.getParentAssocs(doc).size() == 2);

                // Hide the document. The user has the write permissions so he should be able to hide it
                Action hideAction = actionService.createAction(HideRecordAction.NAME);
                actionService.executeAction(hideAction, doc);

                // The document should be removed from the collaboration site
                assertTrue(nodeService.getParentAssocs(doc).size() == 1);

                return null;
            }
        },
        dmCollaborator);
    }
}
