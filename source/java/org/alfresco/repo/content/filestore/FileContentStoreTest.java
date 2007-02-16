/*
 * Copyright (C) 2005 Alfresco, Inc.
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
