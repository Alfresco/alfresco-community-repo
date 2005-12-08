/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.version;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;

/**
 * Tests for retrieving frozen content from a verioned node
 * 
 * @author Roy Wetherall
 */
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
}
