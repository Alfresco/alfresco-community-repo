package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

/**
 * Test for RM-1814
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RM1814Test extends BaseRMTestCase
{
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    public void testRM1814() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                NodeRef hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
                holdService.addToHold(hold, recordTwo);
                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                relationshipService.addRelationship(CUSTOM_REF_VERSIONS.getLocalName(), recordOne, recordThree);
                return null;
            }
        });

        doTestInTransaction(new FailureTest
        (
            "Target node is in a hold."
        )
        {
            @Override
            public void run() throws Exception
            {
                relationshipService.addRelationship(CUSTOM_REF_OBSOLETES.getLocalName(), recordOne, recordTwo);
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                relationshipService.addRelationship(CUSTOM_REF_SUPPORTS.getLocalName(), recordOne, recordFour);
                return null;
            }
        });
    }
}
