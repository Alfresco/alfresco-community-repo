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
package org.alfresco.repo.imap;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.mail.Flags;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.config.RepositoryFolderConfigBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.icegreen.greenmail.store.SimpleStoredMessage;

public class LoadTester extends TestCase
{
    private Log logger = LogFactory.getLog(LoadTester.class);
    

    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
 
    private ImapService imapService;
    private ImporterService importerService;
    private MutableAuthenticationService authenticationService;
    
    private AlfrescoImapUser user;
    // DH: Do not assume the presence of any specific user or password.  Create a new user for the test.
    private static final String USER_NAME = "admin";
    private static final String USER_PASSWORD = "admin";
    private static final String TEST_IMAP_ROOT_FOLDER_NAME = "aaa";
    private static final String TEST_DATA_FOLDER_NAME = "test_data";
    private static final String TEST_FOLDER_NAME = "test_imap1000";
    private static final long MESSAGE_QUANTITY = 1000;
    
    private String anotherUserName;

    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        authenticationService = serviceRegistry.getAuthenticationService();
        imapService = serviceRegistry.getImapService();
        importerService = serviceRegistry.getImporterService();
        NodeService nodeService = serviceRegistry.getNodeService();
        SearchService searchService = serviceRegistry.getSearchService();
        NamespaceService namespaceService = serviceRegistry.getNamespaceService();
        PersonService personService = serviceRegistry.getPersonService();
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        TransactionService transactionService = serviceRegistry.getTransactionService();
        PermissionService permissionService = serviceRegistry.getPermissionService();

        
        // start the transaction
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();

        authenticationService.authenticate(USER_NAME, USER_PASSWORD.toCharArray());

        anotherUserName = "test_imap_user";
        
        NodeRef person = personService.getPerson(anotherUserName);

        if (person != null)
        {
            personService.deletePerson(anotherUserName);
            PropertyMap testUser = new PropertyMap();
            testUser.put(ContentModel.PROP_USERNAME, anotherUserName);
            testUser.put(ContentModel.PROP_FIRSTNAME, anotherUserName);
            testUser.put(ContentModel.PROP_LASTNAME, anotherUserName);
            testUser.put(ContentModel.PROP_EMAIL, anotherUserName + "@alfresco.com");
            testUser.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            personService.createPerson(testUser);
            
        }
        if (authenticationService.authenticationExists(anotherUserName))
        {
            authenticationService.deleteAuthentication(anotherUserName);
        }
        authenticationService.createAuthentication(anotherUserName, anotherUserName.toCharArray());
        
        
        user = new AlfrescoImapUser(anotherUserName + "@alfresco.com", anotherUserName, anotherUserName);

        String storePath = "workspace://SpacesStore";
        String companyHomePathInStore = "/app:company_home";

        StoreRef storeRef = new StoreRef(storePath);
        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
        
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore, null, namespaceService, false);
        NodeRef companyHomeNodeRef = nodeRefs.get(0);

        ChildApplicationContextFactory imap = (ChildApplicationContextFactory) ctx.getBean("imap");
        ApplicationContext imapCtx = imap.getApplicationContext();
        ImapServiceImpl imapServiceImpl = (ImapServiceImpl)imapCtx.getBean("imapService");

        
        // Delete test folder
        nodeRefs = searchService.selectNodes(storeRootNodeRef,
                companyHomePathInStore + "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + TEST_IMAP_ROOT_FOLDER_NAME,
                null, namespaceService, false);
        if (nodeRefs.size() == 1)
        {
            NodeRef ch = nodeRefs.get(0);
            nodeService.deleteNode(ch);
        }

        
        // Creating IMAP test folder for IMAP root
        LinkedList<String> folders = new LinkedList<String>();
        folders.add(TEST_IMAP_ROOT_FOLDER_NAME);
        FileFolderServiceImpl.makeFolders(fileFolderService, companyHomeNodeRef, folders, ContentModel.TYPE_FOLDER);
        
        // Setting IMAP root
        RepositoryFolderConfigBean imapHome = new RepositoryFolderConfigBean();
        imapHome.setStore(storePath);
        imapHome.setRootPath(companyHomePathInStore);
        imapHome.setFolderPath(TEST_IMAP_ROOT_FOLDER_NAME);
        imapServiceImpl.setImapHome(imapHome);
        
        // Starting IMAP
        imapServiceImpl.startupInTxn();
 
        nodeRefs = searchService.selectNodes(storeRootNodeRef,
                companyHomePathInStore + "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + TEST_IMAP_ROOT_FOLDER_NAME,
                null,
                namespaceService,
                false);

        // Used to create User's folder
        NodeRef userFolderRef = imapService.getUserImapHomeRef(anotherUserName);
        permissionService.setPermission(userFolderRef, anotherUserName, PermissionService.ALL_PERMISSIONS, true);

        importTestData("imap/load_test_data.acp", userFolderRef);

        reauthenticate(anotherUserName, anotherUserName);
        
        AlfrescoImapFolder testDataFolder = imapService.getOrCreateMailbox(user, TEST_DATA_FOLDER_NAME, true, false);

        SimpleStoredMessage m = testDataFolder.getMessages().get(0);
        m = testDataFolder.getMessage(m.getUid());
        
        AlfrescoImapFolder folder = imapService.getOrCreateMailbox(user, TEST_FOLDER_NAME, false, true);

        logger.info("Creating folders...");
        long t = System.currentTimeMillis();

        try 
        {
            for (int i = 0; i < MESSAGE_QUANTITY; i++)
            {
                System.out.println("i = " + i);
                folder.appendMessage(m.getMimeMessage(), new Flags(), new Date());
            }
        }
        catch (Exception e)
        {
            logger.error(e, e);
        }

        t = System.currentTimeMillis() - t;
        logger.info("Create time: " + t + " ms (" + t/1000 + " s (" + t/60000 + " min))");
        
        txn.commit();
    }
    
    
    private void reauthenticate(String name, String password)
    {
        authenticationService.invalidateTicket(authenticationService.getCurrentTicket());
        authenticationService.clearCurrentSecurityContext();
        authenticationService.authenticate(name, password.toCharArray());
    }

    
    public void tearDown() throws Exception
    {
    }

    
    public void testList()
    {
        logger.info("Listing folders...");

        long t = System.currentTimeMillis();
        List<AlfrescoImapFolder> list = imapService.listMailboxes(user, TEST_FOLDER_NAME + "*", false);
        t = System.currentTimeMillis() - t;
        
        logger.info("List time: " + t + " ms (" + t/1000 + " s)");
        logger.info("List size: " + list.size());
        
    }
    
    
    private void importTestData(String acpName, NodeRef space) throws IOException
    {
        ClassPathResource acpResource = new ClassPathResource(acpName);
        ACPImportPackageHandler acpHandler = new ACPImportPackageHandler(acpResource.getFile(), null);
        Location importLocation = new Location(space);
        importerService.importView(acpHandler, importLocation, null, null);
    }

    
    
}
