/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.thumbnail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.thumbnail.script.ScriptThumbnailService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.thumbnail.FailedThumbnailInfo;
import org.alfresco.service.cmr.thumbnail.ThumbnailException;
import org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.TempFileProvider;

/**
 * Thumbnail service implementation unit test
 * 
 * @author Roy Wetherall
 * @author Neil McErlean
 */
public class ThumbnailServiceImplTest extends BaseAlfrescoSpringTest
{
    private RenditionService renditionService;
    private ThumbnailService thumbnailService;
    private ScriptThumbnailService scriptThumbnailService;
    private ScriptService scriptService;
    private MimetypeMap mimetypeMap;
    private RetryingTransactionHelper transactionHelper;
    private ServiceRegistry services;
    private NodeRef folder;

    /**
     * Called during the transaction setup
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();

        // Get the required services
        this.renditionService = (RenditionService) this.applicationContext.getBean("RenditionService");
        this.thumbnailService = (ThumbnailService) this.applicationContext.getBean("ThumbnailService");
        this.scriptThumbnailService = (ScriptThumbnailService) this.applicationContext.getBean("thumbnailServiceScript");
        this.mimetypeMap = (MimetypeMap) this.applicationContext.getBean("mimetypeService");
        this.scriptService = (ScriptService) this.applicationContext.getBean("ScriptService");
        this.services = (ServiceRegistry) this.applicationContext.getBean("ServiceRegistry");
        this.transactionHelper = (RetryingTransactionHelper) this.applicationContext.getBean("retryingTransactionHelper");

        // Create a folder and some content
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        this.folder = this.nodeService.createNode(this.rootNodeRef, ContentModel.ASSOC_CHILDREN,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"), ContentModel.TYPE_FOLDER)
                    .getChildRef();
    }

    private void checkTransformer()
    {
        ContentTransformer transformer = this.contentService.getImageTransformer();
        assertNotNull("No transformer returned for 'getImageTransformer'", transformer);

        // Check that it is working
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        if (!transformer.isTransformable(MimetypeMap.MIMETYPE_IMAGE_JPEG, MimetypeMap.MIMETYPE_IMAGE_JPEG,
                    imageTransformationOptions))
        {
            fail("Image transformer is not working.  Please check your image conversion command setup.");
        }
    }

    public void testCreateRenditionThumbnailFromImage() throws Exception
    {
        QName qname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "doclib");

        ThumbnailDefinition details = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(
                    qname.getLocalName());
        assertEquals("doclib", details.getName());
        assertEquals("image/png", details.getMimetype());
        assertEquals("alfresco/thumbnail/thumbnail_placeholder_doclib.png", details.getPlaceHolderResourcePath());

        checkTransformer();

        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);

        NodeRef thumbnail0 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, details.getTransformationOptions(), "doclib");
        assertNotNull(thumbnail0);
        checkRenditioned(jpgOrig, "doclib");
        checkRendition("doclib", thumbnail0);
        outputThumbnailTempContentLocation(thumbnail0, "jpg", "doclib test");
    }
    
    public void testCreateRenditionThumbnailFromPdf() throws Exception
    {
        QName qname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "doclib");

        ThumbnailDefinition details = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(
                    qname.getLocalName());
        assertEquals("doclib", details.getName());
        assertEquals("image/png", details.getMimetype());
        assertEquals("alfresco/thumbnail/thumbnail_placeholder_doclib.png", details.getPlaceHolderResourcePath());

        checkTransformer();

        NodeRef pdfOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_PDF);

        NodeRef thumbnail0 = this.thumbnailService.createThumbnail(pdfOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, details.getTransformationOptions(), "doclib");
        assertNotNull(thumbnail0);
        checkRenditioned(pdfOrig, "doclib");
        checkRendition("doclib", thumbnail0);
        outputThumbnailTempContentLocation(thumbnail0, "jpg", "doclib test");
    }

    public void testCreateThumbnailFromImage() throws Exception
    {
        checkTransformer();

        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        NodeRef gifOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_GIF);

        // ===== small: 64x64, marked as thumbnail ====

        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);
        // ThumbnailDetails createOptions = new ThumbnailDetails();

        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions, "small");
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig, "small");
        checkRendition("small", thumbnail1);
        outputThumbnailTempContentLocation(thumbnail1, "jpg", "small - 64x64, marked as thumbnail");

        // ===== small2: 64x64, aspect not maintained ====

        ImageResizeOptions imageResizeOptions2 = new ImageResizeOptions();
        imageResizeOptions2.setWidth(64);
        imageResizeOptions2.setHeight(64);
        imageResizeOptions2.setMaintainAspectRatio(false);
        ImageTransformationOptions imageTransformationOptions2 = new ImageTransformationOptions();
        imageTransformationOptions2.setResizeOptions(imageResizeOptions2);
        // ThumbnailDetails createOptions2 = new ThumbnailDetails();
        NodeRef thumbnail2 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions2, "small2");
        checkRenditioned(jpgOrig, "small2");
        checkRendition("small2", thumbnail2);
        outputThumbnailTempContentLocation(thumbnail2, "jpg", "small2 - 64x64, aspect not maintained");

        // ===== half: 50%x50 =====

        ImageResizeOptions imageResizeOptions3 = new ImageResizeOptions();
        imageResizeOptions3.setWidth(50);
        imageResizeOptions3.setHeight(50);
        imageResizeOptions3.setPercentResize(true);
        ImageTransformationOptions imageTransformationOptions3 = new ImageTransformationOptions();
        imageTransformationOptions3.setResizeOptions(imageResizeOptions3);
        // ThumbnailDetails createOptions3 = new ThumbnailDetails();
        NodeRef thumbnail3 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions3, "half");
        checkRenditioned(jpgOrig, "half");
        checkRendition("half", thumbnail3);
        outputThumbnailTempContentLocation(thumbnail3, "jpg", "half - 50%x50%");

        // ===== half2: 50%x50 from gif =====

        ImageResizeOptions imageResizeOptions4 = new ImageResizeOptions();
        imageResizeOptions4.setWidth(50);
        imageResizeOptions4.setHeight(50);
        imageResizeOptions4.setPercentResize(true);
        ImageTransformationOptions imageTransformationOptions4 = new ImageTransformationOptions();
        imageTransformationOptions4.setResizeOptions(imageResizeOptions4);
        // ThumbnailDetails createOptions4 = new ThumbnailDetails();
        NodeRef thumbnail4 = this.thumbnailService.createThumbnail(gifOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions4, "half2");
        checkRenditioned(gifOrig, "half2");
        checkRendition("half2", thumbnail4);
        outputThumbnailTempContentLocation(thumbnail4, "jpg", "half2 - 50%x50%, from gif");
    }

    public void testDuplicationNames() throws Exception
    {
        checkTransformer();

        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);
        // ThumbnailDetails createOptions = new ThumbnailDetails();
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions, "small");
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig, "small");
        checkRendition("small", thumbnail1);

        // the origional thumbnail is returned if we are attempting to create a duplicate
        NodeRef duplicate = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, MimetypeMap.MIMETYPE_IMAGE_JPEG,
                        imageTransformationOptions, "small");
        assertNotNull(duplicate);
        assertEquals(duplicate, thumbnail1);
 
    }

    /**
     * @since 3.5.0
     */
    public void testCreateFailingThumbnail() throws Exception
    {
        final NodeRef corruptNode = this.createCorruptedContent(folder);
        logger.debug("Running failing thumbnail on " + corruptNode);
        
        // Make sure the source node is correctly set up before we start
        // It should not be renditioned and should not be marked as having any failed thumbnails.
        assertFalse(nodeService.hasAspect(corruptNode, RenditionModel.ASPECT_RENDITIONED));
        assertFalse(nodeService.hasAspect(corruptNode, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE));
        
        setComplete();
        endTransaction();

        // Attempt to perform a thumbnail that we know will fail.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    ThumbnailDefinition thumbnailDef = thumbnailService.getThumbnailRegistry().getThumbnailDefinition("doclib");
                    
                    Action createThumbnailAction = ThumbnailHelper.createCreateThumbnailAction(thumbnailDef, services);
                    actionService.executeAction(createThumbnailAction, corruptNode, true, true);
                    return null;
                }
            });
        // The thumbnail attempt has now failed. But a compensating action should have been scheduled that will mark the
        // source node with a failure aspect. As that is an asynchronous action, we need to wait for that to complete.
        
        Thread.sleep(3000); // This should be long enough for the compensating action to run.

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertFalse("corrupt node should not have renditioned aspect", nodeService.hasAspect(corruptNode, RenditionModel.ASPECT_RENDITIONED));
                assertTrue("corrupt node should have failed thumbnails aspect", nodeService.hasAspect(corruptNode, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE));

                Map<String, FailedThumbnailInfo> failedThumbnails = thumbnailService.getFailedThumbnails(corruptNode);
                assertEquals("Wrong number of failed thumbnails", 1, failedThumbnails.size());
              
                assertTrue("Missing QName for failed thumbnail", failedThumbnails.containsKey("doclib"));
                final FailedThumbnailInfo doclibFailureInfo = failedThumbnails.get("doclib");
                assertNotNull("Failure info was null", doclibFailureInfo);
                assertEquals("Failure count was wrong.", 1, doclibFailureInfo.getFailureCount());
                assertEquals("thumbnail name was wrong.", "doclib", doclibFailureInfo.getThumbnailDefinitionName());

                return null;
            }
        });
        
        // If you uncomment this line and set the timeout to a value greater than ${system.thumbnail.minimum.retry.period} * 1000.
        // Then the retry period will have passed, the below re-thumbnail attempt will be made and the test will fail with a
        // failureCount == 2.
        //
        // Thread.sleep(150 * 1000);

        // Run the thumbnail again. It should not run because the action condition should prevent it.
        // We can check that it does not run by ensuring the failureCount does not change.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        ThumbnailDefinition thumbnailDef = thumbnailService.getThumbnailRegistry().getThumbnailDefinition("doclib");
                        
                        Action createThumbnailAction = ThumbnailHelper.createCreateThumbnailAction(thumbnailDef, services);
                        actionService.executeAction(createThumbnailAction, corruptNode, true, true);
                        return null;
                    }
                });
        // Pause to let the async action be considered for running (but not run).
        Thread.sleep(3000);
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        Map<String, FailedThumbnailInfo> failedThumbnails = thumbnailService.getFailedThumbnails(corruptNode);
                        assertEquals("Wrong number of failed thumbnails", 1, failedThumbnails.size());
                      
                        assertTrue("Missing QName for failed thumbnail", failedThumbnails.containsKey("doclib"));
                        final FailedThumbnailInfo doclibFailureInfo = failedThumbnails.get("doclib");
                        assertNotNull("Failure info was null", doclibFailureInfo);
                        assertEquals("Failure count was wrong.", 1, doclibFailureInfo.getFailureCount());
                        assertEquals("thumbnail name was wrong.", "doclib", doclibFailureInfo.getThumbnailDefinitionName());

                        return null;
                    }
                });
    }


    public void testThumbnailUpdate() throws Exception
    {
        checkTransformer();

        // First create a thumbnail
        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions, "small");

        // Thumbnails should always be of type cm:thumbnail.
        assertEquals(ContentModel.TYPE_THUMBNAIL, nodeService.getType(thumbnail1));

        // Update the thumbnail
        this.thumbnailService.updateThumbnail(thumbnail1, imageTransformationOptions);
        
        // ALF-2047. Thumbnails were changing to type cm:content after update.
        assertEquals(ContentModel.TYPE_THUMBNAIL, nodeService.getType(thumbnail1));
    }

    public void testGetThumbnailByName() throws Exception
    {
        checkTransformer();

        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);

        // Check for missing thumbnail
        NodeRef result1 = this.thumbnailService.getThumbnailByName(jpgOrig, ContentModel.PROP_CONTENT, "small");
        assertNull("The thumbnail 'small' should have been missing", result1);

        // Create the thumbnail
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);
        this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, MimetypeMap.MIMETYPE_IMAGE_JPEG,
                    imageTransformationOptions, "small");

        // Try and retrieve the thumbnail
        NodeRef result2 = this.thumbnailService.getThumbnailByName(jpgOrig, ContentModel.PROP_CONTENT, "small");
        assertNotNull(result2);
        checkRendition("small", result2);

        // Check for an other thumbnail that doesn't exist
        NodeRef result3 = this.thumbnailService.getThumbnailByName(jpgOrig, ContentModel.PROP_CONTENT, "anotherone");
        assertNull("The thumbnail 'anotherone' should have been missing", result3);
    }

    private void checkRenditioned(NodeRef thumbnailed, String assocName)
    {
        assertTrue("Renditioned aspect should have been applied", this.nodeService.hasAspect(thumbnailed,
                    RenditionModel.ASPECT_RENDITIONED));
        if (assocName != null)
        {
            List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(thumbnailed, RegexQNamePattern.MATCH_ALL,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, assocName));
            assertNotNull(assocs);
            assertEquals(1, assocs.size());
        }
    }

    private void checkRendition(String thumbnailName, NodeRef thumbnail)
    {
        // Check the thumbnail is of the correct type
        assertTrue("Thumbnail should have been a rendition",
                renditionService.isRendition(thumbnail));

        // Check the name
        if (thumbnailName != null)
        {
            assertEquals(thumbnailName, this.nodeService.getProperty(thumbnail, ContentModel.PROP_NAME));
        }

        // Check the content property value
        assertEquals(ContentModel.PROP_CONTENT, this.nodeService.getProperty(thumbnail,
                    ContentModel.PROP_CONTENT_PROPERTY_NAME));
        
        // Check the thumbnail is of type cm:thumbnail.
        assertEquals("The thumbnail node should be of type cm:thumbnail!",
                    ContentModel.TYPE_THUMBNAIL, nodeService.getType(thumbnail));
        
        // Check the thumbnail name property is correctly set on thumbnail.
        assertEquals( thumbnailName, nodeService.getProperty(thumbnail, ContentModel.PROP_THUMBNAIL_NAME));
    }

    private void outputThumbnailTempContentLocation(NodeRef thumbnail, String ext, String message) throws IOException
    {
        File tempFile = TempFileProvider.createTempFile("thumbnailServiceImplTest", "." + ext);
        ContentReader reader = this.contentService.getReader(thumbnail, ContentModel.PROP_CONTENT);
        reader.getContent(tempFile);
        System.out.println(message + ": " + tempFile.getPath());
    }

    /**
     * This method creates a node under the specified folder whose content is
     * taken from the quick file corresponding to the specified MIME type.
     * 
     * @param parentFolder
     * @param mimetype
     * @return
     * @throws IOException
     */
    private NodeRef createOriginalContent(NodeRef parentFolder, String mimetype) throws IOException
    {
        String ext = this.mimetypeMap.getExtension(mimetype);
        File origFile = AbstractContentTransformerTest.loadQuickTestFile(ext);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "origional." + ext);
        NodeRef node = this.nodeService.createNode(parentFolder, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "original." + ext),
                    ContentModel.TYPE_CONTENT, props).getChildRef();

        ContentWriter writer = this.contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetype);
        writer.setEncoding("UTF-8");
        writer.putContent(origFile);

        return node;
    }
    
    private NodeRef createCorruptedContent(NodeRef parentFolder) throws IOException
    {
        // The below pdf file has been truncated such that it is identifiable as a PDF but otherwise corrupt.
        File corruptPdfFile = AbstractContentTransformerTest.loadNamedQuickTestFile("quickCorrupt.pdf");
        assertNotNull("Failed to load required test file.", corruptPdfFile);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, "corrupt.pdf");
        NodeRef node = this.nodeService.createNode(parentFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "quickCorrupt.pdf"),
                ContentModel.TYPE_CONTENT, props).getChildRef();

        nodeService.setProperty(node, ContentModel.PROP_CONTENT, new ContentData(null,
                    MimetypeMap.MIMETYPE_PDF, 0L, null));
        ContentWriter writer = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
        writer.setEncoding("UTF-8");
        writer.putContent(corruptPdfFile);
        
        return node;
    }
    
    @SuppressWarnings("deprecation")
    public void testAutoUpdate() throws Exception
    {
        checkTransformer();

        final NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);

        ThumbnailDefinition details = this.thumbnailService.getThumbnailRegistry().getThumbnailDefinition("medium");
        @SuppressWarnings("unused")
        final NodeRef thumbnail = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, details
                    .getMimetype(), details.getTransformationOptions(), details.getName());

        setComplete();
        endTransaction();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                String ext = ThumbnailServiceImplTest.this.mimetypeMap.getExtension(MimetypeMap.MIMETYPE_IMAGE_JPEG);
                File origFile = AbstractContentTransformerTest.loadQuickTestFile(ext);

                ContentWriter writer = ThumbnailServiceImplTest.this.contentService.getWriter(jpgOrig,
                            ContentModel.PROP_CONTENT, true);
                writer.putContent(origFile);

                return null;
            }
        });

        // TODO
        // this test should wait for the async action to run .. will need to
        // commit transaction for that thou!

        // Thread.sleep(1000);
    }

    public void testHTMLToImageAndSWF() throws Exception
    {
        NodeRef nodeRef = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_HTML);
        ThumbnailDefinition def = this.thumbnailService.getThumbnailRegistry().getThumbnailDefinition("medium");

        ContentTransformer transformer = this.contentService.getTransformer(MimetypeMap.MIMETYPE_HTML, def
                    .getMimetype(), def.getTransformationOptions());
        if (transformer != null)
        {
            NodeRef thumb = this.thumbnailService.createThumbnail(nodeRef, ContentModel.PROP_CONTENT,
                        def.getMimetype(), def.getTransformationOptions(), def.getName());
            assertNotNull(thumb);
            ContentReader reader = this.contentService.getReader(thumb, ContentModel.PROP_CONTENT);
            assertNotNull(reader);
            assertEquals(def.getMimetype(), reader.getMimetype());
            assertTrue(reader.getSize() != 0);
        }

        def = this.thumbnailService.getThumbnailRegistry().getThumbnailDefinition("webpreview");
        if (transformer != null)
        {
            NodeRef thumb = this.thumbnailService.createThumbnail(nodeRef, ContentModel.PROP_CONTENT,
                        def.getMimetype(), def.getTransformationOptions(), def.getName());
            assertNotNull(thumb);
            ContentReader reader = this.contentService.getReader(thumb, ContentModel.PROP_CONTENT);
            assertNotNull(reader);
            assertEquals(def.getMimetype(), reader.getMimetype());
            assertTrue(reader.getSize() != 0);
        }
    }
    
    public void testThumbnailServiceCreateApi() throws Exception
    {
        // Create a second folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>();
        folderProps.put(ContentModel.PROP_NAME, "otherTestFolder");
        NodeRef otherFolder = this.nodeService.createNode(this.rootNodeRef, ContentModel.ASSOC_CHILDREN,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "otherTestFolder"), ContentModel.TYPE_FOLDER)
                    .getChildRef();

        checkTransformer();
        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);
        
        // Create thumbnail - same MIME type
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                MimetypeMap.MIMETYPE_IMAGE_JPEG, imageTransformationOptions, "smallJpeg");
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig, "smallJpeg");
        checkRendition("smallJpeg", thumbnail1);
        outputThumbnailTempContentLocation(thumbnail1, "jpg", "smallJpeg - 64x64, marked as thumbnail");

        // Create thumbnail - different MIME type
        thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                MimetypeMap.MIMETYPE_IMAGE_PNG, imageTransformationOptions, "smallPng");
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig, "smallPng");
        checkRendition("smallPng", thumbnail1);
        outputThumbnailTempContentLocation(thumbnail1, "png", "smallPng - 64x64, marked as thumbnail");
        
        // Create thumbnail - different content property
        // TODO
        
        // Create thumbnail - different command options
        // We'll pass illegal command options to ImageMagick in order to trigger an exception
        Exception x = null;
        try
        {
            imageTransformationOptions.setCommandOptions("-noSuchOption");
            thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                    MimetypeMap.MIMETYPE_IMAGE_PNG, imageTransformationOptions, "smallCO");
        } catch (ContentIOException ciox)
        {
            x = ciox;
            ciox.printStackTrace();
        }
        assertNotNull("Expected exception from ImageMagick due to invalid option", x);
        // Reset the command options
        imageTransformationOptions.setCommandOptions("");
        
        
        // Create thumbnail - different target assoc details
        ThumbnailParentAssociationDetails tpad
            = new ThumbnailParentAssociationDetails(otherFolder,
                    QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "foo"),
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "bar"));
        thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                MimetypeMap.MIMETYPE_IMAGE_PNG, imageTransformationOptions, "targetDetails", tpad);
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig, "targetDetails");
        checkRendition("targetDetails", thumbnail1);
        outputThumbnailTempContentLocation(thumbnail1, "png", "targetDetails - 64x64, marked as thumbnail");

        
        
        // Create thumbnail - null thumbnail name
        thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT,
                MimetypeMap.MIMETYPE_IMAGE_PNG, imageTransformationOptions, null);
        assertNotNull(thumbnail1);
        checkRenditioned(jpgOrig, null);
        checkRendition(null, thumbnail1);
        outputThumbnailTempContentLocation(thumbnail1, "png", "'null' - 64x64, marked as thumbnail");
    }

    public void testRegistry()
    {
        ThumbnailRegistry thumbnailRegistry = this.thumbnailService.getThumbnailRegistry();
        List<ThumbnailDefinition> defs = thumbnailRegistry.getThumbnailDefinitions(MimetypeMap.MIMETYPE_HTML);
        System.out.println("Definitions ...");
        for (ThumbnailDefinition def : defs)
        {
            System.out.println("Thumbnail Available: " + def.getName());
        }
    }

    // == Test the JavaScript API ==

    public void testJSAPI() throws Exception
    {
        NodeRef jpgOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        NodeRef gifOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_GIF);
        NodeRef pdfOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_PDF);
        NodeRef docOrig = createOriginalContent(this.folder, MimetypeMap.MIMETYPE_WORD);

        Map<String, Object> model = new HashMap<String, Object>(2);
        model.put("jpgOrig", jpgOrig);
        model.put("gifOrig", gifOrig);
        model.put("pdfOrig", pdfOrig);
        model.put("docOrig", docOrig);

        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/thumbnail/script/test_thumbnailAPI.js");
        this.scriptService.executeScript(location, model);
    }

    /**
     * This test method tests the thumbnail placeholders which are handled in the {@link ScriptThumbnailService}.
     * See ALF-6566.
     */
    public void testPlaceHoldersByMimeType() throws Exception
    {
        // Retrieve the classpath paths for all the standard icon resources for doclib.
        
        final String standardDoclibIcon = "alfresco/thumbnail/thumbnail_placeholder_doclib.png";
        
        // This used to be the cogs, but as of ALF-6566 is a generic document icon.
        String doclibIcon = scriptThumbnailService.getPlaceHolderResourcePath("doclib");
        assertEquals(standardDoclibIcon, doclibIcon);
        
        // The same but with explicit null mimetype.
        doclibIcon = scriptThumbnailService.getMimeAwarePlaceHolderResourcePath("doclib", null);
        assertEquals(standardDoclibIcon, doclibIcon);

        // The icon for a .doc mime type - a sample, recognised mime type.
        String docxDoclibIcon = scriptThumbnailService.getMimeAwarePlaceHolderResourcePath("doclib", MimetypeMap.MIMETYPE_WORD);
        assertEquals("alfresco/thumbnail/thumbnail_placeholder_doclib_doc.png", docxDoclibIcon);

        // The icon for an unrecognised mime type.
        String fallbackDoclibIcon = scriptThumbnailService.getMimeAwarePlaceHolderResourcePath("doclib", "application/wibble");
        assertEquals(standardDoclibIcon, fallbackDoclibIcon);
        
        // And one from the 'medium' set.
        String mediumIcon = scriptThumbnailService.getPlaceHolderResourcePath("medium");
        final String standardMediumIcon = "alfresco/thumbnail/thumbnail_placeholder_medium.jpg"; // This one jpg, not png
        assertEquals(standardMediumIcon, mediumIcon);
    }
}
