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

import java.util.Locale;

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentAccessor;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests for CachingContentStore where all the main collaborators are defined as Spring beans.
 * 
 * @author Matt Ward
 */
public class FullTest
{
    private ApplicationContext ctx;
    private CachingContentStore store;

    @Before
    public void setUp()
    {
        String conf = "classpath:cachingstore/test-context.xml";
        ctx = ApplicationContextHelper.getApplicationContext(new String[] { conf });
        
        store = (CachingContentStore) ctx.getBean("cachingContentStore");
        store.setCacheOnInbound(true);
    }

    
    @Test
    public void canUseCachingContentStore()
    {
        // Write through the caching content store - cache during the process.
        ContentWriter writer = store.getWriter(ContentContext.NULL_CONTEXT);
        final String content = makeContent();
        writer.putContent(content);
        
        ContentReader reader = store.getReader(writer.getContentUrl());
        assertEquals("Reader and writer should have same URLs", writer.getContentUrl(), reader.getContentUrl());
        assertEquals("Reader should get correct content", content, reader.getContentString());
    }
    
    
    @Test
    public void writeToCacheWithContentContext()
    {
        // Write through the caching content store - cache during the process.
        final String proposedUrl = FileContentStore.createNewFileStoreUrl();
        ContentWriter writer = store.getWriter(new ContentContext(null, proposedUrl));
        final String content = makeContent();
        writer.putContent(content);
        assertEquals("Writer should have correct URL", proposedUrl, writer.getContentUrl());
        
        ContentReader reader = store.getReader(writer.getContentUrl());
        assertEquals("Reader and writer should have same URLs", writer.getContentUrl(), reader.getContentUrl());
        assertEquals("Reader should get correct content", content, reader.getContentString());
    }

    
    @Test
    public void writeToCacheWithExistingReader()
    {   
        ContentWriter oldWriter = store.getWriter(ContentContext.NULL_CONTEXT);
        oldWriter.putContent("Old content for " + getClass().getSimpleName());
        ContentReader existingReader = oldWriter.getReader();
        
        // Write through the caching content store - cache during the process.
        final String proposedUrl = FileContentStore.createNewFileStoreUrl();
        ContentWriter writer = store.getWriter(new ContentContext(existingReader, proposedUrl));
        final String content = makeContent();
        writer.putContent(content);
        assertEquals("Writer should have correct URL", proposedUrl, writer.getContentUrl());
        
        assertFalse("Old and new writers must have different URLs",
                    oldWriter.getContentUrl().equals(writer.getContentUrl()));
        
        ContentReader reader = store.getReader(writer.getContentUrl());
        assertEquals("Reader and writer should have same URLs", writer.getContentUrl(), reader.getContentUrl());
        assertEquals("Reader should get correct content", content, reader.getContentString());
    }
    
       
    private String makeContent()
    {
        return "Example content for " + getClass().getSimpleName();
    }
}
