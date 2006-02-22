/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.content.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.content.metadata.MetadataExtracter
 * @see org.alfresco.repo.content.metadata.AbstractMetadataExtracter
 * 
 * @author Jesper Steen Møller
 */
public abstract class AbstractMetadataExtracterTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    protected static final String QUICK_TITLE = "The quick brown fox jumps over the lazy dog";
    protected static final String QUICK_DESCRIPTION = "Gym class featuring a brown fox and lazy dog";
    protected static final String QUICK_CREATOR = "Nevin Nollop";

    protected MimetypeMap mimetypeMap;

    protected abstract MetadataExtracter getExtracter();

    /**
     * Ensures that the temp locations are cleaned out before the tests start
     */
    @Override
    public void setUp() throws Exception
    {
        this.mimetypeMap = (MimetypeMap) ctx.getBean("mimetypeService");
        
        // perform a little cleaning up
        long now = System.currentTimeMillis();
        TempFileProvider.TempFileCleanerJob.removeFiles(now);
    }

    /**
     * Check that all objects are present
     */
    public void testSetUp() throws Exception
    {
        assertNotNull("MimetypeMap not present", mimetypeMap);
        // check that the quick resources are available
        File sourceFile = AbstractContentTransformerTest.loadQuickTestFile("txt");
        assertNotNull("quick.* files should be available from Tests", sourceFile);
    }
    
    protected void testExtractFromMimetype(String mimetype) throws Exception
    {
        Map<QName, Serializable> properties = extractFromMimetype(mimetype);
        // check
        testCommonMetadata(mimetype, properties);
    }

    protected Map<QName, Serializable> extractFromMimetype(String mimetype) throws Exception
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        
        // get the extension for the mimetype
        String ext = mimetypeMap.getExtension(mimetype);

        // attempt to get a source file for each mimetype
        File sourceFile = AbstractContentTransformerTest.loadQuickTestFile(ext);
        if (sourceFile == null)
        {
            throw new FileNotFoundException("No quick." + ext + " file found for test");
        }

        // construct a reader onto the source file
        ContentReader sourceReader = new FileContentReader(sourceFile);
        sourceReader.setMimetype(mimetype);
        getExtracter().extract(sourceReader, properties);
        return properties;
    }

    protected void testCommonMetadata(String mimetype, Map<QName, Serializable> properties)
    {
        assertEquals(
                "Property " + ContentModel.PROP_TITLE + " not found for mimetype " + mimetype,
                QUICK_TITLE, properties.get(ContentModel.PROP_TITLE));
        assertEquals(
                "Property " + ContentModel.PROP_DESCRIPTION + " not found for mimetype " + mimetype,
                QUICK_DESCRIPTION, properties.get(ContentModel.PROP_DESCRIPTION));
    }
}
