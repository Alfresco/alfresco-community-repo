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
package org.alfresco.repo.thumbnail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.thumbnail.ThumbnailException;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.BaseAlfrescoSpringTest;

/**
 * Thumbnail service implementation unit test
 * 
 * @author Roy Wetherall
 */
public class ThumbnailServiceImplTest extends BaseAlfrescoSpringTest 
{
    private ThumbnailService thumbnailService;    
    private ScriptService scriptService;
    private MimetypeMap mimetypeMap;    
    private NodeRef folder;
    
    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the required services
        this.thumbnailService = (ThumbnailService)this.applicationContext.getBean("ThumbnailService");
        this.mimetypeMap = (MimetypeMap)this.applicationContext.getBean("mimetypeService");
        this.scriptService = (ScriptService)this.applicationContext.getBean("ScriptService");
        
        // Create a folder and some content
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        this.folder = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
    }

    private void checkTransformer()
    {
        ContentTransformer transformer = this.contentService.getImageTransformer();
        if (transformer == null)
        {
            fail("No transformer returned for 'getImageTransformer'");
        }
        // Check that it is working
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        if (!transformer.isTransformable(
                MimetypeMap.MIMETYPE_IMAGE_JPEG,
                MimetypeMap.MIMETYPE_IMAGE_JPEG,
                imageTransformationOptions))
        {
            fail("Image transformer is not working.  Please check your image conversion command setup.");
        }
    }
    
    public void testCreateThumbnailFromImage() throws Exception
    {
        checkTransformer();
        
        NodeRef jpgOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        NodeRef gifOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_GIF);
        
        // ===== small: 64x64, marked as thumbnail ====
        
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);     
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);        
        //ThumbnailDetails createOptions = new ThumbnailDetails();     
        
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, 
                MimetypeMap.MIMETYPE_IMAGE_JPEG,
                imageTransformationOptions,
                "small");        
        assertNotNull(thumbnail1);
        checkThumbnailed(jpgOrig, "small");
        checkThumbnail("small", thumbnail1);
        outputThumbnailTempContentLocation(thumbnail1, "jpg", "small - 64x64, marked as thumbnail");
        
        // ===== small2: 64x64, aspect not maintained ====
        
        ImageResizeOptions imageResizeOptions2 = new ImageResizeOptions();
        imageResizeOptions2.setWidth(64);
        imageResizeOptions2.setHeight(64);   
        imageResizeOptions2.setMaintainAspectRatio(false);
        ImageTransformationOptions imageTransformationOptions2 = new ImageTransformationOptions();
        imageTransformationOptions2.setResizeOptions(imageResizeOptions2);        
        //ThumbnailDetails createOptions2 = new ThumbnailDetails();  
        NodeRef thumbnail2 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, 
                MimetypeMap.MIMETYPE_IMAGE_JPEG,
                imageTransformationOptions2,
                "small2");
        checkThumbnailed(jpgOrig, "small2");
        checkThumbnail("small2", thumbnail2);
        outputThumbnailTempContentLocation(thumbnail2, "jpg", "small2 - 64x64, aspect not maintained");
        
        // ===== half: 50%x50  =====
        
        ImageResizeOptions imageResizeOptions3 = new ImageResizeOptions();
        imageResizeOptions3.setWidth(50);
        imageResizeOptions3.setHeight(50);   
        imageResizeOptions3.setPercentResize(true);
        ImageTransformationOptions imageTransformationOptions3 = new ImageTransformationOptions();
        imageTransformationOptions3.setResizeOptions(imageResizeOptions3);        
        // ThumbnailDetails createOptions3 = new ThumbnailDetails();  
        NodeRef thumbnail3 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, 
                MimetypeMap.MIMETYPE_IMAGE_JPEG,
                imageTransformationOptions3,
                "half");
        checkThumbnailed(jpgOrig, "half");
        checkThumbnail("half", thumbnail3);
        outputThumbnailTempContentLocation(thumbnail3, "jpg", "half - 50%x50%");
        
        
        // ===== half2: 50%x50 from gif  =====
        
        ImageResizeOptions imageResizeOptions4 = new ImageResizeOptions();
        imageResizeOptions4.setWidth(50);
        imageResizeOptions4.setHeight(50);   
        imageResizeOptions4.setPercentResize(true);
        ImageTransformationOptions imageTransformationOptions4 = new ImageTransformationOptions();
        imageTransformationOptions4.setResizeOptions(imageResizeOptions4);        
        // ThumbnailDetails createOptions4 = new ThumbnailDetails();  
        NodeRef thumbnail4 = this.thumbnailService.createThumbnail(gifOrig, ContentModel.PROP_CONTENT, 
                MimetypeMap.MIMETYPE_IMAGE_JPEG,
                imageTransformationOptions4,
                "half2");
        checkThumbnailed(gifOrig, "half2");
        checkThumbnail("half2", thumbnail4);
        outputThumbnailTempContentLocation(thumbnail4, "jpg", "half2 - 50%x50%, from gif");
    }
    
    public void testDuplicationNames()
        throws Exception
    {
        checkTransformer();
        
        NodeRef jpgOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);     
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);        
        //ThumbnailDetails createOptions = new ThumbnailDetails();        
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, 
                MimetypeMap.MIMETYPE_IMAGE_JPEG,
                imageTransformationOptions,
                "small");        
        assertNotNull(thumbnail1);
        checkThumbnailed(jpgOrig, "small");
        checkThumbnail("small", thumbnail1);
        
        try
        {
            this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, 
                    MimetypeMap.MIMETYPE_IMAGE_JPEG,
                    imageTransformationOptions,
                    "small");
            fail("A duplicate exception should have been raised");
        }
        catch (ThumbnailException exception)
        {
            // OK since this should have been thrown
        }
    }
    
    public void testThumbnailUpdate() 
        throws Exception
    {
        checkTransformer();
        
        // First create a thumbnail
        NodeRef jpgOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);     
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);              
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, 
                MimetypeMap.MIMETYPE_IMAGE_JPEG,
                imageTransformationOptions,
                "small");
        
        // Update the thumbnail
        this.thumbnailService.updateThumbnail(thumbnail1, imageTransformationOptions);
    }
    
    public void testGetThumbnailByName()
        throws Exception
    {
        checkTransformer();
        
        NodeRef jpgOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        
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
        this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, 
                MimetypeMap.MIMETYPE_IMAGE_JPEG,
                imageTransformationOptions,
                "small");   
        
        // Try and retrieve the thumbnail
        NodeRef result2 = this.thumbnailService.getThumbnailByName(jpgOrig, ContentModel.PROP_CONTENT, "small");
        assertNotNull(result2);
        checkThumbnail("small", result2);
        
        // Check for an other thumbnail that doesn't exist
        NodeRef result3 = this.thumbnailService.getThumbnailByName(jpgOrig, ContentModel.PROP_CONTENT, "anotherone");
        assertNull("The thumbnail 'anotherone' should have been missing", result3);
    }
    
    // TODO test getThumbnails
    
    private void checkThumbnailed(NodeRef thumbnailed, String assocName)
    {
        assertTrue("Thumbnailed aspect should have been applied", this.nodeService.hasAspect(thumbnailed, ContentModel.ASPECT_THUMBNAILED));
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(thumbnailed, RegexQNamePattern.MATCH_ALL, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, assocName));
        assertNotNull(assocs);
        assertEquals(1, assocs.size());
    }
    
    private void checkThumbnail(String thumbnailName, NodeRef thumbnail)
    {
        // Check the thumbnail is of the correct type
        assertEquals(ContentModel.TYPE_THUMBNAIL, this.nodeService.getType(thumbnail));
        
        // Check the name 
        assertEquals(thumbnailName, this.nodeService.getProperty(thumbnail, ContentModel.PROP_THUMBNAIL_NAME));
        
        // Check the contet property value
        assertEquals(ContentModel.PROP_CONTENT, this.nodeService.getProperty(thumbnail, ContentModel.PROP_CONTENT_PROPERTY_NAME));
        
    }
    
    private void outputThumbnailTempContentLocation(NodeRef thumbnail, String ext, String message)
        throws IOException
    {
        File tempFile = File.createTempFile("thumbnailServiceImpTest", "." + ext);
        ContentReader reader = this.contentService.getReader(thumbnail, ContentModel.PROP_CONTENT);
        reader.getContent(tempFile);
        System.out.println(message + ": " + tempFile.getPath());   
    }
    
    private NodeRef createOrigionalContent(NodeRef folder, String mimetype)
        throws IOException
    {
        String ext = this.mimetypeMap.getExtension(mimetype);
        File origFile = AbstractContentTransformerTest.loadQuickTestFile(ext);
        
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "origional." + ext);        
        NodeRef node = this.nodeService.createNode(
                        folder, 
                        ContentModel.ASSOC_CONTAINS, 
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "origional." + ext),
                        ContentModel.TYPE_CONTENT, 
                        props).getChildRef();    
        
        ContentWriter writer = this.contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetype);
        writer.setEncoding("UTF-8");
        writer.putContent(origFile);
        
        return node;
    }
    
    public void testAutoUpdate() throws Exception
    {
        checkTransformer();
        
        final NodeRef jpgOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        
        ThumbnailDefinition details = this.thumbnailService.getThumbnailRegistry().getThumbnailDefinition("medium");
        @SuppressWarnings("unused")
        final NodeRef thumbnail = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, details.getMimetype(), details.getTransformationOptions(), details.getName());
        
        setComplete();
        endTransaction();
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {            
                String ext = ThumbnailServiceImplTest.this.mimetypeMap.getExtension(MimetypeMap.MIMETYPE_IMAGE_JPEG);
                File origFile = AbstractContentTransformerTest.loadQuickTestFile(ext);
                
                ContentWriter writer = ThumbnailServiceImplTest.this.contentService.getWriter(jpgOrig, ContentModel.PROP_CONTENT, true);
                writer.putContent(origFile);
        
                return null;
            }
        });
        
        // TODO 
        // this test should wait for the async action to run .. will need to commit transaction for that thou!
        
        //Thread.sleep(1000);            
    }
    
    public void testHTMLToImageAndSWF() throws Exception
    {
        NodeRef nodeRef = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_HTML);
        ThumbnailDefinition def = this.thumbnailService.getThumbnailRegistry().getThumbnailDefinition("medium");
        
        if (this.contentService.getTransformer(MimetypeMap.MIMETYPE_HTML, def.getMimetype(), def.getTransformationOptions()) != null)
        {        
            NodeRef thumb = this.thumbnailService.createThumbnail(
                    nodeRef, 
                    ContentModel.PROP_CONTENT, 
                    def.getMimetype(), 
                    def.getTransformationOptions(), 
                    def.getName());
            assertNotNull(thumb);
            ContentReader reader = this.contentService.getReader(thumb, ContentModel.PROP_CONTENT);
            assertNotNull(reader);
            assertEquals(def.getMimetype(), reader.getMimetype());
            assertTrue(reader.getSize() != 0);
        }
        

        def = this.thumbnailService.getThumbnailRegistry().getThumbnailDefinition("webpreview");
        if (this.contentService.getTransformer(MimetypeMap.MIMETYPE_HTML, def.getMimetype(), def.getTransformationOptions()) != null)
        {        
            NodeRef thumb = this.thumbnailService.createThumbnail(
                    nodeRef, 
                    ContentModel.PROP_CONTENT, 
                    def.getMimetype(), 
                    def.getTransformationOptions(), 
                    def.getName());
            assertNotNull(thumb);
            ContentReader reader = this.contentService.getReader(thumb, ContentModel.PROP_CONTENT);
            assertNotNull(reader);
            assertEquals(def.getMimetype(), reader.getMimetype());
            assertTrue(reader.getSize() != 0);
        }
    }
    
    public void testRegistry()
    {
        ThumbnailRegistry thumbnailRegistry = this.thumbnailService.getThumbnailRegistry();
        List<ThumbnailDefinition> defs = thumbnailRegistry.getThumnailDefintions(MimetypeMap.MIMETYPE_HTML);
        System.out.println("Definitions ...");
        for (ThumbnailDefinition def : defs)
        {
            System.out.println("Thumbnail Available: " + def.getName());
        }
    }
    
    
    
    // == Test the JavaScript API ==
    
    public void testJSAPI() throws Exception
    {
        NodeRef jpgOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        NodeRef gifOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_GIF);
        NodeRef pdfOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_PDF);
        NodeRef docOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_WORD);
        
        Map<String, Object> model = new HashMap<String, Object>(2);
        model.put("jpgOrig", jpgOrig);
        model.put("gifOrig", gifOrig);
        model.put("pdfOrig", pdfOrig);
        model.put("docOrig", docOrig);
        
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/thumbnail/script/test_thumbnailAPI.js");
        this.scriptService.executeScript(location, model);
    }
}
