package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;

/**
 * Hide Inplace Record Test
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class HideInplaceRecordTest extends BaseRMTestCase
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isCollaborationSiteTest()
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /**
     * Tests hiding inplace records
     */
    public void testHideInplaceRecord()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void given()
            {
                // Check that the document is not a record
                assertFalse(recordService.isRecord(dmDocument));

                // Check that the record has one parent association
                assertEquals(1, nodeService.getParentAssocs(dmDocument).size());

                // Declare the document as a record
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // Declare record
                        recordService.createRecord(filePlan, dmDocument);

                        return null;
                    }
                 }, dmCollaborator);

                // Check that the document is a record
                assertTrue(recordService.isRecord(dmDocument));

                // Check that the record has two parent associations
                assertEquals(2, nodeService.getParentAssocs(dmDocument).size());
            }

            public void when()
            {
                // Hide the document
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // Hide record
                        inplaceRecordService.hideRecord(dmDocument);

                        return null;
                    }
                 }, dmCollaborator);
            }

            public void then()
            {
                // Check that the record has one parent association
                assertEquals(1, nodeService.getParentAssocs(dmDocument).size());
            }
        });
    }
}
