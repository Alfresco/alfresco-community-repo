/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.download;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Tests for the Download webscripts.
 *
 * @author Alex Miller
 */
public class DownloadRestApiTest extends BaseWebScriptTest
{
    // Test COnstants
    private static final long MAX_TIME = 5000;
    private static final long PAUSE_TIME = 1000;
    
    private static final String TEST_USERNAME = "downloadTestUser";
    
    // Urls
    public static final String URL_DOWNLOADS       = "/api/internal/downloads";
    public static final String URL_DOWNLOAD_STATUS = "/api/internal/downloads/{0}/{1}/{2}/status";
    

    // Various supporting services
    private AuthenticationComponent      authenticationComponent;
    private MutableAuthenticationService authenticationService;
    private ContentService               contentService;
    private NodeService                  nodeService;
    private PersonService                personService;
    
    // Test Content 
    private NodeRef rootFolder;
    private NodeRef rootFile;
    private NodeRef level1File;
    private NodeRef level1Folder;
    private NodeRef level2File;

    
    public void setUp()
    {
        // Resolve required services
        authenticationService = getServer().getApplicationContext().getBean("AuthenticationService", MutableAuthenticationService.class);
        authenticationComponent = getServer().getApplicationContext().getBean("authenticationComponent", AuthenticationComponent.class);
        contentService = getServer().getApplicationContext().getBean("ContentService", ContentService.class);
        nodeService = getServer().getApplicationContext().getBean("NodeService", NodeService.class);
        personService = getServer().getApplicationContext().getBean("PersonService", PersonService.class);
        
        // Authenticate as user
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // if user with given user name doesn't already exist then create user
        if (this.authenticationService.authenticationExists(TEST_USERNAME) == false)
        {
            // create user
            this.authenticationService.createAuthentication(TEST_USERNAME, "password".toCharArray());
            
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, TEST_USERNAME);
            personProps.put(ContentModel.PROP_FIRSTNAME, "FirstName123");
            personProps.put(ContentModel.PROP_LASTNAME, "LastName123");
            personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");
            
            // create person node for user
            this.personService.createPerson(personProps);
        }

        Repository repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
        NodeRef companyHome = repositoryHelper.getCompanyHome();
        
        
        // Create some static test content
       rootFolder = createNode(companyHome, "rootFolder", ContentModel.TYPE_FOLDER, AuthenticationUtil.getAdminUserName());
       rootFile = createNodeWithTextContent(companyHome, "rootFile", ContentModel.TYPE_CONTENT, AuthenticationUtil.getAdminUserName(), "Root file content");
       
       level1File = createNodeWithTextContent(rootFolder, "level1File", ContentModel.TYPE_CONTENT, AuthenticationUtil.getAdminUserName(), "Level 1 file content");
       level1Folder = createNode(rootFolder, "level1Folder", ContentModel.TYPE_FOLDER, AuthenticationUtil.getAdminUserName());
       
       level2File = createNodeWithTextContent(level1Folder, "level2File", ContentModel.TYPE_CONTENT, AuthenticationUtil.getAdminUserName(), "Level 2 file content");
    }
    
    public void tearDown()
    {
        nodeService.deleteNode(level2File);
        nodeService.deleteNode(level1Folder);
        nodeService.deleteNode(level1File);
        nodeService.deleteNode(rootFolder);
        nodeService.deleteNode(rootFile);
        
        personService.deletePerson(TEST_USERNAME);
        if (this.authenticationService.authenticationExists(TEST_USERNAME))
        {
           this.authenticationService.deleteAuthentication(TEST_USERNAME);
        }

    }

    
    public NodeRef createNodeWithTextContent(NodeRef parentNode, String nodeCmName, QName nodeType, String ownerUserName, String content) 
    {
        NodeRef nodeRef = createNode(parentNode, nodeCmName, nodeType, ownerUserName);
        
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


    private NodeRef createNode(NodeRef parentNode, String nodeCmName, QName nodeType, String ownerUserName)
    {
        QName childName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, nodeCmName);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, nodeCmName);
        ChildAssociationRef childAssoc = nodeService.createNode(parentNode,
                    ContentModel.ASSOC_CONTAINS,
                    childName,
                    nodeType,
                    props);
        return childAssoc.getChildRef();
    }

    @Test 
    public void testCreateAndGetDownload() throws UnsupportedEncodingException, IOException, JSONException 
    {
        // CReate the download
        String postData = "[{ \"nodeRef\": \"" +
        		              rootFile + 
        		              "\"}, { \"nodeRef\": \"" +
        		              rootFolder +
        		          "\"}]";
        Response response = sendRequest(new PostRequest(URL_DOWNLOADS, postData, "application/json"), 200);    

        // Parse the response
        JSONObject result = new JSONObject(response.getContentAsString());
        
        NodeRef downloadNodeRef = new NodeRef(result.getString("nodeRef"));
        
        // Get the status
        String statusUrl = MessageFormat.format(URL_DOWNLOAD_STATUS, downloadNodeRef.getStoreRef().getProtocol(), downloadNodeRef.getStoreRef().getIdentifier(), downloadNodeRef.getId());
        Response statusResponse = sendRequest(new GetRequest(statusUrl), 200);
    }
}
