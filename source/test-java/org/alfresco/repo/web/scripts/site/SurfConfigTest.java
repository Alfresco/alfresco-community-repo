/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.web.scripts.site;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class SurfConfigTest extends AbstractSiteServiceTest
{
    private SiteService siteService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private FileFolderService fileFolderService;
    private static final long RD = System.currentTimeMillis();
    private static final String USER_ONE = "SiteUserOne" + RD;
    private static final String USER_TWO = "SiteUserTwo" + RD;
    private static final String USER_THREE = "SiteUserThree" + RD;
    private static final String URL_SITES = "/api/sites";
    private static final String URL_MEMBERSHIPS = "/memberships";
    private static final String URL_ADM = "/remoteadm/";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        this.siteService = (SiteService) getServer().getApplicationContext().getBean("SiteService");
        this.nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        this.permissionService = (PermissionService) getServer().getApplicationContext().getBean("PermissionService");
        this.fileFolderService = (FileFolderService) getServer().getApplicationContext().getBean("FileFolderService");

        createUser(USER_ONE);
        createUser(USER_TWO);
        createUser(USER_THREE);

        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        deleteUser(USER_ONE);
        deleteUser(USER_TWO);
        deleteUser(USER_THREE);

        //Delete the sites
        deleteSites();

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    //MNT-16371
    public void testSurfConfigPermissions() throws Exception
    {
        // Create a site as USER_ONE
        String shortName = UUID.randomUUID().toString();
        JSONObject result = createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        assertEquals("myPreset", result.get("sitePreset"));
        assertEquals(shortName, result.get("shortName"));
        assertEquals("myTitle", result.get("title"));
        assertEquals("myDescription", result.get("description"));
        assertEquals(SiteVisibility.PUBLIC.toString(), result.get("visibility"));

        // Make ADMRemoteStore to create the surf-config folder and the dashboard.xml file.
        sendRequest(new PostRequest(URL_ADM + "CREATE/alfresco/site-data/pages/site/" + shortName + "/dashboard.xml?s=sitestore",
                    new JSONObject().toString(), "application/json"), 200);

        // {siteName}/cm:surf-config/
        NodeRef surfConfigFolderRef = nodeService
                    .getChildByName(siteService.getSite(shortName).getNodeRef(), ContentModel.ASSOC_CONTAINS, "surf-config");
        assertEquals("surf-config", nodeService.getProperty(surfConfigFolderRef, ContentModel.PROP_NAME));

        String owner = (String) nodeService.getProperty(surfConfigFolderRef, ContentModel.PROP_OWNER);
        assertFalse(USER_ONE.equalsIgnoreCase(owner));
        assertEquals(AuthenticationUtil.getAdminUserName(), owner);

        assertFalse("Inherit Permissions should be off.", permissionService.getInheritParentPermissions(surfConfigFolderRef));

        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(surfConfigFolderRef);
        assertEquals(1, permissions.size());
        String siteManagerGroup = siteService.getSiteRoleGroup(shortName, SiteModel.SITE_MANAGER);
        AccessPermission accessPermission = permissions.iterator().next();
        assertEquals(siteManagerGroup, accessPermission.getAuthority());
        assertEquals(SiteModel.SITE_MANAGER, accessPermission.getPermission());
        assertTrue(accessPermission.getAccessStatus() == AccessStatus.ALLOWED);

        // This is the method that finally gets called when ALF-21643 steps are followed.
        PagingResults<FileInfo> pageResults = fileFolderService
                    .list(surfConfigFolderRef, true, true, null, null, null, new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));
        List<FileInfo> fileInfos = pageResults.getPage();
        assertNotNull(fileInfos);
        assertEquals(1, fileInfos.size());
        // {siteName}/cm:surf-config/pages
        assertEquals("pages", fileInfos.get(0).getName());

        // Add USER_TWO as a site collaborator
        JSONObject membership = new JSONObject();
        membership.put("role", SiteModel.SITE_COLLABORATOR);
        JSONObject person = new JSONObject();
        person.put("userName", USER_TWO);
        membership.put("person", person);
        // Post the membership
        Response response = sendRequest(new PostRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, membership.toString(), "application/json"),
                    200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(SiteModel.SITE_COLLABORATOR, result.get("role"));
        assertEquals(USER_TWO, result.getJSONObject("authority").get("userName"));

        // Add USER_THREE as a site manager
        membership.put("role", SiteModel.SITE_MANAGER);
        person.put("userName", USER_THREE);
        membership.put("person", person);
        // Post the membership
        response = sendRequest(new PostRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, membership.toString(), "application/json"), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(SiteModel.SITE_MANAGER, result.get("role"));
        assertEquals(USER_THREE, result.getJSONObject("authority").get("userName"));

        // USER_TWO is a site collaborator so he should not be able to access the surf-config folder
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        try
        {
            fileFolderService.list(surfConfigFolderRef, true, true, null, null, null, new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));
            fail("USER_TWO dose not have the appropriate permissions to perform this operation.");
        }
        catch (AccessDeniedException ex)
        {
            //expected
        }

        // USER_THREE is a site manager so he is able to access the surf-config folder
        AuthenticationUtil.setFullyAuthenticatedUser(USER_THREE);
        pageResults = fileFolderService
                    .list(surfConfigFolderRef, true, true, null, null, null, new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));
        fileInfos = pageResults.getPage();
        assertNotNull(fileInfos);
        assertEquals(1, fileInfos.size());
        // {siteName}/cm:surf-config/pages
        assertEquals("pages", fileInfos.get(0).getName());

        // Update USER_ONE role from SiteManager to SiteContributor.
        membership.put("role", SiteModel.SITE_CONTRIBUTOR);
        person.put("userName", USER_ONE);
        membership.put("person", person);
        response = sendRequest(new PutRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, membership.toString(), "application/json"), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(SiteModel.SITE_CONTRIBUTOR, result.get("role"));
        assertEquals(USER_ONE, result.getJSONObject("authority").get("userName"));

        // USER_ONE is no longer a site manager
        // USER_ONE tries to access "{siteName}/cm:surf-config" children
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        try
        {
            fileFolderService.list(surfConfigFolderRef, true, true, null, null, null, new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));
            fail("USER_ONE is not the owner and he is no longer a site manager, so does not have the appropriate permissions to perform this operation");
        }
        catch (AccessDeniedException ex)
        {
            //expected
        }

        // USER_ONE tries to access "{siteName}/cm:surf-config/pages" children
        try
        {
            fileFolderService.list(fileInfos.get(0).getNodeRef(), true, true, null, null, null,
                        new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));
            fail("USER_ONE is not the owner and he is no longer a site manager, so does not have the appropriate permissions to perform this operation");
        }
        catch (AccessDeniedException ex)
        {
            //expected
        }
    }
}
