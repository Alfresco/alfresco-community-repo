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
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentIOException;
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
    private TransactionService transactionService;
    private NodeRef contentNodeRef;
    private NodeService nodeService;
    private LinkedList<NodeRef> nodesToDeleteAfterTest = new LinkedList<>();
    private ContentService contentService;
    
    private int NEITHER = 0;
    private int RETRY = 1;
    private int STRICT = 2;
    private static boolean SUCCESS = true;
    private static boolean FAILURE = false;

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
        
        assertNotNull("MimetypeMap not present", this.mimetypeService);
        assertNotNull("ServiceRegistry not present", serviceRegistry);
        assertNotNull("TransformerDebug not present", transformerDebug);
        assertNotNull("TransformerConfig not present", transformerConfig);
        assertNotNull("transactionService not present", transactionService);
        assertNotNull("repositoryHelper not present", repositoryHelper);
        assertNotNull("nodeService not present", nodeService);
        assertNotNull("contentService not present", contentService);
    }

    // The file has the correct declared and detected mimetypes,
    // so can be processed by all approaches
    
    public void testMimetypesCorrects() throws IOException
    {
        testTransformToJPeg("quick.gif",
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            NEITHER, SUCCESS);
    }

    public void testMimetypesCorrectsRetry() throws IOException
    {
        testTransformToJPeg("quick.gif",
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            RETRY, SUCCESS);
    }

    public void testMimetypesCorrectsStrict() throws IOException
    {
        testTransformToJPeg("quick.gif",
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            STRICT, SUCCESS);
    }
    
    public void testMimetypesCorrectsRetryStrict() throws IOException
    {
        testTransformToJPeg("quick.gif",
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            RETRY|STRICT, SUCCESS);
    }
    
    // The file has the wrong extension (it is a .png rather than .docx)
    // so can only be processed by the RETRY approach. If strict the transform
    // should have been discarded before a retry could be tried.
    
    public void testWrongExtension() throws IOException
    {
        testTransformToJPeg("quick-differentMimetype.docx",
            MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
            MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
            MimetypeMap.MIMETYPE_IMAGE_PNG,
            NEITHER, FAILURE);
    }
    
    public void testWrongExtensionRetry() throws IOException
    {
        testTransformToJPeg("quick-differentMimetype.docx",
            MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
            MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
            MimetypeMap.MIMETYPE_IMAGE_PNG,
            RETRY, SUCCESS);
    }
    
    public void testWrongExtensionStrict() throws IOException
    {
        testTransformToJPeg("quick-differentMimetype.docx",
            MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
            MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
            MimetypeMap.MIMETYPE_IMAGE_PNG,
            STRICT, FAILURE);
    }
    
    public void testWrongExtensionStrictRetry() throws IOException
    {
        testTransformToJPeg("quick-differentMimetype.docx",
            MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
            MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
            MimetypeMap.MIMETYPE_IMAGE_PNG,
            RETRY|STRICT, FAILURE);
    }
    
    // The file has the correct extension and content, but the content is incorrectly detected
    // as .pdf. As .ai to .pdf is in the white list and has the same format as pdf all 3 can
    // only process the file.
    
    public void testDetectedInWhiteList() throws IOException
    {
        // The transformer can read the file as a pdf and does not know the node had the wrong mimetype.
        testTransformToJPeg("quickCS5.ai",
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_PDF,
            NEITHER, SUCCESS);
    }
    
    public void testDetectedInWhiteListRetry() throws IOException
    {
        // Same as testDetectedInWhiteList, so did not use the retry
        testTransformToJPeg("quickCS5.ai",
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_PDF,
            RETRY, SUCCESS);
    }
    
    public void testDetectedInWhiteListStrict() throws IOException
    {
        // Works because ai to pdf is in the white list, so we pass it to the transformer and it can read pdf.
        testTransformToJPeg("quickCS5.ai",
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_PDF,
            STRICT, SUCCESS);
    }
    
    public void testDetectedInWhiteListRetryStrict() throws IOException
    {
        // Works in the same way as testDetectedInWhiteListStrict.
        testTransformToJPeg("quickCS5.ai",
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_PDF,
            RETRY|STRICT, SUCCESS);
    }
    
    // The file has the correct extension and content, but has the wrong declared mimetype.
    // As .ai to .doc is not in the white list a strict check will fail.
    
    public void testDetectedNotInWhiteList() throws IOException
    {
        // The transformer for .ai to .jpg can do nothing with .doc files, so fails
        testTransformToJPeg("quick.doc",
            MimetypeMap.MIMETYPE_WORD,
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_WORD,
            NEITHER, FAILURE);
    }
    
    public void testDetectedNotInWhiteListRetry() throws IOException
    {
        // The retry fails without soffice (which is not available on Bamboo)
        testTransformToJPeg("quick.doc",
            MimetypeMap.MIMETYPE_WORD,
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_WORD,
            RETRY, FAILURE);
    }
    
    public void testDetectedNotInWhiteListStrict() throws IOException
    {
        // ai to doc not in white list
        testTransformToJPeg("quick.doc",
            MimetypeMap.MIMETYPE_WORD,
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_WORD,
            STRICT, FAILURE);
    }

    public void testDetectedNotInWhiteListRetryStrict() throws IOException
    {
        // ai to doc not in white list
        testTransformToJPeg("quick.doc",
            MimetypeMap.MIMETYPE_WORD,
            MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR,
            MimetypeMap.MIMETYPE_WORD,
            RETRY|STRICT, FAILURE);
    }

    /**
     * Method tries to transform a quick file to a JPeg, setting the declared source Mimetype.
     * @param quickname name of the quickfile.
     * @param extensionMimetype the expected mimetype of the quick file from the extension
     *        (this might be wrong - checked).
     * @param declaredMimetype source mimetype to be set on the node
     *        (this may be wrong - simply used).
     * @param detectedMimetype the expected mimetype detected from the content
     *        (this might be wrong - checked).
     * @param approach indicates that the initial transform should be using
     *        STRICT mimetype checking,
     *        RETRY with another transformer if the declared and derived mimetypes
     *        don't match, or
     *        NEITHER if it should uses the original approach with no
     *        retries and no strict checking.
     * @param expectSuccess indicates if the transform is expected to work.
     * @throws IOException if the quick file cannot be loaded.
     */
    private void testTransformToJPeg(String quickname, String extensionMimetype,
        String declaredMimetype, String detectedMimetype, int approach,
        boolean expectSuccess) throws IOException
    {
        boolean retry = (approach&RETRY) != 0;
        boolean strict = (approach&STRICT) != 0;
        
        testFile = AbstractContentTransformerTest.loadNamedQuickTestFile(quickname);
        options = new TransformationOptions();
        createContentNodeRef();
        
        String fileName = testFile.getName();
        String actualExtensionMimetype = mimetypeService.guessMimetype(fileName);
        assertEquals("The "+quickname+" extension indicates a mimetype of "+actualExtensionMimetype+
            " rather than "+extensionMimetype, extensionMimetype, actualExtensionMimetype);
        
        String targetMimeType = MimetypeMap.MIMETYPE_IMAGE_JPEG;
        String outputFileExtension = mimetypeService.getExtension(targetMimeType);

        AuthenticationUtil.RunAsWork<ContentReader> createTargetWork = new AuthenticationUtil.RunAsWork<ContentReader>()
        {
            @Override
            public ContentReader doWork() throws Exception
            {
                return contentService.getReader(contentNodeRef, ContentModel.PROP_CONTENT);
            }
        };
        ContentReader contentReader =  AuthenticationUtil.runAs(createTargetWork, AuthenticationUtil.getSystemUserName());
        contentReader.setMimetype(declaredMimetype);
        String actualDetectedMimetype = mimetypeService.getMimetypeIfNotMatches(contentReader);
        if (actualDetectedMimetype != null)
        {
            assertEquals("The mimetype detected from the content was "+actualDetectedMimetype+
                " rather than "+detectedMimetype, detectedMimetype, actualDetectedMimetype);
        }

        long size = contentReader.getSize();
        contentTransformer = (AbstractContentTransformer2)registry.getTransformer(declaredMimetype, size, targetMimeType, options);
        assertNotNull("Transformer not found", contentTransformer);
        boolean originalRetry = (Boolean)contentTransformer.getRetryTransformOnDifferentMimeType();
        boolean originalStrict = contentTransformer.getStrictMimeTypeCheck();
        assertTrue("Content Transformations should be 'retry' by default", originalRetry);
        assertTrue("Content Transformations should be 'strict' by default", originalStrict);
        
        contentTransformer.setRetryTransformOnDifferentMimeType(retry);
        contentTransformer.setStrictMimeTypeCheck(strict);
        File outputFile = createATempFileForTheDifferentMimeTypeTest(outputFileExtension);
        try
        {
            ContentWriter outputWriter = new FileContentWriter(outputFile);
            outputWriter.setMimetype(targetMimeType);
            contentTransformer.transform(contentReader.getReader(), outputWriter, options);
            if (!expectSuccess)
            {
                if (strict)
                {
                    fail("The contentTransformer should have failed with an UnsupportedTransformationException");
                }
                else
                {
                    fail("The contentTransformer should have failed with a ContentIOException");
                }
            }
            // After successful transformation image size should be grater than 0
            assertTrue("File transformation failed. Output file size is '0'", outputWriter.getSize() > 0);
        }
        catch (ContentIOException e)
        {
            if (expectSuccess)
            {
                fail("The contentTransformer should NOT have failed with an ContentIOException "+e);
                e.printStackTrace();
            }
        }
        catch (UnsupportedTransformationException e)
        {
            if (expectSuccess)
            {
                fail("The contentTransformer should NOT have failed with an UnsupportedTransformationException "+e);
                e.printStackTrace();
            }
            if (strict)
            {
                validateErrorMessage(fileName, declaredMimetype, detectedMimetype, e);
            }
        }
        finally
        {
            contentTransformer.setRetryTransformOnDifferentMimeType(originalRetry);
            contentTransformer.setStrictMimeTypeCheck(originalStrict);
            outputFile.deleteOnExit();
        }
    }

    private void validateErrorMessage(String fileName, String declaredMimetype, String detectedMimetype,
            UnsupportedTransformationException e)
    {
        String message = e.getMessage();
        assertTrue("Message should contain the original filename (" + fileName + ")", message.contains(fileName));
        assertTrue("Message should contain the declared source mimetype (" + declaredMimetype + ")",
                message.contains(declaredMimetype));
        assertTrue("Message should contain the detected source mimetype (" + detectedMimetype + ")",
                message.contains(detectedMimetype));
    }

    private File createATempFileForTheDifferentMimeTypeTest(String outputFileExtension)
    {
        return TempFileProvider.createTempFile("DifferentMimeTypeTest-results-", "." + outputFileExtension);
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
        nodesToDeleteAfterTest.add(contentNodeRef);
    }
    
    //MNT-16381 related: make sure we match a mime-type with known type aliases and don't dismiss documents as having non-matching type<->actual content
    public void testTypeAliasesMatch() throws Exception
    {
        File testFile = AbstractContentTransformerTest.loadNamedQuickTestFile("quick.xml");
        ContentReader sourceReader = new FileContentReader(testFile);
        sourceReader.setMimetype(MimetypeMap.MIMETYPE_XML); // "text/xml"
        // Detected mimetype is "application/xml"
        assertNull(mimetypeService.getMimetypeIfNotMatches(sourceReader));
       
        testFile = AbstractContentTransformerTest.loadNamedQuickTestFile("quick.bmp");
        sourceReader = new FileContentReader(testFile);
        sourceReader.setMimetype("image/bmp");
        // Detected mimetype is "image/x-ms-bmp"
        assertNull(mimetypeService.getMimetypeIfNotMatches(sourceReader));
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
    }
}
