/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.content.transform;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
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

import junit.framework.TestCase;


/**
 * Tests that ContentTransformers only correctly process source nodes if the
 * mimetype of the node matches the content. This is the opposite of what this
 * test class was originally written to prove for MNT-11015. The current version
 * was reworked for MNT-16381.
 */
public class DifferrentMimeTypeTest extends TestCase
{
    private static Log log = LogFactory.getLog(DifferrentMimeTypeTest.class);

    private AbstractContentTransformer2 contentTransformer;
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
        String fileName = testFile.getName();
        String sourceMimeType = mimetypeService.guessMimetype(fileName);
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

        // verify that there is a desired transformer for actual file MIME type
        String actualSourceMimetype = mimetypeService.getMimetypeIfNotMatches(contentReader);
        ContentTransformer actualTransformer = this.registry.getTransformer(actualSourceMimetype,contentReader.getSize(), targetMimeType, options);
        String assertMessageActualTransformer = "Transformer not found for converting " + actualSourceMimetype + " to " + targetMimeType;
        assertNotNull(assertMessageActualTransformer, actualTransformer);

        contentTransformer = (AbstractContentTransformer2)registry.getTransformer(sourceMimeType, contentReader.getSize(), targetMimeType, options);
        String assertMessageContentTransformer = "Transformer not found for converting " +sourceMimeType + " to " + targetMimeType;
        assertNotNull(assertMessageContentTransformer, contentTransformer);

        // Try to transform file with inaccurate MIME type
        boolean originalStrict = contentTransformer.getStrictMimeTypeCheck();
        assertTrue("Content Transformations should be 'strict' by default", originalStrict);
        for (boolean strict: new boolean[] {false, true})
        {
            try
            {
                contentTransformer.setStrictMimeTypeCheck(strict);
                contentTransformer.transform(contentReader.getReader(), outputWriter, options);
                if (strict)
                {
                    fail("The contentTransformer should have failed with an UnsupportedTransformationException");
                }
                // After successful transformation image size should be grater than 0
                assertTrue("File transformation failed. Output file size is '0'", outputWriter.getSize() > 0);
            }
            catch (UnsupportedTransformationException e)
            {
                if (!strict)
                {
                    fail("The contentTransformer should NOT have failed with an UnsupportedTransformationException "+e);
                }
                String message = e.getMessage();
                assertTrue("Message should contain the original filename ("+fileName+")",                    message.contains(fileName));
                assertTrue("Message should contain the declared source mimetype ("+sourceMimeType+")",       message.contains(sourceMimeType));
                assertTrue("Message should contain the detected source mimetype ("+actualSourceMimetype+")", message.contains(actualSourceMimetype));
            }
            finally
            {
                ((AbstractContentTransformer2)contentTransformer).setStrictMimeTypeCheck(originalStrict);
            }
        }

        // Try to transform file with accurate MIME type
        contentReader.setMimetype(actualSourceMimetype);
        actualTransformer.transform(contentReader, outputWriter, options);
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
                
                // Make sure the error message contains the file name. Without this it is null.
                nodeService.setProperty(node, ContentModel.PROP_NAME, fileName);
                options.setSourceNodeRef(node);

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
