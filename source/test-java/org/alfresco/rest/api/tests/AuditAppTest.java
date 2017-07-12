/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.AuditApps;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.AuditApp;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuditAppTest extends AbstractSingleNetworkSiteTest
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

    @Test
    public void testGetAuditApps() throws Exception
    {
        try
        {

            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
            testGetAuditAppsSkipPaging();

        }
        finally
        {

        }
    }

    @Test
    public void testGetAuditApp() throws Exception
    {

        final AuditApps auditAppsProxy = publicApiClient.auditApps();

        // Enable system audit
        AuthenticationUtil.setFullyAuthenticatedUser(networkAdmin);
        enableSystemAudit();

        // Negative tests
        // Check with invalid audit application id.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
            auditAppsProxy.getAuditApp("invalidAuditId", HttpServletResponse.SC_NOT_FOUND);
        }

        // Check that non-admin user doesn't have access to audit applications
        {
            setRequestContext(networkOne.getId(), user1, null);
            auditAppsProxy.getAuditApp("randomAuditId", HttpServletResponse.SC_FORBIDDEN);
        }

        // Check that response code 501 is received when system audit is disabled
        {
            // Get an enabled audit application
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            int skipCount = 0;
            int maxItems = 4;
            Paging paging = getPaging(skipCount, maxItems);

            ListResponse<AuditApp> auditApps = getAuditApps(paging);
            AuditApp auditApp = auditAppsProxy.getAuditApp(auditApps.getList().get(0).getId());

            // Disable system audit
            AuthenticationUtil.setFullyAuthenticatedUser(networkAdmin);
            disableSystemAudit();

            // Check response code
            auditAppsProxy.getAuditApp(auditApp.getId(), HttpServletResponse.SC_NOT_IMPLEMENTED);

            // Re-enable system audit
            enableSystemAudit();
        }

        // Positive tests
        // Get audit application information
        {
            // Get the list of audit applications in the system
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            int skipCount = 0;
            int maxItems = 4;
            Paging paging = getPaging(skipCount, maxItems);

            ListResponse<AuditApp> auditApps = getAuditApps(paging);

            // Get audit application info
            AuditApp auditApp = auditAppsProxy.getAuditApp(auditApps.getList().get(0).getId());
            validateAuditApplicationFields(auditApp);
        }
    }

    private void testGetAuditAppsSkipPaging() throws Exception
    {
        // +ve: check skip count.
        {

            // Paging and list auditApp

            int skipCount = 0;
            int maxItems = 4;
            Paging paging = getPaging(skipCount, maxItems);

            ListResponse<AuditApp> resp = getAuditApps(paging);

            // Paging and list groups with skip count.

            skipCount = 2;
            maxItems = 2;
            paging = getPaging(skipCount, maxItems);

            ListResponse<AuditApp> sublistResponse = getAuditApps(paging);

            List<AuditApp> expectedSublist = sublist(resp.getList(), skipCount, maxItems);
            checkList(expectedSublist, sublistResponse.getPaging(), sublistResponse);
        }

        // -ve: check skip count.
        {
            getAuditApps(getPaging(-1, null), "", HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    private ListResponse<AuditApp> getAuditApps(final PublicApiClient.Paging paging, String errorMessage, int expectedStatus) throws Exception
    {
        final AuditApps auditAppsProxy = publicApiClient.auditApps();
        return auditAppsProxy.getAuditApps(createParams(paging), errorMessage, expectedStatus);
    }

    private ListResponse<AuditApp> getAuditApps(final PublicApiClient.Paging paging) throws Exception
    {
        return getAuditApps(paging, "Failed to get audit applications", HttpServletResponse.SC_OK);
    }

    protected Map<String, String> createParams(Paging paging)
    {
        Map<String, String> params = new HashMap<String, String>(2);
        if (paging != null)
        {
            if (paging.getSkipCount() != null)
            {
                params.put("skipCount", String.valueOf(paging.getSkipCount()));
            }
            if (paging.getMaxItems() != null)
            {
                params.put("maxItems", String.valueOf(paging.getMaxItems()));
            }
        }

        return params;
    }

    private void validateAuditApplicationFields(AuditApp auditApp)
    {
        assertNotNull(auditApp);
        assertNotNull(auditApp.getId());
        assertNotNull(auditApp.getName());
        assertNotNull(auditApp.getIsEnabled());
        assertFalse(auditApp.getId().isEmpty());
        assertFalse(auditApp.getName().isEmpty());
        assertTrue(auditApp.getIsEnabled());
    }


    @Test
    public void testEnableDisableAuditApplication() throws Exception
    {
        AuditApp requestAuditApp = new AuditApp();
        AuditApp responseAuditApp = null;
        String appId = null;

        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

        //Get one of the audit app ids (default tagging)
        ListResponse<AuditApp> apps = publicApiClient.auditApps().getAuditApps(null, "Getting audit apps error ", HttpServletResponse.SC_OK);
        appId = (apps.getList().size()>0) ? apps.getList().get(0).getId() : "tagging";

        // +ve
        // Disable audit app
        requestAuditApp.setIsEnabled(false);
        responseAuditApp = publicApiClient.auditApps().updateAuditApp(appId,requestAuditApp,null, HttpServletResponse.SC_OK);
        assertFalse("Wrong response for request to disable audit app.", responseAuditApp.getIsEnabled());
        assertFalse("Disable audit app test failed.", publicApiClient.auditApps().getAuditApp(appId).getIsEnabled());

        // Enable audit app
        requestAuditApp.setIsEnabled(true);
        responseAuditApp = publicApiClient.auditApps().updateAuditApp(appId,requestAuditApp,null, HttpServletResponse.SC_OK);
        assertTrue("Wrong response for request to enable audit app.", responseAuditApp.getIsEnabled());
        assertTrue("Enable audit app test failed.", publicApiClient.auditApps().getAuditApp(appId).getIsEnabled());

        // -ve
        // 400
        publicApiClient.auditApps().update("audit-applications",appId,null,null,"badBody",null,"Was expecting error 400",HttpServletResponse.SC_BAD_REQUEST);
        // 401
        setRequestContext(networkOne.getId(), networkAdmin, "fakepswd");
        publicApiClient.auditApps().updateAuditApp(appId,requestAuditApp,null, HttpServletResponse.SC_UNAUTHORIZED);
        // 403
        setRequestContext(networkOne.getId(), user1, null);
        publicApiClient.auditApps().updateAuditApp(appId,requestAuditApp,null, HttpServletResponse.SC_FORBIDDEN);
        // 404
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        publicApiClient.auditApps().updateAuditApp("fakeid",requestAuditApp,null, HttpServletResponse.SC_NOT_FOUND);
        // 501

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
