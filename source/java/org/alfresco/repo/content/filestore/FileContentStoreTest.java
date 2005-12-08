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
package org.alfresco.repo.content.filestore;

import java.io.File;

import org.alfresco.repo.content.AbstractContentReadWriteTest;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.TempFileProvider;

/**
 * Tests read and write functionality for the store.
 * 
 * @see org.alfresco.repo.content.filestore.FileContentStore
 * 
 * @author Derek Hulley
 */
public class FileContentStoreTest extends AbstractContentReadWriteTest
{
    private FileContentStore store;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        // create a store that uses a subdirectory of the temp directory
        File tempDir = TempFileProvider.getTempDir();
        store = new FileContentStore(
                tempDir.getAbsolutePath() +
                File.separatorChar +
                getName());
    }
    
    @Override
    protected ContentStore getStore()
    {
        return store;
    }
    
    public void testGetSafeContentReader() throws Exception
    {
        String template = "ABC {0}{1}";
        String arg0 = "DEF";
        String arg1 = "123";
        String fakeContent = "ABC DEF123";

        // get a good reader
        ContentReader reader = getReader();
        assertFalse("No content has been written to the URL yet", reader.exists());
        
        // now create a file for it
        File file = store.createNewFile(reader.getContentUrl());
        assertTrue("File store did not connect new file", file.exists());
        assertTrue("Reader did not detect creation of the underlying file", reader.exists());
        
        // remove the underlying content
        file.delete();
        assertFalse("File not missing", file.exists());
        assertFalse("Reader doesn't show missing content", reader.exists());
        
        // make a safe reader
        ContentReader safeReader = FileContentReader.getSafeContentReader(reader, template, arg0, arg1);
        // check it
        assertTrue("Fake content doesn't exist", safeReader.exists());
        assertEquals("Fake content incorrect", fakeContent, safeReader.getContentString());
        assertEquals("Fake mimetype incorrect", MimetypeMap.MIMETYPE_TEXT_PLAIN, safeReader.getMimetype());
        assertEquals("Fake encoding incorrect", "UTF-8", safeReader.getEncoding());
        
        // now repeat with a null reader
        reader = null;
        safeReader = FileContentReader.getSafeContentReader(reader, template, arg0, arg1);
        // check it
        assertTrue("Fake content doesn't exist", safeReader.exists());
        assertEquals("Fake content incorrect", fakeContent, safeReader.getContentString());
    }
}
