/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.content.caching;

import java.io.File;

import org.alfresco.repo.content.AbstractWritableContentStoreTest;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;

/**
 * Tests for the CachingContentStore that use a full spring context.
 * 
 * @author Matt Ward
 */
public class CachingContentStoreSpringTest extends AbstractWritableContentStoreTest
{
    private CachingContentStore store;
    private FileContentStore backingStore;
    private ContentCache cache;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        File tempDir = TempFileProvider.getTempDir();
        
        backingStore = new FileContentStore(ctx,
                tempDir.getAbsolutePath() +
                File.separatorChar +
                getName());
        
        cache = new ContentCacheImpl();
        store = new CachingContentStore(backingStore, cache, false);
    }
    
    
    public void testStoreWillReadFromCacheWhenAvailable()
    {
        final String content = "Content for " + getName() + " test.";
        
        // Write some content to the backing store.
        ContentWriter writer = backingStore.getWriter(ContentContext.NULL_CONTEXT);
        writer.putContent(content);
        final String contentUrl = writer.getContentUrl();
        
        // Read content using the CachingContentStore - will cause content to be cached.
        String retrievedContent = store.getReader(contentUrl).getContentString();
        assertEquals(content, retrievedContent);
        
        // Remove the original content from the backing store.
        backingStore.delete(contentUrl);
        assertFalse("Original content should have been deleted", backingStore.exists(contentUrl));
        
        // The cached version is still available.
        String contentAfterDelete = store.getReader(contentUrl).getContentString();
        assertEquals(content, contentAfterDelete);
    }

    
    public void testCacheOnInbound()
    {
        store = new CachingContentStore(backingStore, cache, true);
        final String content = "Content for " + getName() + " test.";
        final String contentUrl = FileContentStore.createNewFileStoreUrl();
        
        assertFalse("Content shouldn't be cached yet", cache.contains(contentUrl));
        
        // Write some content using the caching store
        ContentWriter writer = store.getWriter(new ContentContext(null, contentUrl));
        writer.putContent(content);
        
        assertTrue("Cache should contain content after write", cache.contains(contentUrl));
        // Check DIRECTLY with the cache, since a getReader() from the CachingContentStore would result
        // in caching, but we're checking that caching was caused by the write operation.
        String retrievedContent = cache.getReader(contentUrl).getContentString(); 
        assertEquals(content, retrievedContent);
        
        // The content should have been written through to the backing store.
        String fromBackingStore = backingStore.getReader(contentUrl).getContentString();
        assertEquals("Content should be in backing store", content, fromBackingStore);
        
        // Remove the original content from the backing store.
        backingStore.delete(contentUrl);
        assertFalse("Original content should have been deleted", backingStore.exists(contentUrl));
        
        // The cached version is still available
        String contentAfterDelete = store.getReader(contentUrl).getContentString();
        assertEquals(content, contentAfterDelete);
    }
    
    
    
    /*
     * @see org.alfresco.repo.content.AbstractReadOnlyContentStoreTest#getStore()
     */
    @Override
    protected ContentStore getStore()
    {
        return store;
    }    
}
    
