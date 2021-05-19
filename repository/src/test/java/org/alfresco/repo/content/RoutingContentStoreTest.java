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
package org.alfresco.repo.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Ensures that the routing of URLs based on context is working.  A combination
 * of fully featured and incompletely featured stores is used to ensure that
 * all routing scenarios are handled.
 * 
 * @see AbstractRoutingContentStore
 * @since 2.1
 * 
 * @author Derek Hulley
 */
@Category(OwnJVMTestsCategory.class)
public class RoutingContentStoreTest extends AbstractWritableContentStoreTest
{
    private ContentStore storeA;
    private ContentStore storeB;
    private ContentStore storeC;
    private ContentStore storeD;
    private ContentStore routingStore;
    
    @Before
    public void before() throws Exception
    {
        File tempDir = TempFileProvider.getTempDir();
        // Create a subdirectory for A
        File storeADir = new File(tempDir, "A");
        storeA = new FileContentStore(ctx, storeADir);
        // Create a subdirectory for B
        File storeBDir = new File(tempDir, "B");
        storeB = new FileContentStore(ctx, storeBDir);
        // Create a subdirectory for C
        File storeCDir = new File(tempDir, "C");
        storeC = new DumbReadOnlyFileStore(new FileContentStore(ctx, storeCDir));
        // No subdirectory for D
        storeD = new SupportsNoUrlFormatStore();
        // Create the routing store
        routingStore = new RandomRoutingContentStore(storeA, storeB, storeC, storeD);
    }
    
    @Override
    protected ContentStore getStore()
    {
        return routingStore;
    }

    @Test
    public void testSetUp() throws Exception
    {
        assertNotNull(routingStore);
    }
    
    private void checkForContent(String contentUrl, String content)
    {
        for (ContentStore store : new ContentStore[] {storeA, storeB})
        {
            // Does the store have it
            if (store.exists(contentUrl))
            {
                // Check it
                ContentReader reader = store.getReader(contentUrl);
                String checkContent = reader.getContentString();
                assertEquals("Content found but is incorrect", content, checkContent);
                return;
            }
        }
        fail("Content not found in any of the stores: " + contentUrl);
    }
    
    /**
     * Checks that requests for missing content URLs are served.
     */
    @Test
    public void testMissingUrl()
    {
        String missingContentUrl = FileContentStore.createNewFileStoreUrl();
        
        ContentReader reader = routingStore.getReader(missingContentUrl);
        assertNotNull("Missing URL should not return null", reader);
        assertFalse("Empty reader should say content doesn't exist.", reader.exists());
        try
        {
            reader.getContentString();
            fail("Empty reader cannot return content.");
        }
        catch (Throwable e)
        {
            // Expected
        }
    }
    
    @Test
    public void testGeneralUse()
    {
        for (int i = 0 ; i < 20; i++)
        {
            ContentContext contentContext = new ContentContext(null, null);
            ContentWriter writer = routingStore.getWriter(contentContext);
            String content = "This was generated by " + this.getClass().getName() + "#testGeneralUse number " + i;
            writer.putContent(content);
            // Check that it exists
            String contentUrl = writer.getContentUrl();
            checkForContent(contentUrl, content);
            
            // Now go direct to the routing store and check that it is able to find the appropriate URLs
            ContentReader reader = routingStore.getReader(contentUrl);
            assertNotNull("Null reader returned", reader);
            assertTrue("Reader should be onto live content", reader.exists());
        }
    }

    @Test
    public void testIsStorageClassesSupported()
    {
        assertTrue(routingStore.isStorageClassesSupported(null));
    }
    
    /**
     * A test routing store that directs content writes to a randomly-chosen store.
     * Matching of content URLs back to the stores is handled by the base class.
     * 
     * @author Derek Hulley
     */
    private static class RandomRoutingContentStore extends AbstractRoutingContentStore
    {
        private List<ContentStore> stores;
        
        public RandomRoutingContentStore(ContentStore ... stores)
        {
            this.stores = new ArrayList<ContentStore>(5);
            for (ContentStore store : stores)
            {
                this.stores.add(store);
            }
            SimpleCache<Pair<String,String>, ContentStore> cache = new DefaultSimpleCache<Pair<String,String>, ContentStore>(11, getClass().getName());
            super.setStoresCache(cache);
        }
        
        @Override
        protected List<ContentStore> getAllStores()
        {
            return stores;
        }

        @Override
        protected ContentStore selectWriteStore(ContentContext ctx)
        {
            // Shuffle the list of writable stores
            List<ContentStore> shuffled = new ArrayList<ContentStore>(stores);
            Collections.shuffle(shuffled);
            // Pick the first writable store
            for (ContentStore store : shuffled)
            {
                if (store.isWriteSupported())
                {
                    return store;
                }
            }
            // Nothing found
            fail("A request came for a writer when there is no writable store to choose from");
            return null;
        }
    }
    
    /**
     * The simplest possible store.
     * 
     * @author Derek Hulley
     */
    private static class DumbReadOnlyFileStore extends AbstractContentStore
    {
        FileContentStore fileStore;
        public DumbReadOnlyFileStore(FileContentStore fileStore)
        {
            this.fileStore = fileStore;
        }

        public boolean isWriteSupported()
        {
            return false;
        }

        public ContentReader getReader(String contentUrl)
        {
            return fileStore.getReader(contentUrl);
        }
    }
    
    /**
     * This store supports nothing.  It is designed to catch the routing code out.
     * 
     * @author Derek Hulley
     */
    private static class SupportsNoUrlFormatStore extends AbstractContentStore
    {
        public SupportsNoUrlFormatStore()
        {
        }

        public boolean isWriteSupported()
        {
            return false;
        }

        public ContentReader getReader(String contentUrl)
        {
            throw new UnsupportedContentUrlException(this, contentUrl);
        }
    }
}
