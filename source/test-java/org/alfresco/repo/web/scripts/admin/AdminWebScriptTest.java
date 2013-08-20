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
package org.alfresco.repo.web.scripts.admin;

import java.util.Date;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.alfresco.service.cmr.admin.RepoUsageStatus;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Test the admin web scripts
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class AdminWebScriptTest extends BaseWebScriptTest
{
    private ApplicationContext ctx;
    private RepoAdminService repoAdminService;
    private DescriptorService descriptorService;
    private String admin;
    private String guest;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ctx = getServer().getApplicationContext();
        repoAdminService = (RepoAdminService) ctx.getBean("RepoAdminService");
        descriptorService = (DescriptorService) ctx.getBean("DescriptorService");
        admin = AuthenticationUtil.getAdminUserName();
        guest = AuthenticationUtil.getGuestUserName();

        AuthenticationUtil.setFullyAuthenticatedUser(admin);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testGetRestrictions() throws Exception
    {
        RepoUsage restrictions = repoAdminService.getRestrictions();
        
        String url = "/api/admin/restrictions";
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        
        Response response = sendRequest(req, Status.STATUS_OK, guest);
        JSONObject json = new JSONObject(response.getContentAsString());
        Long maxUsers = json.isNull(AbstractAdminWebScript.JSON_KEY_USERS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_USERS);
        assertEquals("Mismatched max users", restrictions.getUsers(), maxUsers);
        Long maxDocuments = json.isNull(AbstractAdminWebScript.JSON_KEY_DOCUMENTS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_DOCUMENTS);
        assertEquals("Mismatched max documents", restrictions.getDocuments(), maxDocuments);
    }
    
    public void testGetUsage() throws Exception
    {
        RepoUsageStatus usageStatus = repoAdminService.getUsageStatus();
        RepoUsage usage = usageStatus.getUsage();
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        Date validUntil = (licenseDescriptor == null) ? null : licenseDescriptor.getValidUntil(); // might be null
        Integer checkLevel = new Integer(usageStatus.getLevel().ordinal());
        
        String url = "/api/admin/usage";
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        
        Response response = sendRequest(req, Status.STATUS_OK, guest);
        System.out.println(response.getContentAsString());
        JSONObject json = new JSONObject(response.getContentAsString());
        Long users = json.isNull(AbstractAdminWebScript.JSON_KEY_USERS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_USERS);
        assertEquals("Mismatched users", usage.getUsers(), users);
        Long documents = json.isNull(AbstractAdminWebScript.JSON_KEY_DOCUMENTS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_DOCUMENTS);
        assertEquals("Mismatched documents", usage.getDocuments(), documents);
        String licenseMode = json.isNull(AbstractAdminWebScript.JSON_KEY_LICENSE_MODE) ? null : json.getString(AbstractAdminWebScript.JSON_KEY_LICENSE_MODE);
        assertEquals("Mismatched licenseMode", usage.getLicenseMode().toString(), licenseMode);
        boolean readOnly = json.getBoolean(AbstractAdminWebScript.JSON_KEY_READ_ONLY);
        assertEquals("Mismatched readOnly", usage.isReadOnly(), readOnly);
        boolean updated = json.getBoolean(AbstractAdminWebScript.JSON_KEY_UPDATED);
        assertEquals("Mismatched updated", false, updated);
        Long licenseValidUntil = json.isNull(AbstractAdminWebScript.JSON_KEY_LICENSE_VALID_UNTIL) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_LICENSE_VALID_UNTIL);
        assertEquals("Mismatched licenseValidUntil",
                (validUntil == null) ? null : validUntil.getTime(),
                licenseValidUntil);
        Integer level = json.isNull(AbstractAdminWebScript.JSON_KEY_LEVEL) ? null : json.getInt(AbstractAdminWebScript.JSON_KEY_LEVEL);
        assertEquals("Mismatched level", checkLevel, level);
        json.getJSONArray(AbstractAdminWebScript.JSON_KEY_WARNINGS);
        json.getJSONArray(AbstractAdminWebScript.JSON_KEY_ERRORS);
    }
    
    public void testUpdateUsageWithoutPermissions() throws Exception
    {
        String url = "/api/admin/usage";
        TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
        sendRequest(req, 401, AuthenticationUtil.getGuestRoleName());
    }
    
    public void testUpdateUsage() throws Exception
    {
        repoAdminService.updateUsage(UsageType.USAGE_ALL);
        RepoUsage usage = repoAdminService.getUsage();
        
        String url = "/api/admin/usage";
        TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
        
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        System.out.println(response.getContentAsString());
        JSONObject json = new JSONObject(response.getContentAsString());
        Long users = json.isNull(AbstractAdminWebScript.JSON_KEY_USERS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_USERS);
        assertEquals("Mismatched users", usage.getUsers(), users);
        Long documents = json.isNull(AbstractAdminWebScript.JSON_KEY_DOCUMENTS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_DOCUMENTS);
        assertEquals("Mismatched documents", usage.getDocuments(), documents);
        String licenseMode = json.isNull(AbstractAdminWebScript.JSON_KEY_LICENSE_MODE) ? null : json.getString(AbstractAdminWebScript.JSON_KEY_LICENSE_MODE);
        assertEquals("Mismatched licenseMode", usage.getLicenseMode().toString(), licenseMode);
        boolean readOnly = json.getBoolean(AbstractAdminWebScript.JSON_KEY_READ_ONLY);
        assertEquals("Mismatched readOnly", usage.isReadOnly(), readOnly);
        boolean updated = json.getBoolean(AbstractAdminWebScript.JSON_KEY_UPDATED);
        assertEquals("Mismatched updated", true, updated);
    }
}
