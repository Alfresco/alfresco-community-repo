/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.thumbnail.CreateOptions;
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
        
        // Create a folder and some content
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        this.folder = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
    }
	
    public void testCreateThumbnailFromImage() throws Exception
    {
        NodeRef jpgOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        NodeRef gifOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_GIF);
        
        // ===== small: 64x64, marked as thumbnail ====
        
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);     
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);        
        CreateOptions createOptions = new CreateOptions(
                                                    MimetypeMap.MIMETYPE_IMAGE_JPEG,
                                                    imageTransformationOptions,
                                                    "small");        
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, createOptions);        
        assertNotNull(thumbnail1);
        checkThumbnailed(jpgOrig, "small");
        checkThumbnail(thumbnail1, imageTransformationOptions);
        outputThumbnailTempContentLocation(thumbnail1, "jpg", "small - 64x64, marked as thumbnail");
        
        // ===== small2: 64x64, aspect not maintained ====
        
        ImageResizeOptions imageResizeOptions2 = new ImageResizeOptions();
        imageResizeOptions2.setWidth(64);
        imageResizeOptions2.setHeight(64);   
        imageResizeOptions2.setMaintainAspectRatio(false);
        ImageTransformationOptions imageTransformationOptions2 = new ImageTransformationOptions();
        imageTransformationOptions2.setResizeOptions(imageResizeOptions2);        
        CreateOptions createOptions2 = new CreateOptions(
                                                    MimetypeMap.MIMETYPE_IMAGE_JPEG,
                                                    imageTransformationOptions2,
                                                    "small2");  
        NodeRef thumbnail2 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, createOptions2);
        checkThumbnailed(jpgOrig, "small2");
        checkThumbnail(thumbnail2, imageTransformationOptions2);
        outputThumbnailTempContentLocation(thumbnail2, "jpg", "small2 - 64x64, aspect not maintained");
        
        // ===== half: 50%x50  =====
        
        ImageResizeOptions imageResizeOptions3 = new ImageResizeOptions();
        imageResizeOptions3.setWidth(50);
        imageResizeOptions3.setHeight(50);   
        imageResizeOptions3.setPercentResize(true);
        ImageTransformationOptions imageTransformationOptions3 = new ImageTransformationOptions();
        imageTransformationOptions3.setResizeOptions(imageResizeOptions3);        
        CreateOptions createOptions3 = new CreateOptions(
                                                    MimetypeMap.MIMETYPE_IMAGE_JPEG,
                                                    imageTransformationOptions3,
                                                    "half");  
        NodeRef thumbnail3 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, createOptions3);
        checkThumbnailed(jpgOrig, "half");
        checkThumbnail(thumbnail3, imageTransformationOptions3);
        outputThumbnailTempContentLocation(thumbnail3, "jpg", "half - 50%x50%");
        
        
        // ===== half2: 50%x50 from gif  =====
        
        ImageResizeOptions imageResizeOptions4 = new ImageResizeOptions();
        imageResizeOptions4.setWidth(50);
        imageResizeOptions4.setHeight(50);   
        imageResizeOptions4.setPercentResize(true);
        ImageTransformationOptions imageTransformationOptions4 = new ImageTransformationOptions();
        imageTransformationOptions4.setResizeOptions(imageResizeOptions4);        
        CreateOptions createOptions4 = new CreateOptions(
                                                    MimetypeMap.MIMETYPE_IMAGE_JPEG,
                                                    imageTransformationOptions4,
                                                    "half2");  
        NodeRef thumbnail4 = this.thumbnailService.createThumbnail(gifOrig, ContentModel.PROP_CONTENT, createOptions4);
        checkThumbnailed(gifOrig, "half2");
        checkThumbnail(thumbnail4, imageTransformationOptions4);
        outputThumbnailTempContentLocation(thumbnail4, "jpg", "half2 - 50%x50%, from gif");
        
    }
    
    public void testDuplicationNames()
        throws Exception
    {
        NodeRef jpgOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);     
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);        
        CreateOptions createOptions = new CreateOptions(
                                                    MimetypeMap.MIMETYPE_IMAGE_JPEG,
                                                    imageTransformationOptions,
                                                    "small");        
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, createOptions);        
        assertNotNull(thumbnail1);
        checkThumbnailed(jpgOrig, "small");
        checkThumbnail(thumbnail1, imageTransformationOptions);
        
        try
        {
            this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, createOptions);
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
        // First create a thumbnail
        NodeRef jpgOrig = createOrigionalContent(this.folder, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setWidth(64);
        imageResizeOptions.setHeight(64);     
        imageResizeOptions.setResizeToThumbnail(true);
        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);        
        CreateOptions createOptions = new CreateOptions(
                                                    MimetypeMap.MIMETYPE_IMAGE_JPEG,
                                                    imageTransformationOptions,
                                                    "small");        
        NodeRef thumbnail1 = this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, createOptions);
        
        // Update the thumbnail
        this.thumbnailService.updateThumbnail(thumbnail1);
        
        
    }
    
    public void testGetThumbnailByName()
        throws Exception
    {
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
        CreateOptions createOptions = new CreateOptions(
                                                    MimetypeMap.MIMETYPE_IMAGE_JPEG,
                                                    imageTransformationOptions,
                                                    "small");        
        this.thumbnailService.createThumbnail(jpgOrig, ContentModel.PROP_CONTENT, createOptions);   
        
        // Try and retrieve the thumbnail
        NodeRef result2 = this.thumbnailService.getThumbnailByName(jpgOrig, ContentModel.PROP_CONTENT, "small");
        assertNotNull(result2);
        checkThumbnail(result2, imageTransformationOptions);
        
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
    
    private void checkThumbnail(NodeRef thumbnail, TransformationOptions transformationOptions)
    {
        // Check the thumbnail is of the correct type
        assertEquals(ContentModel.TYPE_THUMBNAIL, this.nodeService.getType(thumbnail));
        
        // Check the meta data on the thumnail
        assertEquals(ContentModel.PROP_CONTENT, this.nodeService.getProperty(thumbnail, ContentModel.PROP_CONTENT_PROPERTY_NAME));
        assertEquals(transformationOptions.getClass().getName(), this.nodeService.getProperty(thumbnail, ContentModel.PROP_TRANSFORMATION_OPTIONS_CLASS));
        
        if (transformationOptions instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imageTransformationOptions = (ImageTransformationOptions)transformationOptions;            
            assertTrue(this.nodeService.hasAspect(thumbnail, ImageTransformationOptions.ASPECT_IMAGE_TRANSFORATION_OPTIONS));
            assertEquals(
                    imageTransformationOptions.getCommandOptions(), 
                    this.nodeService.getProperty(thumbnail, ImageTransformationOptions.PROP_COMMAND_OPTIONS));
            ImageResizeOptions resizeOptions = imageTransformationOptions.getResizeOptions();
            if (resizeOptions != null)
            {
                assertTrue((Boolean)this.nodeService.getProperty(thumbnail, ImageTransformationOptions.PROP_RESIZE));
                assertEquals(
                    imageTransformationOptions.getResizeOptions().getHeight(),
                    this.nodeService.getProperty(thumbnail, ImageTransformationOptions.PROP_RESIZE_HEIGHT));
                assertEquals(
                    imageTransformationOptions.getResizeOptions().getWidth(),
                    this.nodeService.getProperty(thumbnail, ImageTransformationOptions.PROP_RESIZE_WIDTH));
                assertEquals(
                        imageTransformationOptions.getResizeOptions().isMaintainAspectRatio(),
                        this.nodeService.getProperty(thumbnail, ImageTransformationOptions.PROP_RESIZE_MAINTAIN_ASPECT_RATIO));
                assertEquals(
                        imageTransformationOptions.getResizeOptions().isPercentResize(),
                        this.nodeService.getProperty(thumbnail, ImageTransformationOptions.PROP_RESIZE_PERCENT));
                assertEquals(
                        imageTransformationOptions.getResizeOptions().isResizeToThumbnail(),
                        this.nodeService.getProperty(thumbnail, ImageTransformationOptions.PROP_RESIZE_TO_THUMBNAIL));
            }
            else
            {
                assertFalse((Boolean)this.nodeService.getProperty(thumbnail, ImageTransformationOptions.PROP_RESIZE));
            }
            
        }
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

}
