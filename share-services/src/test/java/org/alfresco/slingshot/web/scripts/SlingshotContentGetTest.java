/*
 * #%L
 * Alfresco Share Services AMP
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
package org.alfresco.slingshot.web.scripts;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for SlingshotContentGet web script
 * @author alex.mukha
 * @since 5.0.0
 */
public class SlingshotContentGetTest extends BaseWebScriptTest
{
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private SiteService siteService;
    private NodeService nodeService;
    private ContentService contentService;
    private PermissionService permissionService;

    private static final String USER_ONE = "SlingshotContentGetTestOne";
    private static final String URL_SITES = "/api/sites";
    private static final String URL_CONTENT_DOWNLOAD = "/slingshot/node/content/workspace/SpacesStore/";
    private List<String> createdSites = new ArrayList<String>(1);

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.permissionService = (PermissionService)getServer().getApplicationContext().getBean("PermissionService");
        this.contentService = (ContentService)getServer().getApplicationContext().getBean("ContentService");
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        createUser(USER_ONE);
    }

    private void createUser(String userName)
    {
        if (!this.authenticationService.authenticationExists(userName))
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());

            PropertyMap ppOne = new PropertyMap(5);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            this.personService.createPerson(ppOne);
        }
    }
    private void deleteUser(String username)
    {
        this.personService.deletePerson(username);
        if(this.authenticationService.authenticationExists(username))
        {
            this.authenticationService.deleteAuthentication(username);
        }
    }


    private JSONObject createSite(String sitePreset, String shortName, String title, String description, SiteVisibility visibility, int expectedStatus)
            throws Exception
    {
        JSONObject site = new JSONObject();
        site.put("sitePreset", sitePreset);
        site.put("shortName", shortName);
        site.put("title", title);
        site.put("description", description);
        site.put("visibility", visibility.toString());
        TestWebScriptServer.Response response = sendRequest(new TestWebScriptServer.PostRequest(URL_SITES, site.toString(), "application/json"), expectedStatus);
        this.createdSites.add(shortName);
        return new JSONObject(response.getContentAsString());
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        // Clear the user
        deleteUser(USER_ONE);
        // Tidy-up any site's create during the execution of the test
        for (String shortName : this.createdSites)
        {
            sendRequest(new TestWebScriptServer.DeleteRequest(URL_SITES + "/" + shortName), 0);
        }
        // Clear the list
        this.createdSites.clear();
        this.authenticationComponent.clearCurrentSecurityContext();
    }

    public void testDownloadBySiteMemberFromPrivateSite() throws Exception
    {
        String shortName  = GUID.generate();
        // Create a new site
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PRIVATE, 200);

        // Ensure we have th document library
        NodeRef docLib = siteService.createContainer(shortName, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);

        NodeRef doc = nodeService.createNode(docLib, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT).getChildRef();
        nodeService.setProperty(doc, ContentModel.PROP_CONTENT, new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null));
        nodeService.setProperty(doc, ContentModel.PROP_TITLE, "title");
        ContentWriter writer = contentService.getWriter(doc, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("test");

        String uri = URL_CONTENT_DOWNLOAD + doc.getId() + "?a=true";
        sendRequest(new GetRequest(uri), 200);
    }

    public void testDownloadByNonSiteMemberFromPrivateSite() throws Exception
    {
        String shortName  = GUID.generate();
        // Create a new site
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PRIVATE, 200);

        NodeRef docLib = siteService.createContainer(shortName, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
        NodeRef doc = nodeService.createNode(docLib, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT).getChildRef();
        nodeService.setProperty(doc, ContentModel.PROP_CONTENT, new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null));
        nodeService.setProperty(doc, ContentModel.PROP_TITLE, "title");
        ContentWriter writer = contentService.getWriter(doc, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("test");

        permissionService.setPermission(doc, USER_ONE, PermissionService.CONSUMER, true);

        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);

        String uri = URL_CONTENT_DOWNLOAD + doc.getId() + "?a=true";
        sendRequest(new GetRequest(uri), 200);
    }

    /**
     * MNT-16380
     */
    public void testRelativePath() throws Exception
    {
        Repository repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
        NodeRef companyHome = repositoryHelper.getCompanyHome();

        NodeRef rootFolder = createNode(companyHome, "rootFolder", ContentModel.TYPE_FOLDER);

        NodeRef doc1 = createNodeWithTextContent(rootFolder, "doc1", ContentModel.TYPE_CONTENT, "doc1 file content");

        NodeRef folderX = createNode(rootFolder, "X", ContentModel.TYPE_FOLDER);
        NodeRef folderY = createNode(folderX, "Y", ContentModel.TYPE_FOLDER);
        NodeRef folderZ = createNode(folderY, "Z", ContentModel.TYPE_FOLDER);

        NodeRef doc2 = createNodeWithTextContent(folderZ, "doc2", ContentModel.TYPE_CONTENT, "doc2 file content");

        // uri with relative path at the end
        String uri = URL_CONTENT_DOWNLOAD + doc1.getId() + "/X/Y/Z/doc2";
        TestWebScriptServer.Response resp = sendRequest(new GetRequest(uri), 200);

        // check if we really have doc2 as target
        Assert.assertEquals("doc2 file content", resp.getContentAsString());

        nodeService.deleteNode(rootFolder);
    }

    public NodeRef createNodeWithTextContent(NodeRef parentNode, String nodeCmName, QName nodeType, String content)
    {
        NodeRef nodeRef = createNode(parentNode, nodeCmName, nodeType);

        // If there is any content, add it.
        if (content != null)
        {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(content);
        }
        return nodeRef;

    }

    private NodeRef createNode(NodeRef parentNode, String nodeCmName, QName nodeType)
    {
        QName childName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, nodeCmName);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, nodeCmName);
        ChildAssociationRef childAssoc = nodeService
                .createNode(parentNode, ContentModel.ASSOC_CONTAINS, childName, nodeType, props);
        return childAssoc.getChildRef();
    }
}
