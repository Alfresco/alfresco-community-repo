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

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.AuditApps;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.data.AuditApp;
import org.alfresco.rest.api.tests.client.data.AuditEntry;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.ISO8601DateFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

/**
 * @author anechifor, aforascu, eknizat, janv
 */
public class AuditAppTest extends AbstractSingleNetworkSiteTest 
{
    protected PermissionService permissionService;
    protected AuthorityService authorityService;
    protected AuditService auditService;
    protected static String AUDIT_APP_ID = "alfresco-access";

    @Before
    public void setup() throws Exception 
    {
        super.setup();

        permissionService = applicationContext.getBean("permissionService", PermissionService.class);
        authorityService = (AuthorityService) applicationContext.getBean("AuthorityService");
        auditService = applicationContext.getBean("AuditService", AuditService.class);

        AuditModelRegistryImpl auditModelRegistry = (AuditModelRegistryImpl) applicationContext.getBean("auditModel.modelRegistry");

        // Register the test model
        URL testModelUrl = ResourceUtils.getURL("classpath:alfresco/audit/alfresco-audit-access.xml");
        auditModelRegistry.registerModel(testModelUrl);
        auditModelRegistry.loadAuditModels();
    }

    @After
    public void tearDown() throws Exception 
    {
        super.tearDown();
    }

    @Test
    public void testGetAuditApps() throws Exception 
    {
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        testGetAuditAppsSkipPaging();
    }

    @Test
    public void testGetAuditApp() throws Exception 
    {
        final AuditApps auditAppsProxy = publicApiClient.auditApps();

        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        String appId = getFirstAuditAppId();

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

        // Check that response code 501 is received when system audit is
        // disabled
        {
            // Get an enabled audit application
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            // Disable system audit
            AuthenticationUtil.setFullyAuthenticatedUser(networkAdmin);
            disableSystemAudit();

            // Check response code
            auditAppsProxy.getAuditApp(appId, HttpServletResponse.SC_NOT_IMPLEMENTED);

            // Re-enable system audit
            enableSystemAudit();
        }

        // Positive tests
        // Get audit application information
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            AuditApp auditApp = auditAppsProxy.getAuditApp(appId);
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

    private ListResponse<AuditApp> getAuditApps(final PublicApiClient.Paging paging, String errorMessage,
            int expectedStatus) throws Exception 
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

    private void validateAuditEntryFields(AuditEntry auditEntry, AuditApp auditApp) 
    {
        String auditAppid = auditApp.getId();

        assertNotNull(auditEntry);
        assertNotNull(auditEntry.getId());
        assertNotNull(auditEntry.getAuditApplicationId());
        assertNotNull(auditEntry.getCreatedAt());
        assertNotNull(auditEntry.getCreatedByUser());
        assertFalse(auditEntry.getId().toString().isEmpty());
        assertFalse(auditEntry.getAuditApplicationId().isEmpty());

        if (auditApp.getId().equals(AUDIT_APP_ID)) 
        {
            assertTrue(auditEntry.getAuditApplicationId().toString().equals(auditAppid));
        }
    }

    private String getFirstAuditAppId() throws PublicApiException 
    {
        // Get one of the audit app ids ( fail test if there are no audit apps
        // in the system )
        ListResponse<AuditApp> apps = publicApiClient.auditApps().getAuditApps(null, "Getting audit apps error ",
                HttpServletResponse.SC_OK);
        if (apps.getList().size() == 0) 
        {
            fail("There are no audit applications to run this test against.");
        }
        return apps.getList().get(0).getId();
    }

    @Test
    public void testEnableDisableAuditApplication() throws Exception 
    {
        AuditApp requestAuditApp = new AuditApp();
        AuditApp responseAuditApp = null;

        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        String appId = getFirstAuditAppId();

        // +ve
        // Disable audit app
        requestAuditApp.setIsEnabled(false);
        responseAuditApp = publicApiClient.auditApps().updateAuditApp(appId, requestAuditApp, null,
                HttpServletResponse.SC_OK);
        assertFalse("Wrong response for request to disable audit app.", responseAuditApp.getIsEnabled());
        assertFalse("Disable audit app test failed.", publicApiClient.auditApps().getAuditApp(appId).getIsEnabled());

        // Enable audit app
        requestAuditApp.setIsEnabled(true);
        responseAuditApp = publicApiClient.auditApps().updateAuditApp(appId, requestAuditApp, null,
                HttpServletResponse.SC_OK);
        assertTrue("Wrong response for request to enable audit app.", responseAuditApp.getIsEnabled());
        assertTrue("Enable audit app test failed.", publicApiClient.auditApps().getAuditApp(appId).getIsEnabled());

        // -ve
        // 400
        publicApiClient.auditApps().update("audit-applications", appId, null, null, "badBody", null,
                "Was expecting error 400", HttpServletResponse.SC_BAD_REQUEST);
        // 401
        setRequestContext(networkOne.getId(), networkAdmin, "fakepswd");
        publicApiClient.auditApps().updateAuditApp(appId, requestAuditApp, null, HttpServletResponse.SC_UNAUTHORIZED);
        // 403
        setRequestContext(networkOne.getId(), user1, null);
        publicApiClient.auditApps().updateAuditApp(appId, requestAuditApp, null, HttpServletResponse.SC_FORBIDDEN);
        // 404
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        publicApiClient.auditApps().updateAuditApp("fakeid", requestAuditApp, null, HttpServletResponse.SC_NOT_FOUND);
        // 501
        AuthenticationUtil.setFullyAuthenticatedUser(networkAdmin);
        disableSystemAudit();
        publicApiClient.auditApps().updateAuditApp(appId, requestAuditApp, null,
                HttpServletResponse.SC_NOT_IMPLEMENTED);
        enableSystemAudit();
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

    @Test
    public void testAuditEntries() throws Exception 
    {
        final AuditApps auditAppsProxy = publicApiClient.auditApps();

        // Get and enable audit app
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        AuditApp auditApp = auditAppsProxy.getAuditApp("alfresco-access");

        testGetAuditEntries(auditAppsProxy, auditApp);
        testAuditEntriesSorting(auditAppsProxy, auditApp);
        testAuditEntriesWhereDate(auditAppsProxy, auditApp);
        testAuditEntriesWhereId(auditAppsProxy, auditApp);
        testAuditEntriesWithInclude(auditAppsProxy, auditApp);
        testAuditEntriesSkipCount(auditAppsProxy, auditApp);
        testRetrieveAuditEntry(auditAppsProxy, auditApp);
        testDeleteAuditEntry(auditAppsProxy, auditApp);
        testDeleteAuditEntries(auditAppsProxy, auditApp);
    }

    private void testGetAuditEntries(AuditApps auditAppsProxy, AuditApp auditApp) throws Exception 
    {
        // Positive tests
        ListResponse<AuditEntry> auditEntries = auditAppsProxy.getAuditAppEntries(auditApp.getId(), null,
                HttpServletResponse.SC_OK);
        for (AuditEntry ae : auditEntries.getList()) 
        {
            validateAuditEntryFields(ae, auditApp);
        }

        // Negative tests
        // 401
        setRequestContext(networkOne.getId(), networkAdmin, "wrongPassword");
        auditAppsProxy.getAuditAppEntries(auditApp.getId(), null, HttpServletResponse.SC_UNAUTHORIZED);
        // 403
        setRequestContext(networkOne.getId(), user1, null);
        auditAppsProxy.getAuditAppEntries(auditApp.getId(), null, HttpServletResponse.SC_FORBIDDEN);
        // 404
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        auditAppsProxy.getAuditAppEntries("randomId", null, HttpServletResponse.SC_NOT_FOUND);
        // 501
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        AuthenticationUtil.setFullyAuthenticatedUser(networkAdmin);
        disableSystemAudit();
        auditAppsProxy.getAuditAppEntries("randomId", null, HttpServletResponse.SC_NOT_IMPLEMENTED);
        enableSystemAudit();
    }

    private void testAuditEntriesSorting(AuditApps auditAppsProxy, AuditApp auditApp) throws Exception 
    {
        // paging
        Paging paging = getPaging(0, 10);
        Map<String, String> otherParams = new HashMap<>();

        // Default order.
        addOrderBy(otherParams, org.alfresco.rest.api.Audit.CREATED_AT, null);
        ListResponse<AuditEntry> auditEntries = auditAppsProxy.getAuditAppEntries(auditApp.getId(),
                createParams(paging, otherParams), HttpServletResponse.SC_OK);
        assertNotNull(auditEntries);
        assertTrue("audit entry size more that 2", auditEntries.getList().size() > 1);
        assertTrue("auditEntries order not valid",
                auditEntries.getList().get(0).getCreatedAt().before(auditEntries.getList().get(1).getCreatedAt()));

    }

    private void testAuditEntriesWhereDate(AuditApps auditAppsProxy, AuditApp auditApp) throws Exception 
    {
        // paging
        Paging paging = getPaging(0, 10);

        Date dateBefore = new Date();

        String dateBeforeWhere = ISO8601DateFormat.format(dateBefore);

        createUser("usern-" + RUNID, "userNPassword", networkOne);

        login("usern-" + RUNID, "userNPassword");

        Date dateAfter = new Date();

        String dateAfterWhere = ISO8601DateFormat.format(dateAfter);

        Map<String, String> otherParams = new HashMap<>();
        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between (" + "\'" + dateBeforeWhere + "\'" + ", " + "\'"
                + dateAfterWhere + "\'" + "))");

        ListResponse<AuditEntry> auditEntries = auditAppsProxy.getAuditAppEntries(auditApp.getId(), createParams(paging, otherParams),
                HttpServletResponse.SC_OK);
        assertNotNull(auditEntries);
        assertTrue(auditEntries.getList().size() > 0);

        for (AuditEntry ae : auditEntries.getList())
        {
            validateAuditEntryFields(ae, auditApp);
        }

        //
        // note: sanity test parsing of a few ISO8601 formats (non-exhaustive) - eg. +01:00, +0000, Z
        //

        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between ("
                + "\'2016-06-02T12:13:51.593+01:00\'" + ", " + "\'2017-06-04T10:05:16.536+01:00\'" + "))");
        auditAppsProxy.getAuditAppEntries(auditApp.getId(), createParams(paging, otherParams), HttpServletResponse.SC_OK);

        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between ("
                + "\'2016-06-02T11:13:51.593+0000\'" + ", " + "\'2017-06-04T09:05:16.536+0000\'" + "))");
        auditAppsProxy.getAuditAppEntries(auditApp.getId(), createParams(paging, otherParams), HttpServletResponse.SC_OK);

        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between ("
                + "\'2016-06-02T11:13:51.593Z\'" + ", " + "\'2017-06-04T09:05:16.536Z\'" + "))");
        auditAppsProxy.getAuditAppEntries(auditApp.getId(), createParams(paging, otherParams), HttpServletResponse.SC_OK);

        // Negative tests
        otherParams = new HashMap<>();
        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between ("
                + "\'2017-06-04T10:05:16.536+01:00\'" + ", " + "\'2016-06-02T12:13:51.593+01:00\'" + "))");
        auditAppsProxy.getAuditAppEntries(auditApp.getId(), createParams(paging, otherParams), HttpServletResponse.SC_BAD_REQUEST);
    }

    private void testAuditEntriesWhereId(AuditApps auditAppsProxy, AuditApp auditApp) throws Exception 
    {
        // paging
        Paging paging = getPaging(0, 10);

        // where params
        Map<String, String> otherParams = new HashMap<>();
        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.ID + " between (" + "\'1\' , \'10\'" + "))");

        ListResponse<AuditEntry> auditEntries = auditAppsProxy.getAuditAppEntries(auditApp.getId(),
                createParams(paging, otherParams), HttpServletResponse.SC_OK);
        assertNotNull(auditEntries);

        // Negative tests
        otherParams = new HashMap<>();
        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between between (" + "\'10\' , \'1\'" + "))");
        auditAppsProxy.getAuditAppEntries(auditApp.getId(), createParams(paging, otherParams), HttpServletResponse.SC_BAD_REQUEST);
    }

    private void testAuditEntriesWithInclude(AuditApps auditAppsProxy, AuditApp auditApp) throws Exception 
    {
        Paging paging = getPaging(0, 10);
        Map<String, String> otherParams = new HashMap<>();
        ListResponse<AuditEntry> auditEntriesWithoutValues = auditAppsProxy.getAuditAppEntries(auditApp.getId(), createParams(paging, otherParams),
                HttpServletResponse.SC_OK);
        assertNotNull(auditEntriesWithoutValues);

        for (AuditEntry ae : auditEntriesWithoutValues.getList())
        {
            validateAuditEntryFields(ae, auditApp);
            assertNull(ae.getValues());
        }

        otherParams.put("include", org.alfresco.rest.api.Audit.PARAM_INCLUDE_VALUES);
        // list auditEntries with values
        ListResponse<AuditEntry> auditEntriesWithValues = auditAppsProxy.getAuditAppEntries(auditApp.getId(), createParams(paging, otherParams),
                HttpServletResponse.SC_OK);
        assertNotNull(auditEntriesWithValues);

        for (AuditEntry ae : auditEntriesWithValues.getList())
        {
            validateAuditEntryFields(ae, auditApp);
            assertNotNull(ae.getValues());
            assertTrue("audit values not empty", ae.getValues().keySet().size() > 0);
        }

    }

    private void testAuditEntriesSkipCount(AuditApps auditAppsProxy, AuditApp auditApp) throws Exception 
    {
        int skipCount = 0;
        int maxItems = 4;
        Paging paging = getPaging(skipCount, maxItems);

        Map<String, String> otherParams = new HashMap<>();
        ListResponse<AuditEntry> resp = auditAppsProxy.getAuditAppEntries(auditApp.getId(),
                createParams(paging, otherParams), HttpServletResponse.SC_OK);

        // Paging and list groups with skip count.
        skipCount = 2;
        maxItems = 2;
        paging = getPaging(skipCount, maxItems);

        ListResponse<AuditEntry> sublistResponse = auditAppsProxy.getAuditAppEntries(auditApp.getId(),
                createParams(paging, otherParams), HttpServletResponse.SC_OK);
        List<AuditEntry> expectedSublist = sublist(resp.getList(), skipCount, maxItems);
        checkList(expectedSublist, sublistResponse.getPaging(), sublistResponse);
    }

    private void testRetrieveAuditEntry(AuditApps auditAppsProxy, AuditApp auditApp) throws Exception
    {
        int skipCount = 0;
        int maxItems = 4;
        Paging paging = getPaging(skipCount, maxItems);

        Map<String, String> otherParams = new HashMap<>();
        ListResponse<AuditEntry> resp = auditAppsProxy.getAuditAppEntries(auditApp.getId(),
                createParams(paging, otherParams), HttpServletResponse.SC_OK);
        String id = resp.getList().get(0).getId().toString();

        //Positive tests
        //200
        AuditEntry entryResp = auditAppsProxy.getAuditEntry(auditApp.getId(), id, null, HttpServletResponse.SC_OK);
        validateAuditEntryFields(entryResp, auditApp);
        assertNotNull(entryResp.getValues());

        // Negative tests
        // 400
        auditAppsProxy.getAuditEntry(auditApp.getId(), id+"invalidIdText", null, HttpServletResponse.SC_BAD_REQUEST);
        // 401
        setRequestContext(networkOne.getId(), networkAdmin, "wrongPassword");
        auditAppsProxy.getAuditEntry(auditApp.getId(), id, null, HttpServletResponse.SC_UNAUTHORIZED);
        // 403
        setRequestContext(networkOne.getId(), user1, null);
        auditAppsProxy.getAuditEntry(auditApp.getId(), id, null, HttpServletResponse.SC_FORBIDDEN);
        // 404
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        auditAppsProxy.getAuditEntry(auditApp.getId(), "" + Math.abs(new Random().nextLong()), null, HttpServletResponse.SC_NOT_FOUND);
        // 501
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        AuthenticationUtil.setFullyAuthenticatedUser(networkAdmin);
        disableSystemAudit();
        auditAppsProxy.getAuditEntry(auditApp.getId(), id, null, HttpServletResponse.SC_NOT_IMPLEMENTED);
        enableSystemAudit();
    }

    private void testDeleteAuditEntry(AuditApps auditAppsProxy, AuditApp auditApp) throws Exception
    {
        int skipCount = 0;
        int maxItems = 4;
        Paging paging = getPaging(skipCount, maxItems);

        Map<String, String> otherParams = new HashMap<>();
        ListResponse<AuditEntry> resp = auditAppsProxy.getAuditAppEntries(auditApp.getId(),
                createParams(paging, otherParams), HttpServletResponse.SC_OK);
        String id = resp.getList().get(0).getId().toString();

        // Negative tests
        // 400
        auditAppsProxy.getAuditEntry(auditApp.getId(), id+"invalidIdText", null, HttpServletResponse.SC_BAD_REQUEST);
        // 401
        setRequestContext(networkOne.getId(), networkAdmin, "wrongPassword");
        auditAppsProxy.deleteAuditEntry(auditApp.getId(), id, null, HttpServletResponse.SC_UNAUTHORIZED);
        // 403
        setRequestContext(networkOne.getId(), user1, null);
        auditAppsProxy.deleteAuditEntry(auditApp.getId(), id, null, HttpServletResponse.SC_FORBIDDEN);
        // 404
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        auditAppsProxy.deleteAuditEntry(auditApp.getId(), "" + Math.abs(new Random().nextLong()), null, HttpServletResponse.SC_NOT_FOUND);
        // 501
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        AuthenticationUtil.setFullyAuthenticatedUser(networkAdmin);
        disableSystemAudit();
        auditAppsProxy.deleteAuditEntry(auditApp.getId(), id, null, HttpServletResponse.SC_NOT_IMPLEMENTED);
        enableSystemAudit();

        //Positive tests
        //204
        auditAppsProxy.deleteAuditEntry(auditApp.getId(), id, null, HttpServletResponse.SC_NO_CONTENT);
    }

    private void testDeleteAuditEntries(AuditApps auditAppsProxy, AuditApp auditApp) throws Exception
    {
        int skipCount = 0;
        int maxItems = 4;
        Paging paging = getPaging(skipCount, maxItems);

        Map<String, String> otherParams = new HashMap<>();
        ListResponse<AuditEntry> resp = auditAppsProxy.getAuditAppEntries(auditApp.getId(), createParams(paging, otherParams),
                HttpServletResponse.SC_OK);
        String dateBefore = ISO8601DateFormat.format(resp.getList().get(0).getCreatedAt());
        String dateAfter = ISO8601DateFormat.format(resp.getList().get(2).getCreatedAt());
        Long secondDeleteEntryId = resp.getList().get(1).getId();
        Long lastDeleteEntryId = resp.getList().get(2).getId();

        // Negative tests
        // 400 - switched dates and consecutive switched IDs
        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between (" + "\'" + dateAfter + "\'" + ", " + "\'"
                + dateBefore + "\'" + "))");
        auditAppsProxy.deleteAuditEntries(auditApp.getId(), otherParams, HttpServletResponse.SC_BAD_REQUEST);

        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.ID + " between (" + "\'" + lastDeleteEntryId + "\'" + ", " + "\'"
                + secondDeleteEntryId + "\'" + "))");
        auditAppsProxy.deleteAuditEntries(auditApp.getId(), otherParams, HttpServletResponse.SC_BAD_REQUEST);
        // 401
        // put correct dates back
        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between (" + "\'" + dateBefore + "\'" + ", " + "\'"
                + dateAfter + "\'" + "))");
        setRequestContext(networkOne.getId(), networkAdmin, "wrongPassword");
        auditAppsProxy.deleteAuditEntries(auditApp.getId(), otherParams, HttpServletResponse.SC_UNAUTHORIZED);
        // 403
        setRequestContext(networkOne.getId(), user1, null);
        auditAppsProxy.deleteAuditEntries(auditApp.getId(), otherParams, HttpServletResponse.SC_FORBIDDEN);
        // 404
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        auditAppsProxy.deleteAuditEntries("invalidAppId", otherParams, HttpServletResponse.SC_NOT_FOUND);
        // 501
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        AuthenticationUtil.setFullyAuthenticatedUser(networkAdmin);
        disableSystemAudit();
        auditAppsProxy.deleteAuditEntries(auditApp.getId(), otherParams, HttpServletResponse.SC_NOT_IMPLEMENTED);;
        enableSystemAudit();

        // Positive tests
        // 200
        auditAppsProxy.deleteAuditEntries(auditApp.getId(), otherParams, HttpServletResponse.SC_NO_CONTENT);

        // check that the entries were deleted.
        // firstEntry should have id > than lastDeleteEntryId
        resp = auditAppsProxy.getAuditAppEntries(auditApp.getId(), createParams(paging, new HashMap<>()),
                HttpServletResponse.SC_OK);
        assertTrue("Entries were not deleted", resp.getList().get(0).getId() > lastDeleteEntryId);
    }

    /**
     * Perform a login attempt (to be used to create audit entries)
     */
    private void login(final String username, final String password) throws Exception 
    {
        // Force a failed login
        RunAsWork<Void> failureWork = new RunAsWork<Void>() 
        {
            @Override
            public Void doWork() throws Exception 
            {
                try 
                {
                    authenticationService.authenticate(username, password.toCharArray());
                    fail("Failed to force authentication failure");
                } 
                catch (AuthenticationException e) 
                {
                    // Expected
                }
                return null;
            }
        };
        AuthenticationUtil.runAs(failureWork, AuthenticationUtil.getSystemUserName());
    }

    protected Map<String, String> createParams(Paging paging, Map<String, String> otherParams) 
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
        if (otherParams != null) 
        {
            params.putAll(otherParams);
        }
        return params;
    }

    private void addOrderBy(Map<String, String> otherParams, String sortColumn, Boolean asc) 
    {
        otherParams.put("orderBy",
                sortColumn + (asc != null ? " " + (asc ? SortColumn.ASCENDING : SortColumn.DESCENDING) : ""));
    }

    private String createFolderInPrivateSite(String siteTitle, String folderName) throws Exception
    {
        String siteId = createSite(siteTitle, SiteVisibility.PRIVATE).getId();
        String siteDocLibNodeId = getSiteContainerNodeId(siteId, "documentLibrary");
        return createFolder(siteDocLibNodeId, folderName).getId();
    }
    
    private void renameNode(String nodeId, String newName) throws Exception
    {
        Node nUpdate = new Node();
        nUpdate.setName(newName);
        put(URL_NODES, nodeId, toJsonAsStringNonNull(nUpdate), null, 200);
    }

    @Test
    public void testAuditEntriesByNodeId() throws Exception
    {
        final AuditApps auditAppsProxy = publicApiClient.auditApps();

        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        String adminNodeId = createFolderInPrivateSite("testSiteAdmin" + RUNID, "testFolderAdmin" + RUNID);

        // requires admin
        AuditApp alfAccessAuditApp = auditAppsProxy.getAuditApp(AUDIT_APP_ID);

        setRequestContext(user1);
        String nodeId = createFolderInPrivateSite("testSiteUser" + RUNID, "testFolderUser1" + RUNID);
        renameNode(nodeId, "testFolderUser1" + RUNID + "b");

        testGetAuditEntriesByNodeId(auditAppsProxy, nodeId, alfAccessAuditApp);
        testAuditEntriesSortingByNodeId(auditAppsProxy, nodeId);
        testAuditEntriesWhereDateByNodeId(auditAppsProxy, nodeId);
        testAuditEntriesWithIncludeByNodeId(auditAppsProxy, nodeId, alfAccessAuditApp);
        testAuditEntriesSkipCountByNodeId(auditAppsProxy, nodeId);

        testAuditEntriesWhereUserByNodeId(auditAppsProxy, adminNodeId, alfAccessAuditApp);
    }

    private void testAuditEntriesSkipCountByNodeId(AuditApps auditAppsProxy, String nodeId) throws Exception
    {
        setRequestContext(user1);

        int skipCount = 0;
        int maxItems = 4;
        final Paging paging = getPaging(skipCount, maxItems);
        Map<String, String> params;

        Map<String, String> otherParams = new HashMap<>();
        params = createParams(paging, otherParams);
        ListResponse<AuditEntry> resp = auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, params, HttpServletResponse.SC_OK);

        // Paging and list groups with skip count.
        skipCount = 2;
        maxItems = 2;
        final Paging pagingSkip = getPaging(skipCount, maxItems);
        params = createParams(pagingSkip, otherParams);
        ListResponse<AuditEntry> sublistResponse = auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, params, HttpServletResponse.SC_OK);

        List<AuditEntry> expectedSublist = sublist(resp.getList(), skipCount, maxItems);
        checkList(expectedSublist, sublistResponse.getPaging(), sublistResponse);
    }

    private void testAuditEntriesWithIncludeByNodeId(AuditApps auditAppsProxy, String nodeId, AuditApp alfAccessAuditApp) throws Exception
    {
        setRequestContext(user1);

        Paging paging = getPaging(0, 10);
        Map<String, String> otherParams = new HashMap<>();
        Map<String, String> params = createParams(paging, otherParams);

        ListResponse<AuditEntry> auditEntriesWithoutValues = auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, params, HttpServletResponse.SC_OK);
        assertNotNull(auditEntriesWithoutValues);
        
        for (AuditEntry ae : auditEntriesWithoutValues.getList())
        {
            validateAuditEntryFields(ae, alfAccessAuditApp);
            assertNull(ae.getValues());
        }

        otherParams.put("include", org.alfresco.rest.api.Audit.PARAM_INCLUDE_VALUES);
        params = createParams(paging, otherParams);
        // list auditEntries with values
        ListResponse<AuditEntry> auditEntriesWithValues = auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, params, HttpServletResponse.SC_OK);
        assertNotNull(auditEntriesWithValues);

        for (AuditEntry ae : auditEntriesWithValues.getList())
        {
            validateAuditEntryFields(ae, alfAccessAuditApp);
            assertNotNull(ae.getValues());
            assertTrue("audit values not empty", ae.getValues().keySet().size() > 0);
        }
    }

    private void testAuditEntriesWhereDateByNodeId(AuditApps auditAppsProxy, String nodeId) throws Exception
    {
        setRequestContext(user1);

        // paging
        Paging paging = getPaging(0, 10);
        final Map<String, String> otherParams = new HashMap<>();
        Map<String, String> params;

        //
        // note: sanity test parsing of a few ISO8601 formats (non-exhaustive) -
        // eg. +01:00, +0000, Z
        //

        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between (" + "\'2016-06-02T12:13:51.593+01:00\'" + ", "
                + "\'2017-06-04T10:05:16.536+01:00\'" + "))");
        params = createParams(paging, otherParams);
        auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, params, HttpServletResponse.SC_OK);

        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between (" + "\'2016-06-02T11:13:51.593+0000\'" + ", "
                + "\'2017-06-04T09:05:16.536+0000\'" + "))");
        params = createParams(paging, otherParams);
        auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, params, HttpServletResponse.SC_OK);

        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between (" + "\'2016-06-02T11:13:51.593Z\'" + ", "
                + "\'2017-06-04T09:05:16.536Z\'" + "))");
        params = createParams(paging, otherParams);
        auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, params, HttpServletResponse.SC_OK);

        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_AT + " between (" + "\'2017-06-04T10:05:16.536+01:00\'" + ", "
                + "\'2016-06-02T12:13:51.593+01:00\'" + "))");
        params = createParams(paging, otherParams);
        auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, params, HttpServletResponse.SC_BAD_REQUEST);
    }

    private void testAuditEntriesWhereUserByNodeId(AuditApps auditAppsProxy, String nodeId, AuditApp alfAccessAuditApp) throws Exception
    {
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

        Paging paging = getPaging(0, 10);
        final Map<String, String> otherParams = new HashMap<>();
        Map<String, String> params;
        int countEntries;

        // Get audit entries for node using 'CREATED_BY_USER' param with admin
        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_BY_USER + "=" + "'" + networkAdmin + "'" + ")");
        params = createParams(paging, otherParams);
        ListResponse<AuditEntry> auditEntries = auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, params, HttpServletResponse.SC_OK);
        countEntries = auditEntries.getList().size();

        // Get audit entries for node using 'admin' user and '-me-' alias
        otherParams.put("where", "(" + org.alfresco.rest.api.Audit.CREATED_BY_USER + "=" + "'" + "-me-" + "'" + ")");
        params = createParams(paging, otherParams);
        auditEntries = auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, params, HttpServletResponse.SC_OK);

        // Check the audit entries list from the second GET call
        assertTrue("The number of entries should be the same in both GET calls", countEntries == auditEntries.getList().size());
        assertNotNull(auditEntries);
        for (AuditEntry ae : auditEntries.getList())
        {
            validateAuditEntryFields(ae, alfAccessAuditApp);
        }
    }

    private void testGetAuditEntriesByNodeId(AuditApps auditAppsProxy, String nodeId, AuditApp alfAccessAuditApp) throws Exception
    {
        setRequestContext(user1);

        ListResponse<AuditEntry> auditEntries = auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, null, HttpServletResponse.SC_OK);
        
        for (AuditEntry ae : auditEntries.getList())
        {
            validateAuditEntryFields(ae, alfAccessAuditApp);
        }

        // Negative tests
        // 401
        setRequestContext(networkOne.getId(), networkAdmin, "wrongPassword");
        auditAppsProxy.getAuditAppEntries(nodeId, null, HttpServletResponse.SC_UNAUTHORIZED);
        // 403
        setRequestContext(networkOne.getId(), user1, null);
        auditAppsProxy.getAuditAppEntries(nodeId, null, HttpServletResponse.SC_FORBIDDEN);
        // 404
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        auditAppsProxy.getAuditAppEntries("randomId", null, HttpServletResponse.SC_NOT_FOUND);
        // 501
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        AuthenticationUtil.setFullyAuthenticatedUser(networkAdmin);
        disableSystemAudit();
        auditAppsProxy.getAuditAppEntries("randomId", null, HttpServletResponse.SC_NOT_IMPLEMENTED);
        enableSystemAudit();
    }

    private void testAuditEntriesSortingByNodeId(AuditApps auditAppsProxy, String nodeId) throws Exception
    {
        setRequestContext(user1);

        // paging
        Paging paging = getPaging(0, 10);
        Map<String, String> otherParams = new HashMap<>();

        // Default order.
        addOrderBy(otherParams, org.alfresco.rest.api.Audit.CREATED_AT, null);
        Map<String, String> params = createParams(paging, otherParams);

        ListResponse<AuditEntry> auditEntries = auditAppsProxy.getAuditAppEntriesByNodeRefId(nodeId, params, HttpServletResponse.SC_OK);

        assertNotNull(auditEntries);
        assertTrue("audit entry size less than 2", auditEntries.getList().size() > 1);
        assertTrue("auditEntries order not valid", auditEntries.getList().get(0).getCreatedAt().before(auditEntries.getList().get(1).getCreatedAt()));
    }
}
