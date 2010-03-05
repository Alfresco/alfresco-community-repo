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

package org.alfresco.repo.rendition;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.rendition.executer.ImageRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Neil McErlean
 * @since 3.3
 */
public class RenditionServicePermissionsTest extends BaseAlfrescoSpringTest
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RenditionServicePermissionsTest.class);

    private final static QName RESCALE_RENDER_DEFN_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
            ImageRenderingEngine.NAME + System.currentTimeMillis());

    private NodeRef nodeWithImageContent;
    private NodeRef testFolder;

    private PermissionService permissionService;
    private PersonService personService;
    private RenditionService renditionService;
    private Repository repositoryHelper;
    private RetryingTransactionHelper transactionHelper;
    private String testFolderName;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        this.permissionService = (PermissionService)this.applicationContext.getBean("PermissionService");
        this.personService = (PersonService)this.applicationContext.getBean("PersonService");
        this.renditionService = (RenditionService) this.applicationContext.getBean("renditionService");
        this.repositoryHelper = (Repository) this.applicationContext.getBean("repositoryHelper");
        this.transactionHelper = (RetryingTransactionHelper) this.applicationContext
                    .getBean("retryingTransactionHelper");
        
        NodeRef companyHome = this.repositoryHelper.getCompanyHome();

        // Create the test folder used for these tests
        this.testFolderName= "Test-folder-"+ System.currentTimeMillis();
        this.testFolder = nodeService.createNode(companyHome,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.APP_MODEL_1_0_URI,testFolderName),
                                ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(testFolder, ContentModel.PROP_NAME, testFolderName);
        // Create the node used as a content supplier for tests
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, "Test-image-node-" + System.currentTimeMillis());

        String testImageNodeName = "testImageNode" + System.currentTimeMillis();
        this.nodeWithImageContent = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.APP_MODEL_1_0_URI, testImageNodeName),
                    ContentModel.TYPE_CONTENT, props).getChildRef();
        // Stream some well-known image content into the node.
        URL url = RenditionServicePermissionsTest.class.getClassLoader().getResource("images/gray21.512.png");
        assertNotNull("url of test image was null", url);
        File imageFile = new File(url.getFile());
        assertTrue(imageFile.exists());

        nodeService.setProperty(nodeWithImageContent, ContentModel.PROP_CONTENT, new ContentData(null,
                    MimetypeMap.MIMETYPE_IMAGE_PNG, 0L, null));
        ContentWriter writer = contentService.getWriter(nodeWithImageContent, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_IMAGE_PNG);
        writer.setEncoding("UTF-8");
        writer.putContent(imageFile);
    }
    


    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        nodeService.deleteNode(nodeWithImageContent);
        nodeService.deleteNode(testFolder);
    }

    /**
     * This test method uses the RenditionService to render a test document and place the
     * rendition under a folder for which the user does not have write permissions.
     * This should be allowed as all renditions are performed as system.
     */
    @SuppressWarnings("deprecation")
    public void testRenditionAccessPermissions() throws Exception
    {
        this.setComplete();
        this.endTransaction();
        
        final String normalUser = "renditionUser";

        // As admin, create a user who has read-only access to the testFolder
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Set the current security context as admin
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                assertEquals("admin", authenticationService.getCurrentUserName());

                if (authenticationService.authenticationExists(normalUser) == false)
                {
                    authenticationService.createAuthentication(normalUser, "PWD".toCharArray());
                    
                    PropertyMap personProperties = new PropertyMap();
                    personProperties.put(ContentModel.PROP_USERNAME, normalUser);
                    personProperties.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, "title" + normalUser);
                    personProperties.put(ContentModel.PROP_FIRSTNAME, "firstName");
                    personProperties.put(ContentModel.PROP_LASTNAME, "lastName");
                    personProperties.put(ContentModel.PROP_EMAIL, "email@email.com");
                    personProperties.put(ContentModel.PROP_JOBTITLE, "jobTitle");
                    
                    personService.createPerson(personProperties);
                }
                
                // Restrict write access to the test folder
                permissionService.setPermission(testFolder, normalUser, PermissionService.CONSUMER, true);
                
                return null;
            }
        });

        // As the user, render a piece of content with the rendition going to testFolder
        final NodeRef rendition = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Set the current security context as a rendition user
                AuthenticationUtil.setFullyAuthenticatedUser(normalUser);
                assertEquals(normalUser, authenticationService.getCurrentUserName());
                
                assertFalse("Source node has unexpected renditioned aspect.", nodeService.hasAspect(nodeWithImageContent,
                            RenditionModel.ASPECT_RENDITIONED));

                String path = testFolderName+"/testRendition.png";
                // Create the rendering action.
                RenditionDefinition action = makeRescaleImageAction();
                action.setParameterValue(RenditionService.PARAM_DESTINATION_PATH_TEMPLATE, path);

                // Perform the action with an explicit destination folder
                logger.debug("Creating rendition of: " + nodeWithImageContent);
                ChildAssociationRef renditionAssoc = renditionService.render(nodeWithImageContent, action);
                logger.debug("Created rendition: " + renditionAssoc.getChildRef());
                
                NodeRef renditionNode = renditionAssoc.getChildRef();
                
                assertEquals("The parent node was not correct", nodeWithImageContent, renditionAssoc.getParentRef());
                logger.debug("rendition's primary parent: " + nodeService.getPrimaryParent(renditionNode));
                assertEquals("The parent node was not correct", testFolder, nodeService.getPrimaryParent(renditionNode).getParentRef());

                // Now the source content node should have the rn:renditioned aspect
                assertTrue("Source node is missing renditioned aspect.", nodeService.hasAspect(nodeWithImageContent,
                            RenditionModel.ASPECT_RENDITIONED));
                
                return renditionNode;
            }
        });

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // Set the current security context as admin
                        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

                        final Serializable renditionCreator = nodeService.getProperty(rendition, ContentModel.PROP_CREATOR);
                        assertEquals("Incorrect creator", normalUser, renditionCreator);

                        return null;
                    }
                });
        }

    /**
     * Creates a RenditionDefinition for the RescaleImageActionExecutor.
     * 
     * @return A new RenderingAction.
     */
    private RenditionDefinition makeRescaleImageAction()
    {
        RenditionDefinition result = renditionService.createRenditionDefinition(RESCALE_RENDER_DEFN_NAME,
                    ImageRenderingEngine.NAME);
        result.setParameterValue(ImageRenderingEngine.PARAM_RESIZE_WIDTH, 42);
        return result;
    }
}
