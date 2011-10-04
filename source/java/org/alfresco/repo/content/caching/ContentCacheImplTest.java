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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.content.caching.ContentCacheImpl.NumericFileNameComparator;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the ContentCacheImpl class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentCacheImplTest
{
    private ContentCacheImpl contentCache;
    private @Mock SimpleCache<Key, String> lookupTable;
    

    @Before
    public void setUp() throws Exception
    {
        contentCache = new ContentCacheImpl();
        contentCache.setMemoryStore(lookupTable);
        contentCache.setCacheRoot(TempFileProvider.getTempDir());
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void cannotSetNullCacheRoot()
    {
        contentCache.setCacheRoot(null);
    }
    
    
    @Test
    public void willCreateNonExistentCacheRoot()
    {
        File cacheRoot = new File(TempFileProvider.getTempDir(), GUID.generate());
        cacheRoot.deleteOnExit();
        assertFalse("Pre-condition of test is that cacheRoot does not exist", cacheRoot.exists());
        
        contentCache.setCacheRoot(cacheRoot);
        
        assertTrue("cacheRoot should have been created", cacheRoot.exists());
    }
    
    
    @Test
    public void canGetReaderForItemInCacheHavingLiveFile()
    {
        final String url = "store://content/url.bin";
        Mockito.when(lookupTable.contains(Key.forUrl(url))).thenReturn(true);
        final String path = tempfile().getAbsolutePath();
        Mockito.when(lookupTable.get(Key.forUrl(url))).thenReturn(path);
        
        FileContentReader reader = (FileContentReader) contentCache.getReader(url);
        
        assertEquals("Reader should have correct URL", url, reader.getContentUrl());
        assertEquals("Reader should be for correct cached content file", path, reader.getFile().getAbsolutePath());
        // Important the get(path) was called, so that the timeToIdle is reset
        // for the 'reverse lookup' as well as the URL to path mapping.
        Mockito.verify(lookupTable).get(Key.forCacheFile(path));
    }

    
    @Test(expected=CacheMissException.class)
    public void getReaderForItemInCacheButMissingContentFile()
    {
        final String url = "store://content/url.bin";
        Mockito.when(lookupTable.contains(Key.forUrl(url))).thenReturn(true);
        final String path = "/no/content/file/at/this/path.bin";
        Mockito.when(lookupTable.get(Key.forUrl(url))).thenReturn(path);
          
        try
        {
            contentCache.getReader(url);
        }
        finally
        {
            // Important the get(path) was called, so that the timeToIdle is reset
            // for the 'reverse lookup' as well as the URL to path mapping.
            Mockito.verify(lookupTable).get(Key.forCacheFile(path));
        }
    }


    @Test(expected=CacheMissException.class)
    public void getReaderWhenItemNotInCache()
    {
        final String url = "store://content/url.bin";
        Mockito.when(lookupTable.contains(Key.forUrl(url))).thenReturn(false);
        
        contentCache.getReader(url);
    }
    
    
    @Test
    public void contains()
    {
        final String url = "store://content/url.bin";
        
        Mockito.when(lookupTable.contains(Key.forUrl(url))).thenReturn(true);
        assertTrue(contentCache.contains(Key.forUrl(url)));
        assertTrue(contentCache.contains(url));
        
        Mockito.when(lookupTable.contains(Key.forUrl(url))).thenReturn(false);
        assertFalse(contentCache.contains(Key.forUrl(url)));
        assertFalse(contentCache.contains(url));
    }
    
    
    @Test
    public void putIntoLookup()
    {
        final Key key = Key.forUrl("store://some/url");
        final String value = "/some/path";
        
        contentCache.putIntoLookup(key, value);
        
        Mockito.verify(lookupTable).put(key, value);
    }
    
    
    @Test
    public void getCacheFilePath()
    {
        final String url = "store://some/url.bin";
        final String expectedPath = "/some/cache/file/path";
        Mockito.when(lookupTable.get(Key.forUrl(url))).thenReturn(expectedPath);
        
        String path = contentCache.getCacheFilePath(url);
        
        assertEquals("Paths must match", expectedPath, path);
    }
    
    
    @Test
    public void getContentUrl()
    {
        final File cacheFile = new File("/some/path");
        final String expectedUrl = "store://some/url";
        Mockito.when(lookupTable.get(Key.forCacheFile(cacheFile))).thenReturn(expectedUrl);
        
        String url = contentCache.getContentUrl(cacheFile);
        
        assertEquals("Content URLs should match", expectedUrl, url);
    }
    
    
    @Test
    public void putForZeroLengthFile()
    {
        ContentReader contentReader = Mockito.mock(ContentReader.class);
        Mockito.when(contentReader.getSize()).thenReturn(0L);
        
        boolean putResult = contentCache.put("", contentReader);
        
        assertFalse("Zero length files should not be cached", putResult);
    }
    
    
    @Test
    public void putForNonEmptyFile()
    {
        ContentReader contentReader = Mockito.mock(ContentReader.class);
        Mockito.when(contentReader.getSize()).thenReturn(999000L);
        final String url = "store://some/url.bin";
        boolean putResult = contentCache.put(url, contentReader);
        
        assertTrue("Non-empty files should be cached", putResult);
        ArgumentCaptor<File> cacheFileArg = ArgumentCaptor.forClass(File.class);
        Mockito.verify(contentReader).getContent(cacheFileArg.capture());
        // Check cached item is recorded properly in ehcache
        Mockito.verify(lookupTable).put(Key.forUrl(url), cacheFileArg.getValue().getAbsolutePath());
        Mockito.verify(lookupTable).put(Key.forCacheFile(cacheFileArg.getValue().getAbsolutePath()), url);
    }
    
    
    @Test
    public void remove()
    {
        final String url = "store://some/url.bin";
        final String path = "/some/path";
        Mockito.when(lookupTable.get(Key.forUrl(url))).thenReturn(path);
        
        contentCache.remove(url);
        
        Mockito.verify(lookupTable).remove(Key.forUrl(url));
        Mockito.verify(lookupTable).remove(Key.forCacheFile(path));
    }
    
    @Test
    public void deleteFile()
    {
        File cacheFile = tempfile();
        assertTrue("Temp file should have been written", cacheFile.exists());
        Mockito.when(contentCache.getCacheFilePath("url")).thenReturn(cacheFile.getAbsolutePath());
        
        contentCache.deleteFile("url");
        
        assertFalse("File should have been deleted", cacheFile.exists());
    }
    
    @Test
    public void getWriter()
    {
        final String url = "store://some/url.bin";
        
        FileContentWriter writer = (FileContentWriter) contentCache.getWriter(url);
        writer.putContent("Some test content for " + getClass().getName());
        
        assertEquals(url, writer.getContentUrl());
        // Check cached item is recorded properly in ehcache
        Mockito.verify(lookupTable).put(Key.forUrl(url), writer.getFile().getAbsolutePath());
        Mockito.verify(lookupTable).put(Key.forCacheFile(writer.getFile().getAbsolutePath()), url);
    }
    
    @Test
    public void compareNumericFileNames()
    {
        NumericFileNameComparator comparator = new NumericFileNameComparator();
        assertEquals(-1, comparator.compare(new File("1"), new File("2")));
        assertEquals(0, comparator.compare(new File("2"), new File("2")));
        assertEquals(1, comparator.compare(new File("2"), new File("1")));
        
        // Make sure that ordering is numeric and not by string value
        assertEquals(-1, comparator.compare(new File("3"), new File("20")));
        assertEquals(1, comparator.compare(new File("20"), new File("3")));
        
        assertEquals(-1, comparator.compare(new File("3"), new File("non-numeric")));
        assertEquals(1, comparator.compare(new File("non-numeric"), new File("3")));
    }
    
    @Test
    public void canVisitOldestDirsFirst()
    {
        File cacheRoot = new File(TempFileProvider.getTempDir(), GUID.generate());
        cacheRoot.deleteOnExit();
        contentCache.setCacheRoot(cacheRoot);
        
        File f1 = tempfile(createDirs("2000/3/30/17/45/31"), "files-are-unsorted.bin");
        File f2 = tempfile(createDirs("2000/3/4/17/45/31"), "another-file.bin");
        File f3 = tempfile(createDirs("2010/12/24/23/59/58"), "a-second-before.bin");
        File f4 = tempfile(createDirs("2010/12/24/23/59/59"), "last-one.bin");
        File f5 = tempfile(createDirs("2000/1/7/2/7/12"), "first-one.bin");
        
        // Check that directories and files are visited in correct order
        FileHandler handler = Mockito.mock(FileHandler.class);
        contentCache.processFiles(handler);
        
        InOrder inOrder = Mockito.inOrder(handler);
        inOrder.verify(handler).handle(f5);
        inOrder.verify(handler).handle(f2);
        inOrder.verify(handler).handle(f1);
        inOrder.verify(handler).handle(f3);
        inOrder.verify(handler).handle(f4);
    }
    


    private File tempfile()
    {
        return tempfile("cached-content", ".bin");
    }
    
    private File tempfile(String name, String suffix)
    {
        File file = TempFileProvider.createTempFile(name, suffix);
        file.deleteOnExit();
        return file;
    }
    
    private File tempfile(File dir, String name)
    {
        File f = new File(dir, name);
        try
        {
            f.createNewFile();
        }
        catch (IOException error)
        {
            throw new RuntimeException(error);
        }
        f.deleteOnExit();
        return f;
    }

    private File createDirs(String path)
    {
        File f = new File(contentCache.getCacheRoot(), path);
        f.mkdirs();
        return f;
    }
}
