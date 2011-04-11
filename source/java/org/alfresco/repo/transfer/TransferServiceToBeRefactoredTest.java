/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferEventReport;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.ResourceUtils;

/**
 * Unit test for TransferServiceImpl
 * 
 * Contains some integration tests for the transfer service
 * 
 * These tests need their transaction management to be re-factored then they can be re-instated into
 * TransferServiceImplTest
 *
 * @author Mark Rogers
 */
@SuppressWarnings("deprecation")
public class TransferServiceToBeRefactoredTest extends BaseAlfrescoSpringTest 
{
    private TransferService transferService;
    private ContentService contentService;
    private TransferServiceImpl2 transferServiceImpl;
    private SearchService searchService;
    private TransactionService transactionService;
    private TransferReceiver receiver;
    private TransferManifestNodeFactory transferManifestNodeFactory; 
    private PermissionService permissionService;
    private LockService lockService;
    private PersonService personService;
    private DescriptorService descriptorService;
    private CopyService copyService;
    private Descriptor serverDescriptor;
    
    String COMPANY_HOME_XPATH_QUERY = "/{http://www.alfresco.org/model/application/1.0}company_home";
    String GUEST_HOME_XPATH_QUERY = "/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/application/1.0}guest_home";

    String REPO_ID_A = "RepoIdA";
    String REPO_ID_B;
    String REPO_ID_C = "RepoIdC";
    
    @Override
    public void runBare() throws Throwable
    {
        preventTransaction();
        super.runBare();
    }

    /**
     * Called during the transaction setup
     */
    protected void onSetUp() throws Exception
    {
        // Catch transactions left dangling by inadequate transaction handling during test failures
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            fail("Dangling transaction at start of test.");
        }
        
        super.onSetUp();
        
        // Get the required services
        this.transferService = (TransferService)this.applicationContext.getBean("TransferService");
        this.contentService = (ContentService)this.applicationContext.getBean("ContentService");
        this.transferServiceImpl = (TransferServiceImpl2)this.applicationContext.getBean("transferService2");
        this.searchService = (SearchService)this.applicationContext.getBean("SearchService");
        this.transactionService = (TransactionService)this.applicationContext.getBean("TransactionService");
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");
        this.authenticationService = (MutableAuthenticationService) this.applicationContext.getBean("authenticationService");
        this.actionService = (ActionService)this.applicationContext.getBean("actionService");
        this.permissionService = (PermissionService)this.applicationContext.getBean("permissionService");
        this.receiver = (TransferReceiver)this.applicationContext.getBean("transferReceiver");
        this.transferManifestNodeFactory = (TransferManifestNodeFactory)this.applicationContext.getBean("transferManifestNodeFactory");
        this.authenticationComponent = (AuthenticationComponent) this.applicationContext.getBean("authenticationComponent");
        this.lockService = (LockService) this.applicationContext.getBean("lockService");
        this.personService = (PersonService)this.applicationContext.getBean("PersonService");
        this.descriptorService = (DescriptorService)this.applicationContext.getBean("DescriptorService");
        this.copyService = (CopyService)this.applicationContext.getBean("CopyService");
        this.serverDescriptor = descriptorService.getServerDescriptor();
        
        REPO_ID_B = descriptorService.getCurrentRepositoryDescriptor().getId();
        
        authenticationComponent.setSystemUserAsCurrentUser();
        setTransactionDefinition(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        assertNotNull("receiver is null", this.receiver);     
    }
    
   /**
     * Test the transfer report.
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testTransferReport() throws Exception
    {
        setDefaultRollback(false);
        
        /**
          * Get guest home
          */
        String guestHomeQuery = "/app:company_home/app:guest_home";
        ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
        assertEquals("", 1, guestHomeResult.length());
        final NodeRef guestHome = guestHomeResult.getNodeRef(0);

        /**
          *  For unit test 
          *  - replace the HTTP transport with the in-process transport
          *  - replace the node factory with one that will map node refs, paths etc.
          */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(this.receiver, this.contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));
        
        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);
        
        /**
          * Now go ahead and create our first transfer target
          * This needs to be committed before we can call transfer asycnc.
          */
        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_NAME_A = "Report Node A";
        final String CONTENT_NAME_B = "Report Node B";
        final Locale CONTENT_LOCALE = Locale.GERMAN;
        final String CONTENT_STRING = "Hello";

        class TestData
        {
            NodeRef nodeRefA;
            NodeRef nodeRefB;
            NodeRef testFolder;
            NodeRef transferReport;
            NodeRef transferDestReport;
        }
        final TestData testData = new TestData();

        final String targetName = "testTransferReport";

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                {
                    String name = GUID.generate();
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_FOLDER);
                    testData.testFolder = child.getChildRef();
                    nodeService.setProperty(testData.testFolder, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.testFolder, ContentModel.PROP_NAME, name);
                }

                {
                    /**
                     * Create a test node that we will read and write
                     */
                    ChildAssociationRef child = nodeService.createNode(testData.testFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(GUID.generate()), ContentModel.TYPE_CONTENT);
                    testData.nodeRefA = child.getChildRef();
                    nodeService.setProperty(testData.nodeRefA, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.nodeRefA, ContentModel.PROP_NAME, CONTENT_NAME_A);

                    ContentWriter writer = contentService.getWriter(testData.nodeRefA, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }

                {
                    ChildAssociationRef child = nodeService.createNode(testData.testFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(GUID.generate()), ContentModel.TYPE_CONTENT);
                    testData.nodeRefB = child.getChildRef();
                    nodeService.setProperty(testData.nodeRefB, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.nodeRefB, ContentModel.PROP_NAME, CONTENT_NAME_B);

                    ContentWriter writer = contentService.getWriter(testData.nodeRefB, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }

                /**
                 * Now go ahead and create our first transfer target
                 */
                if (!transferService.targetExists(targetName))
                {
                    createTransferTarget(targetName);
                }
                return null;
            }
        });

        /**
         * Step 1.
         * Call the transfer method. to get a failed transfer - orphan nodes exist
         */
        setDefaultRollback(true);
        startNewTransaction();
        try
        {
            TestTransferCallback callback = new TestTransferCallback();
            Set<TransferCallback> callbacks = new HashSet<TransferCallback>();
            callbacks.add(callback);
            TransferDefinition definition = new TransferDefinition();
            Set<NodeRef> nodes = new HashSet<NodeRef>();
            nodes.add(testData.nodeRefA);
            nodes.add(testData.nodeRefB);
            // missing the folder node (testFolder)
            definition.setNodes(nodes);

            // Do the transfer here

            try
            {
                transferService.transfer(targetName, definition, callbacks);
                fail("transfer should have failed with an orphan not found exception");
            }
            catch (TransferException te)
            {
                logger.debug("deliberatly caught and ignored exception");
            }

            // Can't dirty read transfer report here

            boolean foundSourceReport = false;
            boolean foundDestReport = false;

            for (TransferEvent event : callback.getEvents())
            {
                if (event instanceof TransferEventReport)
                {
                    TransferEventReport reportEvent = (TransferEventReport) event;
                    switch (reportEvent.getReportType())
                    {
                    case DESTINATION:
                        foundDestReport = true;
                        testData.transferDestReport = reportEvent.getNodeRef();
                        assertNotNull("dest transfer nodeId null", testData.transferDestReport);
                        break;

                    case SOURCE:
                        foundSourceReport = true;
                        testData.transferReport = reportEvent.getNodeRef();
                        break;
                    }
                }
            }

            assertTrue("source report not found", foundSourceReport);
            assertTrue("dest report not found", foundDestReport);
        }
        finally
        {
            endTransaction();
        }

        setDefaultRollback(false);

        /**
         * Now validate the client side error transfer report against the xsd file
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ContentReader reader = contentService.getReader(testData.transferReport, ContentModel.PROP_CONTENT);
                assertNotNull("transfer reader is null", reader);
                assertEquals("client report mimetype not set", reader.getMimetype(), MimetypeMap.MIMETYPE_XML);
                String name = (String) nodeService.getProperty(testData.transferReport, ContentModel.PROP_NAME);
                assertTrue("client report does not end with .xml", name.endsWith(".xml"));

                logger.debug("This report should have failed");
                if (logger.isDebugEnabled())
                {
                    dumpToSystemOut(testData.transferReport);
                }

                // Now validate the client side transfer report against the XSD
                Source transferReportSource = new StreamSource(reader.getContentInputStream());
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                final String TRANSFER_REPORT_SCHEMA_LOCATION = "classpath:org/alfresco/repo/transfer/report/TransferReport2.xsd";
                Schema schema = sf.newSchema(ResourceUtils.getURL(TRANSFER_REPORT_SCHEMA_LOCATION));
                Validator validator = schema.newValidator();
                try
                {
                    validator.validate(transferReportSource);
                }
                catch (Exception e)
                {
                    fail(e.getMessage());
                }
                return null;
            }
        });

       /**
         * Step 2
         * Call the transfer method to get a good success transfer report
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                {
                    TestTransferCallback callback = new TestTransferCallback();
                    Set<TransferCallback> callbacks = new HashSet<TransferCallback>();
                    callbacks.add(callback);
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.nodeRefA);
                    nodes.add(testData.nodeRefB);
                    nodes.add(testData.testFolder);
                    definition.setNodes(nodes);

                    testData.transferReport = transferService.transfer(targetName, definition, callbacks);
                    assertNotNull("transfer report is null", testData.transferReport);
                    // Can't dirty read transfer report here

                    boolean foundSourceReport = false;
                    boolean foundDestReport = false;

                    for (TransferEvent event : callback.getEvents())
                    {
                        if (event instanceof TransferEventReport)
                        {
                            TransferEventReport reportEvent = (TransferEventReport) event;
                            switch (reportEvent.getReportType())
                            {
                            case DESTINATION:
                                foundDestReport = true;
                                testData.transferDestReport = reportEvent.getNodeRef();
                                assertNotNull("dest transfer nodeId null", testData.transferDestReport);
                                assertFalse("dest transfer nodeId not correct", testData.transferReport.equals(testData.transferDestReport));
                                break;

                            case SOURCE:
                                foundSourceReport = true;

                                assertEquals("source transfer nodeId not correct", testData.transferReport, reportEvent.getNodeRef());
                                break;
                            }
                        }
                    }

                    assertTrue("source report not found", foundSourceReport);
                    assertTrue("dest report not found", foundDestReport);
                }
                return null;
            }
        });

        /**
         * Now validate the client side transfer report against the xsd file
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ContentReader reader = contentService.getReader(testData.transferReport, ContentModel.PROP_CONTENT);
                assertNotNull("transfer reader is null", reader);

                logger.debug("This report should succeed");
                if (logger.isDebugEnabled())
                {
                    dumpToSystemOut(testData.transferReport);
                }

                // Now validate the client side transfer report against the XSD
                Source transferReportSource = new StreamSource(reader.getContentInputStream());
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                final String TRANSFER_REPORT_SCHEMA_LOCATION = "classpath:org/alfresco/repo/transfer/report/TransferReport2.xsd";
                Schema schema = sf.newSchema(ResourceUtils.getURL(TRANSFER_REPORT_SCHEMA_LOCATION));
                Validator validator = schema.newValidator();
                try
                {
                    validator.validate(transferReportSource);
                }
                catch (Exception e)
                {
                    fail(e.getMessage());
                }
                return null;
            }
        });
        /**
         * Now validate the destination side transfer report against its xsd file
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ContentReader reader = contentService.getReader(testData.transferDestReport, ContentModel.PROP_CONTENT);
                assertNotNull("transfer reader is null", reader);

                assertEquals("dest report mimetype not set", reader.getMimetype(), MimetypeMap.MIMETYPE_XML);
                String name = (String) nodeService.getProperty(testData.transferReport, ContentModel.PROP_NAME);
                assertTrue("dest report does not end with .xml", name.endsWith(".xml"));

                if (logger.isDebugEnabled())
                {
                    dumpToSystemOut(testData.transferDestReport);
                }

                // Now validate the destination side transfer report against the XSD
                Source transferReportSource = new StreamSource(reader.getContentInputStream());
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                final String TRANSFER_REPORT_SCHEMA_LOCATION = "classpath:org/alfresco/repo/transfer/reportd/TransferDestinationReport.xsd";
                Schema schema = sf.newSchema(ResourceUtils.getURL(TRANSFER_REPORT_SCHEMA_LOCATION));
                Validator validator = schema.newValidator();
                try
                {
                    validator.validate(transferReportSource);
                }
                catch (Exception e)
                {
                    fail("Destination Transfer Report " + e.getMessage());
                }
                return null;
            }
        });
        /**
         * Now validate all transfer reports.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {

                String query = "TYPE:\"trx:transferReportDest\"";
                ResultSet results = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, query);

                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                final String TRANSFER_REPORT_SCHEMA_LOCATION = "classpath:org/alfresco/repo/transfer/reportd/TransferDestinationReport.xsd";
                Schema schema = sf.newSchema(ResourceUtils.getURL(TRANSFER_REPORT_SCHEMA_LOCATION));
                Validator validator = schema.newValidator();

                for (ResultSetRow result : results)
                {
                    NodeRef reportNode = result.getNodeRef();

                    logger.debug("validating  reportNode " + reportNode);
                    // Now validate the destination side transfer report against the XSD
                    ContentReader reader = contentService.getReader(reportNode, ContentModel.PROP_CONTENT);
                    assertNotNull("transfer reader is null", reader);
                    if (reader.getMimetype().equals(MimetypeMap.MIMETYPE_XML))
                    {
                        Source transferReportSource = new StreamSource(reader.getContentInputStream());
                        try
                        {
                            validator.validate(transferReportSource);
                        }
                        catch (Exception e)
                        {
                            fail("Destination Transfer Report reportNode:" + reportNode + " message :" + e.getMessage());
                        }
                    }
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {

                logger.debug("now delete the target:" + targetName);

                transferService.deleteTransferTarget(targetName);
                return null;
            }
        });
    } // test transfer report

    private void dumpToSystemOut(NodeRef nodeRef) throws IOException
    {
        ContentReader reader2 = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        assertNotNull("transfer reader is null", reader2);
        InputStream is = reader2.getContentInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String s = br.readLine();
        while (s != null)
        {
            System.out.println(s);
            s = br.readLine();
        }
    }

    private UnitTestTransferManifestNodeFactory unitTestKludgeToTransferGuestHomeToCompanyHome()
    {
        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(this.receiver, this.contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        return testNodeFactory;

    }

    /**
     * Test the transfer method behaviour with respect to sync folders - sending a complete set 
     * of nodes and implying delete from the absence of an association.
     * 
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     * 
     * Tree of nodes
     * 
     *      A1
     *   |      |    
     *   A2     A3 (Content Node)   
     *   |
     * A4 A5 (Content Node) 
     * 
     * Test steps - 
     * 1 add A1
     *   transfer(sync)
     * 2 add A2, A3, A4, A5
     *   transfer(sync)
     * 3 remove A2 
     *   transfer(sync) A4 and A5 should cascade delete on source
     * 4 remove A3
     *   transfer(sync)
     * 5 add back A3 - new node ref
     *   transfer(sync)
     * 6 add A2, A4, A5
     *   transfer(sync)
     * 7 test delete and restore of a single node
     *   remove A3 .  
     *   transfer         
     *   restore node A3
     *   transfer
     * 8 test delete and restore of a tree of nodes
     *   remove A2 (A4 and A5 should cascade delete on source as well)
     *   transfer
     *   restore node A2 (and A4 and A5 cascade restore)
     *   transfer
     */
    public void testTransferSyncNodes() throws Exception
    {
        setDefaultRollback(false);

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.GERMAN;
        final String CONTENT_STRING = "Hello";

        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        /**
         * Now go ahead and create our first transfer target
         */
        final String targetName = "testTransferSyncNodes";
        class TestData
        {
            TransferTarget transferMe;
            NodeRef A1NodeRef;
            NodeRef A2NodeRef;
            NodeRef A3NodeRef;
            NodeRef A4NodeRef;
            NodeRef A5NodeRef;
            NodeRef destNodeRef;
        }

        final TestData testData = new TestData();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0);

                /**
                 * Create a test nodes A1 through A5 that we will read and write
                 */
                {
                    // Node A1
                    String name = GUID.generate();
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_FOLDER);
                    testData.A1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_NAME, name);
                }

                {
                    // Node A2
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A2"), ContentModel.TYPE_FOLDER);
                    testData.A2NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_NAME, "A2");
                }

                {
                    // Node A3
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A3"), ContentModel.TYPE_CONTENT);
                    testData.A3NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_NAME, "A3");

                    ContentWriter writer = contentService.getWriter(testData.A3NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }
                {
                    // Node A4
                    ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A4"), ContentModel.TYPE_CONTENT);
                    testData.A4NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_NAME, "A4");

                    ContentWriter writer = contentService.getWriter(testData.A4NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }
                {
                    // Node A5
                    ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A5"), ContentModel.TYPE_CONTENT);
                    testData.A5NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_NAME, "A5");

                    ContentWriter writer = contentService.getWriter(testData.A5NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }

                // Create the transfer target if it does not already exist
                if (!transferService.targetExists(targetName))
                {
                    testData.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    testData.transferMe = transferService.getTransferTarget(targetName);
                }
                return null;
            }
        });

        /**
         * Step 1. Add Node A1.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Node A with no children
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                assertFalse("unit test stuffed up - comparing with self", testData.destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node ref does not exist", nodeService.exists(testData.destNodeRef));
                assertEquals("title is wrong", (String) nodeService.getProperty(testData.destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE);
                assertEquals("type is wrong", nodeService.getType(testData.A1NodeRef), nodeService.getType(testData.destNodeRef));

                // Check injected transferred aspect.
                assertNotNull("transferredAspect", (String) nodeService.getProperty(testData.destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                return null;
            }
        });
        /**
         * Step 2. Add Node A2, A3, A4, A5.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Node A 1-5
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    nodes.add(testData.A2NodeRef);
                    nodes.add(testData.A3NodeRef);
                    nodes.add(testData.A4NodeRef);
                    nodes.add(testData.A5NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                assertFalse("unit test stuffed up - comparing with self", testData.destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node ref A1 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A1NodeRef)));
                assertTrue("dest node ref A2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A2NodeRef)));
                assertTrue("dest node ref A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertTrue("dest node ref A4 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A4NodeRef)));
                assertTrue("dest node ref A5 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));

                // Check injected transferred aspect.
                assertNotNull("transferredAspect", (String) nodeService.getProperty(testData.destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                return null;
            }
        });
        /**
         * Step 3 - remove folder node A2
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(testData.A2NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Node A 1-5
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    // nodes.add(A2NodeRef);
                    nodes.add(testData.A3NodeRef);
                    // nodes.add(A4NodeRef);
                    // nodes.add(A5NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                assertFalse("unit test stuffed up - comparing with self", testData.destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node ref A1 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A1NodeRef)));
                assertFalse("dest node ref A2 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A2NodeRef)));
                assertTrue("dest node ref A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertFalse("dest node ref A4 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A4NodeRef)));
                assertFalse("dest node ref A5 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));

                // Check injected transferred aspect.
                assertNotNull("transferredAspect", (String) nodeService.getProperty(testData.destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                return null;
            }
        });
        /**
         * Step 4 - remove content node A3
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(testData.A3NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Node A 1-5
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    // nodes.add(A2NodeRef);
                    // nodes.add(A3NodeRef);
                    // nodes.add(A4NodeRef);
                    // nodes.add(A5NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                assertFalse("unit test stuffed up - comparing with self", testData.destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node ref A1 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A1NodeRef)));
                assertFalse("dest node ref A2 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A2NodeRef)));
                assertFalse("dest node ref A3 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertFalse("dest node ref A4 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A4NodeRef)));
                assertFalse("dest node ref A5 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));

                // Check injected transferred aspect.
                assertNotNull("transferredAspect", (String) nodeService.getProperty(testData.destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                return null;
            }
        });
        /**
         * Step 5. Add back A3.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A3"), ContentModel.TYPE_CONTENT);
                testData.A3NodeRef = child.getChildRef();
                nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_NAME, "A3");

                ContentWriter writer = contentService.getWriter(testData.A3NodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Node A 1-5
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    // nodes.add(A2NodeRef);
                    nodes.add(testData.A3NodeRef);
                    // nodes.add(A4NodeRef);
                    // nodes.add(A5NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                assertFalse("unit test stuffed up - comparing with self", testData.destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node ref A1 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A1NodeRef)));
                assertFalse("dest node ref A2 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A2NodeRef)));
                assertTrue("dest node A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertFalse("dest node ref A4 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A4NodeRef)));
                assertFalse("dest node ref A5 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));

                // Check injected transferred aspect.
                assertNotNull("transferredAspect", (String) nodeService.getProperty(testData.destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                return null;
            }
        });
        /**
         * Step 6. add A2, A4, A5
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {

                {
                    // Node A2
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A2"), ContentModel.TYPE_FOLDER);
                    testData.A2NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_NAME, "A2");
                }

                {
                    // Node A4
                    ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A4"), ContentModel.TYPE_CONTENT);
                    testData.A4NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_NAME, "A4");

                    ContentWriter writer = contentService.getWriter(testData.A4NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }
                {
                    // Node A5
                    ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A5"), ContentModel.TYPE_CONTENT);
                    testData.A5NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_NAME, "A5");

                    ContentWriter writer = contentService.getWriter(testData.A5NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Node A 1-5
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    nodes.add(testData.A2NodeRef);
                    nodes.add(testData.A3NodeRef);
                    nodes.add(testData.A4NodeRef);
                    nodes.add(testData.A5NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                assertFalse("unit test stuffed up - comparing with self", testData.destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node A1 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A1NodeRef)));
                assertTrue("dest node A2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A2NodeRef)));
                assertTrue("dest node A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertTrue("dest node A4 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A4NodeRef)));
                assertTrue("dest node A5 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));

                // Check injected transferred aspect.
                assertNotNull("transferredAspect", (String) nodeService.getProperty(testData.destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                return null;
            }
        });
        /** 
         * Step 7 - test delete and restore of a single node
         * remove A3 .  
         * transfer         
         * restore node A3
         * transfer
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                logger.debug("Step 7 - delete node A3");
                nodeService.deleteNode(testData.A3NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Node A 1-5
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    nodes.add(testData.A2NodeRef);
                    // nodes.add(testData.A3NodeRef); A3 has gone.
                    nodes.add(testData.A4NodeRef);
                    nodes.add(testData.A5NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node ref A1 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A1NodeRef)));
                assertTrue("dest node ref A2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A2NodeRef)));
                assertFalse("dest node ref A3 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertTrue("dest node ref A4 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A4NodeRef)));
                assertTrue("dest node ref A5 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef archivedNode = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, testData.A3NodeRef.getId());
                NodeRef newNodeRef = nodeService.restoreNode(archivedNode, testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A3"));
                assertEquals("restored node ref is different", newNodeRef, testData.A3NodeRef);
                logger.debug("Step 7 - restore node A3");
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Node A 1-5. (So we are seeing what happens to node 3 on the target
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    nodes.add(testData.A2NodeRef);
                    nodes.add(testData.A3NodeRef);
                    nodes.add(testData.A4NodeRef);
                    nodes.add(testData.A5NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                assertFalse("unit test stuffed up - comparing with self", testData.destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node ref A1 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A1NodeRef)));
                assertTrue("dest node ref A2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A2NodeRef)));
                assertTrue("dest node ref A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertTrue("dest node ref A4 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A4NodeRef)));
                assertTrue("dest node ref A5 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));
                return null;
            }
        });
        /** 
         * Step 8 - test delete and restore of a tree
         * remove A2 (A4, A5) should cascade delete.  
         * transfer         
         * restore node A2
         * transfer
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(testData.A2NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Node A 1-5
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    // nodes.add(testData.A2NodeRef);
                    nodes.add(testData.A3NodeRef);
                    // nodes.add(testData.A4NodeRef);
                    // nodes.add(testData.A5NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                assertFalse("unit test stuffed up - comparing with self", testData.destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node ref A1 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A1NodeRef)));
                assertFalse("dest node ref A2 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A2NodeRef)));
                assertTrue("dest node ref A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertFalse("dest node ref A4 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A4NodeRef)));
                assertFalse("dest node ref A5 has not been deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef archivedNode = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, testData.A2NodeRef.getId());
                nodeService.restoreNode(archivedNode, testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A2"));
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Node A 1-5. (So we are seeing what happens to node 2, 4, 5 on the target
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    nodes.add(testData.A2NodeRef);
                    nodes.add(testData.A3NodeRef);
                    nodes.add(testData.A4NodeRef);
                    nodes.add(testData.A5NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                assertFalse("unit test stuffed up - comparing with self", testData.destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node ref A1 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A1NodeRef)));
                assertTrue("dest node ref A2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A2NodeRef)));
                assertTrue("dest node ref A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertTrue("dest node ref A4 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A4NodeRef)));
                assertTrue("dest node ref A5 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));
                return null;
            }
        });
    }

    /**
     * Test the transfer method behaviour with respect to sync with (local) alien nodes.
     * 
     * So we have Repository A transferring content and Repository B is the local repo that we
     * add and delete alien nodes.
     * 
     * In general an alien node will prevent deletion of the parent folders
     * 
     * <pre>
     * Tree of nodes
     * 
     *      A1
     *      |      |                      | 
     *      A2     A3 (Content Node)      B9 (Alien Content Node)
     *      |
     *   A4 A5 B10 (Alien Content Node)   A6
     *   |                                |
     *   A7 B11 (Alien Content Node)      A8 B12 B13 (Alien Content Node)
     *                                        |
     *                                       B14
     * </pre>
     * Test steps -
     * <ol> 
     * <li>add A1, A2, A3, A4, A5, A6, A7, A8
     *   transfer(sync)</li>
     * <li>add Alien node B9.  A1 becomes Alien.</li>
     * <li>remove alien node B9.  A1 becomes non Alien.</li>
     * <li>add Alien node B10. A1 and A2 become Alien</li>
     * <li>remove Alien node B10.  A1 and A2 become non Alien</li>
     * <li>add B12 and B14 A6, A2, A1 becomes Alien</li>
     * <li>remove B14, B12, A6, A2, A1 remain Alien</li>
     * <li>add B13 A6, A2, A1 remains Alien</li>
     * <li>remove B13 A6, A2, A1 remains Alien</li>
     * <li>remove B12 A6, A2, A1 becomes non Alien.</li>
     * <li>add B9 and B10 A1 and A2 become Alien</li>
     * <li>remove B10 A2 becomes non alien A1 remains alien.</li>
     * <li>Add B11, delete A2
     * transfer sync</li>
     * (A5, A6, A7 and A8 should be deleted A2 and A4 remain since they contain alien content.)</li>
     * </ol>   
     */
    public void testTransferInvadedByLocalAlienNodes() throws Exception
    {
        setDefaultRollback(false);

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.JAPAN;
        final String CONTENT_STRING = "Hello";

        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         *
         * Mock the transfer service to be from Repo A
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));

        final String localRepositoryId = descriptorService.getCurrentRepositoryDescriptor().getId();

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        /**
         * Now go ahead and create our first transfer target
         */
        final String targetName = "testSyncWithAlienNodes";

        class TestData
        {
            TransferTarget transferMe;
            NodeRef A1NodeRef;
            NodeRef A2NodeRef;
            NodeRef A3NodeRef;
            NodeRef A4NodeRef;
            NodeRef A5NodeRef;
            NodeRef A6NodeRef;
            NodeRef A7NodeRef;
            NodeRef A8NodeRef;
            NodeRef B9NodeRef;
            NodeRef B10NodeRef;
            NodeRef B11NodeRef;
            NodeRef B12NodeRef;
            NodeRef B13NodeRef;
            NodeRef B14NodeRef;
            NodeRef destNodeRef;
        }
        final TestData testData = new TestData();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0);

                /**
                 * Create a test nodes A1 through A8 that we will read and write
                 */
                {
                    // Node A1
                    String name = GUID.generate();
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_FOLDER);
                    testData.A1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_NAME, name);
                }

                {
                    // Node A2
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A2"), ContentModel.TYPE_FOLDER);
                    testData.A2NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_NAME, "A2");
                }

                {
                    // Node A3
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A3"), ContentModel.TYPE_CONTENT);
                    testData.A3NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_NAME, "A3");

                    ContentWriter writer = contentService.getWriter(testData.A3NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }
                {
                    // Node A4
                    ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A4"), ContentModel.TYPE_FOLDER);
                    testData.A4NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_NAME, "A4");
                }
                {
                    // Node A5
                    ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A5"), ContentModel.TYPE_CONTENT);
                    testData.A5NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_NAME, "A5");

                    ContentWriter writer = contentService.getWriter(testData.A5NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }

                {
                    // Node A6
                    ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A6"), ContentModel.TYPE_FOLDER);
                    testData.A6NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A6NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A6NodeRef, ContentModel.PROP_NAME, "A6");
                }
                {
                    // Node A7
                    ChildAssociationRef child = nodeService.createNode(testData.A4NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A7"), ContentModel.TYPE_CONTENT);
                    testData.A7NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A7NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A7NodeRef, ContentModel.PROP_NAME, "A7");

                    ContentWriter writer = contentService.getWriter(testData.A7NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }
                {
                    // Node A8
                    ChildAssociationRef child = nodeService.createNode(testData.A6NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A8"), ContentModel.TYPE_CONTENT);
                    testData.A8NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A8NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A8NodeRef, ContentModel.PROP_NAME, "A8");

                    ContentWriter writer = contentService.getWriter(testData.A8NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }

                // Create the transfer target if it does not already exist
                if (!transferService.targetExists(targetName))
                {
                    testData.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    testData.transferMe = transferService.getTransferTarget(targetName);
                }
                return null;
            }
        });

        /**
         * Step 1. add A1, A2, A3, A4, A5, A6, A7, A8 transfer(sync)
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Nodes A1 through A8
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    nodes.add(testData.A2NodeRef);
                    nodes.add(testData.A3NodeRef);
                    nodes.add(testData.A4NodeRef);
                    nodes.add(testData.A5NodeRef);
                    nodes.add(testData.A6NodeRef);
                    nodes.add(testData.A7NodeRef);
                    nodes.add(testData.A8NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                assertFalse("unit test stuffed up - comparing with self", A1destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node ref does not exist", nodeService.exists(A1destNodeRef));
                assertEquals("title is wrong", (String) nodeService.getProperty(A1destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE);
                assertEquals("type is wrong", nodeService.getType(testData.A1NodeRef), nodeService.getType(A1destNodeRef));
                assertFalse("A1 is alien", nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));

                // Check injected transferred aspect.
                assertNotNull("transferredAspect", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                return null;
            }
        });
        /**
         * Step 2 add Alien node B9 child of A1(dest). A1(dest) becomes Alien because it contains an alien child.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                ChildAssociationRef child = nodeService.createNode(testData.destNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B9"), ContentModel.TYPE_CONTENT);
                testData.B9NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B9NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.B9NodeRef, ContentModel.PROP_NAME, "B9");

                ContentWriter writer = contentService.getWriter(testData.B9NodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);

                assertTrue("dest node ref does not exist", nodeService.exists(A1destNodeRef));
                // Check injected transferred aspect.
                assertTrue("node A1 is not alien aspect", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertNotNull("repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                assertNotNull("from repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID));
                assertTrue("node B9 is not alien", (Boolean) nodeService.hasAspect(testData.B9NodeRef, TransferModel.ASPECT_ALIEN));

                // Temp code
                List<String> invaders = (List<String>) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_INVADED_BY);
                assertTrue("invaders contains local repository Id", invaders.contains(localRepositoryId));
                assertFalse("invaders contains REPO_ID_A", invaders.contains(REPO_ID_A));
                return null;
            }
        });
        /**
         * Step 3 remove alien node B9. A1 becomes non Alien.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                logger.debug("delete node B9");
                nodeService.deleteNode(testData.B9NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A3destNodeRef = testNodeFactory.getMappedNodeRef(testData.A3NodeRef);
                List<String> invaders = (List<String>) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_INVADED_BY);
                assertTrue("dest node ref does not exist", nodeService.exists(A1destNodeRef));
                assertFalse("node A1 is still alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertFalse("node A3 is alien", (Boolean) nodeService.hasAspect(A3destNodeRef, TransferModel.ASPECT_ALIEN));
                assertNotNull("repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                assertNotNull("from repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID));
                return null;
            }
        });
        /**
         * 4 add Alien node B10 child of A2. A1 and A2 become Alien
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);
                ChildAssociationRef child = nodeService.createNode(testData.destNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B10"), ContentModel.TYPE_CONTENT);
                testData.B10NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B10NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.B10NodeRef, ContentModel.PROP_NAME, "B10");

                ContentWriter writer = contentService.getWriter(testData.B10NodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);

                assertTrue("node A1 is not alien aspect", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A2 is not alien aspect", (Boolean) nodeService.hasAspect(A2destNodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * 5 remove Alien node B10. A1 and A2 become non Alien
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                logger.debug("delete node B10");
                nodeService.deleteNode(testData.B10NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);

                assertFalse("node A1 is still alien aspect", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertFalse("node A2 is still alien aspect", (Boolean) nodeService.hasAspect(A2destNodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * Step 6 add B12 (child of A6) and B14 A6, A2, A1 becomes Alien
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A6NodeRef);
                ChildAssociationRef child = nodeService.createNode(testData.destNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B12"), ContentModel.TYPE_FOLDER);
                testData.B12NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B12NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.B12NodeRef, ContentModel.PROP_NAME, "B12");

                child = nodeService.createNode(testData.B12NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B14"), ContentModel.TYPE_CONTENT);
                testData.B14NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B14NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.B14NodeRef, ContentModel.PROP_NAME, "B14");
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);
                NodeRef A6destNodeRef = testNodeFactory.getMappedNodeRef(testData.A6NodeRef);

                assertTrue("node A1 is not alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A2 is not alien", (Boolean) nodeService.hasAspect(A2destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A6 is not alien", (Boolean) nodeService.hasAspect(A6destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node B14 is not alien", (Boolean) nodeService.hasAspect(testData.B14NodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node B12 is not alien", (Boolean) nodeService.hasAspect(testData.B12NodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * Step 7 Delete B14. B12 remains alien
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(testData.B14NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);
                NodeRef A6destNodeRef = testNodeFactory.getMappedNodeRef(testData.A6NodeRef);

                assertTrue("node A1 is not alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A2 is not alien", (Boolean) nodeService.hasAspect(A2destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A6 is not alien", (Boolean) nodeService.hasAspect(A6destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node B12 is not alien", (Boolean) nodeService.hasAspect(testData.B12NodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * Step 8 add B13 A6, A2, A1 remains Alien
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A6NodeRef);
                ChildAssociationRef child = nodeService.createNode(testData.destNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B13"), ContentModel.TYPE_CONTENT);
                testData.B13NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B13NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.B13NodeRef, ContentModel.PROP_NAME, "B13");

                ContentWriter writer = contentService.getWriter(testData.B13NodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);
                NodeRef A6destNodeRef = testNodeFactory.getMappedNodeRef(testData.A6NodeRef);

                assertTrue("node A1 is not alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A2 is not alien", (Boolean) nodeService.hasAspect(A2destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A6 is not alien", (Boolean) nodeService.hasAspect(A6destNodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * Step 9 remove B13 A6, A2, A1 remains Alien Due to B12
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(testData.B13NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);
                NodeRef A6destNodeRef = testNodeFactory.getMappedNodeRef(testData.A6NodeRef);

                assertTrue("node A1 is not alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A2 is not alien", (Boolean) nodeService.hasAspect(A2destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A6 is not alien", (Boolean) nodeService.hasAspect(A6destNodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * Step 10 remove B12 A6, A2, A1 becomes non Alien.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(testData.B12NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);
                NodeRef A6destNodeRef = testNodeFactory.getMappedNodeRef(testData.A6NodeRef);

                assertFalse("node A1 is still alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertFalse("node A2 is still alien", (Boolean) nodeService.hasAspect(A2destNodeRef, TransferModel.ASPECT_ALIEN));
                assertFalse("node A6 is still alien", (Boolean) nodeService.hasAspect(A6destNodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * Step 11 add B9 and B10 A1 and A2 become Alien
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                ChildAssociationRef child = nodeService.createNode(testData.destNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B9"), ContentModel.TYPE_CONTENT);
                testData.B9NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B9NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.B9NodeRef, ContentModel.PROP_NAME, "B9");

                ContentWriter writer = contentService.getWriter(testData.B9NodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);

                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);
                child = nodeService.createNode(testData.destNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B10"), ContentModel.TYPE_CONTENT);
                testData.B10NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B10NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.B10NodeRef, ContentModel.PROP_NAME, "B10");

                writer = contentService.getWriter(testData.B10NodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);

                assertTrue("node A1 is not alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A2 is not alien", (Boolean) nodeService.hasAspect(A2destNodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });

        /**
         * Step 12 remove B10 A2 becomes non alien A1 remains alien.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(testData.B10NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);

                // BUGBUG
                assertTrue("node A1 is still alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertFalse("node A2 is still alien", (Boolean) nodeService.hasAspect(A2destNodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });

        /**
         * 13 Add Alien node B11.
         */
        logger.debug("Step 12 Add Node B11, Delete A2 and sync");
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A4NodeRef);
                ChildAssociationRef child = nodeService.createNode(testData.destNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B11"), ContentModel.TYPE_CONTENT);
                testData.B11NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B11NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.B11NodeRef, ContentModel.PROP_NAME, "B11");

                ContentWriter writer = contentService.getWriter(testData.B11NodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);

                nodeService.deleteNode(testData.A2NodeRef);
                return null;
            }
        });
        /**
         * Step 14 delete A2 (will cascade delete A4, A5, A6, A7, A8 transfer sync (A5, A6, A7, A8 and should be deleted
         * A2 and A4 remain since they contain alien content.)
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate A1, A2 and A4 are alien
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);
                NodeRef A4destNodeRef = testNodeFactory.getMappedNodeRef(testData.A4NodeRef);

                assertTrue("node A1 is not alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A2 is not alien", (Boolean) nodeService.hasAspect(A2destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A4 is not alien", (Boolean) nodeService.hasAspect(A4destNodeRef, TransferModel.ASPECT_ALIEN));

                assertFalse("test error: node A2 not deleted", nodeService.exists(testData.A2NodeRef));
                assertFalse("test error: node A4 not deleted", nodeService.exists(testData.A4NodeRef));
                assertFalse("test error: node A5 not deleted", nodeService.exists(testData.A5NodeRef));
                assertFalse("test error: node A6 not deleted", nodeService.exists(testData.A6NodeRef));
                assertFalse("test error: node A7 not deleted", nodeService.exists(testData.A7NodeRef));
                assertFalse("test error: node A8 not deleted", nodeService.exists(testData.A8NodeRef));

                assertTrue("test error: node does not exist", nodeService.exists(testData.A3NodeRef));

                /**
                 * Transfer Nodes A1 through A8
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    nodes.add(testData.A3NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);
                NodeRef A3destNodeRef = testNodeFactory.getMappedNodeRef(testData.A3NodeRef);
                NodeRef A4destNodeRef = testNodeFactory.getMappedNodeRef(testData.A4NodeRef);
                NodeRef A5destNodeRef = testNodeFactory.getMappedNodeRef(testData.A5NodeRef);
                NodeRef A6destNodeRef = testNodeFactory.getMappedNodeRef(testData.A6NodeRef);
                NodeRef A7destNodeRef = testNodeFactory.getMappedNodeRef(testData.A7NodeRef);
                NodeRef A8destNodeRef = testNodeFactory.getMappedNodeRef(testData.A8NodeRef);

                assertTrue("node A1 not alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A2 not alien", (Boolean) nodeService.hasAspect(A2destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node A4 not alien", (Boolean) nodeService.hasAspect(A4destNodeRef, TransferModel.ASPECT_ALIEN));
                assertTrue("node B11 does not exist", nodeService.exists(testData.B11NodeRef));

                assertTrue("node A3 deleted", nodeService.exists(A3destNodeRef));

                assertFalse("node A5 not deleted", nodeService.exists(A5destNodeRef));
                assertFalse("node A6 not deleted", nodeService.exists(A6destNodeRef));
                assertFalse("node A7 not deleted", nodeService.exists(A7destNodeRef));
                assertFalse("node A8 not deleted", nodeService.exists(A8destNodeRef));
                return null;
            }
        });
    }

    /**
     * Test restore of a local node.
     * <pre>
     * Tree of nodes
     *     A1   B1
     *     |
     *     B2
     *     |
     *     B3
     * <pre>
     * <ol>    
     *     <li>Add B2.   A1 is alien.</li>
     *     <li>Delete B2.  A1 not alien</li>
     *     <li>Restore B2. A1 is alien</li>
     *     <li>Add B3.  A1 is alien</li>
     *     <li>Delete B2.  A1 not alien</li>
     *     <li>Restore to B1.  B2 and B3 not alien.</li>
     * </ol>
     * @throws Exception
     */
    public void testLocalAlienRestore() throws Exception
    {
        setDefaultRollback(false);

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.JAPAN;
        final String CONTENT_STRING = "Hello";

        /**
         * Now go ahead and create our first transfer target
         */
        final String targetName = "testRestoreOfAlienNodes";

        class TestData
        {
            TransferTarget transferMe;
            NodeRef S0NodeRef;
            NodeRef A0NodeRef;
            NodeRef A1NodeRef;
            NodeRef B1NodeRef;
            NodeRef B2NodeRef;
            NodeRef B3NodeRef;
            NodeRef destNodeRef;
        }
        final TestData testData = new TestData();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0);

                /**
                 * Create a test nodes A1 through A8 that we will read and write
                 */
                {
                    // Node S0
                    String name = GUID.generate();
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_FOLDER);
                    testData.S0NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.S0NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.S0NodeRef, ContentModel.PROP_NAME, name);
                }
                {
                    // Node A1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A0"), ContentModel.TYPE_FOLDER);
                    testData.A0NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A0NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A0NodeRef, ContentModel.PROP_NAME, "A0");
                }
                {
                    // Node A1
                    ChildAssociationRef child = nodeService.createNode(testData.A0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A1"), ContentModel.TYPE_FOLDER);
                    testData.A1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_NAME, "A1");
                }
                {
                    // Node B1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B1"), ContentModel.TYPE_FOLDER);
                    testData.B1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_NAME, "B1");
                }

                // Create the transfer target if it does not already exist
                if (!transferService.targetExists(targetName))
                {
                    testData.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    testData.transferMe = transferService.getTransferTarget(targetName);
                }
                return null;
            }
        });
        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         *
         * Mock the transfer service to be from Repo A
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        pathMap.add(new Pair(nodeService.getPath(testData.A0NodeRef), nodeService.getPath(testData.B1NodeRef)));
        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        /**
         * Step 1. add A1 transfer(sync)
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer Nodes A1
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A1NodeRef);
                    definition.setNodes(nodes);
                    definition.setSync(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                assertTrue("dest node ref does not exist", nodeService.exists(A1destNodeRef));
                assertFalse("A1 is alien", nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                // Check injected transferred aspect.
                assertNotNull("transferredAspect", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                return null;
            }
        });
        /**
         * Step 2 add Alien node B1 child of A1(dest).
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                ChildAssociationRef child = nodeService.createNode(testData.destNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B2"), ContentModel.TYPE_FOLDER);
                testData.B2NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B2NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.B2NodeRef, ContentModel.PROP_NAME, "B2");
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists and has similar properties to the source
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);

                assertTrue("dest node ref does not exist", nodeService.exists(A1destNodeRef));
                // Check injected transferred aspect.
                assertTrue("node A1 is not alien aspect", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertNotNull("repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                assertNotNull("from repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID));
                assertTrue("node B2 is not alien", (Boolean) nodeService.hasAspect(testData.B2NodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * Step 3 remove alien node B2. A1 becomes non Alien.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                logger.debug("delete node B2");
                nodeService.deleteNode(testData.B2NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                List<String> invaders = (List<String>) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_INVADED_BY);
                assertTrue("dest node ref does not exist", nodeService.exists(A1destNodeRef));
                assertFalse("node A1 is still alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertNotNull("repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                assertNotNull("from repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID));
                return null;
            }
        });
        /**
         * Step 4 restore alien node B2. A1 becomes Alien again
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                logger.debug("restore node B2");
                NodeRef B2ArchiveNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, testData.B2NodeRef.getId());
                nodeService.restoreNode(B2ArchiveNodeRef, testNodeFactory.getMappedNodeRef(testData.A1NodeRef), ContentModel.ASSOC_CONTAINS, QName.createQName("B2"));
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                assertTrue("dest node ref does not exist", nodeService.exists(A1destNodeRef));
                assertTrue("node A1 is not alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertNotNull("repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                assertNotNull("from repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID));
                return null;
            }
        });
        /**
         * Step 5 - add B3
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ChildAssociationRef child = nodeService.createNode(testData.B2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B3"), ContentModel.TYPE_FOLDER);
                testData.B3NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B3NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.B3NodeRef, ContentModel.PROP_NAME, "B3");
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("node B3 is not alien", (Boolean) nodeService.hasAspect(testData.B3NodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * Step 5 remove alien node B2. A1 becomes non Alien (again).
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                logger.debug("delete node B2");
                nodeService.deleteNode(testData.B2NodeRef);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef A1destNodeRef = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);
                List<String> invaders = (List<String>) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_INVADED_BY);
                assertTrue("dest node ref does not exist", nodeService.exists(A1destNodeRef));
                assertFalse("node A1 is still alien", (Boolean) nodeService.hasAspect(A1destNodeRef, TransferModel.ASPECT_ALIEN));
                assertNotNull("repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_REPOSITORY_ID));
                assertNotNull("from repository id is null", (String) nodeService.getProperty(A1destNodeRef, TransferModel.PROP_FROM_REPOSITORY_ID));
                return null;
            }
        });
        /**
         * Step6 restore B2 and B3 to B1.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                logger.debug("restore node B2");
                NodeRef B2ArchiveNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, testData.B2NodeRef.getId());
                nodeService.restoreNode(B2ArchiveNodeRef, testData.B1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B2"));
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertFalse("node A1 is still alien", (Boolean) nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A1NodeRef), TransferModel.ASPECT_ALIEN));
                assertFalse("node A2 is still alien", (Boolean) nodeService.hasAspect(testData.B2NodeRef, TransferModel.ASPECT_ALIEN));
                assertFalse("node A3 is still alien", (Boolean) nodeService.hasAspect(testData.B3NodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
    }

    /**
     * Test the transfer method with regard to permissions on a node.
     * <p>
     * Step 1:  
     * Create a node with a single permission 
     *     Inherit:false
     *     Read, Admin, Allow
     *     Transfer
     * <p>
     * Step 2:
     * Update it to have several permissions 
     *     Inherit:false
     *     Read, Everyone, DENY
     *     Read, Admin, Allow
     * <p>
     * Step 3:
     * Remove a permission
     *     Inherit:false
     *     Read, Admin, Allow
     * <p>
     * Step 4:
     * Revert to inherit all permissions
     *     Inherit:true
     * <p>
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testTransferWithPermissions() throws Exception
    {
        setDefaultRollback(false);

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.GERMAN;
        final String CONTENT_STRING = "Hello";

        /**
         * For unit test - replace the HTTP transport with the in-process transport - replace the node factory with one
         * that will map node refs, paths etc.
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        /**
         * Now go ahead and create our transfer target
         */
        final String targetName = "testTransferWithPermissions";
        class TestData
        {
            TransferTarget transferMe;
            NodeRef contentNodeRef;
            NodeRef destNodeRef;
        }
        final TestData testData = new TestData();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0);

                /**
                 * Create a test node that we will read and write
                 */
                String name = GUID.generate();
                ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_CONTENT);
                testData.contentNodeRef = child.getChildRef();
                nodeService.setProperty(testData.contentNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.contentNodeRef, ContentModel.PROP_NAME, name);

                ContentWriter writer = contentService.getWriter(testData.contentNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);

                permissionService.setInheritParentPermissions(testData.contentNodeRef, false);
                permissionService.setPermission(testData.contentNodeRef, "admin", PermissionService.READ, true);

                if (!transferService.targetExists(targetName))
                {
                    testData.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    testData.transferMe = transferService.getTransferTarget(targetName);
                }
                return null;
            }
        });
        /**
         * Step 1
         */
        logger.debug("First transfer - create new node with inheritParent permission off");
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer our transfer target node
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.contentNodeRef);
                    definition.setNodes(nodes);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists with the correct permissions
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.contentNodeRef);
                assertFalse("unit test stuffed up - comparing with self", testData.destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node ref does not exist", nodeService.exists(testData.destNodeRef));
                assertEquals("title is wrong", (String) nodeService.getProperty(testData.destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE);
                assertEquals("type is wrong", nodeService.getType(testData.contentNodeRef), nodeService.getType(testData.destNodeRef));

                // Check ACL of destination node
                boolean srcInherit = permissionService.getInheritParentPermissions(testData.contentNodeRef);
                Set<AccessPermission> srcPerm = permissionService.getAllSetPermissions(testData.contentNodeRef);

                boolean destInherit = permissionService.getInheritParentPermissions(testData.destNodeRef);
                Set<AccessPermission> destPerm = permissionService.getAllSetPermissions(testData.destNodeRef);

                assertFalse("inherit parent permissions (src) flag is incorrect", srcInherit);
                assertFalse("inherit parent permissions (dest) flag is incorrect", destInherit);

                // Check destination has the source's permissions
                for (AccessPermission p : srcPerm)
                {
                    logger.debug("checking permission :" + p);
                    assertTrue("permission is missing", destPerm.contains(p));
                }
                return null;
            }
        });
        /**
         * Step 2
         * Update it to have several permissions 
         *     Inherit:false
         *     Read, Everyone, DENY
         *     Read, Admin, Allow 
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                permissionService.setPermission(testData.contentNodeRef, "EVERYONE", PermissionService.READ, false);
                permissionService.setPermission(testData.contentNodeRef, "admin", PermissionService.FULL_CONTROL, true);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer our transfer target node
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.contentNodeRef);
                    definition.setNodes(nodes);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists with the correct permissions
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.contentNodeRef);

                // Check ACL of destination node
                boolean srcInherit = permissionService.getInheritParentPermissions(testData.contentNodeRef);
                Set<AccessPermission> srcPerm = permissionService.getAllSetPermissions(testData.contentNodeRef);

                boolean destInherit = permissionService.getInheritParentPermissions(testData.destNodeRef);
                Set<AccessPermission> destPerm = permissionService.getAllSetPermissions(testData.destNodeRef);

                assertFalse("inherit parent permissions (src) flag is incorrect", srcInherit);
                assertFalse("inherit parent permissions (dest) flag is incorrect", destInherit);

                // Check destination has the source's permissions
                for (AccessPermission p : srcPerm)
                {
                    logger.debug("checking permission :" + p);
                    assertTrue("Step2, permission is missing", destPerm.contains(p));
                }
                return null;
            }
        });
        /**
         * Step 3 Remove a permission
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                permissionService.deletePermission(testData.contentNodeRef, "admin", PermissionService.FULL_CONTROL);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer our transfer target node
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.contentNodeRef);
                    definition.setNodes(nodes);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists with the correct permissions
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.contentNodeRef);

                // Check ACL of destination node
                boolean srcInherit = permissionService.getInheritParentPermissions(testData.contentNodeRef);
                Set<AccessPermission> srcPerm = permissionService.getAllSetPermissions(testData.contentNodeRef);

                boolean destInherit = permissionService.getInheritParentPermissions(testData.destNodeRef);
                Set<AccessPermission> destPerm = permissionService.getAllSetPermissions(testData.destNodeRef);

                assertFalse("inherit parent permissions (src) flag is incorrect", srcInherit);
                assertFalse("inherit parent permissions (dest) flag is incorrect", destInherit);

                // Check destination has the source's permissions
                for (AccessPermission p : srcPerm)
                {
                    logger.debug("checking permission :" + p);
                    assertTrue("permission is missing", destPerm.contains(p));
                }
                return null;
            }
        });
        /**
         * Step 4 Revert to inherit all permissions
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                permissionService.setInheritParentPermissions(testData.contentNodeRef, true);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer our transfer target node
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.contentNodeRef);
                    definition.setNodes(nodes);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists with the correct permissions
                testData.destNodeRef = testNodeFactory.getMappedNodeRef(testData.contentNodeRef);
                assertFalse("unit test stuffed up - comparing with self", testData.destNodeRef.equals(testData.transferMe.getNodeRef()));
                assertTrue("dest node ref does not exist", nodeService.exists(testData.destNodeRef));
                assertEquals("title is wrong", (String) nodeService.getProperty(testData.destNodeRef, ContentModel.PROP_TITLE), CONTENT_TITLE);
                assertEquals("type is wrong", nodeService.getType(testData.contentNodeRef), nodeService.getType(testData.destNodeRef));

                // Check ACL of destination node
                boolean srcInherit = permissionService.getInheritParentPermissions(testData.contentNodeRef);
                Set<AccessPermission> srcPerm = permissionService.getAllSetPermissions(testData.contentNodeRef);

                boolean destInherit = permissionService.getInheritParentPermissions(testData.destNodeRef);
                Set<AccessPermission> destPerm = permissionService.getAllSetPermissions(testData.destNodeRef);

                assertTrue("inherit parent permissions (src) flag is incorrect", srcInherit);
                assertTrue("inherit parent permissions (dest) flag is incorrect", destInherit);

                // Check destination has the source's permissions
                for (AccessPermission p : srcPerm)
                {
                    if (p.isSetDirectly())
                    {
                        logger.debug("checking permission :" + p);
                        assertTrue("permission is missing:" + p, destPerm.contains(p));
                    }
                }
                return null;
            }
        });
    }

    /**
     * Transfer with read only flag
     * 
     * Node tree for this test
     * <pre> 
     *           A (Folder)
     *   |                 | 
     *   B (Content)   C (Folder)
     *                     |
     *                     D (Content)
     * </pre>
     * Step 1
     * transfer Nodes ABCD with read only flag set - content should all be locked on destination
     * <p>
     * Step 2
     * lock B (Content node) as user fred
     * transfer (read only) - destination lock should change to Admin
     * <p>
     * Step 3
     * lock C (Folder) as user fred
     * transfer (read only) - destination lock should change to Admin
     * <p> 
     * Step 4
     * transfer without read only flag - locks should revert from Admin to Fred.
     * <p>
     * Step 5
     * remove locks on A and B - transfer without read only flag - content should all be unlocked.
     */
    public void testReadOnlyFlag() throws Exception
    {
        setDefaultRollback(false);

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final String CONTENT_NAME = "Demo Node 1";
        final Locale CONTENT_LOCALE = Locale.GERMAN;
        final String CONTENT_STRING = "The quick brown fox";
        final Set<NodeRef> nodes = new HashSet<NodeRef>();
        final String USER_ONE = "TransferServiceImplTest";
        final String PASSWORD = "Password";

        final String targetName = "testReadOnlyFlag";

        class TestData
        {
            NodeRef nodeA;
            NodeRef nodeB;
            NodeRef nodeC;
            NodeRef nodeD;

            ChildAssociationRef child;

            TransferTarget transferMe;
        }
        final TestData testData = new TestData();

        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(this.receiver, this.contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0);

                /**
                 * Create a test node that we will read and write
                 */
                String guid = GUID.generate();

                testData.child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(guid), ContentModel.TYPE_FOLDER);
                testData.nodeA = testData.child.getChildRef();
                nodeService.setProperty(testData.nodeA, ContentModel.PROP_TITLE, guid);
                nodeService.setProperty(testData.nodeA, ContentModel.PROP_NAME, guid);
                nodes.add(testData.nodeA);

                testData.child = nodeService.createNode(testData.nodeA, ContentModel.ASSOC_CONTAINS, QName.createQName("testNodeB"), ContentModel.TYPE_CONTENT);
                testData.nodeB = testData.child.getChildRef();
                nodeService.setProperty(testData.nodeB, ContentModel.PROP_TITLE, CONTENT_TITLE + "B");
                nodeService.setProperty(testData.nodeB, ContentModel.PROP_NAME, "DemoNodeB");

                {
                    ContentWriter writer = contentService.getWriter(testData.nodeB, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                    nodes.add(testData.nodeB);
                }

                testData.child = nodeService.createNode(testData.nodeA, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testNodeC"), ContentModel.TYPE_FOLDER);
                testData.nodeC = testData.child.getChildRef();
                nodeService.setProperty(testData.nodeC, ContentModel.PROP_TITLE, "TestNodeC");
                nodeService.setProperty(testData.nodeC, ContentModel.PROP_NAME, "TestNodeC");
                nodes.add(testData.nodeC);

                testData.child = nodeService.createNode(testData.nodeC, ContentModel.ASSOC_CONTAINS, QName.createQName("testNodeD"), ContentModel.TYPE_CONTENT);
                testData.nodeD = testData.child.getChildRef();
                nodeService.setProperty(testData.nodeD, ContentModel.PROP_TITLE, CONTENT_TITLE + "D");
                nodeService.setProperty(testData.nodeD, ContentModel.PROP_NAME, "DemoNodeD");
                {
                    ContentWriter writer = contentService.getWriter(testData.nodeD, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                    nodes.add(testData.nodeD);
                }

                // Create users
                createUser(USER_ONE, PASSWORD);

                /**
                 * Now go ahead and create our first transfer target
                 */
                if (!transferService.targetExists(targetName))
                {
                    testData.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    testData.transferMe = transferService.getTransferTarget(targetName);
                }
                return null;
            }
        });
        /**
         * Step 1. 
         * transfer Nodes ABCD with read only flag set - content should all be locked on destination  
         */
        logger.debug("transfer read only - step 1");
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer our transfer target nodes
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    definition.setNodes(nodes);
                    definition.setReadOnly(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Check destination nodes are locked.
                assertTrue("dest node A does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeA)));
                assertTrue("dest node B does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeB)));
                assertTrue("dest node C does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeC)));
                assertTrue("dest node D does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeD)));

                assertTrue("dest node A not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeA), ContentModel.ASPECT_LOCKABLE));
                assertTrue("dest node B not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeB), ContentModel.ASPECT_LOCKABLE));
                assertTrue("dest node C not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeC), ContentModel.ASPECT_LOCKABLE));
                assertTrue("dest node D not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeD), ContentModel.ASPECT_LOCKABLE));
                return null;
            }
        });
       /**
         * Step 2
         * lock B (Content node) as user ONE
         * transfer (read only) - destination lock should change user to "Admin" 
         */ 
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                AuthenticationUtil.pushAuthentication();
                AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
                lockService.lock(testData.nodeB, LockType.READ_ONLY_LOCK);
                assertEquals("test error: dest node B lock ownership", nodeService.getProperty(testData.nodeB, ContentModel.PROP_LOCK_OWNER), USER_ONE);
                AuthenticationUtil.popAuthentication();
                return null;
            }
        });
        logger.debug("transfer read only - step 2");
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer our transfer target nodes
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    definition.setNodes(nodes);
                    definition.setReadOnly(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Check destination nodes are locked.
                assertTrue("dest node A does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeA)));
                assertTrue("dest node B does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeB)));
                assertTrue("dest node C does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeC)));
                assertTrue("dest node D does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeD)));

                assertTrue("dest node A not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeA), ContentModel.ASPECT_LOCKABLE));
                assertTrue("dest node B not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeB), ContentModel.ASPECT_LOCKABLE));
                assertTrue("dest node C not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeC), ContentModel.ASPECT_LOCKABLE));
                assertTrue("dest node D not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeD), ContentModel.ASPECT_LOCKABLE));

                // check that the lock owner is no longer USER_ONE
                assertTrue("lock owner not changed", !USER_ONE.equalsIgnoreCase((String) nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.nodeB), ContentModel.PROP_LOCK_OWNER)));
                return null;
            }
        });

        /**
         * Step 3
         * lock C (Folder node) as user ONE
         * transfer (read only) - destination lock should change to Admin
         */ 
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                AuthenticationUtil.pushAuthentication();
                AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
                lockService.lock(testData.nodeC, LockType.READ_ONLY_LOCK);
                assertEquals("test error: dest node C lock ownership", nodeService.getProperty(testData.nodeC, ContentModel.PROP_LOCK_OWNER), USER_ONE);
                AuthenticationUtil.popAuthentication();
                return null;
            }
        });
        logger.debug("transfer read only - step 3");
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer our transfer target nodes
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    definition.setNodes(nodes);
                    definition.setReadOnly(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Check destination nodes are locked.
                assertTrue("dest node A does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeA)));
                assertTrue("dest node B does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeB)));
                assertTrue("dest node C does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeC)));
                assertTrue("dest node D does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeD)));

                assertTrue("dest node A not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeA), ContentModel.ASPECT_LOCKABLE));
                assertTrue("dest node B not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeB), ContentModel.ASPECT_LOCKABLE));
                assertTrue("dest node C not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeC), ContentModel.ASPECT_LOCKABLE));
                assertTrue("dest node D not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeD), ContentModel.ASPECT_LOCKABLE));

                // check that the lock owner is no longer USER_ONE for content node B and folder node C
                assertTrue("lock owner not changed", !USER_ONE.equalsIgnoreCase((String) nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.nodeB), ContentModel.PROP_LOCK_OWNER)));
                assertTrue("lock owner not changed", !USER_ONE.equalsIgnoreCase((String) nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.nodeC), ContentModel.PROP_LOCK_OWNER)));
                return null;
            }
        });

        /**
         * Step 4
         * transfer without read only flag - locks should revert from Admin to USER_ONE.
         */
        logger.debug("transfer read only - step 4");
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer our transfer target nodes
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    definition.setNodes(nodes);
                    definition.setReadOnly(false); // turn off read-only
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Check destination nodes are not locked.
                assertTrue("dest node A does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeA)));
                assertTrue("dest node B does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeB)));
                assertTrue("dest node C does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeC)));
                assertTrue("dest node D does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeD)));

                assertFalse("dest node A not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeA), ContentModel.ASPECT_LOCKABLE));
                assertTrue("dest node B not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeB), ContentModel.ASPECT_LOCKABLE));
                assertTrue("dest node C not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeC), ContentModel.ASPECT_LOCKABLE));
                assertFalse("dest node D not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeD), ContentModel.ASPECT_LOCKABLE));

                assertEquals("dest node B lock ownership", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.nodeB), ContentModel.PROP_LOCK_OWNER), USER_ONE);
                assertEquals("dest node C lock ownership", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.nodeC), ContentModel.PROP_LOCK_OWNER), USER_ONE);

                return null;
            }
        });

       /**
        * Step 5
        * remove locks on A and B - transfer without read only flag - content should all be unlocked.
        */
        logger.debug("transfer read only - step 5");
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                lockService.unlock(testData.nodeB);
                lockService.unlock(testData.nodeC);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer our transfer target nodes
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    definition.setNodes(nodes);
                    definition.setReadOnly(false); // turn off read-only
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Check destination nodes are not locked.
                assertTrue("dest node A does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeA)));
                assertTrue("dest node B does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeB)));
                assertTrue("dest node C does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeC)));
                assertTrue("dest node D does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.nodeD)));

                assertFalse("test fail: dest node B is still locked", nodeService.hasAspect(testData.nodeB, ContentModel.ASPECT_LOCKABLE));
                assertFalse("test fail: dest node C is still locked", nodeService.hasAspect(testData.nodeC, ContentModel.ASPECT_LOCKABLE));

                assertFalse("dest node A not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeA), ContentModel.ASPECT_LOCKABLE));
                assertFalse("dest node B not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeB), ContentModel.ASPECT_LOCKABLE));
                assertFalse("dest node C not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeC), ContentModel.ASPECT_LOCKABLE));
                assertFalse("dest node D not locked", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.nodeD), ContentModel.ASPECT_LOCKABLE));
                return null;
            }
        });
    } // end test read only flag

    /**
     * Transfer sync from multiple repos.
     * 
     * This is a unit test so does lots of shenanigans to fake transfer from three repositories on a single repo.
     * 
     * Multi-repo sync depends upon the following pieces of functionality
     * a) transferred nodes are tagged with a trx:transferred aspect containing the originating repository 
     * id and the from repository id
     * b) to support hub and spoke - when syncing don't imply delete nodes that are not "from" the transferring system
     * 
     *      * Tree of nodes 
     *      A1
     *   |      |    
     *   A2     A3 (Content Node) B6 (Content Node)   
     *   |
     * A4 A5 B7
     * 
     * Step 1
     * Hub and Spoke Sync
     * create Tree A1...A5
     * transfer (sync)
     * check the transfered aspects on destination
     * create node B6.  Fake its transfered aspect to be from Repo B.
     * transfer (sync)
     *  
     * Step 2
     * Chain Sync
     * Create Node A7 "from repo B".
     * Change Nodes A1 ... A5 source to be received "from repo B"
     * transfer 
     * 
     */
    public void testTwoRepoSync() throws Exception
    {
        /**
        * Step 1
        * create Tree A1...A6
        * transfer (sync)
        * check the transfered aspect
        * create node B6.  Fake its transfered aspect to be from Repo B, Non Alien.
        * transfer (sync)
        */
        setDefaultRollback(false);

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.GERMAN;
        final String CONTENT_STRING = "Hello";

        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - replace the node factory with one that will map node refs, paths etc.
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map company_home/guest_home to company_home so tranferred nodes and moved "up" one level.
        pathMap.add(new Pair<Path, Path>(PathHelper.stringToPath(GUEST_HOME_XPATH_QUERY), PathHelper.stringToPath(COMPANY_HOME_XPATH_QUERY)));

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        final String repositoryId = REPO_ID_A;

        /**
         * Now go ahead and create our first transfer target
         */
        final String targetName = "testTransferSyncNodes";
        class TestData
        {
            TransferTarget transferMe;
            NodeRef A1NodeRef;
            NodeRef A2NodeRef;
            NodeRef A3NodeRef;
            NodeRef A4NodeRef;
            NodeRef A5NodeRef;
            NodeRef B6NodeRef;
            NodeRef A7NodeRef;
        }
        final TestData testData = new TestData();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0);

                /**
                 * Create a test nodes A1 through A5 that we will read and write
                 */
                {
                    // Node A1
                    String name = GUID.generate();
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_FOLDER);
                    testData.A1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_NAME, name);
                }

                {
                    // Node A2
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A2"), ContentModel.TYPE_FOLDER);
                    testData.A2NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_NAME, "A2");
                }

                {
                    // Node A3
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A3"), ContentModel.TYPE_CONTENT);
                    testData.A3NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_NAME, "A3");

                    ContentWriter writer = contentService.getWriter(testData.A3NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }
                {
                    // Node A4
                    ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A4"), ContentModel.TYPE_CONTENT);
                    testData.A4NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_NAME, "A4");

                    ContentWriter writer = contentService.getWriter(testData.A4NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }
                {
                    // Node A5
                    ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A5"), ContentModel.TYPE_CONTENT);
                    testData.A5NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_NAME, "A5");

                    ContentWriter writer = contentService.getWriter(testData.A5NodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setLocale(CONTENT_LOCALE);
                    writer.putContent(CONTENT_STRING);
                }

                // Create the transfer target if it does not already exist
                if (!transferService.targetExists(targetName))
                {
                    testData.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    testData.transferMe = transferService.getTransferTarget(targetName);
                }
                return null;
            }
        });
        final Set<NodeRef> nodes = new HashSet<NodeRef>();
        nodes.add(testData.A1NodeRef);
        nodes.add(testData.A2NodeRef);
        nodes.add(testData.A3NodeRef);
        nodes.add(testData.A4NodeRef);
        nodes.add(testData.A5NodeRef);

        /**
          * transfer (sync)
          * check the transfered aspect
          * create node B6.  Fake its transfered aspect to be from Repo B, Non Alien.
          * transfer (sync)
          */ 
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Node B6 - faked transfer from repository B. Child of Destination node A1
                NodeRef a1Dest = testNodeFactory.getMappedNodeRef(testData.A1NodeRef);

                assertTrue("dest node A does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A1NodeRef)));
                assertEquals("dest node A1 From RepositoryId", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A1NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), repositoryId);
                assertEquals("dest node A1 Repository Id", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A1NodeRef), TransferModel.PROP_REPOSITORY_ID), repositoryId);
                assertEquals("dest node A2 From RepositoryId", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A2NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), repositoryId);
                assertEquals("dest node A2 Repository Id", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A2NodeRef), TransferModel.PROP_REPOSITORY_ID), repositoryId);
                assertEquals("dest node A3 From RepositoryId", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), repositoryId);
                assertEquals("dest node A3 Repository Id", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), TransferModel.PROP_REPOSITORY_ID), repositoryId);
                assertEquals("dest node A4 From RepositoryId", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A4NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), repositoryId);
                assertEquals("dest node A4 Repository Id", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A4NodeRef), TransferModel.PROP_REPOSITORY_ID), repositoryId);
                assertEquals("dest node A5 From RepositoryId", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A5NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), repositoryId);
                assertEquals("dest node A5 Repository Id", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A5NodeRef), TransferModel.PROP_REPOSITORY_ID), repositoryId);

                ChildAssociationRef child = nodeService.createNode(a1Dest, ContentModel.ASSOC_CONTAINS, QName.createQName("B6"), ContentModel.TYPE_CONTENT);
                testData.B6NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B6NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                nodeService.setProperty(testData.B6NodeRef, ContentModel.PROP_NAME, "B6");

                /**
                 * The first tranfer was mocked to repository A - this is repository B.
                 */

                // This is repository B so there's no need to fake it
                // nodeService.setProperty(testData.B6NodeRef, TransferModel.PROP_FROM_REPOSITORY_ID, REPO_ID_B);
                // nodeService.setProperty(testData.B6NodeRef, TransferModel.PROP_REPOSITORY_ID, REPO_ID_B);

                ContentWriter writer = contentService.getWriter(testData.B6NodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);

                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Does node B6 still exist ?
                assertTrue("dest node B6 does not exist", nodeService.exists(testData.B6NodeRef));
                assertTrue("B6 not alien", nodeService.hasAspect(testData.B6NodeRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
       /** Step 2
        * Chain Sync
        * Change Nodes A1 ... A5 source to be received "from repo B"
        * Create Node A7 - Fake it to be received "from repo B"
        * transfer
        */ 
        final String NEW_TITLE = "Chain sync";

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_TITLE, NEW_TITLE);
                nodeService.setProperty(testData.A1NodeRef, TransferModel.PROP_FROM_REPOSITORY_ID, REPO_ID_B);
                nodeService.setProperty(testData.A1NodeRef, TransferModel.PROP_REPOSITORY_ID, REPO_ID_B);

                nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_TITLE, NEW_TITLE);
                nodeService.setProperty(testData.A2NodeRef, TransferModel.PROP_FROM_REPOSITORY_ID, REPO_ID_B);
                nodeService.setProperty(testData.A2NodeRef, TransferModel.PROP_REPOSITORY_ID, REPO_ID_B);

                nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_TITLE, NEW_TITLE);
                nodeService.setProperty(testData.A3NodeRef, TransferModel.PROP_FROM_REPOSITORY_ID, REPO_ID_B);
                nodeService.setProperty(testData.A3NodeRef, TransferModel.PROP_REPOSITORY_ID, REPO_ID_B);

                /**
                 * The repository was mocked to repoistory A. This is repository B
                 */
                ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A7"), ContentModel.TYPE_CONTENT);
                testData.A7NodeRef = child.getChildRef();
                nodeService.setProperty(testData.A7NodeRef, ContentModel.PROP_TITLE, NEW_TITLE);
                nodeService.setProperty(testData.A7NodeRef, ContentModel.PROP_NAME, "A7");
                nodeService.setProperty(testData.A7NodeRef, ContentModel.PROP_TITLE, NEW_TITLE);
                nodeService.setProperty(testData.A7NodeRef, TransferModel.PROP_FROM_REPOSITORY_ID, REPO_ID_B);
                nodeService.setProperty(testData.A7NodeRef, TransferModel.PROP_REPOSITORY_ID, REPO_ID_B);

                ContentWriter writer = contentService.getWriter(testData.A3NodeRef, ContentModel.PROP_CONTENT, true);
                writer.setLocale(CONTENT_LOCALE);
                writer.putContent(CONTENT_STRING);
                return null;
            }
        });
        nodes.add(testData.A7NodeRef);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node A7 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A7NodeRef)));

                assertEquals("dest node A1 Title", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A1NodeRef), ContentModel.PROP_TITLE), NEW_TITLE);
                assertEquals("dest node A1 Repository Id", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A1NodeRef), TransferModel.PROP_REPOSITORY_ID), REPO_ID_B);
                assertEquals("dest node A1 Repository Id", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A1NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), repositoryId);

                assertEquals("dest node A2 Title", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A2NodeRef), ContentModel.PROP_TITLE), NEW_TITLE);
                assertEquals("dest node A2 Repository Id", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A2NodeRef), TransferModel.PROP_REPOSITORY_ID), REPO_ID_B);
                assertEquals("dest node A2 Repository Id", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A2NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), repositoryId);

                assertEquals("dest node A3 Title", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), ContentModel.PROP_TITLE), NEW_TITLE);
                assertEquals("dest node A3 Repository Id", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), TransferModel.PROP_REPOSITORY_ID), REPO_ID_B);
                assertEquals("dest node A3 Repository Id", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), repositoryId);
                return null;
            }
        });
    } // test two repo sync

    /**
     * Transfer sync from multiple repos.
     * 
     * This is a unit test so does lots of shenanigans to fake transfer from three repositories on a single repo.
     * 
     * Trees of nodes
     *  <pre>
     *      A1              B1               C1
     *      |                                 |    
     *    A2/images                       A2 Dummy/images
     *      |                              |
     *      A3                            C3 
     *                                          
     * Destination   
     *      B1
     *      |
     *    A2/images
     *      |     |
     *      C3    A3 
     *            | 
     *            C4         
     * </pre>
     * Step 1.  Transfer from A to B. 
     * Step 2.  Transfer from C to B (crossing over on A2Dest) 
     * Step 3.  Invade A3Dest via C4
     * Step 4.  Delete C4. Sync from C 
     * Step 5.  Delete C3  - A2 dest images folder uninvaded.
       
     */
    public void testMultiRepoTransfer() throws Exception
    {
        setDefaultRollback(false);

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.GERMAN;
        final String CONTENT_STRING = "Hello";

        final String targetName = "testMultiRepoTransfer";

        class TestData
        {
            TransferTarget transferMe;
            NodeRef S0NodeRef;
            NodeRef A1NodeRef;
            NodeRef A2NodeRef;
            NodeRef A3NodeRef;
            NodeRef B1NodeRef;
            NodeRef C1NodeRef;
            NodeRef C2NodeRef;
            NodeRef C3NodeRef;
            NodeRef C4NodeRef;
            NodeRef A3Dummy;
        }
        final TestData testData = new TestData();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0);

                {
                    /**
                     * Node Source - located under guest home
                     */
                    String name = GUID.generate();
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_FOLDER);
                    testData.S0NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.S0NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.S0NodeRef, ContentModel.PROP_NAME, name);
                }

                {
                    // Node A1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A1"), ContentModel.TYPE_FOLDER);
                    testData.A1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_TITLE, "A1");
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_NAME, "A1");
                }

                {
                    // Node A2
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("images"), ContentModel.TYPE_FOLDER);
                    testData.A2NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_TITLE, "images");
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_NAME, "images");
                }

                {
                    // Node A3
                    ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A3"), ContentModel.TYPE_FOLDER);
                    testData.A3NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_TITLE, "A3");
                    nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_NAME, "A3");
                }

                {
                    // Node B1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B1"), ContentModel.TYPE_FOLDER);
                    testData.B1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_TITLE, "B1");
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_NAME, "B1");
                }

                {
                    // Node C1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("C1"), ContentModel.TYPE_FOLDER);
                    testData.C1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C1NodeRef, ContentModel.PROP_TITLE, "C1");
                    nodeService.setProperty(testData.C1NodeRef, ContentModel.PROP_NAME, "C1");
                }

                {
                    // Node C2/images
                    ChildAssociationRef child = nodeService.createNode(testData.C1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("images"), ContentModel.TYPE_FOLDER);
                    testData.C2NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C2NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.C2NodeRef, ContentModel.PROP_NAME, "images");
                }

                {
                    // Node C3
                    ChildAssociationRef child = nodeService.createNode(testData.C2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("C3"), ContentModel.TYPE_FOLDER);
                    testData.C3NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C3NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.C3NodeRef, ContentModel.PROP_NAME, "C3");
                }

                {
                    // Node A3 (Dummy)
                    ChildAssociationRef child = nodeService.createNode(testData.C2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A3"), ContentModel.TYPE_FOLDER);
                    testData.A3Dummy = child.getChildRef();
                    nodeService.setProperty(testData.A3Dummy, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.A3Dummy, ContentModel.PROP_NAME, "A3 Dummy");
                }

                {
                    // Node C4
                    ChildAssociationRef child = nodeService.createNode(testData.A3Dummy, ContentModel.ASSOC_CONTAINS, QName.createQName("C4"), ContentModel.TYPE_FOLDER);
                    testData.C4NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C4NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.C4NodeRef, ContentModel.PROP_NAME, "C4");
                }

                // Create the transfer target if it does not already exist
                if (!transferService.targetExists(targetName))
                {
                    testData.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    testData.transferMe = transferService.getTransferTarget(targetName);
                }
                return null;
            }
        });
        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - Map path from A1 to B1 (So transfer will transfer by path)
         *  - Map path from C1 to B1
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map Project A/images to Project B/images
        // Map Project C/images to Project A/images
        nodeService.getPath(testData.A2NodeRef);
        pathMap.add(new Pair(nodeService.getPath(testData.A1NodeRef), nodeService.getPath(testData.B1NodeRef)));
        pathMap.add(new Pair(nodeService.getPath(testData.C1NodeRef), nodeService.getPath(testData.B1NodeRef)));
        {
            DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
            transferServiceImpl.setDescriptorService(mockedDescriptorService);
        }

        /**
         * Step 1 Now transfer in A's nodes to Repo B
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();
                nodes.add(testData.A1NodeRef);
                nodes.add(testData.A2NodeRef);
                nodes.add(testData.A3NodeRef);
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node A2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A2NodeRef)));
                assertTrue("dest node A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));

                // Check that A3 dest is a child of A2Dest which is a child of B1
                ChildAssociationRef A3Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A3NodeRef));
                assertEquals("A3 dest is connected to the wrong node", A3Ref.getParentRef(), testNodeFactory.getMappedNodeRef(testData.A2NodeRef));
                ChildAssociationRef A2Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A2NodeRef));
                assertEquals("A2 dest is connected to the wrong node", A2Ref.getParentRef(), testData.B1NodeRef);
                assertEquals("A2 dest owned by wrong repo", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A2NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), REPO_ID_A);
                assertEquals("A3 dest owned by wrong repo", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), REPO_ID_A);
                return null;
            }
        });
        /**
         * Step 2 Now transfer in C's nodes B2 (Owned by A) gets invaded by C
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_C);
                transferServiceImpl.setDescriptorService(mockedDescriptorService);
                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();
                nodes.add(testData.C1NodeRef);
                nodes.add(testData.C2NodeRef);
                nodes.add(testData.C3NodeRef);
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertTrue("dest node C3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));

                // Check that A3 dest is a child of A2Dest which is a child of B1
                // Check that C3 dest is a child of A2Dest
                ChildAssociationRef A3Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A3NodeRef));
                assertEquals("A3 dest is connected to the wrong node", A3Ref.getParentRef(), testNodeFactory.getMappedNodeRef(testData.A2NodeRef));
                ChildAssociationRef C3Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A3NodeRef));
                assertEquals("C3 dest is connected to the wrong node", C3Ref.getParentRef(), testNodeFactory.getMappedNodeRef(testData.A2NodeRef));
                ChildAssociationRef A2Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A2NodeRef));
                assertEquals("A2 dest is connected to the wrong node", A2Ref.getParentRef(), testData.B1NodeRef);

                assertTrue("A2 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A2NodeRef), TransferModel.ASPECT_ALIEN));
                assertTrue("C3 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C3NodeRef), TransferModel.ASPECT_ALIEN));
                assertFalse("A3 dest is invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), TransferModel.ASPECT_ALIEN));

                assertEquals("A2 dest owned by wrong repo", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A2NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), REPO_ID_A);
                assertEquals("A3 dest owned by wrong repo", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), REPO_ID_A);
                assertEquals("C3 dest owned by wrong repo", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.C3NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), REPO_ID_C);
                return null;
            }
        });
        /**
         * Step 3 Invade A3Dest via transfer of C4 from C
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_C);
                transferServiceImpl.setDescriptorService(mockedDescriptorService);
                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();
                nodes.add(testData.C4NodeRef);
                definition.setNodes(nodes);
                definition.setSync(false);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertTrue("dest node C3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));
                assertTrue("dest node C4 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C4NodeRef)));

                assertTrue("C4 is not an invader", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C4NodeRef), TransferModel.ASPECT_ALIEN));
                assertTrue("A3 is not an invader", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), TransferModel.ASPECT_ALIEN));

                assertEquals("A2 dest owned by wrong repo", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A2NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), REPO_ID_A);
                assertEquals("A3 dest owned by wrong repo", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), REPO_ID_A);
                assertEquals("C3 dest owned by wrong repo", nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.C3NodeRef), TransferModel.PROP_FROM_REPOSITORY_ID), REPO_ID_C);

                return null;
            }
        });
        /**
         * Step 4
         * Uninvade A3 from C by deleting C4
         * Via Sync of A3Dummy (which has the same destination path as A3).
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(testData.C4NodeRef);

                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_C);
                transferServiceImpl.setDescriptorService(mockedDescriptorService);

                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();
                nodes.add(testData.A3Dummy);
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertTrue("dest node C3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));
                assertFalse("dest node C4 not deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C4NodeRef)));

                logger.debug("A3 Dest is " + testNodeFactory.getMappedNodeRef(testData.A3NodeRef));
                assertFalse("A3 Dest still invaded by C4", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * Step 5 - repeat the above test with transfer(non sync) rather than transfer(sync) Uninvade by deleting C3.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(testData.C3NodeRef);

                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_C);
                transferServiceImpl.setDescriptorService(mockedDescriptorService);

                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();

                NodeRef C3Deleted = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, testData.C3NodeRef.getId());
                nodes.add(C3Deleted);

                definition.setNodes(nodes);
                definition.setSync(false);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node A3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A3NodeRef)));
                assertFalse("dest node C3 not deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));
                assertFalse("dest node C4 not deleted", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C4NodeRef)));
                assertFalse("A3 still invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A3NodeRef), TransferModel.ASPECT_ALIEN));
                assertFalse("A2 still invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A2NodeRef), TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
    } // test multi repo sync

    // Utility methods below.
    private TransferTarget createTransferTarget(String name)
    {
        String title = "title";
        String description = "description";
        String endpointProtocol = "http";
        String endpointHost = "MARKR02";
        int endpointPort = 7080;
        String endpointPath = "/alfresco/service/api/transfer";
        String username = "admin";
        char[] password = "admin".toCharArray();

        /**
         * Now go ahead and create our first transfer target
         */
        TransferTarget target = transferService.createAndSaveTransferTarget(name, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
        return target;
    }

    /**
     * Test the transfer method behaviour with respect to move and alien nodes.
     * 
     * So we have Repository A transferring content and Repository B is the local repo that we
     * move alien nodes in and out.
     * 
     * Tree
     * <pre>
     *         B1
     *    |          |         |
     *    C2(p1)    C3(p2)     A4
     *    |          
     *    A5        
     *    |
     *    B6
     * </pre>
     * 
     * Step 1: Tansfer in C's nodes to Repo B
     * 
     * Step 2. Transfer in A's nodes to Repo B
     * 
     * Setup tree above. Validat that A2 is child of C2 dest.
     * A4 is a child of B1
     * 
     * Step 3. Move A5 from C2 to C3 via transfer.   
     * C2Dest should stop being invaded by A5, C3Dest should be invaded by A5.
     * 
     * Step 4. Invade A5 by B6. Move from C3 to C2 via transfer.
     * C2Dest should be invaded by A and B.
     * C3Dest should not be invaded.
     * 
     * Step 5. Move A5 to A4.
     * A4 should be invaded by B due to B6 but not by A.
     * C2Dest should not be invaded.
     */
    public void testMultiRepoTransferMove() throws Exception
    {
        setDefaultRollback(false);

        final String localRepositoryId = descriptorService.getCurrentRepositoryDescriptor().getId();

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.GERMAN;
        final String CONTENT_STRING = "Hello";

        final String targetName = "testMultiRepoTransferMove";
        class TestData
        {
            TransferTarget transferMe;
            NodeRef S0NodeRef;
            NodeRef A1NodeRef;
            NodeRef B1NodeRef;
            NodeRef C1NodeRef;
            NodeRef C2NodeRef;
            NodeRef C3NodeRef;
            NodeRef A4NodeRef;
            NodeRef A5NodeRef;
            NodeRef B6NodeRef;
            NodeRef C2DummyNodeRef;
            NodeRef C3DummyNodeRef;
        }
        final TestData testData = new TestData();
        final QName C2Path = QName.createQName("p2");
        final QName C3Path = QName.createQName("p3");

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0);

                {
                    /**
                     * Node Source - located under guest home
                     */
                    String name = GUID.generate();
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_FOLDER);
                    testData.S0NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.S0NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.S0NodeRef, ContentModel.PROP_NAME, name);
                }

                {
                    // Node A1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A1"), ContentModel.TYPE_FOLDER);
                    testData.A1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_TITLE, "A1");
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_NAME, "A1");
                }

                {
                    // Node B1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B1"), ContentModel.TYPE_FOLDER);
                    testData.B1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_TITLE, "B1");
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_NAME, "B1");
                }

                {
                    // Node C1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("C1"), ContentModel.TYPE_FOLDER);
                    testData.C1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C1NodeRef, ContentModel.PROP_TITLE, "C1");
                    nodeService.setProperty(testData.C1NodeRef, ContentModel.PROP_NAME, "C1");
                }

                {
                    // Node C2
                    ChildAssociationRef child = nodeService.createNode(testData.C1NodeRef, ContentModel.ASSOC_CONTAINS, C2Path, ContentModel.TYPE_FOLDER);
                    testData.C2NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C2NodeRef, ContentModel.PROP_TITLE, "C2");
                    nodeService.setProperty(testData.C2NodeRef, ContentModel.PROP_NAME, "C2");
                }

                {
                    // Node C3
                    ChildAssociationRef child = nodeService.createNode(testData.C1NodeRef, ContentModel.ASSOC_CONTAINS, C3Path, ContentModel.TYPE_FOLDER);
                    testData.C3NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C3NodeRef, ContentModel.PROP_TITLE, "C3");
                    nodeService.setProperty(testData.C3NodeRef, ContentModel.PROP_NAME, "C3");
                }

                {
                    // Node C2 (Dummy)
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, C2Path, ContentModel.TYPE_FOLDER);
                    testData.C2DummyNodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C2DummyNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.C2DummyNodeRef, ContentModel.PROP_NAME, "C2 Dummy");
                }

                {
                    // Node C3 (Dummy)
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, C3Path, ContentModel.TYPE_FOLDER);
                    testData.C3DummyNodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C3DummyNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.C3DummyNodeRef, ContentModel.PROP_NAME, "C3 Dummy");
                }

                {
                    // Node A4
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("C4"), ContentModel.TYPE_FOLDER);
                    testData.A4NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_TITLE, "A4");
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_NAME, "A4");
                }

                {
                    // Node A5
                    ChildAssociationRef child = nodeService.createNode(testData.C2DummyNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A5"), ContentModel.TYPE_FOLDER);
                    testData.A5NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_TITLE, "A5");
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_NAME, "A5");
                }

                // Create the transfer target if it does not already exist
                if (!transferService.targetExists(targetName))
                {
                    testData.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    testData.transferMe = transferService.getTransferTarget(targetName);
                }
                return null;
            }
        });
        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - Map path from A1 to B1 (So transfer will transfer by path)
         *  - Map path from C1 to B1
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map Project A to Project B
        // Map Project C to Project B
        pathMap.add(new Pair(nodeService.getPath(testData.A1NodeRef), nodeService.getPath(testData.B1NodeRef)));
        pathMap.add(new Pair(nodeService.getPath(testData.C1NodeRef), nodeService.getPath(testData.B1NodeRef)));

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_C);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        /**
         * Step 1
         * Now transfer in C's nodes to Repo B
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();
                nodes.add(testData.C1NodeRef);
                nodes.add(testData.C2NodeRef);
                nodes.add(testData.C3NodeRef);
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node C2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C2NodeRef)));
                assertTrue("dest node C3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));

                // Check that C3 dest is a child of B1
                ChildAssociationRef C3Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.C3NodeRef));
                assertEquals("A3 dest is connected to the wrong node", C3Ref.getParentRef(), testData.B1NodeRef);
                ChildAssociationRef C2Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.C2NodeRef));
                assertEquals("A2 dest is connected to the wrong node", C2Ref.getParentRef(), testData.B1NodeRef);
                return null;
            }
        });
        mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        /**
         * Step 2
         * Now transfer in A's nodes
         * C2 (Dest) gets invaded by A4
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();
                nodes.add(testData.A4NodeRef);
                nodes.add(testData.A5NodeRef);
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node A5 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));
                assertTrue("dest node C3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));
                assertTrue("dest node C2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C2NodeRef)));
                ChildAssociationRef A5Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A5NodeRef));
                assertEquals("A5 dest is connected to the wrong node", A5Ref.getParentRef(), testNodeFactory.getMappedNodeRef(testData.C2NodeRef));
                assertTrue("C2 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C2NodeRef), TransferModel.ASPECT_ALIEN));
                assertFalse("C3 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C3NodeRef), TransferModel.ASPECT_ALIEN));

                ChildAssociationRef A4Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A4NodeRef));
                assertEquals("A4 dest is connected to the wrong node", A4Ref.getParentRef(), testData.B1NodeRef);

                return null;
            }
        });
        /**
         * Step 3
         * Now move A5
         * C3 (Dest) gets invaded by A5
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.moveNode(testData.A5NodeRef, testData.C3DummyNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("C4"));
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();
                nodes.add(testData.A5NodeRef);
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node A5 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));
                assertTrue("dest node C3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));
                assertTrue("dest node C2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C2NodeRef)));

                // Check that A4 dest is a child of C3Dest which is a child of B1
                ChildAssociationRef A5Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A5NodeRef));
                assertEquals("A5 dest is connected to the wrong node", A5Ref.getParentRef(), testNodeFactory.getMappedNodeRef(testData.C3NodeRef));
                assertTrue("A5 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A5NodeRef), TransferModel.ASPECT_ALIEN));
                assertTrue("C3 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C3NodeRef), TransferModel.ASPECT_ALIEN));
                assertFalse("C2 dest is still invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C2NodeRef), TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * Step 4 - multi invasion move via transfer service.
         * Invade A5 by B6.  
         * Transfer from C3 back to C2.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.moveNode(testData.A5NodeRef, testData.C2DummyNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B6"));

                // Node B6
                ChildAssociationRef child = nodeService
                        .createNode(testNodeFactory.getMappedNodeRef(testData.A5NodeRef), ContentModel.ASSOC_CONTAINS, QName.createQName("B6"), ContentModel.TYPE_FOLDER);
                testData.B6NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B6NodeRef, ContentModel.PROP_TITLE, "B6");
                nodeService.setProperty(testData.B6NodeRef, ContentModel.PROP_NAME, "B6");
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();
                nodes.add(testData.A5NodeRef);
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node A5 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));
                assertTrue("dest node C3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));
                assertTrue("dest node C2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C2NodeRef)));

                // Check that A4 dest is a child of C2Dest which is a child of B1
                ChildAssociationRef A5Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A5NodeRef));
                ChildAssociationRef B6Ref = nodeService.getPrimaryParent(testData.B6NodeRef);
                assertEquals("A5 dest is connected to the wrong node", A5Ref.getParentRef(), testNodeFactory.getMappedNodeRef(testData.C2NodeRef));
                assertEquals("B6 connected to the wrong node", B6Ref.getParentRef(), testNodeFactory.getMappedNodeRef(testData.A5NodeRef));
                assertTrue("A5 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A5NodeRef), TransferModel.ASPECT_ALIEN));
                assertTrue("C2 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C2NodeRef), TransferModel.ASPECT_ALIEN));
                assertFalse("C3 dest is still invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C3NodeRef), TransferModel.ASPECT_ALIEN));
                List<String> invaders = (List<String>) nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.C2NodeRef), TransferModel.PROP_INVADED_BY);
                assertTrue("invaders is too small", invaders.size() > 1);
                assertTrue("invaders does not contain REPO A", invaders.contains(REPO_ID_A));
                assertTrue("invaders does not contain REPO B", invaders.contains(localRepositoryId));
                return null;
            }
        });
        /**
         * Step 5 Move
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.moveNode(testData.A5NodeRef, testData.A4NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A5"));
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();
                nodes.add(testData.A5NodeRef);
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node A5 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));
                assertTrue("dest node C3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));
                assertTrue("dest node C2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C2NodeRef)));

                // Check that A5dest is a child of A4Dest which is a child of B1
                ChildAssociationRef A5Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A5NodeRef));
                assertEquals("A5 dest is connected to the wrong node", A5Ref.getParentRef(), testNodeFactory.getMappedNodeRef(testData.A4NodeRef));
                assertTrue("A4 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A4NodeRef), TransferModel.ASPECT_ALIEN));
                assertTrue("A5 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A5NodeRef), TransferModel.ASPECT_ALIEN));
                assertTrue("B6 dest is not invaded", nodeService.hasAspect(testData.B6NodeRef, TransferModel.ASPECT_ALIEN));
                assertFalse("C2 dest is still invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C2NodeRef), TransferModel.ASPECT_ALIEN));
                assertFalse("C3 dest is still invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C3NodeRef), TransferModel.ASPECT_ALIEN));

                List<String> invaders = (List<String>) nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A4NodeRef), TransferModel.PROP_INVADED_BY);
                assertTrue("invaders is too big", invaders.size() < 2);
                assertFalse("invaders contains REPO A", invaders.contains(REPO_ID_A));
                assertTrue("invaders does not contains REPO B", invaders.contains(REPO_ID_B));
                return null;
            }
        });
    }

    /**
     * Test the behaviour with regard to copying transferred nodes.
     * <p>
     * Transfer node read only
     * <p>
     * Copy transferred node.
     * <p>
     * New node should not be locked and should not be transferred.
     * <p>
     * This is a unit test so it does some shenanigans to send to the same instance of alfresco.
     */
    public void testCopyTransferredNode() throws Exception
    {
        setDefaultRollback(false);

        final String CONTENT_TITLE = "ContentTitle";

        /**
         * Now go ahead and create our transfer target
         */
        final String targetName = "testCopyTransferredNode";
        class TestData
        {
            TransferTarget transferMe;
            NodeRef S0NodeRef;
            NodeRef A1NodeRef;
            NodeRef A2NodeRef;
            NodeRef A3NodeRef;
            NodeRef B1NodeRef;
            NodeRef B2NodeRef;
        }
        final TestData testData = new TestData();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0);

                /**
                 * Node Source - located under guest home
                 */
                {
                    String name = GUID.generate();
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_FOLDER);
                    testData.S0NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.S0NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.S0NodeRef, ContentModel.PROP_NAME, name);
                }

                {
                    // Node A1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A1"), ContentModel.TYPE_FOLDER);
                    testData.A1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_TITLE, "A1");
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_NAME, "A1");
                }

                {
                    // Node A2
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A2"), ContentModel.TYPE_FOLDER);
                    testData.A2NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_TITLE, "A2");
                    nodeService.setProperty(testData.A2NodeRef, ContentModel.PROP_NAME, "A2");
                }

                {
                    // Node A3
                    ChildAssociationRef child = nodeService.createNode(testData.A2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A3"), ContentModel.TYPE_FOLDER);
                    testData.A3NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_TITLE, "A3");
                    nodeService.setProperty(testData.A3NodeRef, ContentModel.PROP_NAME, "A3");
                }

                {
                    // Node B1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B1"), ContentModel.TYPE_FOLDER);
                    testData.B1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_TITLE, "B1");
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_NAME, "B1");
                }

                {
                    // Node B2
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B2"), ContentModel.TYPE_FOLDER);
                    testData.B2NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_TITLE, "B2");
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_NAME, "B2");
                }

                if (!transferService.targetExists(targetName))
                {
                    testData.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    testData.transferMe = transferService.getTransferTarget(targetName);
                }
                return null;
            }
        });
        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - Map path from A1 to B1 (So transfer will transfer by path)
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();

        // Map Project A to Project B
        pathMap.add(new Pair(nodeService.getPath(testData.A1NodeRef), nodeService.getPath(testData.B1NodeRef)));

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        /**
         * Step 1
         */
        logger.debug("First transfer - ");
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Transfer our transfer target node
                 */
                {
                    TransferDefinition definition = new TransferDefinition();
                    Set<NodeRef> nodes = new HashSet<NodeRef>();
                    nodes.add(testData.A2NodeRef);
                    nodes.add(testData.A3NodeRef);
                    definition.setNodes(nodes);
                    definition.setReadOnly(true);
                    transferService.transfer(targetName, definition);
                }
                return null;
            }
        });

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now validate that the target node exists with the correct permissions
                NodeRef A2destNodeRef = testNodeFactory.getMappedNodeRef(testData.A2NodeRef);
                assertTrue("dest node ref does not exist", nodeService.exists(A2destNodeRef));

                /**
                 * Copy the node A2 Dest
                 */
                NodeRef copiedNode = copyService.copy(A2destNodeRef, testData.B2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A2Copy"));
                assertTrue("copied node does not exist", nodeService.exists(copiedNode));
                System.out.println("copied node is " + copiedNode);
                assertFalse("copied node still has transferred aspect", nodeService.hasAspect(copiedNode, TransferModel.ASPECT_TRANSFERRED));
                assertNull("copied node still has from repository id", nodeService.getProperty(copiedNode, TransferModel.PROP_FROM_REPOSITORY_ID));
                assertNull("copied node still has original repository id", nodeService.getProperty(copiedNode, TransferModel.PROP_REPOSITORY_ID));
                Set<QName> aspects = nodeService.getAspects(copiedNode);

                /**
                 * Copy a chain of transferred nodes - well A2dest and A3dest
                 */
                copiedNode = copyService.copy(A2destNodeRef, testData.B2NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A2Copy2"), true);
                assertTrue("copied node does not exist", nodeService.exists(copiedNode));
                System.out.println("copied node is " + copiedNode);
                assertFalse("copied node still has transferred aspect", nodeService.hasAspect(copiedNode, TransferModel.ASPECT_TRANSFERRED));
                assertNull("copied node still has from repository id", nodeService.getProperty(copiedNode, TransferModel.PROP_FROM_REPOSITORY_ID));
                assertNull("copied node still has original repository id", nodeService.getProperty(copiedNode, TransferModel.PROP_REPOSITORY_ID));

                List<ChildAssociationRef> children = nodeService.getChildAssocs(copiedNode);
                for (ChildAssociationRef child : children)
                {
                    assertFalse("copied node still has transferred aspect", nodeService.hasAspect(child.getChildRef(), TransferModel.ASPECT_TRANSFERRED));
                    assertNull("copied node still has from repository id", nodeService.getProperty(child.getChildRef(), TransferModel.PROP_FROM_REPOSITORY_ID));
                    assertNull("copied node still has original repository id", nodeService.getProperty(child.getChildRef(), TransferModel.PROP_REPOSITORY_ID));
                }
                return null;
            }
        });
    }

    /**
     * Test the behaviour with respect to copy of alien nodes.
     * 
     * So we have Repository A transferring content and Repository B is the local repo that we
     * copy alien nodes in and out.    Copied nodes are not "transferred" so they change 
     * from being "from repository A" to "from the local repository".
     * 
     * Tree
     * <pre>
     *         B1
     *    |          |         |
     *    C2(p1)    C3(p2)     A4
     *    |          
     *    A5        
     *    |
     *    B6
     * </pre>
     * 
     * Step 1: Tansfer in C's nodes to Repo B
     * 
     * Step 2. Transfer in A's nodes to Repo B
     * 
     * Setup tree above. Validat that A2 is child of C2 dest.
     * A4 is a child of B1
     * 
     * Step 3. Copy A5 from C2 to C3.  
     * C2Dest should still be invaded by A5
     * C3Dest should be invaded by A5(copy 1) which is now a "B" invader.
     * 
     * Step 4. Copy A5 from C2 to A4.  
     * C2Dest should still be invaded by A5
     * A4Dest should be invaded by A5(copy 2) which is now a "B" invader.
     *    
     * Step 5. Invade A5 dest with B6.
     * Copy A5(Dest) to B1 (A5 Copy 3)
     * B1 should not be invaded.
     * A5 Copy 3 not invaded.
     * B6 Copy not invaded.
     * 
     * Step 6. Invade A5 dest with B6.
     * Copy A5(Dest) to B1 (A5 Copy 3) with children
     * B1 should not be invaded.
     * A5 Copy 4 not invaded.
     * B6 Copy not invaded.
     * 
     */
    public void testCopyAlien() throws Exception
    {
        setDefaultRollback(false);

        final String CONTENT_TITLE = "ContentTitle";
        final String CONTENT_TITLE_UPDATED = "ContentTitleUpdated";
        final Locale CONTENT_LOCALE = Locale.GERMAN;
        final String CONTENT_STRING = "Hello";

        final String targetName = "testCopyAlien";
        class TestData
        {
            TransferTarget transferMe;
            NodeRef S0NodeRef;
            NodeRef A1NodeRef;
            NodeRef B1NodeRef;
            NodeRef C1NodeRef;
            NodeRef C2NodeRef;
            NodeRef C3NodeRef;
            NodeRef A4NodeRef;
            NodeRef A5NodeRef;
            NodeRef B6NodeRef;
            NodeRef C2DummyNodeRef;
            NodeRef C3DummyNodeRef;
        }
        final TestData testData = new TestData();

        final QName C2Path = QName.createQName("p2");
        final QName C3Path = QName.createQName("p3");

        final String localRepositoryId = descriptorService.getCurrentRepositoryDescriptor().getId();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                /**
                 * Get guest home
                 */
                String guestHomeQuery = "/app:company_home/app:guest_home";
                ResultSet guestHomeResult = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, guestHomeQuery);
                assertEquals("", 1, guestHomeResult.length());
                NodeRef guestHome = guestHomeResult.getNodeRef(0);

                {
                    /**
                     * Node Source - located under guest home
                     */
                    String name = GUID.generate();
                    ChildAssociationRef child = nodeService.createNode(guestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(name), ContentModel.TYPE_FOLDER);
                    testData.S0NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.S0NodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.S0NodeRef, ContentModel.PROP_NAME, name);
                }

                {
                    // Node A1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A1"), ContentModel.TYPE_FOLDER);
                    testData.A1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_TITLE, "A1");
                    nodeService.setProperty(testData.A1NodeRef, ContentModel.PROP_NAME, "A1");
                }

                {
                    // Node B1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("B1"), ContentModel.TYPE_FOLDER);
                    testData.B1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_TITLE, "B1");
                    nodeService.setProperty(testData.B1NodeRef, ContentModel.PROP_NAME, "B1");
                }

                {
                    // Node C1
                    ChildAssociationRef child = nodeService.createNode(testData.S0NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("C1"), ContentModel.TYPE_FOLDER);
                    testData.C1NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C1NodeRef, ContentModel.PROP_TITLE, "C1");
                    nodeService.setProperty(testData.C1NodeRef, ContentModel.PROP_NAME, "C1");
                }

                {
                    // Node C2
                    ChildAssociationRef child = nodeService.createNode(testData.C1NodeRef, ContentModel.ASSOC_CONTAINS, C2Path, ContentModel.TYPE_FOLDER);
                    testData.C2NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C2NodeRef, ContentModel.PROP_TITLE, "C2");
                    nodeService.setProperty(testData.C2NodeRef, ContentModel.PROP_NAME, "C2");
                }

                {
                    // Node C3
                    ChildAssociationRef child = nodeService.createNode(testData.C1NodeRef, ContentModel.ASSOC_CONTAINS, C3Path, ContentModel.TYPE_FOLDER);
                    testData.C3NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C3NodeRef, ContentModel.PROP_TITLE, "C3");
                    nodeService.setProperty(testData.C3NodeRef, ContentModel.PROP_NAME, "C3");
                }

                {
                    // Node C2 (Dummy)
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, C2Path, ContentModel.TYPE_FOLDER);
                    testData.C2DummyNodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C2DummyNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.C2DummyNodeRef, ContentModel.PROP_NAME, "C2 Dummy");
                }

                {
                    // Node C3 (Dummy)
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, C3Path, ContentModel.TYPE_FOLDER);
                    testData.C3DummyNodeRef = child.getChildRef();
                    nodeService.setProperty(testData.C3DummyNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
                    nodeService.setProperty(testData.C3DummyNodeRef, ContentModel.PROP_NAME, "C3 Dummy");
                }

                {
                    // Node A4
                    ChildAssociationRef child = nodeService.createNode(testData.A1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("C4"), ContentModel.TYPE_FOLDER);
                    testData.A4NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_TITLE, "A4");
                    nodeService.setProperty(testData.A4NodeRef, ContentModel.PROP_NAME, "A4");
                }

                {
                    // Node A5
                    ChildAssociationRef child = nodeService.createNode(testData.C2DummyNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A5"), ContentModel.TYPE_FOLDER);
                    testData.A5NodeRef = child.getChildRef();
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_TITLE, "A5");
                    nodeService.setProperty(testData.A5NodeRef, ContentModel.PROP_NAME, "A5");
                }

                // Create the transfer target if it does not already exist
                if (!transferService.targetExists(targetName))
                {
                    testData.transferMe = createTransferTarget(targetName);
                }
                else
                {
                    testData.transferMe = transferService.getTransferTarget(targetName);
                }
                return null;
            }
        });
        /**
         *  For unit test 
         *  - replace the HTTP transport with the in-process transport
         *  - Map path from A1 to B1 (So transfer will transfer by path)
         *  - Map path from C1 to B1
         */
        TransferTransmitter transmitter = new UnitTestInProcessTransmitterImpl(receiver, contentService, transactionService);
        transferServiceImpl.setTransmitter(transmitter);
        final UnitTestTransferManifestNodeFactory testNodeFactory = new UnitTestTransferManifestNodeFactory(this.transferManifestNodeFactory);
        transferServiceImpl.setTransferManifestNodeFactory(testNodeFactory);
        List<Pair<Path, Path>> pathMap = testNodeFactory.getPathMap();
        // Map Project A to Project B
        // Map Project C to Project B
        pathMap.add(new Pair(nodeService.getPath(testData.A1NodeRef), nodeService.getPath(testData.B1NodeRef)));
        pathMap.add(new Pair(nodeService.getPath(testData.C1NodeRef), nodeService.getPath(testData.B1NodeRef)));

        DescriptorService mockedDescriptorService = getMockDescriptorService(REPO_ID_C);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        /**
         * Step 1
         * Now transfer in C's nodes to Repo B
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();
                nodes.add(testData.C1NodeRef);
                nodes.add(testData.C2NodeRef);
                nodes.add(testData.C3NodeRef);
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node C2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C2NodeRef)));
                assertTrue("dest node C3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));

                // Check that C3 dest is a child of B1
                ChildAssociationRef C3Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.C3NodeRef));
                assertEquals("A3 dest is connected to the wrong node", C3Ref.getParentRef(), testData.B1NodeRef);
                ChildAssociationRef C2Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.C2NodeRef));
                assertEquals("A2 dest is connected to the wrong node", C2Ref.getParentRef(), testData.B1NodeRef);
                return null;
            }
        });
        mockedDescriptorService = getMockDescriptorService(REPO_ID_A);
        transferServiceImpl.setDescriptorService(mockedDescriptorService);

        /**
         * Step 2
         * Now transfer in A's nodes
         * C2 (Dest) gets invaded by A4
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TransferDefinition definition = new TransferDefinition();
                Collection<NodeRef> nodes = new ArrayList<NodeRef>();
                nodes.add(testData.A4NodeRef);
                nodes.add(testData.A5NodeRef);
                definition.setNodes(nodes);
                definition.setSync(true);
                transferService.transfer(targetName, definition);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue("dest node A5 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A5NodeRef)));
                assertTrue("dest node C3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));
                assertTrue("dest node C2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C2NodeRef)));
                ChildAssociationRef A5Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A5NodeRef));
                assertEquals("A5 dest is connected to the wrong node", A5Ref.getParentRef(), testNodeFactory.getMappedNodeRef(testData.C2NodeRef));
                assertTrue("C2 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C2NodeRef), TransferModel.ASPECT_ALIEN));
                assertFalse("C3 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C3NodeRef), TransferModel.ASPECT_ALIEN));

                ChildAssociationRef A4Ref = nodeService.getPrimaryParent(testNodeFactory.getMappedNodeRef(testData.A4NodeRef));
                assertEquals("A4 dest is connected to the wrong node", A4Ref.getParentRef(), testData.B1NodeRef);

                return null;
            }
        });
       /** 
        * Step 3. Copy A5 from C2 to C3.  
        * C2Dest should still be invaded by A5
        * C3Dest should be invaded by A5(copy) which is now a "B/local" invader.
        */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef copyRef = copyService.copy(testNodeFactory.getMappedNodeRef(testData.A5NodeRef), testNodeFactory.getMappedNodeRef(testData.C3NodeRef), ContentModel.ASSOC_CONTAINS, QName
                        .createQName("A5 Copy 1"));
                assertTrue("dest node C3 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C3NodeRef)));
                assertTrue("dest node C2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C2NodeRef)));

                assertTrue("A5(copy 1) is not invaded", nodeService.hasAspect(copyRef, TransferModel.ASPECT_ALIEN));
                assertTrue("C3 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C3NodeRef), TransferModel.ASPECT_ALIEN));
                assertTrue("C2 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C2NodeRef), TransferModel.ASPECT_ALIEN));

                List<String> C2invaders = (List<String>) nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.C2NodeRef), TransferModel.PROP_INVADED_BY);
                List<String> C3invaders = (List<String>) nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.C3NodeRef), TransferModel.PROP_INVADED_BY);
                assertTrue("C3 invaders contains local repository Id", C3invaders.contains(localRepositoryId));
                assertFalse("C3 invaders contains REPO_ID_A", C3invaders.contains(REPO_ID_A));

                assertFalse("C2 invaders contains local repository Id", C2invaders.contains(localRepositoryId));
                assertTrue("C2 invaders contains REPO_ID_A", C2invaders.contains(REPO_ID_A));

                return null;
            }
        });
       /**
        * Step 4. Copy A5 from C2 to A4.  
        * C2Dest should still be invaded by A5
        * A4Dest should be invaded by A5(copy 2) which is now a "B" invader.
        */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef copyRef = copyService.copy(testNodeFactory.getMappedNodeRef(testData.A5NodeRef), testNodeFactory.getMappedNodeRef(testData.A4NodeRef), ContentModel.ASSOC_CONTAINS, QName
                        .createQName("A5 Copy 2"));
                assertTrue("dest node A4 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.A4NodeRef)));
                assertTrue("dest node C2 does not exist", nodeService.exists(testNodeFactory.getMappedNodeRef(testData.C2NodeRef)));

                assertTrue("A5(copy 2) is not invaded", nodeService.hasAspect(copyRef, TransferModel.ASPECT_ALIEN));
                assertTrue("A4 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A4NodeRef), TransferModel.ASPECT_ALIEN));
                assertTrue("C2 dest is not invaded", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.C2NodeRef), TransferModel.ASPECT_ALIEN));

                List<String> C2invaders = (List<String>) nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.C2NodeRef), TransferModel.PROP_INVADED_BY);
                List<String> A4invaders = (List<String>) nodeService.getProperty(testNodeFactory.getMappedNodeRef(testData.A4NodeRef), TransferModel.PROP_INVADED_BY);
                assertTrue("A4 invaders contains local repository Id", A4invaders.contains(localRepositoryId));

                assertFalse("C2 invaders contains local repository Id", C2invaders.contains(localRepositoryId));
                assertTrue("C2 invaders contains REPO_ID_A", C2invaders.contains(REPO_ID_A));

                return null;
            }
        });
        /**
         * Step 5. Invade A5 dest with B6.
         * Copy A5(Dest) to B1 (A5 Copy 3) no children
         * B1 should not be invaded.
         * A5 Copy 3 not invaded.
         * B6 Copy not invaded.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ChildAssociationRef child = nodeService
                        .createNode(testNodeFactory.getMappedNodeRef(testData.A5NodeRef), ContentModel.ASSOC_CONTAINS, QName.createQName("B6"), ContentModel.TYPE_FOLDER);
                testData.B6NodeRef = child.getChildRef();
                nodeService.setProperty(testData.B6NodeRef, ContentModel.PROP_TITLE, "B6");
                nodeService.setProperty(testData.B6NodeRef, ContentModel.PROP_NAME, "B6");

                assertTrue("A4 dest is not invaded prior to test - test error", nodeService.hasAspect(testNodeFactory.getMappedNodeRef(testData.A4NodeRef), TransferModel.ASPECT_ALIEN));

                NodeRef copyRef = copyService.copy(testNodeFactory.getMappedNodeRef(testData.A5NodeRef), testData.B1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A5 Copy 3"));

                assertFalse("B1 is invaded", nodeService.hasAspect(testData.B1NodeRef, TransferModel.ASPECT_ALIEN));
                assertFalse("A5 copy 3 is invaded", nodeService.hasAspect(copyRef, TransferModel.ASPECT_ALIEN));
                return null;
            }
        });
        /**
         * Step 6. Invade A5 dest with B6.
         * Copy A5(Dest) to B1 (A5 Copy 3) with children
         * B1 should not be invaded.
         * A5 Copy 4 not invaded.
         * B6 Copy not invaded.
         */
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertFalse("B1 is invaded prior to test - test error", nodeService.hasAspect(testData.B1NodeRef, TransferModel.ASPECT_ALIEN));

                NodeRef copyRef = copyService.copy(testNodeFactory.getMappedNodeRef(testData.A5NodeRef), testData.B1NodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("A5 Copy 4"), true);
                assertFalse("B1 is invaded", nodeService.hasAspect(testData.B1NodeRef, TransferModel.ASPECT_ALIEN));
                assertFalse("A5 copy 4 is invaded", nodeService.hasAspect(copyRef, TransferModel.ASPECT_ALIEN));

                List<ChildAssociationRef> refs = nodeService.getChildAssocs(copyRef);

                assertTrue("can't find child of A5 copy 4", refs.size() == 1);

                for (ChildAssociationRef ref : refs)
                {
                    assertFalse("B6 copy is invaded", nodeService.hasAspect(ref.getChildRef(), TransferModel.ASPECT_ALIEN));
                }
                return null;
            }
        });
    } // copy node

    private void createUser(String userName, String password)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, password.toCharArray());

            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            this.personService.createPerson(ppOne);
        }
    }

    private DescriptorService getMockDescriptorService(String repositoryId)
    {
        DescriptorService descriptorService = mock(DescriptorService.class);
        Descriptor descriptor = mock(Descriptor.class);

        when(descriptor.getId()).thenReturn(repositoryId);
        when(descriptorService.getCurrentRepositoryDescriptor()).thenReturn(descriptor);
        when(descriptorService.getServerDescriptor()).thenReturn(serverDescriptor);

        return descriptorService;
    }
}
