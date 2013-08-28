/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.repo.usage;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.usage.UsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * User Usage unit test
 */
public class UserUsageTest extends TestCase
{
    private static Log logger = LogFactory.getLog(UserUsageTest.class);
    
    private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
    
    protected NodeService nodeService;
    protected FileFolderService fileFolderService;
    protected MutableAuthenticationService authenticationService;
    private MutableAuthenticationDao authenticationDAO;
    protected NodeRef rootNodeRef;
    protected NodeRef systemNodeRef;
    protected AuthenticationComponent authenticationComponent;
    private UserTransaction testTX;
    private TransactionService transactionService;
    private ContentService contentService;
    private PersonService personService;
    private ContentUsageImpl contentUsageImpl;
    private UsageService usageService;
    private OwnableService ownableService;
    private RepoAdminService repoAdminService;
    
    private static final String TEST_RUN = System.currentTimeMillis()+"";
    private static final String TEST_USER = "userUsageTestUser-"+TEST_RUN;
    private static final String TEST_USER_2 = "userUsageTestUser2-"+TEST_RUN;
    protected NodeRef personNodeRef;
    protected NodeRef personNodeRef2;
    
    private static final QName customType = QName.createQName("{my.new.model}sop"); // from exampleModel.xml
    
    protected void setUp() throws Exception
    {
        if (AlfrescoTransactionSupport.isActualTransactionActive())
        {
            fail("Test started with transaction in progress");
        }
        
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        fileFolderService = (FileFolderService) applicationContext.getBean("fileFolderService");
        
        authenticationService = (MutableAuthenticationService) applicationContext.getBean("authenticationService");
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        authenticationDAO = (MutableAuthenticationDao) applicationContext.getBean("authenticationDao");
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
        
        contentService = (ContentService) applicationContext.getBean("contentService");
        personService = (PersonService) applicationContext.getBean("personService");
        
        contentUsageImpl = (ContentUsageImpl) applicationContext.getBean("contentUsageImpl");
        usageService = (UsageService) applicationContext.getBean("usageService");
        
        ownableService = (OwnableService) applicationContext.getBean("ownableService");
        
        repoAdminService = (RepoAdminService) applicationContext.getBean("repoAdminService");
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // get default store (as configured for content usage service)
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        // create test users
        createTestUsers();
        
        // deploy custom model
        InputStream modelStream = getClass().getClassLoader().getResourceAsStream("tenant/exampleModel.xml");
        repoAdminService.deployModel(modelStream, "exampleModel.xml");
        
        testTX.commit();
        
        authenticationComponent.clearCurrentSecurityContext();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
    }
    
    private void createTestUsers()
    {
        // test user 1
        if (personService.personExists(TEST_USER))
        {
            personService.deletePerson(TEST_USER);
        }
        
        Map<QName, Serializable> props = createPersonProperties(TEST_USER);
        personNodeRef = personService.createPerson(props);
        
        if (authenticationDAO.userExists(TEST_USER))
        {
            authenticationService.deleteAuthentication(TEST_USER);
        }
        authenticationService.createAuthentication(TEST_USER, TEST_USER.toCharArray());
        
        // test user 2
        if (personService.personExists(TEST_USER_2))
        {
            personService.deletePerson(TEST_USER_2);
        }
        
        props = createPersonProperties(TEST_USER_2);
        personNodeRef2 = personService.createPerson(props);
        
        if (authenticationDAO.userExists(TEST_USER_2))
        {
            authenticationService.deleteAuthentication(TEST_USER_2);
        }
        authenticationService.createAuthentication(TEST_USER_2, TEST_USER_2.toCharArray());
    }
    
    protected void tearDown() throws Exception
    {
        boolean deltasDeleted = false;
        try
        {
            usageService.deleteDeltas(personNodeRef);
            usageService.deleteDeltas(personNodeRef2);
            deltasDeleted = true;
        }
        finally
        {
            try
            {
                testTX.commit();
            }
            catch (Throwable e)
            {
                AuthenticationUtil.clearCurrentSecurityContext();
                try { testTX.rollback(); } catch (Throwable ee) {}
                if (deltasDeleted)
                {
                    // The deltas did not cause this issue.  So it's something else during commit;
                    throw new Exception("Failed to commit transaction after test", e);
                }
                else
                {
                    // One of the deleteDelats calls failed and an exception is passing through.
                    // Do not rethrow so that we don't mask it.
                    logger.error("Transaction commit failed", e);
                }
            }
        }
    }
    
    protected void runAs(String userName)
    {
        authenticationService.authenticate(userName, userName.toCharArray());
        assertNotNull(authenticationService.getCurrentUserName());
    }
    
    protected void runAsAdmin()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        assertNotNull(authenticationService.getCurrentUserName());
    }
    
    private Map<QName, Serializable> createPersonProperties(String userName)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        return properties;
    }
    
    public void testCreateUpdateDeleteInTx() throws Exception
    {
        if(!contentUsageImpl.getEnabled())
        {
            return;
        }
        
        runAs(TEST_USER);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // add content (in this case, some "panagrams")
        
        NodeRef content1 = addTextContent(folder, "text1.txt", "The quick brown fox jumps over the lazy dog"); // + 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content2 = addTextContent(folder, "text2.txt", "Amazingly few discotheques provide jukeboxes", true); // + 44
        assertEquals(87, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content3 = addTextContent(folder, "text3.txt", "All questions asked by five watch experts amazed the judge"); // + 58
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        // update content in a different order
        
        updateTextContent(content1, "Few black taxis drive up major roads on quiet hazy nights"); // -43 + 57 = +14
        assertEquals(159, contentUsageImpl.getUserUsage(TEST_USER));
        
        updateTextContent(content3, "Heavy boxes perform quick waltzes and jigs"); // -58 + 42 = -16
        assertEquals(143, contentUsageImpl.getUserUsage(TEST_USER));
        
        updateTextContent(content2, "The five boxing wizards jump quickly"); // -44 + 36 = -8
        assertEquals(135, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete content in a different order
        
        delete(content2); // - 36
        assertEquals(99, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content3); // - 42
        assertEquals(57, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content1); // - 57
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete folder to cleanup
        delete(folder);
    }
    
    public void testCreateUpdateDeleteAcrossTx() throws Exception
    {
        if(!contentUsageImpl.getEnabled())
        {
            return;
        }
        
        runAs(TEST_USER);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // add content  (in this case, some "panagrams")
        
        NodeRef content1 = addTextContent(folder, "tqbfjotld.txt", "The quick brown fox jumps over the lazy dog");
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content2 = addTextContent(folder, "afdpj.txt", "Amazingly few discotheques provide jukeboxes", true);
        assertEquals(87, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content3 = addTextContent(folder, "aqabfweatj.txt", "All questions asked by five watch experts amazed the judge");
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runAs(TEST_USER);
        
        // update content in a different order
        
        updateTextContent(content1, "Few black taxis drive up major roads on quiet hazy nights"); // -43 + 57 = +14
        assertEquals(159, contentUsageImpl.getUserUsage(TEST_USER));
        
        updateTextContent(content3, "Heavy boxes perform quick waltzes and jigs"); // -58 + 42 = -16
        assertEquals(143, contentUsageImpl.getUserUsage(TEST_USER));
        
        updateTextContent(content2, "The five boxing wizards jump quickly"); // -44 + 36 = -8
        assertEquals(135, contentUsageImpl.getUserUsage(TEST_USER));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runAs(TEST_USER);
        
        assertEquals(135, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete content in a different order
        
        delete(content2);
        assertEquals(99, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content3);
        assertEquals(57, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content1);
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete folder to cleanup
        delete(folder);
    }
    
    public void testCreateDeleteRestoreInTx() throws Exception
    {
        if(!contentUsageImpl.getEnabled())
        {
            return;
        }
        
        runAs(TEST_USER);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // add content
        
        NodeRef content1 = addTextContent(folder, "text1.txt", "The quick brown fox jumps over the lazy dog"); // + 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content2 = addTextContent(folder, "text2.txt", "Amazingly few discotheques provide jukeboxes", true); // + 44
        assertEquals(87, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content3 = addTextContent(folder, "text3.txt", "All questions asked by five watch experts amazed the judge"); // + 58
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete content in a different order
        
        delete(content2); // - 44
        assertEquals(101, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content3); // - 58
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content1); // - 43
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // restore content in a different order
        
        restore(content3); // + 58
        assertEquals(58, contentUsageImpl.getUserUsage(TEST_USER));
        
        restore(content1); // + 43
        assertEquals(101, contentUsageImpl.getUserUsage(TEST_USER));
        
        restore(content2); // - 44
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete folder to cleanup
        delete(folder);
    }
    
    public void testCreateDeleteRestoreAcrossTx() throws Exception
    {
        if(!contentUsageImpl.getEnabled())
        {
            return;
        }
        
        runAs(TEST_USER);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // add content
        
        NodeRef content1 = addTextContent(folder, "text1.txt", "The quick brown fox jumps over the lazy dog"); // + 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content2 = addTextContent(folder, "text2.txt", "Amazingly few discotheques provide jukeboxes", true); // + 44
        assertEquals(87, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content3 = addTextContent(folder, "text3.txt", "All questions asked by five watch experts amazed the judge"); // + 58
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runAs(TEST_USER);
        
        // delete content in a different order
        
        delete(content2); // - 44
        assertEquals(101, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content3); // - 58
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content1); // - 43
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runAs(TEST_USER);
        
        // restore content in a different order
        
        restore(content3); // + 58
        assertEquals(58, contentUsageImpl.getUserUsage(TEST_USER));
        
        restore(content1); // + 43
        assertEquals(101, contentUsageImpl.getUserUsage(TEST_USER));
        
        restore(content2); // - 44
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete folder to cleanup
        delete(folder);
    }
    
    public void testCreateCopyDeleteInTx() throws Exception
    {
        if(!contentUsageImpl.getEnabled())
        {
            return;
        }
        
        runAs(TEST_USER);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // add content
        
        // add panagram
        NodeRef content1 = addTextContent(folder, "text1.txt", "The quick brown fox jumps over the lazy dog"); // + 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        // copy content
        
        NodeRef content2 = copy(content1, folder, "Copy of text1.txt"); // + 43
        assertEquals(86, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content3 = copy(content1, folder, "Copy of Copy of text1.txt"); // + 43
        assertEquals(129, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete content
        
        delete(content2); // - 43
        assertEquals(86, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content3); // - 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content1); // - 43
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete folder to cleanup
        delete(folder);
    }
    
    public void testCreateCopyDeleteAcrossTx() throws Exception
    {
        if(!contentUsageImpl.getEnabled())
        {
            return;
        }
        
        runAs(TEST_USER);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // add content
        
        // add panagram
        NodeRef content1 = addTextContent(folder, "text1.txt", "The quick brown fox jumps over the lazy dog"); // + 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        // copy content
        
        NodeRef content2 = copy(content1, folder, "Copy of text1.txt"); // + 43
        assertEquals(86, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content3 = copy(content1, folder, "Copy of Copy of text1.txt"); // + 43
        assertEquals(129, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete content
        
        delete(content2); // - 43
        assertEquals(86, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content3); // - 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content1); // - 43
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete folder to cleanup
        delete(folder);
    }
    
    public void testCreateCopyDeleteFolderWithContentInTx() throws Exception
    {
        if(!contentUsageImpl.getEnabled())
        {
            return;
        }
        
        runAs(TEST_USER);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        NodeRef folder1 = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // add content (in this case, some "panagrams")
        
        
        addTextContent(folder1, "text1.txt", "The quick brown fox jumps over the lazy dog"); // + 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        addTextContent(folder1, "text2.txt", "Amazingly few discotheques provide jukeboxes", true); // + 44
        assertEquals(87, contentUsageImpl.getUserUsage(TEST_USER));
        
        addTextContent(folder1, "text3.txt", "All questions asked by five watch experts amazed the judge"); // + 58
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        // copy folder with content
        
        NodeRef folder2 = copy(folder1, folder1, "Copy of testFolder"); // + 145
        assertEquals(290, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete copied folder
        
        delete(folder2); // - 145
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete original folder
        
        delete(folder1); // - 145
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
    }
    
    public void testCreateCopyDeleteFolderWithContentAcrossTx() throws Exception
    {
        if(!contentUsageImpl.getEnabled())
        {
            return;
        }
        
        runAs(TEST_USER);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        NodeRef folder1 = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // add content (in this case, some "panagrams")
        
        addTextContent(folder1, "text1.txt", "The quick brown fox jumps over the lazy dog"); // + 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        addTextContent(folder1, "text2.txt", "Amazingly few discotheques provide jukeboxes", true); // + 44
        assertEquals(87, contentUsageImpl.getUserUsage(TEST_USER));
        
        addTextContent(folder1, "text3.txt", "All questions asked by five watch experts amazed the judge"); // + 58
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runAs(TEST_USER);
        
        // copy folder with content
        
        NodeRef folder2 = copy(folder1, folder1, "Copy of testFolder"); // + 145
        assertEquals(290, contentUsageImpl.getUserUsage(TEST_USER));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runAs(TEST_USER);
        
        // delete copied folder
        
        delete(folder2); // - 145
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runAs(TEST_USER);
        
        // delete original folder
        
        delete(folder1); // - 145
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
    }
    
    public void testCreateTakeOwnershipInTx() throws Exception
    {
        if(!contentUsageImpl.getEnabled())
        {
            return;
        }
        
        runAs(TEST_USER);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // add content (in this case, some "panagrams")
        
        NodeRef content1 = addTextContent(folder, "text1.txt", "The quick brown fox jumps over the lazy dog"); // + 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content2 = addTextContent(folder, "text2.txt", "Amazingly few discotheques provide jukeboxes", true); // + 44
        assertEquals(87, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content3 = addTextContent(folder, "text3.txt", "All questions asked by five watch experts amazed the judge"); // + 58
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        String ADMIN = AuthenticationUtil.getAdminUserName();
        
        runAs(ADMIN);
        
        long before = contentUsageImpl.getUserUsage(ADMIN);
        
        takeOwnership(content1); // +/- 43 (test user -> admin)
        
        assertEquals(102, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(before+43, contentUsageImpl.getUserUsage(ADMIN));
        
        takeOwnership(content2); // +/- 44 (test user -> admin)
        
        assertEquals(58, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(before+87, contentUsageImpl.getUserUsage(ADMIN));
        
        runAs(TEST_USER);
        
        takeOwnership(content1); // +/- 43 (admin -> test user)
        
        assertEquals(101, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(before+44, contentUsageImpl.getUserUsage(ADMIN));
        
        takeOwnership(content3); // note: already the creator
        
        assertEquals(101, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(before+44, contentUsageImpl.getUserUsage(ADMIN));
        
        // delete folder to cleanup
        delete(folder);
    }
    
    public void testCreateTakeOwnershipAcrossTx() throws Exception
    {
        if(!contentUsageImpl.getEnabled())
        {
            return;
        }
        
        runAs(TEST_USER);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // add content (in this case, some "panagrams")
        
        NodeRef content1 = addTextContent(folder, "text1.txt", "The quick brown fox jumps over the lazy dog"); // + 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content2 = addTextContent(folder, "text2.txt", "Amazingly few discotheques provide jukeboxes", true); // + 44
        assertEquals(87, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content3 = addTextContent(folder, "text3.txt", "All questions asked by five watch experts amazed the judge"); // + 58
        assertEquals(145, contentUsageImpl.getUserUsage(TEST_USER));
        
        testTX.commit();
        
        String ADMIN = AuthenticationUtil.getAdminUserName();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        runAs(ADMIN);
        
        long before = contentUsageImpl.getUserUsage(ADMIN);
        
        takeOwnership(content1); // +/- 43 (test user -> admin)
        
        assertEquals(102, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(before+43, contentUsageImpl.getUserUsage(ADMIN));
        
        takeOwnership(content2); // +/- 44 (test user -> admin)
        
        assertEquals(58, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(before+87, contentUsageImpl.getUserUsage(ADMIN));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        runAs(TEST_USER);
        
        takeOwnership(content1); // +/- 43 (admin -> test user)
        
        assertEquals(101, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(before+44, contentUsageImpl.getUserUsage(ADMIN));
        
        takeOwnership(content3); // note: already the creator
        
        assertEquals(101, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(before+44, contentUsageImpl.getUserUsage(ADMIN));
        
        // delete folder to cleanup
        delete(folder);
    }
    
    public void testCreateDeleteRestoreTwoUsersAcrossTx() throws Exception
    {
        if(!contentUsageImpl.getEnabled())
        {
            return;
        }
        
        runAs(TEST_USER);
        
        // Create a (shared) folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // create some content as the first user
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content1 = addTextContent(folder, "tqbfjotld.txt", "The quick brown fox jumps over the lazy dog");
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(TEST_USER, ownableService.getOwner(content1));
        
        NodeRef content2 = addTextContent(folder, "afdpj.txt", "Amazingly few discotheques provide jukeboxes", true);
        assertEquals(87, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(TEST_USER, ownableService.getOwner(content2));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        runAs(TEST_USER_2);
        
        // create some more content as the second user
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER_2));
        
        NodeRef content3 = addTextContent(folder, "aqabfweatj.txt", "All questions asked by five watch experts amazed the judge");
        assertEquals(58, contentUsageImpl.getUserUsage(TEST_USER_2));
        assertEquals(TEST_USER_2, ownableService.getOwner(content3));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        runAs(TEST_USER_2);
        
        assertEquals(87, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(58, contentUsageImpl.getUserUsage(TEST_USER_2));
        
        // as the second user, delete some content owned by first user
        
        delete(content2);
        
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(58, contentUsageImpl.getUserUsage(TEST_USER_2));
        
        delete(content1);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(58, contentUsageImpl.getUserUsage(TEST_USER_2));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        runAs(TEST_USER);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(58, contentUsageImpl.getUserUsage(TEST_USER_2));
        
        // as the first user, delete some content owned by second user
        
        delete(content3);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER_2));
        
        // note: via AlfExp non-admin user can only restore what they have deleted
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        runAs(TEST_USER_2);
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER_2));
        
        restore(content2);
        
        // note: restore sets owner as the user who deleted the content (not the original owner)
        assertEquals(TEST_USER_2, ownableService.getOwner(content2));
        
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(44, contentUsageImpl.getUserUsage(TEST_USER_2));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        runAs(TEST_USER);
        
        restore(content3);
        
        assertEquals(TEST_USER, ownableService.getOwner(content3));
        
        assertEquals(58, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(44, contentUsageImpl.getUserUsage(TEST_USER_2));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        runAsAdmin();
        
        restore(content1);
        
        //note: restore sets owner as the user who deleted the content (not the original owner)
        assertEquals(TEST_USER_2, ownableService.getOwner(content1));
        
        assertEquals(58, contentUsageImpl.getUserUsage(TEST_USER));
        assertEquals(87, contentUsageImpl.getUserUsage(TEST_USER_2));
        
        // delete folder to cleanup
        delete(folder);
    }
    
    private NodeRef addTextContent(NodeRef folderRef, String name, String textData)
    {
        return addTextContent(folderRef, name, textData, false);
    }
    
    private NodeRef addTextContent(NodeRef folderRef, String name, String textData, boolean custom)
    {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);
        
        ChildAssociationRef association = nodeService.createNode(folderRef,
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                (custom == true ? customType: ContentModel.TYPE_CONTENT),
                contentProps);
        
        NodeRef content = association.getChildRef();
        
        ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);
        
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        
        writer.putContent(textData);
        
        return content;
    }
    
    private void updateTextContent(NodeRef contentRef, String textData)
    {
        ContentWriter writer = contentService.getWriter(contentRef, ContentModel.PROP_CONTENT, true);
        
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        
        writer.putContent(textData);
    }
    
    private void delete(NodeRef folderOrContentRef)
    {
        nodeService.deleteNode(folderOrContentRef);
    }
    
    private void restore(NodeRef origfolderOrContentRef)
    {
        NodeRef archiveRootNode = nodeService.getStoreArchiveNode(this.rootNodeRef.getStoreRef());
        
        NodeRef archiveNodeRef = new NodeRef(archiveRootNode.getStoreRef(), origfolderOrContentRef.getId());
        nodeService.restoreNode(archiveNodeRef, null, null, null);
    }
    
    private NodeRef copy(NodeRef sourceFolderOrContentRef, NodeRef targetFolderRef, String newName) throws FileNotFoundException
    {
        return fileFolderService.copy(sourceFolderOrContentRef, targetFolderRef, newName).getNodeRef();
    }
    
    private void takeOwnership(NodeRef nodeRef)
    {
        ownableService.takeOwnership(nodeRef);
    }
}
