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
package org.alfresco.repo.content.replication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.repo.content.AbstractWritableContentStoreTest;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.ContentStore.ContentUrlHandler;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.junit.Before;
import org.junit.experimental.categories.Category;

/**
 * Tests read and write functionality for the aggregating store.
 * <p>
 * 
 * @see org.alfresco.repo.content.replication.AggregatingContentStore
 * 
 * @author Derek Hulley
 * @author Mark Rogers
 */
@Category(OwnJVMTestsCategory.class)
public class AggregatingContentStoreTest extends AbstractWritableContentStoreTest
{
    private static final String SOME_CONTENT = "The No. 1 Ladies' Detective Agency";
    
    private AggregatingContentStore aggregatingStore;
    private ContentStore primaryStore;
    private List<ContentStore> secondaryStores;
    
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
     * @param contentUrl
     * @param mustExist true if the content must exist, false if it must <b>not</b> exist
     */
    private void checkForUrl(String contentUrl, boolean mustExist)
    {
        // check that the URL is present for each of the stores
        for (ContentStore store : secondaryStores)
        {
            final Set<String> urls = new HashSet<String>(1027);
            ContentUrlHandler handler = new ContentUrlHandler()
            {
                public void handle(String contentUrl)
                {
                    urls.add(contentUrl);
                }
            };
            store.getUrls(handler);
            assertTrue("URL of new content not present in store", urls.contains(contentUrl) == mustExist);
        }
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
  

}
