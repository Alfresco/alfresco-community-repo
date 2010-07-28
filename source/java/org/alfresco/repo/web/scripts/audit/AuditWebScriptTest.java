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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.audit.AuditService;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
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

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ctx = getServer().getApplicationContext();
        auditService = (AuditService) ctx.getBean("AuditService");
        admin = AuthenticationUtil.getAdminUserName();
        
        AuthenticationUtil.setFullyAuthenticatedUser(admin);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testGetWithoutPermissions() throws Exception
    {
        
    }
    
    public void testGetIsAuditEnabledGlobally() throws Exception
    {
        boolean checkEnabled = auditService.isAuditEnabled();

        String url = "/api/audit/control";
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        
        //First, we'll try the request as a simple, non-admin user (expect a 401)
        Response response = sendRequest(req, 200, admin);
        JSONObject json = new JSONObject(response.getContentAsString());
        boolean enabled = json.getBoolean("enabled");
        assertEquals("Mismatched global audit enabled", checkEnabled, enabled);
    }
}
