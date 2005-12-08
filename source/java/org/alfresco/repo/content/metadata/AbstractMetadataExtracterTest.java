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
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.TempFileProvider;

/**
 * Provides a base set of tests for testing 
 * {@link org.alfresco.repo.content.metadata.MetadataExtracter} implementations.
 * 
 * @author Jesper Steen Møller
 */
public abstract class AbstractMetadataExtracterTest extends BaseSpringTest
{
    protected static final String QUICK_TITLE = "The quick brown fox jumps over the lazy dog";
    protected static final String QUICK_DESCRIPTION = "Gym class featuring a brown fox and lazy dog";
    protected static final String QUICK_CREATOR = "Nevin Nollop";
    protected static final String[] QUICK_WORDS = new String[] { "quick", "brown", "fox", "jumps", "lazy", "dog" };

    protected MimetypeMap mimetypeMap;
    protected MetadataExtracter transformer;

    public final void setMimetypeMap(MimetypeMap mimetypeMap)
    {
        this.mimetypeMap = mimetypeMap;
    }

    protected abstract MetadataExtracter getExtracter();

    /**
     * Ensures that the temp locations are cleaned out before the tests start
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
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
        File sourceFile = AbstractMetadataExtracterTest.loadQuickTestFile("txt");
        assertNotNull("quick.* files should be available from Tests", sourceFile);
    }

    /**
     * Helper method to load one of the "The quick brown fox" files from the
     * classpath.
     * 
     * @param extension the extension of the file required
     * @return Returns a test resource loaded from the classpath or
     *         <tt>null</tt> if no resource could be found.
     * @throws IOException
     */
    public static File loadQuickTestFile(String extension) throws IOException
    {
        URL url = AbstractMetadataExtracterTest.class.getClassLoader().getResource("quick/quick." + extension);
        if (url == null)
        {
            return null;
        }
        File file = new File(url.getFile());
        if (!file.exists())
        {
            return null;
        }
        return file;
    }

    public Map<QName, Serializable> extractFromExtension(String ext, String mimetype) throws Exception
    {
        Map<QName, Serializable> destination = new HashMap<QName, Serializable>();

        // attempt to get a source file for each mimetype
        File sourceFile = AbstractMetadataExtracterTest.loadQuickTestFile(ext);
        if (sourceFile == null)
        {
            throw new FileNotFoundException("No quick." + ext + " file found for test");
        }

        // construct a reader onto the source file
        ContentReader sourceReader = new FileContentReader(sourceFile);
        sourceReader.setMimetype(mimetype);
        getExtracter().extract(sourceReader, destination);
        return destination;
    }

    public void testCommonMetadata(Map<QName, Serializable> destination)
    {
        assertEquals(QUICK_TITLE, destination.get(ContentModel.PROP_TITLE));
        assertEquals(QUICK_DESCRIPTION, destination.get(ContentModel.PROP_DESCRIPTION));
        assertEquals(QUICK_CREATOR, destination.get(ContentModel.PROP_CREATOR));
    }
}
