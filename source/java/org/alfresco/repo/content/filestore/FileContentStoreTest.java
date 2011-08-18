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
package org.alfresco.repo.content.filestore;

import java.io.File;
import java.nio.ByteBuffer;

import org.alfresco.repo.content.AbstractWritableContentStoreTest;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentExistsException;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;

/**
 * Tests read and write functionality for the store.
 * 
 * @see org.alfresco.repo.content.filestore.FileContentStore
 * 
 * @author Derek Hulley
 */
public class FileContentStoreTest extends AbstractWritableContentStoreTest
{
    private FileContentStore store;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        // create a store that uses a subdirectory of the temp directory
        File tempDir = TempFileProvider.getTempDir();
        store = new FileContentStore(ctx,
                tempDir.getAbsolutePath() +
                File.separatorChar +
                getName());
        
        store.setDeleteEmptyDirs(true);
    }

    @Override
    protected ContentStore getStore()
    {
        return store;
    }
    
    /**
     * Checks that the store disallows concurrent writers to be issued to the same URL.
     */
    @SuppressWarnings("unused")
    public void testConcurrentWriteDetection() throws Exception
    {
        ByteBuffer buffer = ByteBuffer.wrap("Something".getBytes());
        ContentStore store = getStore();

        ContentContext firstContentCtx = ContentStore.NEW_CONTENT_CONTEXT;
        ContentWriter firstWriter = store.getWriter(firstContentCtx);
        String contentUrl = firstWriter.getContentUrl();

        ContentContext secondContentCtx = new ContentContext(null, contentUrl);
        try
        {
            ContentWriter secondWriter = store.getWriter(secondContentCtx);
            fail("Store must disallow more than one writer onto the same content URL: " + store);
        }
        catch (ContentExistsException e)
        {
            // expected
        }
    }

    @Override
    public void testRootLocation() throws Exception
    {
        ContentStore store = getStore();
        String root = store.getRootLocation();
        assertNotNull("Root value can't be null", root);
        File dir = new File(root);
        assertTrue("Root location for FileContentStore must exist", dir.exists());
    }

    /**
     * Ensures that the size is positive
     */
    @Override
    public void testSpaceUsed() throws Exception
    {
        ContentStore store = getStore();
        store.getWriter(new ContentContext(null, null)).putContent("Test content");
        long size = store.getSpaceUsed();
        assertTrue("Size must be positive", size > 0L);
    }
    
    /**
     * Ensures that the size is something other than <tt>-1</tt> or <tt>Long.MAX_VALUE</tt>
     */
    @Override
    public void testSpaceFree() throws Exception
    {
        ContentStore store = getStore();
        long size = store.getSpaceFree();
        assertTrue("Size must be positive", size > 0L);
        assertTrue("Size must not be Long.MAX_VALUE", size < Long.MAX_VALUE);
    }
    
    /**
     * Ensures that the size is something other than <tt>-1</tt> or <tt>Long.MAX_VALUE</tt>
     */
    @Override
    public void testSpaceTotal() throws Exception
    {
        ContentStore store = getStore();
        long size = store.getSpaceTotal();
        assertTrue("Size must be positive", size > 0L);
        assertTrue("Size must not be Long.MAX_VALUE", size < Long.MAX_VALUE);
    }
    
    
    /**
     * Empty parent directories should be removed when a URL is removed.
     */
    public void testDeleteRemovesEmptyDirs() throws Exception
    {
        ContentStore store = getStore();
        String url = "store://1965/12/1/13/12/file.bin";
        
        // Ensure clean test data
        if (store.exists(url)) store.delete(url);
        
        String content = "Content for test: " + getName();
        store.getWriter(new ContentContext(null, url)).putContent(content);
        
        File root = new File(store.getRootLocation());
        
        assertDirExists(root, "");
        assertDirExists(root, "1965/12/1/13/12");
        
        store.delete(url);
        
        assertDirNotExists(root, "1965");
        // root should be untouched.
        assertDirExists(root, "");
    }
    
    /**
     * Only non-empty directories should be deleted.
     */
    public void testDeleteLeavesNonEmptyDirs()
    {
        ContentStore store = getStore();
        String url = "store://1965/12/1/13/12/file.bin";
        
        // Ensure clean test data
        if (store.exists(url)) store.delete(url);
        
        String content = "Content for test: " + getName();
        store.getWriter(new ContentContext(null, url)).putContent(content);
        
        File root = new File(store.getRootLocation());
        
        assertDirExists(root, "");
        assertDirExists(root, "1965/12/1/13/12");
        
        // Make a directory non-empty
        String anotherUrl = "store://1965/12/3/another.bin";
        if (store.exists(anotherUrl)) store.delete(anotherUrl);
        store.getWriter(new ContentContext(null, anotherUrl));
        
        store.delete(url);
        
        // Parents of another.bin cannot be deleted
        assertDirExists(root, "1965");
        assertDirExists(root, "1965/12");
        // Non-parents of another.bin could be deleted
        assertDirNotExists(root, "1965/12/1");
        
        // root should be untouched.
        assertDirExists(root, "");
    }
    
    
    /**
     * Empty parent directories are not deleted if the store is configured not to.
     */
    public void testNoParentDirsDeleted() throws Exception
    {
        store.setDeleteEmptyDirs(false);
        FileContentStore store = (FileContentStore) getStore();
        String url = "store://1965/12/1/13/12/file.bin";
        // Ensure clean test data
        if (store.exists(url)) store.delete(url);
        String content = "Content for test: " + getName();
        store.getWriter(new ContentContext(null, url)).putContent(content);
        File root = new File(store.getRootLocation());
        
        store.delete(url);
        
        assertDirExists(root, "1965/12/1/13/12");
        // root should be untouched.
        assertDirExists(root, "");
    }
    
    
    private void assertDirExists(File root, String dir)
    {
        assertTrue("Directory [" + dir + "] should exist", new File(root, dir).exists());
    }
    
    
    private void assertDirNotExists(File root, String dir)
    {
        assertFalse("Directory [" + dir + "] should NOT exist", new File(root, dir).exists());
    }
}
