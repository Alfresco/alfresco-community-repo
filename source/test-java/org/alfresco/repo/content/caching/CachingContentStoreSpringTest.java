/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.content.AbstractWritableContentStoreTest;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.caching.quota.QuotaManagerStrategy;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.TempFileProvider;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;

/**
 * Tests for the CachingContentStore that benefit from a full set of tests
 * defined in AbstractWritableContentStoreTest.
 * 
 * @author Matt Ward
 */
@RunWith(JUnit38ClassRunner.class)
@Category(OwnJVMTestsCategory.class)
public class CachingContentStoreSpringTest extends AbstractWritableContentStoreTest
{
    private CachingContentStore store;
    private FileContentStore backingStore;
    private TestCenteraLikeContentStore centeraLikeBackingStore;
    private ContentCacheImpl cache;
    
    
    @Before
    public void before() throws Exception
    {
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
    
    private SimpleCache<Key, String> createMemoryStore()
    {
        SimpleCache<Key, String> memoryStore = new DefaultSimpleCache<Key, String>();
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
    
    public void testQuotaOnOverlimitCacheFile()
    {
        store = new CachingContentStore(backingStore, cache, true);
        
        final int overLimitSize = 1;
        QuotaManagerStrategy quota = mock(QuotaManagerStrategy.class);
        store.setQuota(quota);
        when(quota.beforeWritingCacheFile(0)).thenReturn(true);
        when(quota.afterWritingCacheFile(overLimitSize)).thenReturn(false);
        
        char[] chars = new char[overLimitSize];
        // Optional step - unnecessary if you're happy with the array being full of \0
        Arrays.fill(chars, 'f');
        final String content = new String(chars);
        final String contentUrl = FileContentStore.createNewFileStoreUrl();
        assertFalse("Content shouldn't be cached yet", cache.contains(contentUrl));
        
        // Write some content using the caching store
        ContentWriter writer = store.getWriter(new ContentContext(null, contentUrl));
        writer.putContent(content);
        
        assertFalse("Overlimit content should be deleted from cache", cache.contains(contentUrl));
        assertFalse("Overlimit content should be deleted from cache", writer.getReader().exists());

        // The content should have been written through to the backing store.
        String fromBackingStore = backingStore.getReader(contentUrl).getContentString();
        assertEquals("Content should be in backing store", content, fromBackingStore);
        assertEquals("Content should be in backing store", overLimitSize, fromBackingStore.length());
        
        // cache writer should still return actual size
        // so that listeners could be executed correctly
        assertEquals("Cache writer should still return actual size", overLimitSize, writer.getSize());
        assertEquals("Cache writer should still return actual size", overLimitSize, writer.getContentData().getSize());
    }
    
    /*
     * MNT-11758 case test
     */
    public void testBackingStoreBehavesLikeCenteraContentStore()
    {
        String content1 = "test content 1";
        String content2 = "test content 2";
        
        // create backing store that behaves like centera content store, i.e. contentUrl is known only after content is written
        centeraLikeBackingStore = new TestCenteraLikeContentStore(ctx,
                TempFileProvider.getTempDir().getAbsolutePath() +
                File.separatorChar +
                getName() + "_centera");
        
        // create caching content store with centera like backing store
        store = new CachingContentStore(centeraLikeBackingStore, cache, true);
        
        ContentWriter writer1 = store.getWriter(new ContentContext(null, null));
        // for centera like backing content store all writers have the same content url before content was written
        assertEquals(TestCenteraLikeContentWriter.UNKNOWN_ID, writer1.getContentData().getContentUrl());
        writer1.putContent(content1);
        // after content was written into the backing store, content url should change
        assertNotSame(TestCenteraLikeContentWriter.UNKNOWN_ID, writer1.getContentData().getContentUrl());
        
        // get another writer
        ContentWriter writer2 = store.getWriter(new ContentContext(null, null));
        // for centera like backing content store all writers have the same content url before content was written
        assertEquals(TestCenteraLikeContentWriter.UNKNOWN_ID, writer2.getContentData().getContentUrl());
        writer2.putContent(content2);
        // after content was written into the backing store, content url should change
        assertNotSame(TestCenteraLikeContentWriter.UNKNOWN_ID, writer2.getContentData().getContentUrl());
        
        // writers should have different content urls
        assertNotSame(writer1.getContentData().getContentUrl(), writer2.getContentData().getContentUrl());
        
        ContentReader reader = store.getReader(writer1.getContentData().getContentUrl());
        
        assertEquals(content1, reader.getContentString());
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
    
