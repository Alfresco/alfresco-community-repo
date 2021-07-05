/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.content.filestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Locale;

import org.alfresco.repo.content.AbstractWritableContentStoreTest;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentExistsException;
import org.alfresco.repo.content.ContentLimitProvider;
import org.alfresco.repo.content.ContentLimitProvider.SimpleFixedLimitProvider;
import org.alfresco.repo.content.ContentLimitViolationException;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.StorageClassSet;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.TempFileProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests read and write functionality for the store.
 * 
 * @see org.alfresco.repo.content.filestore.FileContentStore
 * 
 * @author Derek Hulley
 */
@Category(OwnJVMTestsCategory.class)
public class FileContentStoreTest extends AbstractWritableContentStoreTest
{
    protected FileContentStore store;
    
    @Before
    public void before() throws Exception
    {
        // create a store that uses a subdirectory of the temp directory
        File tempDir = TempFileProvider.getTempDir();
        store = new FileContentStore(ctx,
                tempDir.getAbsolutePath() +
                File.separatorChar +
                getName());
        
        store.setDeleteEmptyDirs(true);
        // Do not need super class's transactions
    }
    
    @After
    public void after()
    {
        // Do not need super class's transactions
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
    @Test
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
    @Test
    public void testRootLocation() throws Exception
    {
        ContentStore store = getStore();
        String root = store.getRootLocation();
        assertNotNull("Root value can't be null", root);
        File dir = new File(root);
        assertTrue("Root location for FileContentStore must exist", dir.exists());
    }
    
    /**
     * Ensures that the size is something other than <tt>-1</tt> or <tt>Long.MAX_VALUE</tt>
     */
    @Override
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    
    /**
     * This method tests that writing content with a configured {@link ContentLimitProvider limit} fails with
     * the expected exception.
     * @since Thor
     */
    @Test
    public void testWriteFileWithSizeLimit() throws Exception
    {
        ContentWriter writer = getWriter();
        assertEquals("Writer was of wrong type", FileContentWriter.class, writer.getClass());
        
        FileContentWriter fileContentWriter = (FileContentWriter)writer;
        
        // Set a maximum size limit for this writer. We use a limit of 3 bytes.
        ContentLimitProvider limitProvider = new SimpleFixedLimitProvider(3);
        fileContentWriter.setContentLimitProvider(limitProvider);
        
        // Attempt to write content that will exceed the limit.
        boolean expectedExceptionThrown = false;
        try
        {
            writer.putContent("This will exceed the short limit.");
        }
        catch (ContentLimitViolationException clvx)
        {
            expectedExceptionThrown = true;
        }
        
        assertTrue("Expected exception not thrown.", expectedExceptionThrown);
        assertTrue("Stream close not detected", writer.isClosed());
    }
    
    /**
     * Test for MNT-12301 case.
     */
    @Test
    public void testFileAccessOutsideStoreRoot()
    {
        String url = FileContentStore.STORE_PROTOCOL + ContentStore.PROTOCOL_DELIMITER + "../somefile.bin";
        
        try
        {
            store.getReader(url);
            fail("Access to content outside of content store root should not be allowed.");
        }
        catch (ContentIOException e)
        {
            //expected
        }
        
        try
        {
            store.exists(url);
            fail("Access to content outside of content store root should not be allowed.");
        }
        catch (ContentIOException e)
        {
            //expected
        }
        
        try
        {
            store.delete(url);
            fail("Access to content outside of content store root should not be allowed.");
        }
        catch (ContentIOException e)
        {
            //expected
        }
        
        try
        {
            store.getWriterInternal(null, url);
            fail("Access to content outside of content store root should not be allowed.");
        }
        catch (ContentIOException e)
        {
            //expected
        }
    }
    
    /**
     * Ensure that the store is able to produce readers for spoofed text.
     * 
     * @since 5.1
     */
    @Test
    public void testSpoofedContent() throws Exception
    {
        String url = SpoofedTextContentReader.createContentUrl(Locale.ENGLISH, 0L, 1024L);
        ContentContext ctx = new ContentContext(null, url);
        try
        {
            store.getWriter(ctx);
            fail("FileContentStore should report that all 'spoof' content exists.");
        }
        catch (ContentExistsException e)
        {
            // Expected
        }
        assertFalse("Deletion should be 'false'.", store.delete(url));
        assertTrue("All spoofed content already exists!", store.exists(url));
        ContentReader reader = store.getReader(url);
        assertTrue(reader instanceof SpoofedTextContentReader);
        assertEquals(1024L, reader.getContentString().getBytes("UTF-8").length);
    }

    @Test
    public void testSupportsDefaultStorageClass()
    {
        assertTrue(store.isStorageClassesSupported(ContentStore.SCS_DEFAULT));
    }

    @Test
    public void testDoesNotSupportUnknownStorageClass()
    {
        assertFalse(store.isStorageClassesSupported(new StorageClassSet("unknown")));
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
