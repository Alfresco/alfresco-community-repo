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
package org.alfresco.repo.content.metadata;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The test designed for testing the concurrent limitations in
 * {@link AbstractMappingMetadataExtracter#extractRaw(ContentReader, MetadataExtracterLimits)}
 *
 * @author amukha
 */
public class ConcurrencyPdfBoxMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private SlowPdfBoxMetadataExtracter extracter;

    private static final int MAX_CONCURENT_EXTRACTIONS = 5;
    private static final double MAX_DOC_SIZE_MB = 0.03;
    private static final int NUMBER_OF_CONCURRENT_THREADS = 11;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new SlowPdfBoxMetadataExtracter();
        extracter.setDictionaryService(dictionaryService);

        MetadataExtracterLimits pdfLimit = new MetadataExtracterLimits();
        pdfLimit.setMaxConcurrentExtractionsCount(MAX_CONCURENT_EXTRACTIONS);
        pdfLimit.setMaxDocumentSizeMB(MAX_DOC_SIZE_MB);
        Map<String,MetadataExtracterLimits> limits = new HashMap<>();
        limits.put(MimetypeMap.MIMETYPE_PDF,pdfLimit);

        extracter.setMimetypeLimits(limits);
        extracter.setDelay(30*NUMBER_OF_CONCURRENT_THREADS);
        extracter.register();
    }

    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }


    protected void testFileSpecificMetadata(String mimetype, Map<QName, Serializable> properties)
    {
        // not required
    }


    public void testConcurrentExtractions() throws InterruptedException
    {
        final Map<String, Boolean> threadResults = new ConcurrentHashMap<>();
        for (int i = 0; i < NUMBER_OF_CONCURRENT_THREADS; i++)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    System.out.println(Thread.currentThread().getName() + " started " + System.currentTimeMillis());
                    try
                    {
                        Map<QName, Serializable> results = extractFromMimetype(MimetypeMap.MIMETYPE_PDF);
                        System.out.println(Thread.currentThread().getName() + " results are " + results);
                        threadResults.put(Thread.currentThread().getName(), !results.isEmpty());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + " finished " + System.currentTimeMillis());
                }

            }).start();
        }
        int numWaits = NUMBER_OF_CONCURRENT_THREADS*10;
        while (numWaits > 0)
        {
            Thread.sleep(50);
            if (threadResults.size() == NUMBER_OF_CONCURRENT_THREADS)
            {
                break;
            }
            numWaits--;
        }
        Map<Boolean, Integer> counted = new HashMap<>();
        counted.put(Boolean.FALSE, 0);
        counted.put(Boolean.TRUE, 0);
        for (Boolean result : threadResults.values())
        {
            counted.put(result, counted.get(result)+1);
        }
        assertEquals("Wrong number of failed extractions.",
                new Integer(NUMBER_OF_CONCURRENT_THREADS - MAX_CONCURENT_EXTRACTIONS),
                counted.get(Boolean.FALSE));
        assertEquals("Wrong number of successful extractions.",
                new Integer(MAX_CONCURENT_EXTRACTIONS),
                counted.get(Boolean.TRUE));
    }

    private class SlowPdfBoxMetadataExtracter extends PdfBoxMetadataExtracter
    {
        private long delay = 0;

        public void setDelay(long delay)
        {
            this.delay = delay;
        }

        @Override
        protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
        {
            Thread.sleep(delay);
            Map<String, Serializable> results = super.extractRaw(reader);
            System.out.println(Thread.currentThread().getName() + " results are " + results);
            return results;
        }
    }
}
