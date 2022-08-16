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
package org.alfresco.repo.web.scripts.content;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.junit.Assert;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Test for ContentGet web script
 *
 * @author martin.muller
 */
public class ContentGetTest extends BaseWebScriptTest
{
    private MutableAuthenticationService authenticationService;
    private PersonService personService;
    private NodeService nodeService;
    private ContentService contentService;

    NodeRef rootFolder;

    private static final String USER_ONE = "ContentGetTestOne";
    private static final String URL_CONTENT_DOWNLOAD = "/api/node/content/workspace/SpacesStore/";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.authenticationService = (MutableAuthenticationService) getServer().getApplicationContext()
                .getBean("AuthenticationService");
        this.personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");
        this.nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        this.contentService = (ContentService) getServer().getApplicationContext().getBean("ContentService");
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
        if (this.authenticationService.authenticationExists(username))
        {
            this.authenticationService.deleteAuthentication(username);
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        nodeService.deleteNode(rootFolder);
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        // Clear the user
        deleteUser(USER_ONE);
    }

    /**
     * MNT-16380
     */
    public void testRelativePath() throws Exception
    {
        Repository repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
        NodeRef companyHome = repositoryHelper.getCompanyHome();

        rootFolder = createNode(companyHome, "rootFolder", ContentModel.TYPE_FOLDER);

        NodeRef doc1 = createNodeWithTextContent(rootFolder, "doc1", ContentModel.TYPE_CONTENT, "doc1 file content");

        NodeRef folderX = createNode(rootFolder, "X", ContentModel.TYPE_FOLDER);
        NodeRef folderY = createNode(folderX, "Y", ContentModel.TYPE_FOLDER);
        NodeRef folderZ = createNode(folderY, "Z", ContentModel.TYPE_FOLDER);

        NodeRef doc2 = createNodeWithTextContent(folderZ, "doc2", ContentModel.TYPE_CONTENT, "doc2 file content");

        // uri with relative path at the end
        String uri = URL_CONTENT_DOWNLOAD + doc1.getId() + "/X/Y/Z/doc2";
        Response resp = sendRequest(new GetRequest(uri), 200);

        // check if we really have doc2 as target
        Assert.assertEquals("doc2 file content", resp.getContentAsString());
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
