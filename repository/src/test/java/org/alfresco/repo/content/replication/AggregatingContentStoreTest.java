/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.content.AbstractWritableContentStoreTest;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
public class AggregatingContentStoreTest extends AbstractWritableContentStoreTest
{
    private static final String SOME_CONTENT = "The No. 1 Ladies' Detective Agency";
    public static final String X_AMZ_HEADER_1 = "x-amz-header1";
    public static final String VALUE_1 = "value1";
    public static final String X_AMZ_HEADER_2 = "x-amz-header2";
    public static final String VALUE_2 = "value2";

    private AggregatingContentStore aggregatingStore;
    private ContentStore primaryStore;
    private List<ContentStore> secondaryStores;

    @Mock
    ContentStore primaryStoreMock;
    @Mock
    ContentStore secondaryStoreMock;
    @Mock
    AggregatingContentStore aggregatingContentStoreMock;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

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
        // Create the aggregating store
        aggregatingStore = new AggregatingContentStore();
        aggregatingStore.setPrimaryStore(primaryStore);
        aggregatingStore.setSecondaryStores(secondaryStores);
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
    public void testIsContentDirectUrlEnabled()
    {
        // Create the aggregating store
        AggregatingContentStore aggStore = new AggregatingContentStore();
        aggStore.setPrimaryStore(primaryStoreMock);
        aggStore.setSecondaryStores(List.of(secondaryStoreMock));

        // By default it is unsupported
        assertFalse(aggStore.isContentDirectUrlEnabled());

        // Supported if at least one store supports direct access
        {
            when(primaryStoreMock.isContentDirectUrlEnabled()).thenReturn(false);
            when(secondaryStoreMock.isContentDirectUrlEnabled()).thenReturn(true);
            assertTrue(aggStore.isContentDirectUrlEnabled());

            when(primaryStoreMock.isContentDirectUrlEnabled()).thenReturn(true);
            when(secondaryStoreMock.isContentDirectUrlEnabled()).thenReturn(true);
            assertTrue(aggStore.isContentDirectUrlEnabled());

            when(primaryStoreMock.isContentDirectUrlEnabled()).thenReturn(true);
            when(secondaryStoreMock.isContentDirectUrlEnabled()).thenReturn(false);
            assertTrue(aggStore.isContentDirectUrlEnabled());
        }
    }

    @Test
    public void testRequestContentDirectUrl()
    {
        // Create the aggregating store
        AggregatingContentStore aggStore = new AggregatingContentStore();
        aggStore.setPrimaryStore(primaryStoreMock);
        aggStore.setSecondaryStores(List.of(secondaryStoreMock));

        UnsupportedOperationException unsupportedExc = new UnsupportedOperationException();
        UnsupportedContentUrlException unsupportedContentUrlExc = new UnsupportedContentUrlException(aggStore, "");

        // By default it is unsupported
        DirectAccessUrl directAccessUrl = aggStore.requestContentDirectUrl("url", true, "anyfilename", "anyMimetype", 30L);
        assertNull(directAccessUrl);

        // Direct access not supported
        try
        {
            when(primaryStoreMock.requestContentDirectUrl(any(), any(), any(), any(), any())).thenThrow(unsupportedExc);
            when(secondaryStoreMock.requestContentDirectUrl(eq("urlDANotSupported"), any(), any(), any(), any())).thenThrow(unsupportedExc);
            aggStore.requestContentDirectUrl(eq("urlDANotSupported"), true, "anyfilename", 30L);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // Expected
        }

        try
        {
            when(primaryStoreMock.requestContentDirectUrl(eq("urlDANotSupported"), any(), any(), any(), any())).thenThrow(unsupportedContentUrlExc);
            when(secondaryStoreMock.requestContentDirectUrl(eq("urlDANotSupported"), any(), any(), any(), any())).thenThrow(unsupportedExc);
            aggStore.requestContentDirectUrl("urlDANotSupported", true, "anyfilename", "anyMimetype", 30L);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // Expected
        }

        try
        {
            when(primaryStoreMock.requestContentDirectUrl(eq("urlDANotSupported"), any(), any(), any(), any())).thenThrow(unsupportedExc);
            when(secondaryStoreMock.requestContentDirectUrl(eq("urlDANotSupported"), any(), any(), any(), any())).thenThrow(unsupportedContentUrlExc);
            aggStore.requestContentDirectUrl("urlDANotSupported", true, "anyfilename", "anyMimetype", 30L);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // Expected
        }

        // Content url not supported
        try
        {
            when(primaryStoreMock.requestContentDirectUrl(eq("urlNotSupported"), any(), any(), any(), any())).thenThrow(unsupportedContentUrlExc);
            when(secondaryStoreMock.requestContentDirectUrl(eq("urlNotSupported"), any(), any(), any(), any())).thenThrow(unsupportedContentUrlExc);
            aggStore.requestContentDirectUrl("urlNotSupported", true, "anyfilename", "anyMimetype", 30L);
            fail();
        }
        catch (UnsupportedContentUrlException e)
        {
            // Expected
        }

        when(primaryStoreMock.requestContentDirectUrl(eq("urlPriSupported"), any(), any(), any(), any())).thenReturn(new DirectAccessUrl());
        when(secondaryStoreMock.requestContentDirectUrl(eq("urlPriSupported"), any(), any(), any(), any())).thenThrow(unsupportedExc);
        directAccessUrl = aggStore.requestContentDirectUrl("urlPriSupported", true, "anyfilename", "anyMimetype", 30L);
        assertNotNull(directAccessUrl);

        when(primaryStoreMock.requestContentDirectUrl(eq("urlPriSupported"), any(), any(), any(), any())).thenReturn(new DirectAccessUrl());
        when(secondaryStoreMock.requestContentDirectUrl(eq("urlPriSupported"), any(), any(), any(), any())).thenThrow(unsupportedContentUrlExc);
        directAccessUrl = aggStore.requestContentDirectUrl("urlPriSupported", true, "anyfilename", "anyMimetype", 30L);
        assertNotNull(directAccessUrl);

        when(primaryStoreMock.requestContentDirectUrl(eq("urlSecSupported"), any(), any(), any(), any())).thenThrow(unsupportedExc);
        when(secondaryStoreMock.requestContentDirectUrl(eq("urlSecSupported"), any(), any(), any(), any())).thenReturn(new DirectAccessUrl());
        directAccessUrl = aggStore.requestContentDirectUrl("urlSecSupported", true, "anyfilename", "anyMimetype", 30L);
        assertNotNull(directAccessUrl);

        when(primaryStoreMock.requestContentDirectUrl(eq("urlSecSupported"), any(), any(), any(), any())).thenThrow(unsupportedContentUrlExc);
        when(secondaryStoreMock.requestContentDirectUrl(eq("urlSecSupported"), any(), any(), any(), any())).thenReturn(new DirectAccessUrl());
        directAccessUrl = aggStore.requestContentDirectUrl("urlSecSupported", true, "anyfilename", "anyMimetype", 30L);
        assertNotNull(directAccessUrl);

        when(primaryStoreMock.requestContentDirectUrl(eq("urlPriSupported"), any(), any(), any(), any())).thenReturn(new DirectAccessUrl());
        when(secondaryStoreMock.requestContentDirectUrl(eq("urlSecSupported"), any(), any(), any(), any())).thenReturn(new DirectAccessUrl());
        directAccessUrl = aggStore.requestContentDirectUrl("urlPriSupported", true, "anyfilename", "anyMimetype", 30L);
        assertNotNull(directAccessUrl);
        directAccessUrl = aggStore.requestContentDirectUrl("urlSecSupported", true, "anyfilename", "anyMimetype", 30L);
        assertNotNull(directAccessUrl);
    }

}
