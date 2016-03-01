 
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.site.SiteRole;


/**
 * Unit test for RM-804 .. site managers are able to delete file plans
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class RM804Test extends BaseRMTestCase
{
    @Override
    protected void initServices()
    {
        super.initServices();
    }

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    public void testUsersHaveDeletePermissionsOnFilePlan() throws Exception
    {
        // as rmuser
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, capabilityService.getCapabilityAccessState(filePlan, "Delete"));

                return null;
            }
        }, ADMIN_USER);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, capabilityService.getCapabilityAccessState(filePlan, "Delete"));

                return null;
            }
        }, ADMIN_USER);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, capabilityService.getCapabilityAccessState(filePlan, "Delete"));

                return null;
            }
        }, ADMIN_USER);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, capabilityService.getCapabilityAccessState(filePlan, "Delete"));

                return null;
            }
        }, rmUserName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, capabilityService.getCapabilityAccessState(filePlan, "Delete"));

                return null;
            }
        }, userName);
    }

    public void testTryAndDeleteSiteAsSiteManagerOnly()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                siteService.setMembership(siteId, userName, SiteRole.SiteManager.toString());

                return null;
            }
        }, "admin");

        doTestInTransaction(new FailureTest
        (
           "Should not be able to delete site as a site manager only.",
           AlfrescoRuntimeException.class
        )
        {
            @Override
            public void run() throws Exception
            {
                siteService.deleteSite(siteId);

            }
        }, userName);

        // give the user a RM role (but not sufficient to delete the file plan node ref)
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_USER, userName);

                return null;
            }
        }, "admin");

        doTestInTransaction(new FailureTest
        (
           "Should not be able to delete site as a site manager with an RM role that doesn't have the capability.",
           AlfrescoRuntimeException.class
        )
        {
            @Override
            public void run() throws Exception
            {
                siteService.deleteSite(siteId);

            }
        }, userName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_ADMIN, userName);

                return null;
            }
        }, "admin");

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                siteService.deleteSite(siteId);

                return null;
            }
        }, userName);

    }

}
