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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.config.RepositoryFolderConfigBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.RFC822DATA;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.util.ASCIIUtility;

public class ImapMessageTest extends TestCase
{
    private static Log logger = LogFactory.getLog(ImapMessageTest.class);

    // IMAP client settings
    private static final String PROTOCOL = "imap";
    private static final String HOST = "localhost";
    private static final int PORT = 7143;

    private static final String ADMIN_USER_NAME = "admin";
    private static final String ADMIN_USER_PASSWORD = "admin";
    private static final String IMAP_FOLDER_NAME = "test";

    private Session session = null;
    private Store store = null;
    private IMAPFolder folder = null;

    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private ServiceRegistry serviceRegistry;
    private TransactionService transactionService;
    private NodeService nodeService;
    private ImporterService importerService;
    private PersonService personService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private FileFolderService fileFolderService;
    private MutableAuthenticationService authenticationService;
    private AlfrescoImapServer imapServer;

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
        logger.debug("In SetUp");
        serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
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
        imapServer = (AlfrescoImapServer) imapCtx.getBean("imapServer");
        
        if(!imapServer.isImapServerEnabled())
        {
            imapServer.setImapServerEnabled(true);
            imapServer.setHost(HOST);
            imapServer.setPort(PORT);
            imapServer.startup();
        }

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
        //this.store.connect(HOST, PORT, anotherUserName, anotherUserName);
        this.store.connect(imapServer.getHost(), imapServer.getPort(), anotherUserName, anotherUserName);

        // Get folder
        folder = (IMAPFolder) store.getFolder(TEST_FOLDER);
        folder.open(Folder.READ_ONLY);
        
        logger.debug("End SetUp");

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

    public void dontTestMessageCache() throws Exception
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

    public void testEncodedFromToAddresses() throws Exception
    {
        // RFC1342
        String addressString = "ars.kov@gmail.com";
        String personalString = "�?р�?ений Ковальчук";
        InternetAddress address = new InternetAddress(addressString, personalString, "UTF-8");
        
        // Following method returns the address with quoted personal aka <["�?р�?ений Ковальчук"] <ars.kov@gmail.com>>
        // NOTE! This should be coincided with RFC822MetadataExtracter. Would 'addresses' be quoted or not? 
        // String decodedAddress = address.toUnicodeString();
        // So, just using decode, for now
        String decodedAddress = MimeUtility.decodeText(address.toString());
        
        // InternetAddress.toString(new Address[] {address}) - is used in the RFC822MetadataExtracter
        // So, compare with that
        assertFalse("Non ASCII characters in the address should be encoded", decodedAddress.equals(InternetAddress.toString(new Address[] {address})));
        
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, false, "UTF-8");
        
        messageHelper.setText("This is a sample message for ALF-5647");
        messageHelper.setSubject("This is a sample message for ALF-5647");
        messageHelper.setFrom(address);
        messageHelper.addTo(address);
        messageHelper.addCc(address);
        
        // Creating the message node in the repository
        String name = AlfrescoImapConst.MESSAGE_PREFIX + GUID.generate();
        FileInfo messageFile = fileFolderService.create(testImapFolderNodeRef, name, ContentModel.TYPE_CONTENT);
        // Writing a content.
        new IncomingImapMessage(messageFile, serviceRegistry, message);
        
        // Getting the transformed properties from the repository
        // cm:originator, cm:addressee, cm:addressees, imap:messageFrom, imap:messageTo, imap:messageCc
        Map<QName, Serializable> properties = nodeService.getProperties(messageFile.getNodeRef());
        
        String cmOriginator = (String) properties.get(ContentModel.PROP_ORIGINATOR);
        String cmAddressee = (String) properties.get(ContentModel.PROP_ADDRESSEE);
        // TODO cm:addressees is returned as list
        //String cmAddressees = (String) properties.get(ContentModel.PROP_ADDRESSEES);
        String imapMessageFrom = (String) properties.get(ImapModel.PROP_MESSAGE_FROM);
        String imapMessageTo = (String) properties.get(ImapModel.PROP_MESSAGE_TO);
        String imapMessageCc = (String) properties.get(ImapModel.PROP_MESSAGE_CC);
        
        assertNotNull(cmOriginator);
        assertEquals(decodedAddress, cmOriginator);
        assertNotNull(cmAddressee);
        assertEquals(decodedAddress, cmAddressee);
        //assertNotNull(cmAddressees);
        //assertEquals(cmAddressees, encodedAddress);
        assertNotNull(imapMessageFrom);
        assertEquals(decodedAddress, imapMessageFrom);
        assertNotNull(imapMessageTo);
        assertEquals(decodedAddress, imapMessageTo);
        assertNotNull(imapMessageCc);
        assertEquals(decodedAddress, imapMessageCc);
    }
    
    public void testEightBitMessage() throws Exception
    {

        Store lstore = session.getStore(PROTOCOL);
        lstore.connect(imapServer.getHost(), imapServer.getPort(), ADMIN_USER_NAME, ADMIN_USER_PASSWORD);

        String folderName = "Alfresco IMAP/" + IMAP_FOLDER_NAME;
        
        UserTransaction txn = transactionService.getUserTransaction();
        
        IMAPFolder lfolder = (IMAPFolder) lstore.getFolder(folderName);
        lfolder.open(Folder.READ_WRITE);
        


        InputStream messageFileInputStream1 = null;
        InputStream messageFileInputStream2 = null;
        try
        {
            ClassPathResource fileResource = new ClassPathResource("imap/test-8bit-message.eml");
            messageFileInputStream1 = new FileInputStream(fileResource.getFile());
            Message message = new MimeMessage(Session.getDefaultInstance(new Properties()), messageFileInputStream1);
            String subject = message.getSubject();
            
            // get original bytes for further comparation
            messageFileInputStream2 = new FileInputStream(fileResource.getFile());
            byte[] original = ASCIIUtility.getBytes(messageFileInputStream2);
            
            Message[] messages = {message};
            
            lfolder.appendMessages(messages);
            
            
            
            // The search is not implemented. 
            // SearchTerm term = new HeaderTerm("X-Alfresco-Unique", "test8bit");
            // messages = folder.search(term);
            
            // So wee need to get our test message's UID from the repo
            
            String messageXPath = companyHomePathInStore + "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + IMAP_FOLDER_NAME + "/*[like(@cm:title, $cm:title, true)]";
            
            QueryParameterDefinition[] params = new QueryParameterDefinition[1];
            params[0] = new QueryParameterDefImpl(
                  ContentModel.PROP_TITLE,
                  serviceRegistry.getDictionaryService().getDataType(DataTypeDefinition.TEXT),
                  true,
                  subject);

            List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, messageXPath, params, namespaceService, true);
            
            
            // does the message exist
            assertEquals(1, nodeRefs.size());
            
            NodeRef messageNodeRef = nodeRefs.get(0);
            
            // get message UID
            Long dbid = (Long) nodeService.getProperty(messageNodeRef, ContentModel.PROP_NODE_DBID);
            
            // fetch the massage
            RFC822DATA data = getRFC822Message(lfolder, dbid);
            
            assertNotNull("Can't fetch a message from the repositiry", data);
            
            byte[] processed = ASCIIUtility.getBytes(data.getByteArrayInputStream());
            
            assertTrue("Original message doesn't coincide to the message processed by the repository", Arrays.equals(original, processed));
        }
        finally
        {
            if (messageFileInputStream1 != null) messageFileInputStream1.close();
            if (messageFileInputStream2 != null) messageFileInputStream2.close();
        }
            
        // close connection
        lfolder.close(true);
        lstore.close();
        
    }

    
    private static RFC822DATA getRFC822Message(final IMAPFolder folder, final long uid) throws MessagingException
    {
        return (RFC822DATA) folder.doCommand(new IMAPFolder.ProtocolCommand()
        {
            public Object doCommand(IMAPProtocol p) throws ProtocolException
            {
                Response[] r = p.command("UID FETCH " + uid + " (RFC822)", null);
                logResponse(r);
                Response response = r[r.length - 1];
                if (!response.isOK())
                {
                    throw new ProtocolException("Unable to retrieve message in RFC822 format");
                }

                FetchResponse fetchResponse = (FetchResponse) r[0];
                return fetchResponse.getItem(RFC822DATA.class);
            }
        });
       
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
                String command = "FETCH " + msn + " (UID)";
                Response[] r = p.command(command, null);
                logResponse(r);
                Response response = r[r.length - 1];

                // Grab response
                if (!response.isOK())
                {
                    throw new ProtocolException("Unable to retrieve message UID");
                }
                
                for(int i = 0 ; i < r.length; i++)
                {
                    if(r[i] instanceof FetchResponse)
                    {
                        FetchResponse fetchResponse = (FetchResponse) r[0];
                        UID uid = (UID) fetchResponse.getItem(UID.class);
                        logger.debug("MSGNO=" + uid.msgno + ", UID="+uid.uid);
                        return uid.uid;
                    }
                }
                
                /**
                  * Uh-oh - this is where we would intermittently fall over with a class cast exception.
                  * The following code probes why we don't have a FetchResponse
                  */
                StringBuffer sb = new StringBuffer();
                sb.append("command="+command);
                sb.append('\n');
                sb.append("resp length=" + r.length);
                sb.append('\n');
                for(int i = 0 ; i < r.length; i++)
                {
                    logger.error(r[i]);
                    sb.append("class=" + r[i].getClass().getName());
                    IMAPResponse unexpected = (IMAPResponse)r[i];
                    sb.append("key=" + unexpected.getKey());
                    sb.append("number=" + unexpected.getNumber());
                    sb.append("rest=" + unexpected.getRest());
                 
                    sb.append("r[" + i + "]=" + r[i] + '\n');
                }
                throw new ProtocolException("getMessageUid: "+ sb.toString());
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
        }
    }

    @Override
    public void tearDown() throws Exception
    {
        // Deleting created test environment
        logger.debug("tearDown ");

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
        logger.debug("tearDown end");
    }

}
