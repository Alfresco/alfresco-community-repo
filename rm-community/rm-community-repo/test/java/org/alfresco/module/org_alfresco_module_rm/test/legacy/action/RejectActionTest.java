package org.alfresco.module.org_alfresco_module_rm.test.legacy.action;

import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.RejectAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.Action;

/**
 * Reject Action Unit Test
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RejectActionTest extends BaseRMTestCase
{
    /** Reject reason */
    private final String REJECT_REASON = "rejectReason:Not valid!£$%^&*()_+";

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

    public void testRejectAction()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                // Create a record from the document
                Action createAction = actionService.createAction(CreateRecordAction.NAME);
                createAction.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);
                actionService.executeAction(createAction, dmDocument);

                // Check if the document is a record now
                assertTrue(recordService.isRecord(dmDocument));

                // The record should have the original location information
                assertNotNull(nodeService.getProperty(dmDocument, PROP_RECORD_ORIGINATING_LOCATION));

                // Check the parents. In this case the document should have two parents (doclib and fileplan)
                assertTrue(nodeService.getParentAssocs(dmDocument).size() == 2);

                return null;
            }
        },
        dmCollaborator);

        doTestInTransaction(new FailureTest("Cannot reject a record without a reason.", IllegalArgumentException.class)
        {
            public void run()
            {
                // The test should fail if the reject reason is not supplied
                Action rejectAction = actionService.createAction(RejectAction.NAME);
                actionService.executeAction(rejectAction, dmDocument);
            }
        },
        dmCollaborator);

        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                // Create the reject action and add the reject reason
                Action rejectAction = actionService.createAction(RejectAction.NAME);
                rejectAction.setParameterValue(RejectAction.PARAM_REASON, REJECT_REASON);
                actionService.executeAction(rejectAction, dmDocument);

                // The "record" aspect should be removed
                assertFalse(nodeService.hasAspect(dmDocument, ASPECT_RECORD));

                // The "file plan component" should be removed
                assertFalse(nodeService.hasAspect(dmDocument, ASPECT_FILE_PLAN_COMPONENT));

                // The "identifier" property should be removed
                assertNull(nodeService.getProperty(dmDocument, PROP_IDENTIFIER));

                // The record should be removed from the file plan
                assertTrue(nodeService.getParentAssocs(dmDocument).size() == 1);

                // The extended reader information should be removed
                assertNull(extendedSecurityService.getExtendedReaders(dmDocument));

                return null;
            }
        },
        dmCollaborator);
    }
}
