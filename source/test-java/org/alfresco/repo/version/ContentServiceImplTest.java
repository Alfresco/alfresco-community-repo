/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.version;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.experimental.categories.Category;

/**
 * Tests for retrieving frozen content from a verioned node
 * 
 * @author Roy Wetherall
 */
@Category(OwnJVMTestsCategory.class)
public class ContentServiceImplTest extends BaseVersionStoreTest
{   
    /**
     * Test content data
     */
    private final static String UPDATED_CONTENT = "This content has been updated with a new value.";
    
    /**
     * The version content store
     */
    private ContentService contentService;    
    
    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the instance of the required content service
        this.contentService = (ContentService)this.applicationContext.getBean("contentService");
    }
    
    /**
     * Test getReader
     */
    public void testGetReader()
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        NodeRef versionNodeRef = version.getFrozenStateNodeRef();
		
        // Get the content reader for the frozen node
        ContentReader contentReader = this.contentService.getReader(versionNodeRef, ContentModel.PROP_CONTENT);
        assertNotNull(contentReader);
        assertEquals(TEST_CONTENT, contentReader.getContentString());
        
        // Now update the content and verison again
        ContentWriter contentWriter = this.contentService.getWriter(versionableNode, ContentModel.PROP_CONTENT, true);
        assertNotNull(contentWriter);
        contentWriter.putContent(UPDATED_CONTENT);        
        Version version2 = createVersion(versionableNode, this.versionProperties);
        NodeRef version2NodeRef = version2.getFrozenStateNodeRef();
		
        // Get the content reader for the new verisoned content
        ContentReader contentReader2 = this.contentService.getReader(version2NodeRef, ContentModel.PROP_CONTENT);
        assertNotNull(contentReader2);
        assertEquals(UPDATED_CONTENT, contentReader2.getContentString());
    }
    
    /**
     * Test getWriter
     */
    public void testGetWriter()
    {
        // Create a new versionable node
        NodeRef versionableNode = createNewVersionableNode();
        
        // Create a new version
        Version version = createVersion(versionableNode, this.versionProperties);
        
        // Get writer is not supported by the version content service
        try
        {
            ContentWriter contentWriter = this.contentService.getWriter(
                    version.getFrozenStateNodeRef(),
                    ContentModel.PROP_CONTENT,
                    true);
            contentWriter.putContent("bobbins");
            fail("This operation is not supported.");
        }
        catch (Exception exception)
        {
            // An exception should be raised
        }
    }
    
//  Commented out as OpenOffice is not on the build machines.
//    public void testGetTransformer0()
//    {
//        ContentTransformer transformer = contentService.getTransformer("test", "application/vnd.ms-excel", 0,
//                "application/x-shockwave-flash", new TransformationOptions());
//        assertTrue("Should have found a transformer for 0 bytes", transformer != null);
//    }
//
//    public void testGetTransformer10K()
//    {
//        ContentTransformer transformer = contentService.getTransformer("test", "application/vnd.ms-excel", 1024*10,
//                "application/x-shockwave-flash", new TransformationOptions());
//        assertTrue("Should have found a transformer for 10 K", transformer != null);
//    }
//    
//    public void testGetTransformer1M()
//    {
//        ContentTransformer transformer = contentService.getTransformer("test", "application/vnd.ms-excel", 1024*1024,
//                "application/x-shockwave-flash", new TransformationOptions());
//        assertTrue("Should have found a transformer for 1M", transformer != null);
//    }
//    
//    public void testGetTransformer10M()
//    {
//        ContentTransformer transformer = contentService.getTransformer("test", "application/vnd.ms-excel", 1024*1024*10,
//                "application/x-shockwave-flash", new TransformationOptions());
//        assertTrue("Should NOT have found a transformer for 10M as the is a 1M limit on xsl mimetype", transformer == null);
//    }
//    
//    public void testGetMaxSourceSizeByes()
//    {
//        long maxSourceSizeBytes = contentService.getMaxSourceSizeBytes("application/vnd.ms-excel",
//                "application/x-shockwave-flash", new TransformationOptions());
//        assertEquals("Should have found a transformer that can handle 1M", 1024*1024, maxSourceSizeBytes);
//    }
}
