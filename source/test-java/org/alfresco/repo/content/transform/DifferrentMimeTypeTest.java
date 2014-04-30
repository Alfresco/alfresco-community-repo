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
package org.alfresco.repo.content.transform;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.*;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;


/**
 * Tests the ContentTransformer correct work if the file extension is different from its MIME type
 */
public class DifferrentMimeTypeTest extends TestCase
{
    private static Log log = LogFactory.getLog(DifferrentMimeTypeTest.class);

    private ContentTransformer contentTransformer;
    private TransformationOptions options;
    private ServiceRegistry serviceRegistry;
    private MimetypeService mimetypeService;
    private TransformerDebug transformerDebug;
    private TransformerConfig transformerConfig;
    private ContentTransformerRegistry registry;
    private static Repository repositoryHelper;
    private File testFile;
    private File outputFile;
    private TransactionService transactionService;
    private NodeRef contentNodeRef;
    private NodeService nodeService;
    private LinkedList<NodeRef> nodesToDeleteAfterTest = new LinkedList<>();
    private ContentService contentService;

    @Before
    public void setUp() throws Exception
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        mimetypeService = serviceRegistry.getMimetypeService();
        transformerDebug = (TransformerDebug) ctx.getBean("transformerDebug");
        transformerConfig = (TransformerConfig) ctx.getBean("transformerConfig");
        registry = (ContentTransformerRegistry) ctx.getBean("contentTransformerRegistry");
        transactionService = serviceRegistry.getTransactionService();
        repositoryHelper = (Repository) ctx.getBean("repositoryHelper");
        nodeService = serviceRegistry.getNodeService();
        contentService = serviceRegistry.getContentService();

        // Load the MS Word 2003 .doc file with incorrect extension
        testFile = AbstractContentTransformerTest.loadNamedQuickTestFile("quick-differentMimetype.docx");
        options = new TransformationOptions();
        this.createContentNodeRef();
    }

    public void testDifferentMimeType() throws IOException
    {

        final QName propertyQName = ContentModel.PROP_CONTENT;
        String sourceMimeType = mimetypeService.guessMimetype(testFile.getName());
        String targetMimeType = MimetypeMap.MIMETYPE_IMAGE_JPEG;

        // mimetypeService.guessMimetype returns file mime type based entirely on the file extension
        assertEquals("Incorrect file MIME type or guessMimetype#MimetypeService implementation was changed ",
                MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, sourceMimeType);

        AuthenticationUtil.RunAsWork<ContentReader> createTargetWork = new AuthenticationUtil.RunAsWork<ContentReader>()
        {
            @Override
            public ContentReader doWork() throws Exception
            {
                return contentService.getReader(contentNodeRef, propertyQName);
            }
        };

        ContentReader contentReader =  AuthenticationUtil.runAs(createTargetWork, AuthenticationUtil.getSystemUserName());
        assertNotNull("content reader not present ", contentReader);

        String outputFileExtension = mimetypeService.getExtension(targetMimeType);
        outputFile = TempFileProvider.createTempFile("DifferentMimeTypeTest-results-", outputFileExtension);
        ContentWriter outputWriter = new FileContentWriter(outputFile);

        contentReader.setMimetype(sourceMimeType);
        outputWriter.setMimetype(targetMimeType);

        contentTransformer = this.registry.getTransformer(sourceMimeType, contentReader.getSize(), targetMimeType, options);
        assertNotNull("contentTransformer not present", contentTransformer);

        // Try to transform file with inaccurate MIME type
        contentTransformer.transform(contentReader.getReader(), outputWriter, options);
        
        // After successful transformation image size should be grater than 0
        assertTrue("File transformation failed. Output file size is '0'", outputWriter.getSize() > 0);
    }

    public void testSetUp()
    {
        assertNotNull("MimetypeMap not present", this.mimetypeService);
        assertNotNull("ServiceRegistry not present", serviceRegistry);
        assertNotNull("TransformerDebug not present", transformerDebug);
        assertNotNull("TransformerConfig not present", transformerConfig);
        assertNotNull("Transformer options not present", options);
        assertNotNull("transactionService not present", transactionService);
        assertNotNull("repositoryHelper not present", repositoryHelper);
        assertNotNull("nodeService not present", nodeService);
        assertNotNull("contentService not present", contentService);
        assertNotNull("contentNodeRef not present", contentNodeRef);
        assertNotNull("testFile was not created", testFile);
    }


    private void createContentNodeRef()
    {
        AuthenticationUtil.RunAsWork<NodeRef> createTargetWork = new AuthenticationUtil.RunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Creating temporary NodeRefs for testing.");
                }

                final NodeRef companyHome = repositoryHelper.getCompanyHome();
                // Create a folder
                Map<QName, Serializable> folderProps = new HashMap<>();
                folderProps.put(ContentModel.PROP_NAME, this.getClass().getSimpleName() + System.currentTimeMillis());
                NodeRef folderNodeRef = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS,
                        ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER, folderProps).getChildRef();

                // Mark folder for removing after test
                nodesToDeleteAfterTest.add(folderNodeRef);

                String fileName = testFile.getName();
                Map<QName, Serializable> props = new HashMap<>();
                props.put(ContentModel.PROP_NAME, fileName);

                NodeRef node = nodeService.createNode(
                        folderNodeRef,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName),
                        ContentModel.TYPE_CONTENT,
                        props).getChildRef();

                // node should be removed after tests
                nodesToDeleteAfterTest.add(node);

                ContentWriter writer = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
                String targetMimeType = mimetypeService.guessMimetype(fileName);

                writer.setMimetype(targetMimeType);
                writer.setEncoding("UTF-8");
                writer.putContent(testFile);

                return node;
            }
        };

        contentNodeRef = AuthenticationUtil.runAs(createTargetWork, AuthenticationUtil.getSystemUserName());
        this.nodesToDeleteAfterTest.add(contentNodeRef);
    }

    @After
    public void deleteTemporaryNodeRefsAndTempFiles()
    {
        // Tidy up the test nodes we created
        RetryingTransactionHelper.RetryingTransactionCallback<Void> deleteNodeCallback = new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Delete them in reverse order.
                for (Iterator<NodeRef> iter = nodesToDeleteAfterTest.descendingIterator(); iter.hasNext(); )
                {
                    NodeRef nextNodeToDelete = iter.next();

                    if (nodeService.exists(nextNodeToDelete))
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Deleting temporary node " + nextNodeToDelete);
                        }
                        nodeService.deleteNode(nextNodeToDelete);
                    }
                }
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteNodeCallback);

        outputFile.deleteOnExit();
    }
}
