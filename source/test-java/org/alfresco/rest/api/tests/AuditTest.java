package org.alfresco.rest.api.tests;

import static org.junit.Assert.fail;

import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.After;
import org.junit.Before;

/**
 * Added as an extra layer for Rest-Api Audit methods and objects that can be used by all junits
 * E.g enableSystemAudit, disableSystemAudit
 * 
 */

public class AuditTest extends AbstractSingleNetworkSiteTest
{
    protected PermissionService permissionService;
    protected AuthorityService authorityService;
    protected AuditService auditService;

    @Before
    public void setup() throws Exception
    {
        super.setup();

        permissionService = applicationContext.getBean("permissionService", PermissionService.class);
        authorityService = (AuthorityService) applicationContext.getBean("AuthorityService");
        auditService = applicationContext.getBean("AuditService", AuditService.class);
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    protected void enableSystemAudit()
    {
        boolean isEnabled = auditService.isAuditEnabled();
        if (!isEnabled)
        {
            auditService.setAuditEnabled(true);
            isEnabled = auditService.isAuditEnabled();
            if (!isEnabled)
            {
                fail("Failed to enable system audit for testing");
            }
        }

    }

    protected void disableSystemAudit()
    {
        boolean isEnabled = auditService.isAuditEnabled();
        if (isEnabled)
        {
            auditService.setAuditEnabled(false);
            isEnabled = auditService.isAuditEnabled();
            if (isEnabled)
            {
                fail("Failed to disable system audit for testing");
            }
        }

    }
}
