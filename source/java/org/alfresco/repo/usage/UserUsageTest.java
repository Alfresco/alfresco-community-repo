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

package org.alfresco.repo.usage;

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
import org.springframework.context.ApplicationContext;

/**
 * User Usage unit test
 */
public class UserUsageTest extends TestCase
{
    private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
    
    protected NodeService nodeService;
    protected FileFolderService fileFolderService;
    protected MutableAuthenticationService authenticationService;
    private MutableAuthenticationDao authenticationDAO;
    protected NodeRef rootNodeRef;
    protected NodeRef systemNodeRef;
    protected NodeRef personNodeRef;
    protected AuthenticationComponent authenticationComponent;
    private UserTransaction testTX;
    private TransactionService transactionService;
    private ContentService contentService;
    private PersonService personService;
    private ContentUsageImpl contentUsageImpl;
    private UsageService usageService;
    private OwnableService ownableService;
    
    private static final String TEST_USER = "userUsageTestUser";
    
    protected void setUp() throws Exception
    {
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
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // get default store (as configured for content usage service)
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        // create person
        if (personService.personExists(TEST_USER))
        {
            personService.deletePerson(TEST_USER);
        }
        
        Map<QName, Serializable> props = createPersonProperties(TEST_USER);
        personNodeRef = personService.createPerson(props);
        
        // create an authentication object e.g. the user
        if (authenticationDAO.userExists(TEST_USER))
        {
            authenticationService.deleteAuthentication(TEST_USER);
        }
        authenticationService.createAuthentication(TEST_USER, TEST_USER.toCharArray());
        
        authenticationComponent.clearCurrentSecurityContext();
    }
    
    protected void tearDown() throws Exception
    {
        try
        {
            usageService.deleteDeltas(personNodeRef);
            
            testTX.commit();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
            super.tearDown();
        }
    }
    
    protected void runAs(String userName)
    {
        authenticationService.authenticate(userName, userName.toCharArray());
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
        
        NodeRef content2 = addTextContent(folder, "text2.txt", "Amazingly few discotheques provide jukeboxes"); // + 44
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
        
        NodeRef content2 = addTextContent(folder, "afdpj.txt", "Amazingly few discotheques provide jukeboxes");
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
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runAs(TEST_USER);
        
        // copy content
        
        NodeRef content2 = copy(content1, folder, "Copy of text1.txt"); // + 43
        assertEquals(86, contentUsageImpl.getUserUsage(TEST_USER));
        
        NodeRef content3 = copy(content1, folder, "Copy of Copy of text1.txt"); // + 43
        assertEquals(129, contentUsageImpl.getUserUsage(TEST_USER));
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runAs(TEST_USER);
        
        assertEquals(129, contentUsageImpl.getUserUsage(TEST_USER));
        
        // delete content
        
        delete(content2); // - 43
        assertEquals(86, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content3); // - 43
        assertEquals(43, contentUsageImpl.getUserUsage(TEST_USER));
        
        delete(content1); // - 43
        assertEquals(0, contentUsageImpl.getUserUsage(TEST_USER));
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
        
        addTextContent(folder1, "text2.txt", "Amazingly few discotheques provide jukeboxes"); // + 44
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
        
        addTextContent(folder1, "text2.txt", "Amazingly few discotheques provide jukeboxes"); // + 44
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
        
        NodeRef content2 = addTextContent(folder, "text2.txt", "Amazingly few discotheques provide jukeboxes"); // + 44
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
        
        NodeRef content2 = addTextContent(folder, "text2.txt", "Amazingly few discotheques provide jukeboxes"); // + 44
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
    }
    
    private NodeRef addTextContent(NodeRef folderRef, String name, String textData)
    {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);
        
        ChildAssociationRef association = nodeService.createNode(folderRef,
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                ContentModel.TYPE_CONTENT,
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
    
    private NodeRef copy(NodeRef sourceFolderOrContentRef, NodeRef targetFolderRef, String newName) throws FileNotFoundException
    {
        return fileFolderService.copy(sourceFolderOrContentRef, targetFolderRef, newName).getNodeRef();
    }
    
    private void takeOwnership(NodeRef nodeRef)
    {
        ownableService.takeOwnership(nodeRef);
    }
}
