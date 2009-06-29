package org.alfresco.repo.imap;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.mail.Flags;

import junit.framework.TestCase;

import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ApplicationContextHelper;
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
    
    private AlfrescoImapUser user;
    // DH: Do not assume the presence of any specific user or password.  Create a new user for the test.
    private static final String USER_NAME = "admin";
    private static final String USER_PASSWORD = "admin";
    private static final String TEST_DATA_FOLDER_NAME = "test_data";
    private static final String TEST_FOLDER_NAME = "test_imap1000";
    private static final long MESSAGE_QUANTITY = 1000;
    
    
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        AuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        imapService = serviceRegistry.getImapService();
        importerService = serviceRegistry.getImporterService();
        NodeService nodeService = serviceRegistry.getNodeService();
        SearchService searchService = serviceRegistry.getSearchService();
        NamespaceService namespaceService = serviceRegistry.getNamespaceService();

        user = new AlfrescoImapUser(USER_NAME + "@alfresco.com", USER_NAME, USER_PASSWORD);

        authenticationService.authenticate(USER_NAME, USER_PASSWORD.toCharArray());

        StoreRef storeRef = new StoreRef("workspace://SpacesStore");
        NodeRef rootRef = nodeService.getRootNode(storeRef);

        // Delete test folder
        List<NodeRef> nodeRefs = searchService.selectNodes(rootRef, "/app:company_home/imap:imap_home/cm:admin/cm:" + TEST_FOLDER_NAME, null, namespaceService, false);
        if (nodeRefs.size() == 1)
        {
            NodeRef ch = nodeRefs.get(0);
            nodeService.deleteNode(ch);
        }
        // Delete test data folder
        nodeRefs = searchService.selectNodes(rootRef, "/app:company_home/imap:imap_home/cm:admin/cm:" + TEST_DATA_FOLDER_NAME, null, namespaceService, false);
        if (nodeRefs.size() == 1)
        {
            NodeRef ch = nodeRefs.get(0);
            nodeService.deleteNode(ch);
        }

        // DH: Do not assume the presence of a path for the IMAP home.  Create the IMAP home and set it on the service.
        NodeRef adminNodeRef = searchService.selectNodes(rootRef, "/app:company_home/imap:imap_home/cm:admin", null, namespaceService, false).get(0);
        importTestData("test-resources/imap/load_test_data.acp", adminNodeRef);


        AlfrescoImapFolder testDataFolder = imapService.getFolder(user, TEST_DATA_FOLDER_NAME);

        SimpleStoredMessage m = testDataFolder.getMessages().get(0);
        m = testDataFolder.getMessage(m.getUid());
        
        AlfrescoImapFolder folder = imapService.createMailbox(user, TEST_FOLDER_NAME);

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
    }
    
    
    
    public void tearDown() throws Exception
    {
    }

    
    public void testList()
    {
        logger.info("Listing folders...");

        long t = System.currentTimeMillis();
        List<AlfrescoImapFolder> list = imapService.listMailboxes(user, TEST_FOLDER_NAME + "*");
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
