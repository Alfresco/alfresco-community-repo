/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.web.scripts.audit;

import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditApplication;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Test the audit web scripts
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class AuditWebScriptTest extends BaseWebScriptTest
{
    private ApplicationContext ctx;
    private AuditService auditService;
    private String admin;
    private boolean globallyEnabled;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ctx = getServer().getApplicationContext();
        auditService = (AuditService) ctx.getBean("AuditService");
        admin = AuthenticationUtil.getAdminUserName();
        
        AuthenticationUtil.setFullyAuthenticatedUser(admin);
        
        globallyEnabled = auditService.isAuditEnabled();
        // Only enable if required
        if (!globallyEnabled)
        {
            auditService.setAuditEnabled(true);
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        // Leave audit in correct state
        try
        {
            if (!globallyEnabled)
            {
                auditService.setAuditEnabled(false);
            }
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Failed to set audit back to globally enabled/disabled state", e);
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

        // We need to set this back after the test
        try
        {
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
        finally
        {
            auditService.setAuditEnabled(wasEnabled);
        }
    }
    
    private static final String APP_REPO_NAME = "AlfrescoRepository";
    private static final String APP_REPO_PATH = "/repository";
    public void testGetIsAuditEnabledRepo() throws Exception
    {
        boolean wasEnabled = auditService.isAuditEnabled(APP_REPO_NAME, null);

        String url = "/api/audit/control/" + APP_REPO_NAME + APP_REPO_PATH;
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
            assertEquals("Mismatched application audit name", APP_REPO_NAME, appName);
            assertEquals("Mismatched application audit path", APP_REPO_PATH, appPath);
        }
        else
        {
            
        }
    }
    
    public void testSetAuditEnabledRepo() throws Exception
    {
        boolean wasEnabled = auditService.isAuditEnabled(APP_REPO_NAME, APP_REPO_PATH);

        // We need to set this back after the test
        try
        {
            if (wasEnabled)
            {
                String url = "/api/audit/control/" + APP_REPO_NAME + APP_REPO_PATH + "?enable=false";
                TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
                sendRequest(req, Status.STATUS_OK, admin);
            }
            else
            {
                String url = "/api/audit/control/" + APP_REPO_NAME + APP_REPO_PATH + "?enable=true";
                TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
                sendRequest(req, Status.STATUS_OK, admin);
            }
            
            // Check that it worked
            testGetIsAuditEnabledRepo();
        }
        finally
        {
            if (wasEnabled)
            {
                auditService.enableAudit(APP_REPO_NAME, APP_REPO_PATH);
            }
            else
            {
                auditService.disableAudit(APP_REPO_NAME, APP_REPO_PATH);
            }
        }
    }
}
