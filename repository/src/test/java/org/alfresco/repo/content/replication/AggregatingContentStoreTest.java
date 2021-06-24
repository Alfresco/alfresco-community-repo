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
package org.alfresco.repo.content.replication;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.content.AbstractWritableContentStoreTest;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.StorageClass;
import org.alfresco.repo.content.UnsupportedContentUrlException;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.testing.category.NeverRunsTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests read and write functionality for the aggregating store.
 * <p>
 * 
 * @see org.alfresco.repo.content.replication.AggregatingContentStore
 * 
 * @author Derek Hulley
 * @author Mark Rogers
 */
@Category({OwnJVMTestsCategory.class, NeverRunsTests.class})
@RunWith(MockitoJUnitRunner.class)
public class AggregatingContentStoreTest extends AbstractWritableContentStoreTest
{
    private static final String SOME_CONTENT = "The No. 1 Ladies' Detective Agency";
    
    private AggregatingContentStore aggregatingStore;
    private ContentStore primaryStore;
    private List<ContentStore> secondaryStores;

    @Mock
    ContentStore primaryStoreMock;
    @Mock
    ContentStore secondaryStoreMock;
    
    AggregatingContentStore aggregatingContentStoreMock;

    @Before
    public void before() throws Exception
    {
        File tempDir = TempFileProvider.getTempDir();
        // create a primary file store
        String storeDir = tempDir.getAbsolutePath() + File.separatorChar + GUID.generate();
        primaryStore = new FileContentStore(ctx, storeDir);
        // create some secondary file stores
        secondaryStores = new ArrayList<ContentStore>(3);
        for (int i = 0; i < 4; i++)
        {
            storeDir = tempDir.getAbsolutePath() + File.separatorChar + GUID.generate();
            FileContentStore store = new FileContentStore(ctx, storeDir);
            secondaryStores.add(store);
        }
        // Create the aggregating store for Spring tests
        aggregatingStore = new AggregatingContentStore();
        aggregatingStore.setPrimaryStore(primaryStore);
        aggregatingStore.setSecondaryStores(secondaryStores);

        // Create a mocked aggregating store
        aggregatingContentStoreMock = new AggregatingContentStore();
        aggregatingContentStoreMock.setPrimaryStore(primaryStoreMock);
        aggregatingContentStoreMock.setSecondaryStores(List.of(secondaryStoreMock));
    }

    @Override
    public ContentStore getStore()
    {
        return aggregatingStore;
    }
    
    /**
     * Get a writer into the store.  This test class assumes that the store is writable and
     * that it therefore supports the ability to write content.
     * 
     * @return
     *      Returns a writer for new content
     */
    protected ContentWriter getWriter()
    {
        ContentStore store = getStore();
        return store.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
    }
    
    
    /**
     * {@inheritDoc}
     * <p>
     * This implementation creates some content in the store and returns the new content URL.
     */
    protected String getExistingContentUrl()
    {
        ContentWriter writer = getWriter();
        writer.putContent("Content for getExistingContentUrl");
        return writer.getContentUrl();
    }
    
    @Test
    public void testAddContent() throws Exception
    {
        ContentWriter writer = getWriter();
        writer.putContent(SOME_CONTENT);
        String contentUrl = writer.getContentUrl();
        
        checkForUrl(contentUrl, true);
    }
    
    /**
     * Checks that the url is present in each of the stores
     * 
     * @param contentUrl String
     * @param mustExist true if the content must exist, false if it must <b>not</b> exist
     */
    private void checkForUrl(String contentUrl, boolean mustExist)
    {
        ContentReader reader = getReader(contentUrl);
        assertEquals("Reader state differs from expected: " + reader, mustExist, reader.exists());
    }
    
    @Test
    public void testDelete() throws Exception
    {
        
        // write some content
        ContentWriter writer = getWriter();
        writer.putContent(SOME_CONTENT);
        String contentUrl = writer.getContentUrl();
        
        ContentReader reader = primaryStore.getReader(contentUrl);
        assertTrue("Content was not in the primary store", reader.exists());
        assertEquals("The content was incorrect", SOME_CONTENT, reader.getContentString());

        getStore().delete(contentUrl);
        checkForUrl(contentUrl, false);
    }
    
    @Test
    public void testReadFromSecondaryStore()
    {
        // pick a secondary store and write some content to it
        ContentStore secondaryStore = secondaryStores.get(2);
        ContentWriter writer = secondaryStore.getWriter(ContentContext.NULL_CONTEXT);
        writer.putContent(SOME_CONTENT);
        String contentUrl = writer.getContentUrl();
        
        checkForUrl(contentUrl, true);
    }

    @Test
    public void testDirectAccessUnsupportedByDefault()
    {
        // By default it is unsupported
        assertFalse(aggregatingContentStoreMock.isDirectAccessSupported());
        verify(primaryStoreMock, times(1)).isDirectAccessSupported();
        verify(secondaryStoreMock, times(1)).isDirectAccessSupported();
    }

    @Test
    public void testIsDirectAccessSupportedByPrimaryStore()
    {
        when(primaryStoreMock.isDirectAccessSupported()).thenReturn(false);
        when(secondaryStoreMock.isDirectAccessSupported()).thenReturn(true);

        assertTrue(aggregatingContentStoreMock.isDirectAccessSupported());
        verify(primaryStoreMock, times(1)).isDirectAccessSupported();
        verify(secondaryStoreMock, times(1)).isDirectAccessSupported();
    }

    @Test
    public void testIsDirectAccessSupportedBySecondaryStore()
    {
        when(primaryStoreMock.isDirectAccessSupported()).thenReturn(true);

        assertTrue(aggregatingContentStoreMock.isDirectAccessSupported());
        verify(primaryStoreMock, times(1)).isDirectAccessSupported();
        verifyNoInteractions(secondaryStoreMock);
    }
    
    @Test
    public void testGetDirectAccessUrl()
    {
        UnsupportedOperationException unsupportedExc = new UnsupportedOperationException();
        UnsupportedContentUrlException unsupportedContentUrlExc = new UnsupportedContentUrlException(aggregatingContentStoreMock, "");

        // By default it is unsupported
        DirectAccessUrl  directAccessUrl = aggregatingContentStoreMock.getDirectAccessUrl("url", null);
        assertNull(directAccessUrl);

        // Direct access not supported
        try
        {
            when(primaryStoreMock.getDirectAccessUrl(eq("urlDANotSupported"), any())).thenThrow(unsupportedExc);
            when(secondaryStoreMock.getDirectAccessUrl(eq("urlDANotSupported"), any())).thenThrow(unsupportedExc);
            aggregatingContentStoreMock.getDirectAccessUrl("urlDANotSupported", null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // Expected
        }

        try
        {
            when(primaryStoreMock.getDirectAccessUrl(eq("urlDANotSupported"), any())).thenThrow(unsupportedContentUrlExc);
            when(secondaryStoreMock.getDirectAccessUrl(eq("urlDANotSupported"), any())).thenThrow(unsupportedExc);
            aggregatingContentStoreMock.getDirectAccessUrl("urlDANotSupported", null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // Expected
        }

        try
        {
            when(primaryStoreMock.getDirectAccessUrl(eq("urlDANotSupported"), any())).thenThrow(unsupportedExc);
            when(secondaryStoreMock.getDirectAccessUrl(eq("urlDANotSupported"), any())).thenThrow(unsupportedContentUrlExc);
            aggregatingContentStoreMock.getDirectAccessUrl("urlDANotSupported", null);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // Expected
        }

        // Content url not supported
        try
        {
            when(primaryStoreMock.getDirectAccessUrl(eq("urlNotSupported"), any())).thenThrow(unsupportedContentUrlExc);
            when(secondaryStoreMock.getDirectAccessUrl(eq("urlNotSupported"), any())).thenThrow(unsupportedContentUrlExc);
            aggregatingContentStoreMock.getDirectAccessUrl("urlNotSupported", null);
            fail();
        }
        catch (UnsupportedContentUrlException e)
        {
            // Expected
        }

        when(primaryStoreMock.getDirectAccessUrl(eq("urlPriSupported"), any())).thenReturn(new DirectAccessUrl());
        directAccessUrl = aggregatingContentStoreMock.getDirectAccessUrl("urlPriSupported", null);
        assertNotNull(directAccessUrl);

        when(primaryStoreMock.getDirectAccessUrl(eq("urlPriSupported"), any())).thenReturn(new DirectAccessUrl());
        directAccessUrl = aggregatingContentStoreMock.getDirectAccessUrl("urlPriSupported", null);
        assertNotNull(directAccessUrl);

        when(primaryStoreMock.getDirectAccessUrl(eq("urlSecSupported"), any())).thenThrow(unsupportedExc);
        when(secondaryStoreMock.getDirectAccessUrl(eq("urlSecSupported"), any())).thenReturn(new DirectAccessUrl());
        directAccessUrl = aggregatingContentStoreMock.getDirectAccessUrl("urlSecSupported", null);
        assertNotNull(directAccessUrl);

        when(primaryStoreMock.getDirectAccessUrl(eq("urlSecSupported"), any())).thenThrow(unsupportedContentUrlExc);
        when(secondaryStoreMock.getDirectAccessUrl(eq("urlSecSupported"), any())).thenReturn(new DirectAccessUrl());
        directAccessUrl = aggregatingContentStoreMock.getDirectAccessUrl("urlSecSupported", null);
        assertNotNull(directAccessUrl);

        when(primaryStoreMock.getDirectAccessUrl(eq("urlPriSupported"), any())).thenReturn(new DirectAccessUrl());
        when(secondaryStoreMock.getDirectAccessUrl(eq("urlSecSupported"), any())).thenReturn(new DirectAccessUrl());
        directAccessUrl = aggregatingContentStoreMock.getDirectAccessUrl("urlPriSupported", null);
        assertNotNull(directAccessUrl);
        directAccessUrl = aggregatingContentStoreMock.getDirectAccessUrl("urlSecSupported", null);
        assertNotNull(directAccessUrl);
    }

    @Test
    public void testIsStorageClassesSupported()
    {
        final StorageClass sc = new StorageClass("a-certain-storage-class");
        when(primaryStoreMock.isStorageClassSupported(sc)).thenReturn(true);
        
        assertTrue(aggregatingContentStoreMock.isStorageClassSupported(sc));
        verify(primaryStoreMock, times(1)).isStorageClassSupported(sc);
        verifyNoInteractions(secondaryStoreMock);
    }

    @Test
    public void testStorageClassesIsNotSupported()
    {
        final StorageClass sc = new StorageClass("a-certain-storage-class");
        when(primaryStoreMock.isStorageClassSupported(sc)).thenReturn(false);
        
        assertFalse(aggregatingContentStoreMock.isStorageClassSupported(sc));
        verify(primaryStoreMock, times(1)).isStorageClassSupported(sc);
        verifyNoInteractions(secondaryStoreMock);
    }

    @Test
    public void testGetSupportedStorageClasses()
    {
        when(primaryStoreMock.getSupportedStorageClasses()).thenReturn(emptySet());

        assertTrue(aggregatingContentStoreMock.getSupportedStorageClasses().isEmpty());
        verify(primaryStoreMock, times(1)).getSupportedStorageClasses();
        verifyNoInteractions(secondaryStoreMock);
    }

    @Test
    public void testUpdateStorageClassesForGivenContentUrl()
    {
        String contentUrl = "contentUrl";
        final StorageClass sc = new StorageClass("a-certain-storage-class");

        aggregatingContentStoreMock.updateStorageClass(contentUrl, sc, null);

        verify(primaryStoreMock, times(1)).updateStorageClass(contentUrl, sc, null);
        verifyNoInteractions(secondaryStoreMock);
    }

    @Test
    public void testFindStorageClassesForGivenContentUrlInPrimaryStore()
    {
        final StorageClass sc = new StorageClass();
        when(primaryStoreMock.findStorageClass(anyString())).thenReturn(sc);

        assertTrue(aggregatingContentStoreMock.findStorageClass("a-contentUrl").isEmpty());
        verify(primaryStoreMock, times(1)).findStorageClass("a-contentUrl");
        verifyNoInteractions(secondaryStoreMock);
    }

    @Test
    public void testFindStorageClassesForGivenContentUrlInSecondaryStore()
    {
        final StorageClass sc = new StorageClass();
        UnsupportedContentUrlException unsupportedContentUrlExc = new UnsupportedContentUrlException(
            aggregatingContentStoreMock, "");

        when(primaryStoreMock.findStorageClass(anyString())).thenThrow(unsupportedContentUrlExc);
        when(secondaryStoreMock.findStorageClass(anyString())).thenReturn(sc);

        assertTrue(aggregatingContentStoreMock.findStorageClass("a-contentUrl").isEmpty());
        verify(primaryStoreMock, times(1)).findStorageClass("a-contentUrl");
        verify(secondaryStoreMock, times(1)).findStorageClass("a-contentUrl");
    }

    @Test(expected = UnsupportedContentUrlException.class)
    public void testFindStorageClassesForInvalidContentUrl()
    {
        when(primaryStoreMock.findStorageClass(anyString()))
            .thenThrow(new UnsupportedContentUrlException(aggregatingContentStoreMock, ""));
        when(secondaryStoreMock.findStorageClass(anyString()))
            .thenThrow(new UnsupportedContentUrlException(aggregatingContentStoreMock, ""));

        aggregatingContentStoreMock.findStorageClass("a-contentUrl");

        verify(primaryStoreMock, times(1)).findStorageClass("a-contentUrl");
        verify(secondaryStoreMock, times(1)).findStorageClass("a-contentUrl");
    }

    @Test
    public void testGetStorageClassesTransitions()
    {
        when(primaryStoreMock.getStorageClassesTransitions()).thenReturn(emptyMap());

        assertTrue(aggregatingContentStoreMock.getStorageClassesTransitions().isEmpty());
        verify(primaryStoreMock, times(1)).getStorageClassesTransitions();
        verifyNoInteractions(secondaryStoreMock);
    }

    @Test
    public void testFindStorageClassesTransitionsForGivenContentUrl()
    {
        when(primaryStoreMock.findStorageClassesTransitions(anyString())).thenReturn(emptyMap());

        assertTrue(
            aggregatingContentStoreMock.findStorageClassesTransitions("contentUrl").isEmpty());
        verify(primaryStoreMock, times(1)).findStorageClassesTransitions("contentUrl");
        verifyNoInteractions(secondaryStoreMock);
    }

    @Test(expected = UnsupportedContentUrlException.class)
    public void testFindStorageClassesTransitionsForUnsupportedContentUrl()
    {
        when(primaryStoreMock.findStorageClassesTransitions(anyString()))
            .thenThrow(new UnsupportedContentUrlException(aggregatingContentStoreMock, ""));

        aggregatingContentStoreMock.findStorageClassesTransitions("contentUrl");
        
        verify(primaryStoreMock, times(1)).findStorageClassesTransitions("contentUrl");
        verifyNoInteractions(secondaryStoreMock);
    }
}
