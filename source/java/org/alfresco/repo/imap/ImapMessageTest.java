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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
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

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.UID;

import junit.framework.TestCase;

public class ImapMessageTest extends TestCase
{
    private static Log logger = LogFactory.getLog(ImapMessageTest.class);

    // IMAP client settings
    private static final String PROTOCOL = "imap";
    private static final String HOST = "localhost";
    private static final int PORT = 143;

    private static final String ADMIN_USER_NAME = "admin";
    private static final String ADMIN_USER_PASSWORD = "admin";
    private static final String IMAP_FOLDER_NAME = "test";

    private Session session = null;
    private Store store = null;
    private IMAPFolder folder = null;

    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private TransactionService transactionService;
    private NodeService nodeService;
    private ImporterService importerService;
    private PersonService personService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private FileFolderService fileFolderService;
    private MutableAuthenticationService authenticationService;

    String anotherUserName;
    private NodeRef testImapFolderNodeRef;
    private NodeRef storeRootNodeRef;
    private final String storePath = "workspace://SpacesStore";
    private final String companyHomePathInStore = "/app:company_home";

    private static final String TEST_FOLDER = "Alfresco IMAP/" + IMAP_FOLDER_NAME + "/___-___folder_a/" + "___-___folder_a_a";
    private static final String TEST_FILE = "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + IMAP_FOLDER_NAME + "/" + NamespaceService.CONTENT_MODEL_PREFIX
            + ":___-___folder_a/" + NamespaceService.CONTENT_MODEL_PREFIX + ":___-___folder_a_a/" + NamespaceService.CONTENT_MODEL_PREFIX + ":___-___file_a_a";

    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        importerService = serviceRegistry.getImporterService();
        personService = serviceRegistry.getPersonService();
        authenticationService = serviceRegistry.getAuthenticationService();
        searchService = serviceRegistry.getSearchService();
        namespaceService = serviceRegistry.getNamespaceService();
        fileFolderService = serviceRegistry.getFileFolderService();

        // start the transaction
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        authenticationService.authenticate(ADMIN_USER_NAME, ADMIN_USER_PASSWORD.toCharArray());

        // downgrade integrity
        IntegrityChecker.setWarnInTransaction();

        anotherUserName = "user" + System.currentTimeMillis();

        PropertyMap testUser = new PropertyMap();
        testUser.put(ContentModel.PROP_USERNAME, anotherUserName);
        testUser.put(ContentModel.PROP_FIRSTNAME, anotherUserName);
        testUser.put(ContentModel.PROP_LASTNAME, anotherUserName);
        testUser.put(ContentModel.PROP_EMAIL, anotherUserName + "@alfresco.com");
        testUser.put(ContentModel.PROP_JOBTITLE, "jobTitle");

        personService.createPerson(testUser);

        // create the ACEGI Authentication instance for the new user
        authenticationService.createAuthentication(anotherUserName, anotherUserName.toCharArray());

        StoreRef storeRef = new StoreRef(storePath);
        storeRootNodeRef = nodeService.getRootNode(storeRef);

        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore, null, namespaceService, false);
        NodeRef companyHomeNodeRef = nodeRefs.get(0);

        nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore + "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + IMAP_FOLDER_NAME, null,
                namespaceService, false);
        if (nodeRefs != null && nodeRefs.size() > 0)
        {
            fileFolderService.delete(nodeRefs.get(0));
        }

        ChildApplicationContextFactory imap = (ChildApplicationContextFactory) ctx.getBean("imap");
        ApplicationContext imapCtx = imap.getApplicationContext();
        ImapServiceImpl imapServiceImpl = (ImapServiceImpl) imapCtx.getBean("imapService");

        // Creating IMAP test folder for IMAP root
        LinkedList<String> folders = new LinkedList<String>();
        folders.add(IMAP_FOLDER_NAME);
        FileFolderUtil.makeFolders(fileFolderService, companyHomeNodeRef, folders, ContentModel.TYPE_FOLDER);

        // Setting IMAP root
        RepositoryFolderConfigBean imapHome = new RepositoryFolderConfigBean();
        imapHome.setStore(storePath);
        imapHome.setRootPath(companyHomePathInStore);
        imapHome.setFolderPath(IMAP_FOLDER_NAME);
        imapServiceImpl.setImapHome(imapHome);

        // Starting IMAP
        imapServiceImpl.startup();

        nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore + "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + IMAP_FOLDER_NAME, null,
                namespaceService, false);
        testImapFolderNodeRef = nodeRefs.get(0);

        /*
         * Importing test folders: Test folder contains: "___-___folder_a" "___-___folder_a" contains: "___-___folder_a_a", "___-___file_a", "Message_485.eml" (this is IMAP
         * Message) "___-___folder_a_a" contains: "____-____file_a_a"
         */
        importInternal("imap/imapservice_test_folder_a.acp", testImapFolderNodeRef);

        txn.commit();

        // Init mail client session
        Properties props = new Properties();
        props.setProperty("mail.imap.partialfetch", "false");
        this.session = Session.getDefaultInstance(props, null);

        // Get the store
        this.store = session.getStore(PROTOCOL);
        this.store.connect(HOST, PORT, anotherUserName, anotherUserName);

        // Get folder
        folder = (IMAPFolder) store.getFolder(TEST_FOLDER);
        folder.open(Folder.READ_ONLY);

    }

    private void importInternal(String acpName, NodeRef space) throws IOException
    {
        // Importing IMAP test acp
        ClassPathResource acpResource = new ClassPathResource(acpName);
        ACPImportPackageHandler acpHandler = new ACPImportPackageHandler(acpResource.getFile(), null);
        Location importLocation = new Location(space);
        importerService.importView(acpHandler, importLocation, null, null);
    }

    public void testMessageModifiedBetweenReads() throws Exception
    {
        // Get test message UID
        final Long uid = getMessageUid(folder, 1);
        // Get Message size
        final int count = getMessageSize(folder, uid);

        // Get first part
        BODY body = getMessageBodyPart(folder, uid, 0, count - 100);

        // Modify message. The size of letter describing the node may change
        // These changes should be committed because it should be visible from client
        NodeRef contentNode = findNode(companyHomePathInStore + TEST_FILE);
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        ContentWriter writer = fileFolderService.getWriter(contentNode);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 2000; i++)
        {
            sb.append("test string");
        }
        writer.putContent(sb.toString());
        txn.commit();

        // Read second message part
        BODY bodyRest = getMessageBodyPart(folder, uid, count - 10, 10);

        // Creating and parsing message from 2 parts
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()), new SequenceInputStream(new BufferedInputStream(body.getByteArrayInputStream()),
                new BufferedInputStream(bodyRest.getByteArrayInputStream())));

        // Reading first part - should be successful
        MimeMultipart content = (MimeMultipart) message.getContent();
        assertNotNull(content.getBodyPart(0).getContent());

        try
        {
            // Reading second part cause error
            content.getBodyPart(1).getContent();
            fail("Should raise an IOException");
        }
        catch (IOException e)
        {
        }
    }

    public void testMessageRenamedBetweenReads() throws Exception
    {
        // Get test message UID
        final Long uid = getMessageUid(folder, 1);
        // Get Message size
        final int count = getMessageSize(folder, uid);

        // Get first part
        BODY body = getMessageBodyPart(folder, uid, 0, count - 100);

        // Rename message. The size of letter describing the node will change
        // These changes should be committed because it should be visible from client
        NodeRef contentNode = findNode(companyHomePathInStore + TEST_FILE);
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        fileFolderService.rename(contentNode, "testtesttesttesttesttesttesttesttesttest");
        txn.commit();

        // Read second message part
        BODY bodyRest = getMessageBodyPart(folder, uid, count - 100, 100);

        // Creating and parsing message from 2 parts
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()), new SequenceInputStream(new BufferedInputStream(body.getByteArrayInputStream()),
                new BufferedInputStream(bodyRest.getByteArrayInputStream())));

        // Reading first part - should be successful
        MimeMultipart content = (MimeMultipart) message.getContent();
        assertNotNull(content.getBodyPart(0).getContent());

        try
        {
            // Reading second part cause error
            content.getBodyPart(1).getContent();
            fail("Should raise an IOException");
        }
        catch (IOException e)
        {
        }
    }

    public void testMessageCache() throws Exception
    {

        // Create messages
        NodeRef contentNode = findNode(companyHomePathInStore + TEST_FILE);
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();

        // Create messages more than cache capacity
        for (int i = 0; i < 51; i++)
        {
            FileInfo fi = fileFolderService.create(nodeService.getParentAssocs(contentNode).get(0).getParentRef(), "test" + i, ContentModel.TYPE_CONTENT);
            ContentWriter writer = fileFolderService.getWriter(fi.getNodeRef());
            writer.putContent("test");
        }

        txn.commit();

        // Reload folder
        folder.close(false);
        folder = (IMAPFolder) store.getFolder(TEST_FOLDER);
        folder.open(Folder.READ_ONLY);

        // Read all messages
        for (int i = 1; i < 51; i++)
        {
            // Get test message UID
            final Long uid = getMessageUid(folder, i);
            // Get Message size
            final int count = getMessageSize(folder, uid);

            // Get first part
            BODY body = getMessageBodyPart(folder, uid, 0, count - 100);
            // Read second message part
            BODY bodyRest = getMessageBodyPart(folder, uid, count - 100, 100);

            // Creating and parsing message from 2 parts
            MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()), new SequenceInputStream(new BufferedInputStream(body.getByteArrayInputStream()),
                    new BufferedInputStream(bodyRest.getByteArrayInputStream())));

            // Reading first part - should be successful
            MimeMultipart content = (MimeMultipart) message.getContent();
            assertNotNull(content.getBodyPart(0).getContent());
            assertNotNull(content.getBodyPart(1).getContent());
        }
    }

    
    
    public void testUnmodifiedMessage() throws Exception
    {
        // Get test message UID
        final Long uid = getMessageUid(folder, 1);
        // Get Message size
        final int count = getMessageSize(folder, uid);

        // Make multiple message reading
        for (int i = 0; i < 100; i++)
        {
            // Get random offset
            int n = (int) ((int) 100 * Math.random());

            // Get first part
            BODY body = getMessageBodyPart(folder, uid, 0, count - n);
            // Read second message part
            BODY bodyRest = getMessageBodyPart(folder, uid, count - n, n);

            // Creating and parsing message from 2 parts
            MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()), new SequenceInputStream(new BufferedInputStream(body.getByteArrayInputStream()),
                    new BufferedInputStream(bodyRest.getByteArrayInputStream())));

            MimeMultipart content = (MimeMultipart) message.getContent();
            // Reading first part - should be successful
            assertNotNull(content.getBodyPart(0).getContent());
            // Reading second part - should be successful
            assertNotNull(content.getBodyPart(1).getContent());
        }
    }

    /**
     * Returns BODY object containing desired message fragment
     * 
     * @param folder Folder containing the message
     * @param uid Message UID
     * @param from starting byte
     * @param count bytes to read
     * @return BODY containing desired message fragment
     * @throws MessagingException
     */
    private static BODY getMessageBodyPart(IMAPFolder folder, final Long uid, final Integer from, final Integer count) throws MessagingException
    {
        return (BODY) folder.doCommand(new IMAPFolder.ProtocolCommand()
        {
            public Object doCommand(IMAPProtocol p) throws ProtocolException
            {
                Response[] r = p.command("UID FETCH " + uid + " (FLAGS BODY.PEEK[]<" + from + "." + count + ">)", null);
                logResponse(r);
                Response response = r[r.length - 1];

                // Grab response
                if (!response.isOK())
                {
                    throw new ProtocolException("Unable to retrieve message part <" + from + "." + count + ">");
                }

                FetchResponse fetchResponse = (FetchResponse) r[0];
                BODY body = (BODY) fetchResponse.getItem(com.sun.mail.imap.protocol.BODY.class);
                return body;
            }
        });

    }

    /**
     * Finds node by its path
     * 
     * @param path
     * @return NodeRef
     */
    private NodeRef findNode(String path)
    {
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, path, null, namespaceService, false);
        return nodeRefs.size() > 0 ? nodeRefs.get(0) : null;
    }

    /**
     * Returns the UID of the first message in folder
     * 
     * @param folder Folder containing the message
     * @param msn message sequence number
     * @return UID of the first message
     * @throws MessagingException
     */
    private static Long getMessageUid(IMAPFolder folder, final int msn) throws MessagingException
    {
        return (Long) folder.doCommand(new IMAPFolder.ProtocolCommand()
        {
            public Object doCommand(IMAPProtocol p) throws ProtocolException
            {
                Response[] r = p.command("FETCH " + msn + " (UID)", null);
                logResponse(r);
                Response response = r[r.length - 1];

                // Grab response
                if (!response.isOK())
                {
                    throw new ProtocolException("Unable to retrieve message UID");
                }
                FetchResponse fetchResponse = (FetchResponse) r[0];
                UID uid = (UID) fetchResponse.getItem(UID.class);
                return uid.uid;
            }
        });
    }

    /**
     * Returns size of the message
     * 
     * @param folder Folder containing the message
     * @param uid Message UID
     * @return Returns size of the message
     * @throws MessagingException
     */
    private static Integer getMessageSize(IMAPFolder folder, final Long uid) throws MessagingException
    {
        return (Integer) folder.doCommand(new IMAPFolder.ProtocolCommand()
        {
            public Object doCommand(IMAPProtocol p) throws ProtocolException
            {
                Response[] r = p.command("UID FETCH " + uid + " (FLAGS BODY.PEEK[])", null);
                logResponse(r);
                Response response = r[r.length - 1];

                // Grab response
                if (!response.isOK())
                {
                    throw new ProtocolException("Unable to retrieve message size");
                }
                FetchResponse fetchResponse = (FetchResponse) r[0];
                BODY body = (BODY) fetchResponse.getItem(BODY.class);
                return body.data.getCount();
            }
        });
    }

    /**
     * Simple util for logging response
     * 
     * @param r response
     */
    private static void logResponse(Response[] r)
    {
        for (int i = 0; i < r.length; i++)
        {
            logger.debug(r[i]);
            //logger.info(r[i]);
        }
    }

    @Override
    public void tearDown() throws Exception
    {
        // Deleting created test environment

        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();

        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore + "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + IMAP_FOLDER_NAME, null,
                namespaceService, false);
        if (nodeRefs != null && nodeRefs.size() > 0)
        {
            fileFolderService.delete(nodeRefs.get(0));
        }

        authenticationService.deleteAuthentication(anotherUserName);
        personService.deletePerson(anotherUserName);

        txn.commit();

        // Closing client connection
        folder.close(false);
        store.close();
    }

}
