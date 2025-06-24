/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.audit;

import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.springframework.util.ResourceUtils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.audit.AuditServiceImpl;
import org.alfresco.repo.audit.UserAuditFilter;
import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditApplication;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.GUID;
import org.alfresco.util.testing.category.LuceneTests;

/**
 * Test the audit web scripts
 * 
 * @author Derek Hulley
 * @since 3.4
 */
@Category({OwnJVMTestsCategory.class, LuceneTests.class})
public class AuditWebScriptTest extends BaseWebScriptTest
{
    private static final String APP_REPOTEST_NAME = "AlfrescoRepositoryTest";
    private static final String APP_REPOTEST_PATH = "/repositorytest";
    private static final String APP_SEARCHTEST_NAME = "SearchAudit";
    private static final String APP_SEARCHTEST_PATH = "/searchaudit";

    private ApplicationContext ctx;
    private AuditService auditService;
    private SearchService searchService;
    private AuthenticationService authenticationService;
    private FileFolderService fileFolderService;
    private Repository repositoryHelper;

    private String admin;
    private boolean wasGloballyEnabled;
    boolean wasRepoEnabled;
    private boolean wasSearchEnabled;

    private NodeRef testRoot;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ctx = getServer().getApplicationContext();
        // MNT-10807 : Auditing does not take into account audit.filter.alfresco-access.transaction.user
        UserAuditFilter userAuditFilter = new UserAuditFilter();
        userAuditFilter.setUserFilterPattern("System;.*");
        userAuditFilter.afterPropertiesSet();
        AuditComponent auditComponent = (AuditComponent) ctx.getBean("auditComponent");
        auditComponent.setUserAuditFilter(userAuditFilter);
        AuditServiceImpl auditServiceImpl = (AuditServiceImpl) ctx.getBean("auditService");
        auditServiceImpl.setAuditComponent(auditComponent);
        authenticationService = (AuthenticationService) ctx.getBean("AuthenticationService");
        auditService = (AuditService) ctx.getBean("AuditService");
        searchService = (SearchService) ctx.getBean("SearchService");
        repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
        fileFolderService = (FileFolderService) getServer().getApplicationContext().getBean("FileFolderService");
        admin = AuthenticationUtil.getAdminUserName();

        // Register the test models
        AuditModelRegistryImpl auditModelRegistry = (AuditModelRegistryImpl) ctx.getBean("auditModel.modelRegistry");
        URL testModelUrl = ResourceUtils.getURL("classpath:alfresco/testaudit/alfresco-audit-test-repository.xml");
        URL testModelUrl1 = ResourceUtils.getURL("classpath:alfresco/testaudit/alfresco-audit-test-mnt-16748.xml");
        auditModelRegistry.registerModel(testModelUrl);
        auditModelRegistry.registerModel(testModelUrl1);
        auditModelRegistry.loadAuditModels();

        AuthenticationUtil.setFullyAuthenticatedUser(admin);

        wasGloballyEnabled = auditService.isAuditEnabled();
        wasRepoEnabled = auditService.isAuditEnabled(APP_REPOTEST_NAME, APP_REPOTEST_PATH);
        wasSearchEnabled = auditService.isAuditEnabled(APP_SEARCHTEST_NAME, APP_SEARCHTEST_PATH);
        // Only enable if required
        if (!wasGloballyEnabled)
        {
            auditService.setAuditEnabled(true);
            wasGloballyEnabled = auditService.isAuditEnabled();
            if (!wasGloballyEnabled)
            {
                fail("Failed to enable global audit for test");
            }
        }
        if (!wasRepoEnabled)
        {
            auditService.enableAudit(APP_REPOTEST_NAME, APP_REPOTEST_PATH);
            wasRepoEnabled = auditService.isAuditEnabled(APP_REPOTEST_NAME, APP_REPOTEST_PATH);
            if (!wasRepoEnabled)
            {
                fail("Failed to enable repo audit for test");
            }
        }
        if (!wasSearchEnabled)
        {
            auditService.enableAudit(APP_SEARCHTEST_NAME, APP_SEARCHTEST_PATH);
            wasSearchEnabled = auditService.isAuditEnabled(APP_SEARCHTEST_NAME, APP_SEARCHTEST_PATH);
            if (!wasSearchEnabled)
            {
                fail("Failed to enable search audit for test");
            }
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        // Leave audit in correct state
        try
        {
            if (!wasGloballyEnabled)
            {
                auditService.setAuditEnabled(false);
            }
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Failed to set audit back to globally enabled/disabled state", e);
        }
        try
        {
            if (wasRepoEnabled)
            {
                auditService.enableAudit(APP_REPOTEST_NAME, APP_REPOTEST_PATH);
            }
            else
            {
                auditService.disableAudit(APP_REPOTEST_NAME, APP_REPOTEST_PATH);
            }
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Failed to set repo audit back to enabled/disabled state", e);
        }
        try
        {
            if (wasSearchEnabled)
            {
                auditService.enableAudit(APP_SEARCHTEST_NAME, APP_SEARCHTEST_PATH);
            }
            else
            {
                auditService.disableAudit(APP_SEARCHTEST_NAME, APP_SEARCHTEST_PATH);
            }
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Failed to set search audit back to enabled/disabled state", e);
        }
    }

    public void testGetWithoutPermissions() throws Exception
    {
        String url = "/api/audit/control";
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        sendRequest(req, 401, AuthenticationUtil.getGuestRoleName());
    }

    public void testGetIsAuditEnabledGlobally() throws Exception
    {
        boolean wasEnabled = auditService.isAuditEnabled();
        Map<String, AuditApplication> checkApps = auditService.getAuditApplications();

        String url = "/api/audit/control";
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);

        Response response = sendRequest(req, Status.STATUS_OK, admin);
        JSONObject json = new JSONObject(response.getContentAsString());
        boolean enabled = json.getBoolean(AbstractAuditWebScript.JSON_KEY_ENABLED);
        assertEquals("Mismatched global audit enabled", wasEnabled, enabled);
        JSONArray apps = json.getJSONArray(AbstractAuditWebScript.JSON_KEY_APPLICATIONS);
        assertEquals("Incorrect number of applications reported", checkApps.size(), apps.length());
    }

    public void testGetIsAuditEnabledMissingApp() throws Exception
    {
        String url = "/api/audit/control/xxx";
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);

        sendRequest(req, 404, admin);
    }

    public void testSetAuditEnabledGlobally() throws Exception
    {
        boolean wasEnabled = auditService.isAuditEnabled();

        if (wasEnabled)
        {
            String url = "/api/audit/control?enable=false";
            TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
            sendRequest(req, Status.STATUS_OK, admin);
        }
        else
        {
            String url = "/api/audit/control?enable=true";
            TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
            sendRequest(req, Status.STATUS_OK, admin);
        }

        // Check that it worked
        testGetIsAuditEnabledGlobally();
    }

    public void testGetIsAuditEnabledRepo() throws Exception
    {
        boolean wasEnabled = auditService.isAuditEnabled(APP_REPOTEST_NAME, null);

        String url = "/api/audit/control/" + APP_REPOTEST_NAME + APP_REPOTEST_PATH;
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);

        if (wasEnabled)
        {
            Response response = sendRequest(req, Status.STATUS_OK, admin);
            JSONObject json = new JSONObject(response.getContentAsString());
            JSONArray apps = json.getJSONArray(AbstractAuditWebScript.JSON_KEY_APPLICATIONS);
            assertEquals("Incorrect number of applications reported", 1, apps.length());
            JSONObject app = apps.getJSONObject(0);
            String appName = app.getString(AbstractAuditWebScript.JSON_KEY_NAME);
            String appPath = app.getString(AbstractAuditWebScript.JSON_KEY_PATH);
            boolean appEnabled = app.getBoolean(AbstractAuditWebScript.JSON_KEY_ENABLED);
            assertEquals("Mismatched application audit enabled", wasEnabled, appEnabled);
            assertEquals("Mismatched application audit name", APP_REPOTEST_NAME, appName);
            assertEquals("Mismatched application audit path", APP_REPOTEST_PATH, appPath);
        }
    }

    public void testGetAuditSearchService() throws Exception
    {
        // Delete search audit entries (if any)
        String url = "/api/audit/clear/" + APP_SEARCHTEST_NAME;
        TestWebScriptServer.PostRequest postReq = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
        Response response = sendRequest(postReq, Status.STATUS_OK, admin);
        JSONObject json = new JSONObject(response.getContentAsString());
        assertTrue(json.getInt(AbstractAuditWebScript.JSON_KEY_CLEARED) >= 0);

        // create a file
        this.testRoot = this.repositoryHelper.getCompanyHome();
        String filename = "test_doc" + GUID.generate() + ".txt";
        NodeRef testFile = this.fileFolderService.create(this.testRoot, filename, ContentModel.TYPE_CONTENT).getNodeRef();

        // search the newly created file
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("=cm:name:" + filename);
        sp.addStore(testFile.getStoreRef());
        searchService.query(sp);

        // construct the get audit request
        url = "/api/audit/query/" + APP_SEARCHTEST_NAME + "/searchaudit/queryX/searchParametersX?verbose=true";
        TestWebScriptServer.GetRequest getReq = new TestWebScriptServer.GetRequest(url);

        response = sendRequest(getReq, Status.STATUS_OK, admin);
        json = new JSONObject(response.getContentAsString());

        JSONArray jsonEntries = json.getJSONArray(AbstractAuditWebScript.JSON_KEY_ENTRIES);
        assertEquals("Incorrect number of entries reported", 1, jsonEntries.length());

        JSONObject values = (JSONObject) ((JSONObject) jsonEntries.get(0)).get(AbstractAuditWebScript.JSON_KEY_ENTRY_VALUES);
        assertTrue("Audit entry was not found", values.toString(0).contains("query==cm:name:" + filename));

        // clear audit entries for the application
        auditService.clearAudit(APP_SEARCHTEST_NAME, null, null);
    }

    public void testSetAuditEnabledRepo() throws Exception
    {
        boolean wasEnabled = auditService.isAuditEnabled(APP_REPOTEST_NAME, APP_REPOTEST_PATH);

        if (wasEnabled)
        {
            String url = "/api/audit/control/" + APP_REPOTEST_NAME + APP_REPOTEST_PATH + "?enable=false";
            TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
            sendRequest(req, Status.STATUS_OK, admin);
        }
        else
        {
            String url = "/api/audit/control/" + APP_REPOTEST_NAME + APP_REPOTEST_PATH + "?enable=true";
            TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
            sendRequest(req, Status.STATUS_OK, admin);
        }

        // Check that it worked
        testGetIsAuditEnabledRepo();
    }

    /**
     * Perform a failed login attempt
     */
    private void loginWithFailure(final String username) throws Exception
    {
        // Force a failed login
        RunAsWork<Void> failureWork = new RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception
            {
                try
                {
                    authenticationService.authenticate(username, "crud".toCharArray());
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

    public synchronized void testClearAuditRepo() throws Exception
    {
        long now = System.currentTimeMillis() - 10L; // Accuracy can be a problem
        long future = Long.MAX_VALUE;

        loginWithFailure(getName());

        // Wait for the background thread to run
        try
        {
            this.wait(100);
        }
        catch (Throwable e)
        {}

        // Delete audit entries that could not have happened
        String url = "/api/audit/clear/" + APP_REPOTEST_NAME + "?fromTime=" + future;
        TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        JSONObject json = new JSONObject(response.getContentAsString());
        int cleared = json.getInt(AbstractAuditWebScript.JSON_KEY_CLEARED);
        assertEquals("Could not have cleared more than 0", 0, cleared);

        // ALF-3055 : auditing of failures is now asynchronous, so loop 60 times with a
        // 1 second sleep to ensure that the audit is processed
        for (int i = 0; i < 60; i++)
        {
            // Delete the entry (at least)
            url = "/api/audit/clear/" + APP_REPOTEST_NAME + "?fromTime=" + now + "&toTime=" + future;
            req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
            response = sendRequest(req, Status.STATUS_OK, admin);
            json = new JSONObject(response.getContentAsString());
            cleared = json.getInt(AbstractAuditWebScript.JSON_KEY_CLEARED);
            if (cleared > 0)
            {
                break;
            }

            Thread.sleep(1000);
        }
        assertTrue("Should have cleared at least 1 entry", cleared > 0);

        // Delete all entries
        url = "/api/audit/clear/" + APP_REPOTEST_NAME;
        ;
        req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
        response = sendRequest(req, Status.STATUS_OK, admin);
        json = new JSONObject(response.getContentAsString());
        cleared = json.getInt(AbstractAuditWebScript.JSON_KEY_CLEARED);
    }

    @SuppressWarnings("unused")
    public void testQueryAuditRepo() throws Exception
    {
        long now = System.currentTimeMillis();
        long future = Long.MAX_VALUE;

        auditService.setAuditEnabled(true);
        auditService.enableAudit(APP_REPOTEST_NAME, APP_REPOTEST_PATH);

        loginWithFailure(getName());

        // Query for audit entries that could not have happened
        String url = "/api/audit/query/" + APP_REPOTEST_NAME + "?fromTime=" + now + "&verbose=true";
        JSONArray jsonEntries = null;
        TestWebScriptServer.GetRequest req = null;
        Response response = null;
        JSONObject json = null;
        Long entryCount = null;
        // ALF-3055 : auditing of failures is now asynchronous, so loop 60 times with a
        // 1 second sleep to ensure that the audit is processed
        for (int i = 0; i < 60; i++)
        {
            req = new TestWebScriptServer.GetRequest(url);
            response = sendRequest(req, Status.STATUS_OK, admin);
            json = new JSONObject(response.getContentAsString());
            entryCount = json.getLong(AbstractAuditWebScript.JSON_KEY_ENTRY_COUNT);
            jsonEntries = json.getJSONArray(AbstractAuditWebScript.JSON_KEY_ENTRIES);

            if (jsonEntries.length() > 0)
            {
                break;
            }
            Thread.sleep(1000);
        }
        assertTrue("Expected at least one entry", jsonEntries.length() > 0);
        assertEquals("Entry count and physical count don't match", Long.valueOf(jsonEntries.length()), entryCount);
        JSONObject jsonEntry = jsonEntries.getJSONObject(0);
        Long entryId = jsonEntry.getLong(AbstractAuditWebScript.JSON_KEY_ENTRY_ID);
        assertNotNull("No entry ID", entryId);
        String entryTimeStr = jsonEntry.getString(AbstractAuditWebScript.JSON_KEY_ENTRY_TIME);
        assertNotNull("No entry time String", entryTimeStr);
        Date entryTime = ISO8601DateFormat.parse((String) entryTimeStr); // Check conversion
        JSONObject jsonValues = jsonEntry.getJSONObject(AbstractAuditWebScript.JSON_KEY_ENTRY_VALUES);
        String entryUsername = jsonValues.getString("/repositorytest/login/error/user");
        assertEquals("Didn't find the login-failure-user", getName(), entryUsername);

        // Query using well-known ID
        Long fromEntryId = entryId; // Search is inclusive on the 'from' side
        Long toEntryId = entryId.longValue() + 1L; // Search is exclusive on the 'to' side
        url = "/api/audit/query/" + APP_REPOTEST_NAME + "?fromId=" + fromEntryId + "&toId=" + toEntryId;
        req = new TestWebScriptServer.GetRequest(url);
        response = sendRequest(req, Status.STATUS_OK, admin);
        json = new JSONObject(response.getContentAsString());
        jsonEntries = json.getJSONArray(AbstractAuditWebScript.JSON_KEY_ENTRIES);
        assertEquals("Incorrect number of search results", 1, jsonEntries.length());

        // Query using a non-existent entry path
        url = "/api/audit/query/" + APP_REPOTEST_NAME + "/repositorytest/login/error/userXXX" + "?verbose=true";
        req = new TestWebScriptServer.GetRequest(url);
        response = sendRequest(req, Status.STATUS_OK, admin);
        json = new JSONObject(response.getContentAsString());
        jsonEntries = json.getJSONArray(AbstractAuditWebScript.JSON_KEY_ENTRIES);
        assertTrue("Should not have found anything", jsonEntries.length() == 0);

        // Query using a good entry path
        url = "/api/audit/query/" + APP_REPOTEST_NAME + "/repositorytest/login/error/user" + "?verbose=true";
        req = new TestWebScriptServer.GetRequest(url);
        response = sendRequest(req, Status.STATUS_OK, admin);
        json = new JSONObject(response.getContentAsString());
        jsonEntries = json.getJSONArray(AbstractAuditWebScript.JSON_KEY_ENTRIES);
        assertTrue("Should have found entries", jsonEntries.length() > 0);

        // Now login with failure using a GUID and ensure that we can find it
        String missingUser = Long.valueOf(System.currentTimeMillis()).toString();

        // Query for event that has not happened
        url = "/api/audit/query/" + APP_REPOTEST_NAME + "/repositorytest/login/error/user" + "?value=" + missingUser;
        req = new TestWebScriptServer.GetRequest(url);
        response = sendRequest(req, Status.STATUS_OK, admin);
        json = new JSONObject(response.getContentAsString());
        jsonEntries = json.getJSONArray(AbstractAuditWebScript.JSON_KEY_ENTRIES);
        assertEquals("Incorrect number of search results", 0, jsonEntries.length());

        loginWithFailure(missingUser);

        // Query for event that has happened once
        url = "/api/audit/query/" + APP_REPOTEST_NAME + "/repositorytest/login/error/user" + "?value=" + missingUser;
        // ALF-3055 : auditing of failures is now asynchronous, so loop 60 times with a
        // 1 second sleep to ensure that the audit is processed
        for (int i = 0; i < 60; i++)
        {
            req = new TestWebScriptServer.GetRequest(url);
            response = sendRequest(req, Status.STATUS_OK, admin);
            json = new JSONObject(response.getContentAsString());
            jsonEntries = json.getJSONArray(AbstractAuditWebScript.JSON_KEY_ENTRIES);
            if (jsonEntries.length() == 1)
            {
                break;
            }
            Thread.sleep(1000);
        }
        assertEquals("Incorrect number of search results", 1, jsonEntries.length());

        // Query for event, but casting the value to the incorrect type
        url = "/api/audit/query/" + APP_REPOTEST_NAME + "/repositorytest/login/error/user" + "?value=" + missingUser + "&valueType=java.lang.Long";
        req = new TestWebScriptServer.GetRequest(url);
        response = sendRequest(req, Status.STATUS_OK, admin);
        json = new JSONObject(response.getContentAsString());
        jsonEntries = json.getJSONArray(AbstractAuditWebScript.JSON_KEY_ENTRIES);
        assertEquals("Incorrect number of search results", 0, jsonEntries.length());

        // Test what happens when the target data needs encoding
        now = System.currentTimeMillis();

        String oddUser = "%$£\\\"\'";
        loginWithFailure(oddUser);

        // Query for the event limiting to one by count and descending (i.e. get last)
        url = "/api/audit/query/" + APP_REPOTEST_NAME + "?forward=false&limit=1&verbose=true&fromTime=" + now;
        // ALF-3055 : auditing of failures is now asynchronous, so loop 60 times with a
        // 1 second sleep to ensure that the audit is processed
        for (int i = 0; i < 60; i++)
        {
            req = new TestWebScriptServer.GetRequest(url);
            response = sendRequest(req, Status.STATUS_OK, admin);
            json = new JSONObject(response.getContentAsString());
            jsonEntries = json.getJSONArray(AbstractAuditWebScript.JSON_KEY_ENTRIES);
            if (jsonEntries.length() == 1)
            {
                break;
            }
            Thread.sleep(1000);
        }
        assertEquals("Incorrect number of search results", 1, jsonEntries.length());

        jsonEntry = jsonEntries.getJSONObject(0);
        entryId = jsonEntry.getLong(AbstractAuditWebScript.JSON_KEY_ENTRY_ID);
        assertNotNull("No entry ID", entryId);
        jsonValues = jsonEntry.getJSONObject(AbstractAuditWebScript.JSON_KEY_ENTRY_VALUES);
        entryUsername = jsonValues.getString("/repositorytest/login/error/user");
        assertEquals("Didn't find the login-failure-user", oddUser, entryUsername);
    }
}
