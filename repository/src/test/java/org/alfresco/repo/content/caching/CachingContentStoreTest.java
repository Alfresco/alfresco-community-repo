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
package org.alfresco.repo.content.caching;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentRestoreParams;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.caching.quota.QuotaManagerStrategy;
import org.alfresco.repo.content.caching.quota.UnlimitedQuotaStrategy;
import org.alfresco.repo.content.filestore.SpoofedTextContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for the CachingContentStore class. Tests use mock backing store and cache.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingContentStoreTest
{
    private CachingContentStore cachingStore;
    private ContentReader sourceContent;
    private ContentReader cachedContent;
    
    @Mock
    private ContentStore backingStore;
    
    @Mock
    private ContentCache cache;


    @Before
    public void setUp() throws Exception
    {
        cachingStore = new CachingContentStore(backingStore, cache, false);
        cachingStore.setQuota(new UnlimitedQuotaStrategy());
        
        sourceContent = mock(ContentReader.class, "sourceContent");
        cachedContent = mock(ContentReader.class, "cachedContent");
    }

    
    @Test
    public void getReaderForItemInCache()
    {
        when(cache.contains("url")).thenReturn(true);
        when(cache.getReader("url")).thenReturn(cachedContent);
        
        ContentReader returnedReader = cachingStore.getReader("url");        
        
        assertSame(returnedReader, cachedContent);
        verify(backingStore, never()).getReader(anyString());
    }
    
    
    @Test
    // Item isn't in cache, so will be cached and returned.
    public void getReaderForItemMissingFromCache()
    {
        when(cache.getReader("url")).thenReturn(cachedContent);
        when(backingStore.getReader("url")).thenReturn(sourceContent);
        when(sourceContent.getSize()).thenReturn(1274L);
        when(cache.put("url", sourceContent)).thenReturn(true);
        
        QuotaManagerStrategy quota = mock(QuotaManagerStrategy.class);
        cachingStore.setQuota(quota);
        when(quota.beforeWritingCacheFile(1274L)).thenReturn(true);
        when(quota.afterWritingCacheFile(1274L)).thenReturn(true);
        
        ContentReader returnedReader = cachingStore.getReader("url");
        
        assertSame(returnedReader, cachedContent);
        verify(quota).afterWritingCacheFile(1274L);
        // Check backing store reader is only acquired once
        verify(backingStore, only()).getReader("url");
    }
    
    
    @Test
    public void getReaderForItemMissingFromCacheWillGiveUpAfterRetrying()
    {
        when(cache.getReader("url")).thenThrow(new CacheMissException("url"));
        when(backingStore.getReader("url")).thenReturn(sourceContent);
        when(cache.put("url", sourceContent)).thenReturn(true);
        
        ContentReader returnedReader = cachingStore.getReader("url");
        
        // Upon failure, item is removed from cache
        verify(cache, atLeastOnce()).remove("url");
        
        // The content comes direct from the backing store
        assertSame(returnedReader, sourceContent);
    }
    
    
    @Test
    public void getReaderForItemMissingFromCacheWillRetryAndCanSucceed()
    {
        when(cache.getReader("url")).
            thenThrow(new CacheMissException("url")).
            thenReturn(cachedContent);
        when(backingStore.getReader("url")).thenReturn(sourceContent);
        when(cache.put("url", sourceContent)).thenReturn(true);
        
        ContentReader returnedReader = cachingStore.getReader("url");
        
        assertSame(returnedReader, cachedContent);
    }
    
    
    @Test
    public void getReaderForItemMissingFromCacheButNoContentToCache()
    {
        when(cache.getReader("url")).thenThrow(new CacheMissException("url"));
        when(backingStore.getReader("url")).thenReturn(sourceContent);
        when(cache.put("url", sourceContent)).thenReturn(false);
        
        cachingStore.getReader("url");
    }
    
    
    @Test
    // When attempting to read uncached content.
    public void quotaManagerCanVetoCacheFileWriting()
    {
        when(backingStore.getReader("url")).thenReturn(sourceContent);
        QuotaManagerStrategy quota = mock(QuotaManagerStrategy.class);
        cachingStore.setQuota(quota);
        when(sourceContent.getSize()).thenReturn(1274L);
        when(quota.beforeWritingCacheFile(1274L)).thenReturn(false);
        
        ContentReader returnedReader = cachingStore.getReader("url");
        
        verify(cache, never()).put("url", sourceContent);
        assertSame(returnedReader, sourceContent);
        verify(quota, never()).afterWritingCacheFile(anyLong());
    }
    
    
    @Test
    public void getWriterWhenNotCacheOnInbound()
    {   
        QuotaManagerStrategy quota = mock(QuotaManagerStrategy.class);
        cachingStore.setQuota(quota);
        
        ContentContext ctx = ContentContext.NULL_CONTEXT;
        
        cachingStore.getWriter(ctx);
        
        verify(backingStore).getWriter(ctx);
        // No quota manager interaction - as no caching happening.
        verify(quota, never()).beforeWritingCacheFile(anyLong());
        verify(quota, never()).afterWritingCacheFile(anyLong());
    }

    
    @Test
    public void getWriterWhenCacheOnInbound() throws ContentIOException, IOException
    {
        cachingStore = new CachingContentStore(backingStore, cache, true);
        ContentContext ctx = ContentContext.NULL_CONTEXT;
        ContentWriter bsWriter = mock(ContentWriter.class);
        when(backingStore.getWriter(ctx)).thenReturn(bsWriter);
        when(bsWriter.getContentUrl()).thenReturn("url");
        ContentWriter cacheWriter = mock(ContentWriter.class);
        when(cache.getWriter("url")).thenReturn(cacheWriter);
        ContentReader readerFromCacheWriter = mock(ContentReader.class);
        when(cacheWriter.getReader()).thenReturn(readerFromCacheWriter);
        when(readerFromCacheWriter.exists()).thenReturn(true);
        when(cacheWriter.getSize()).thenReturn(54321L);
        QuotaManagerStrategy quota = mock(QuotaManagerStrategy.class);
        cachingStore.setQuota(quota);
        
        // Quota manager interceptor is fired.
        when(quota.beforeWritingCacheFile(0L)).thenReturn(true);
        
        cachingStore.getWriter(ctx);
        
        // Check that a listener was attached to cacheWriter with the correct behaviour
        ArgumentCaptor<ContentStreamListener> arg = ArgumentCaptor.forClass(ContentStreamListener.class);
        verify(cacheWriter).addListener(arg.capture());
        // Simulate a stream close
        arg.getValue().contentStreamClosed();
        // Check behaviour of the listener
        verify(bsWriter).putContent(readerFromCacheWriter);
        // Post caching quota manager hook is fired.
        verify(quota).afterWritingCacheFile(54321L);
    }
    
    
    @Test
    // When attempting to perform write-through caching, i.e. cacheOnInbound = true
    public void quotaManagerCanVetoInboundCaching()
    {
        cachingStore = new CachingContentStore(backingStore, cache, true);
        QuotaManagerStrategy quota = mock(QuotaManagerStrategy.class);
        cachingStore.setQuota(quota);
        
        ContentContext ctx = ContentContext.NULL_CONTEXT;
        ContentWriter backingStoreWriter = mock(ContentWriter.class);
        when(backingStore.getWriter(ctx)).thenReturn(backingStoreWriter);
        when(quota.beforeWritingCacheFile(0L)).thenReturn(false);
        
        ContentWriter returnedWriter = cachingStore.getWriter(ctx);
        
        assertSame("Should be writing direct to backing store", backingStoreWriter, returnedWriter);
        verify(quota, never()).afterWritingCacheFile(anyLong());
    }
    
    
    @Test
    public void quotaManagerCanRequestFileDeletionFromCacheAfterWrite()
    {
        cachingStore = new CachingContentStore(backingStore, cache, true);
        ContentContext ctx = ContentContext.NULL_CONTEXT;
        ContentWriter bsWriter = mock(ContentWriter.class);
        when(backingStore.getWriter(ctx)).thenReturn(bsWriter);
        when(bsWriter.getContentUrl()).thenReturn("url");
        ContentWriter cacheWriter = mock(ContentWriter.class);
        when(cache.getWriter("url")).thenReturn(cacheWriter);
        ContentReader readerFromCacheWriter = mock(ContentReader.class);
        when(cacheWriter.getReader()).thenReturn(readerFromCacheWriter);
        when(readerFromCacheWriter.exists()).thenReturn(true);
        when(cacheWriter.getSize()).thenReturn(54321L);
        QuotaManagerStrategy quota = mock(QuotaManagerStrategy.class);
        cachingStore.setQuota(quota);
        
        // Quota manager interceptor is fired.
        when(quota.beforeWritingCacheFile(0L)).thenReturn(true);
        
        cachingStore.getWriter(ctx);
        
        // Check that a listener was attached to cacheWriter with the correct behaviour
        ArgumentCaptor<ContentStreamListener> arg = ArgumentCaptor.forClass(ContentStreamListener.class);
        verify(cacheWriter).addListener(arg.capture());
        
        // Don't keep the new cache file
        when(quota.afterWritingCacheFile(54321L)).thenReturn(false);
        
        // Simulate a stream close
        arg.getValue().contentStreamClosed();
        // Check behaviour of the listener
        verify(bsWriter).putContent(readerFromCacheWriter);
        // Post caching quota manager hook is fired.
        verify(quota).afterWritingCacheFile(54321L);
        // The item should be deleted from the cache (lookup table and content cache file)
        verify(cache).deleteFile("url");
        verify(cache).remove("url");
    }
    
    @Test
    public void quotaManagerCanRequestFileDeletionFromCacheAfterWriteWhenNotCacheOnInbound()
    {
        when(cache.getReader("url")).thenReturn(cachedContent);
        when(backingStore.getReader("url")).thenReturn(sourceContent);
        when(sourceContent.getSize()).thenReturn(1274L);
        when(cache.put("url", sourceContent)).thenReturn(true);
        
        QuotaManagerStrategy quota = mock(QuotaManagerStrategy.class);
        cachingStore.setQuota(quota);
        
        // Don't veto writing the cache file.
        when(quota.beforeWritingCacheFile(1274L)).thenReturn(true);
        // Do request cache file deletion.
        when(quota.afterWritingCacheFile(1234L)).thenReturn(false);
        
        ContentReader returnedReader = cachingStore.getReader("url");
        
        // Was the file deleted?
        verify(cache).deleteFile("url");
        verify(cache).remove("url");
        // As the cache file has been deleted, the reader must come from the backing store
        // rather than the cache.
        assertSame(returnedReader, sourceContent);
    }
    
    @Test(expected=RuntimeException.class)
    // Check that exceptions raised by the backing store's putContent(ContentReader)
    // aren't swallowed and can therefore cause the transaction to fail.
    public void exceptionRaisedWhenCopyingTempToBackingStoreIsPropogatedCorrectly() 
        throws ContentIOException, IOException
    {
        cachingStore = new CachingContentStore(backingStore, cache, true);
        ContentContext ctx = ContentContext.NULL_CONTEXT;
        ContentWriter bsWriter = mock(ContentWriter.class);
        when(backingStore.getWriter(ctx)).thenReturn(bsWriter);
        when(bsWriter.getContentUrl()).thenReturn("url");
        ContentWriter cacheWriter = mock(ContentWriter.class);
        when(cache.getWriter("url")).thenReturn(cacheWriter);
        ContentReader readerFromCacheWriter = mock(ContentReader.class);
        when(cacheWriter.getReader()).thenReturn(readerFromCacheWriter);
        
        doThrow(new RuntimeException()).when(bsWriter).putContent(any(ContentReader.class));
        
        cachingStore.getWriter(ctx);
        
        // Get the stream listener and trigger it
        ArgumentCaptor<ContentStreamListener> arg = ArgumentCaptor.forClass(ContentStreamListener.class);
        verify(cacheWriter).addListener(arg.capture());
        // Simulate a stream close
        arg.getValue().contentStreamClosed();
    }
    
    
    @Test
    public void encodingAttrsCopiedToBackingStoreWriter()
    {
        cachingStore = new CachingContentStore(backingStore, cache, true);
        ContentContext ctx = ContentContext.NULL_CONTEXT;
        ContentWriter bsWriter = mock(ContentWriter.class);
        when(backingStore.getWriter(ctx)).thenReturn(bsWriter);
        when(bsWriter.getContentUrl()).thenReturn("url");
        ContentWriter cacheWriter = mock(ContentWriter.class);
        when(cache.getWriter("url")).thenReturn(cacheWriter);
        ContentReader readerFromCacheWriter = mock(ContentReader.class);
        when(cacheWriter.getReader()).thenReturn(readerFromCacheWriter);
        
        when(cacheWriter.getEncoding()).thenReturn("UTF-8");
        when(cacheWriter.getLocale()).thenReturn(Locale.UK);
        when(cacheWriter.getMimetype()).thenReturn("not/real/mimetype");       
        
        cachingStore.getWriter(ctx);
 
        // Get the stream listener and trigger it
        ArgumentCaptor<ContentStreamListener> arg = ArgumentCaptor.forClass(ContentStreamListener.class);
        verify(cacheWriter).addListener(arg.capture());
        // Simulate a stream close
        arg.getValue().contentStreamClosed();
 
        verify(bsWriter).setEncoding("UTF-8");
        verify(bsWriter).setLocale(Locale.UK);
        verify(bsWriter).setMimetype("not/real/mimetype");
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Tests for spoofed content follow...
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    @Test
    public void spoofedGetReader()
    {
        cachingStore = new CachingContentStore(backingStore, cache, true);
        String url = SpoofedTextContentReader.createContentUrl(Locale.ENGLISH, 0L, 1024L);
        ContentReader reader = cachingStore.getReader(url);
        assertTrue(reader.exists());
        assertEquals(1024, reader.getSize());
        verify(backingStore, never()).getReader(anyString());
    }
    
    @Test
    public void spoofedDelete()
    {
        cachingStore = new CachingContentStore(backingStore, cache, true);
        String url = SpoofedTextContentReader.createContentUrl(Locale.ENGLISH, 0L, 1024L);
        boolean deleted = cachingStore.delete(url);
        assertFalse(deleted);
        verify(backingStore, never()).delete(anyString());
    }
    
    @Test
    public void spoofedExists()
    {
        cachingStore = new CachingContentStore(backingStore, cache, true);
        String url = SpoofedTextContentReader.createContentUrl(Locale.ENGLISH, 0L, 1024L);
        boolean exists = cachingStore.exists(url);
        assertTrue(exists);
        verify(backingStore, never()).exists(anyString());
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Tests for delegated methods follow...
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void delegatedIsContentUrlSupported()
    {
        when(backingStore.isContentUrlSupported("url")).thenReturn(true);
        assertTrue(cachingStore.isContentUrlSupported("url"));
        
        when(backingStore.isContentUrlSupported("url")).thenReturn(false);
        assertFalse(cachingStore.isContentUrlSupported("url"));
    }
    
 
    @Test
    public void delegatedIsWriteSupported()
    {
        when(backingStore.isWriteSupported()).thenReturn(true);
        assertTrue(cachingStore.isWriteSupported());
        
        when(backingStore.isWriteSupported()).thenReturn(false);
        assertFalse(cachingStore.isWriteSupported());
    }
    
 
    @Test
    public void delegatedGetSpaceFree()
    {
        when(backingStore.getSpaceFree()).thenReturn(124L);
        assertEquals(124L, cachingStore.getSpaceFree());
    }
    
    
    @Test
    public void delegatedGetSpaceTotal()
    {
        when(backingStore.getSpaceTotal()).thenReturn(4234L);
        assertEquals(4234L, cachingStore.getSpaceTotal());
    }
    
    
    @Test
    public void delegatedGetRootLocation()
    {
        when(backingStore.getRootLocation()).thenReturn("/random/root/dir");
        assertEquals("/random/root/dir", cachingStore.getRootLocation());
    }
    
    
    @Test
    public void delegatedExists()
    {
        when(backingStore.exists("url")).thenReturn(true);
        assertTrue(cachingStore.exists("url"));
        
        when(backingStore.exists("url")).thenReturn(false);
        assertFalse(cachingStore.exists("url"));
    }
    
    
    @Test
    public void delegatedDelete()
    {
        when(backingStore.delete("url")).thenReturn(true);
        assertTrue(cachingStore.delete("url"));
        
        when(backingStore.delete("url")).thenReturn(false);
        assertFalse(cachingStore.delete("url"));
    }

    @Test
    public void isContentDirectUrlSupported()
    {
        assertFalse(cachingStore.isContentDirectUrlEnabled());

        when(backingStore.isContentDirectUrlEnabled()).thenReturn(true);
        assertTrue(cachingStore.isContentDirectUrlEnabled());
    }

    @Test
    public void getRequestContentDirectUrlUnsupported()
    {
        try
        {
            when(backingStore.requestContentDirectUrl(anyString(), eq(true), anyString(), anyString(), anyLong())).thenThrow(new UnsupportedOperationException());
            cachingStore.requestContentDirectUrl("url", true,"someFile", "someMimetype", 30L);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
    }

    @Test
    public void getRequestContentDirectUrl()
    {
        when(backingStore.requestContentDirectUrl(anyString(), eq(true), anyString(), anyString(), anyLong())).thenReturn(new DirectAccessUrl());
        cachingStore.requestContentDirectUrl("url", true,"someFile", "someMimeType", 30L);
    }

    @Test
    public void shouldReturnSomeStorageProperties()
    {
        final Map<String, String> propertiesMap = Map.of("x-amz-header1", "value1", "x-amz-header2", "value2");
        final String contentUrl = "url";
        when(backingStore.getStorageProperties(contentUrl)).thenReturn(propertiesMap);
        final Map<String, String> storageProperties = cachingStore.getStorageProperties(contentUrl);
        assertFalse(storageProperties.isEmpty());
        assertEquals(propertiesMap, storageProperties);
    }

    @Test
    public void shouldReturnEmptyStorageProperties()
    {
        Map<String, String> storageProperties = cachingStore.getStorageProperties("url");
        assertTrue(storageProperties.isEmpty());
    }

    @Test
    public void shouldCompleteArchiveContentRequest()
    {
        final boolean expectedResult = true;
        final String contentUrl = "url";
        final Map<String, Serializable> archiveParams = Collections.emptyMap();
        when(backingStore.requestSendContentToArchive(contentUrl, archiveParams)).thenReturn(expectedResult);

        final boolean sendContentToArchive = cachingStore.requestSendContentToArchive(contentUrl, archiveParams);

        assertEquals(expectedResult, sendContentToArchive);
    }

    @Test
    public void shouldThrowExceptionOnArchiveContentRequest()
    {
        final String contentUrl = "url";
        final Map<String, Serializable> archiveParams = Collections.emptyMap();
        when(backingStore.requestSendContentToArchive(contentUrl, archiveParams)).thenCallRealMethod();

        assertThrows(UnsupportedOperationException.class, () -> {
            cachingStore.requestSendContentToArchive(contentUrl, archiveParams);
        });
    }

    @Test
    public void shouldCompleteRestoreContentFromArchiveRequest()
    {
        final String contentUrl = "url";
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), "High");
        final boolean expectedResult = true;
        when(backingStore.requestRestoreContentFromArchive(contentUrl, restoreParams)).thenReturn(expectedResult);

        final boolean sendContentToArchive = cachingStore.requestRestoreContentFromArchive(contentUrl, restoreParams);

        assertEquals(expectedResult, sendContentToArchive);
    }

    @Test
    public void shouldThrowExceptionOnRestoreContentFromArchiveRequest()
    {
        final String contentUrl = "url";
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), "High");
        when(backingStore.requestRestoreContentFromArchive(contentUrl, restoreParams)).thenCallRealMethod();

        assertThrows(UnsupportedOperationException.class, () -> {
            cachingStore.requestRestoreContentFromArchive(contentUrl, restoreParams);
        });
    }
}
