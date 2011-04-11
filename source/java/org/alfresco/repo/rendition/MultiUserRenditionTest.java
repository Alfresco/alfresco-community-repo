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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * This test class tests the use of the {@link RenditionService} by multiple users.
 * 
 * @author Neil McErlean
 * @since 3.3.4
 */
public class MultiUserRenditionTest
{
    private static ApplicationContext appContext;

    private static String ADMIN_USER;
    private static final String NON_ADMIN_USER = "nonAdmin";
    
    private static MutableAuthenticationService authenticationService;
    private static ContentService contentService;
    private static NodeService nodeService;
    private static PermissionService permissionService;
    private static PersonService personService;
    private static RenditionService renditionService;
    private static Repository repositoryHelper;
    private static RetryingTransactionHelper txnHelper;
    private static TransactionService transactionService;
    
    private List<NodeRef> nodesToBeTidiedUp = new ArrayList<NodeRef>();
    private NodeRef testFolder;

    @BeforeClass
    public static void initContextAndCreateUser()
    {
        appContext = ApplicationContextHelper.getApplicationContext();

        authenticationService = (MutableAuthenticationService) appContext.getBean("AuthenticationService");
        contentService = (ContentService) appContext.getBean("ContentService");
        nodeService = (NodeService) appContext.getBean("NodeService");
        permissionService = (PermissionService) appContext.getBean("PermissionService");
        personService = (PersonService) appContext.getBean("PersonService");
        renditionService = (RenditionService) appContext.getBean("RenditionService");
        repositoryHelper = (Repository) appContext.getBean("repositoryHelper");
        transactionService = (TransactionService) appContext.getBean("TransactionService");
        txnHelper = transactionService.getRetryingTransactionHelper();

        ADMIN_USER = AuthenticationUtil.getAdminUserName();
        
        // Create a nonAdminUser
        createUser(NON_ADMIN_USER);

    }

    public static void createUser(String userName)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);

        if (!authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, userName.toCharArray());
        }
        
        if (!personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap();
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }
    }

    @Before public void createTestFolder()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        final NodeRef companyHome = repositoryHelper.getCompanyHome();
        
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, this.getClass() + "_testFolder");
        testFolder = nodeService.createNode(companyHome, 
                                                ContentModel.ASSOC_CONTAINS, 
                                                ContentModel.ASSOC_CONTAINS, 
                                                ContentModel.TYPE_FOLDER,
                                                props).getChildRef();
        // Let anyone (meaning non-admin) do anything (meaning create new content)
        permissionService.setPermission(testFolder, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
        this.nodesToBeTidiedUp.add(testFolder);
    }

    /**
     * This test method ensures that users who cause renditions (thumbnails) to
     * be created on nodes are not made the modifier of the source node.
     */
    @Test
    public void renditioningShouldNotChangeModifierOnSourceNode_ALF3991()
    {
        // Create a doc as admin
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        
        final NodeRef adminPdfNode = txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        return createPdfDocumentAsCurrentlyAuthenticatedUser(ADMIN_USER + "_content");
                    }
                });
        this.nodesToBeTidiedUp.add(adminPdfNode);

        
        // Create another doc as non-admin
        AuthenticationUtil.setFullyAuthenticatedUser(NON_ADMIN_USER);
        
        final NodeRef nonAdminPdfNode = txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        return createPdfDocumentAsCurrentlyAuthenticatedUser(NON_ADMIN_USER + "_content");
                    }
                });
        this.nodesToBeTidiedUp.add(nonAdminPdfNode);
        
        // Now have each user create a rendition (thumbnail) of the other user's content node.
        final QName doclibRendDefQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "doclib");
        
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        renditionService.render(nonAdminPdfNode, doclibRendDefQName);
                        return null;
                    }
                });

        AuthenticationUtil.setFullyAuthenticatedUser(NON_ADMIN_USER);
        txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        renditionService.render(adminPdfNode, doclibRendDefQName);
                        return null;
                    }
                });
        
        // And now check that the two source nodes still have the correct modifier property.
        // This will ensure that the auditable properties behaviour has been correctly filtered.
        txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        assertEquals("Incorrect modifier property", ADMIN_USER, nodeService.getProperty(adminPdfNode, ContentModel.PROP_MODIFIER));
                        assertEquals("Incorrect modifier property", NON_ADMIN_USER, nodeService.getProperty(nonAdminPdfNode, ContentModel.PROP_MODIFIER));
                        return null;
                    }
                });
    }
    
    @After public void tidyUpUnwantedNodeRefs()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        
        txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        for (NodeRef node : nodesToBeTidiedUp)
                        {
                            if (nodeService.exists(node))
                                nodeService.deleteNode(node);
                        }
                        return null;
                    }
                });  
        nodesToBeTidiedUp.clear();
    }
    
    private NodeRef createPdfDocumentAsCurrentlyAuthenticatedUser(final String nodeName)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, nodeName);
        NodeRef result = nodeService.createNode(testFolder, 
                                                ContentModel.ASSOC_CONTAINS, 
                                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeName), 
                                                ContentModel.TYPE_CONTENT,
                                                props).getChildRef();
        
        File file = loadQuickPdfFile();

        // Set the content
        ContentWriter writer = contentService.getWriter(result, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
        writer.setEncoding("UTF-8");
        
        writer.putContent(file);
        
        return result;
    }

    private File loadQuickPdfFile()
    {
        URL url = AbstractContentTransformerTest.class.getClassLoader().getResource("quick/quick.pdf");
        if (url == null)
        {
            fail("Could not load pdf file");
        }
        File file = new File(url.getFile());
        if (!file.exists())
        {
            fail("Could not load pdf file");
        }
        return file;
    }          
}