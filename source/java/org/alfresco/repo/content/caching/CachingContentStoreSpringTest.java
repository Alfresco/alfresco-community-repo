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

import net.sf.ehcache.CacheManager;

import org.alfresco.repo.cache.EhCacheAdapter;
import org.alfresco.repo.content.AbstractWritableContentStoreTest;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;


/**
 * Tests for the CachingContentStore that benefit from a full set of tests
 * defined in AbstractWritableContentStoreTest.
 * 
 * @author Matt Ward
 */
@RunWith(JUnit38ClassRunner.class)
public class CachingContentStoreSpringTest extends AbstractWritableContentStoreTest
{
    private static final String EHCACHE_NAME = "cache.test.cachingContentStoreCache";
    private static final int T24_HOURS = 86400;
    private CachingContentStore store;
    private FileContentStore backingStore;
    private ContentCacheImpl cache;
    
    
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
        cache.setCacheRoot(TempFileProvider.getLongLifeTempDir("cached_content_test"));
        cache.setMemoryStore(createMemoryStore());
        store = new CachingContentStore(backingStore, cache, false);
    }
    
    private EhCacheAdapter<Key, String> createMemoryStore()
    {
        CacheManager manager = CacheManager.getInstance();
        
        // Create the cache if it hasn't already been created.
        if (!manager.cacheExists(EHCACHE_NAME))
        {
            net.sf.ehcache.Cache memoryOnlyCache = 
                new net.sf.ehcache.Cache(EHCACHE_NAME, 50, false, false, T24_HOURS, T24_HOURS);
            
            manager.addCache(memoryOnlyCache);
        }
        
        EhCacheAdapter<Key, String> memoryStore = new EhCacheAdapter<Key, String>();
        memoryStore.setCache(manager.getCache(EHCACHE_NAME));
        
        return memoryStore;
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
    
    
    public void testStoreWillRecoverFromDeletedCacheFile()
    {
        final String content = "Content for " + getName() + " test.";
        
        // Write some content to the backing store.
        ContentWriter writer = backingStore.getWriter(ContentContext.NULL_CONTEXT);
        writer.putContent(content);
        final String contentUrl = writer.getContentUrl();
        
        // Read content using the CachingContentStore - will cause content to be cached.
        String retrievedContent = store.getReader(contentUrl).getContentString();
        assertEquals(content, retrievedContent);
        
        // Remove the cached disk file
        File cacheFile = new File(cache.getCacheFilePath(contentUrl));
        cacheFile.delete();   
        assertTrue("Cached content should have been deleted", !cacheFile.exists());
      
        // Should still be able to ask for this content, even though the cache file was
        // deleted and the record of the cache is still in the in-memory cache/lookup.
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
    
