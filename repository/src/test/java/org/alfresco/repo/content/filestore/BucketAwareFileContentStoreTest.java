/*
 * Copyright (C) 2005-2010 Alfresco Software Limited./*
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
package org.alfresco.repo.content.filestore;

import java.io.File;

import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link FileContentStore FileContentStore} which uses
 * {@link TimeBasedFileContentUrlProvider TimeBasedFileContentUrlProvider}
 * configured for provisioning splitting data into buckets within the
 * <b>minute</b> range
 * 
 * @author Andreea Dragoi
 *
 */
@Category(OwnJVMTestsCategory.class)
public class BucketAwareFileContentStoreTest extends FileContentStoreTest
{
    private static final int BUCKETS_PER_MINUTE = 20;
    private static final int ITERATIONS = 5;

    @Before
    public void before() throws Exception
    {
        super.before();

        TimeBasedFileContentUrlProvider fileContentUrlProvider = new TimeBasedFileContentUrlProvider();
        // configure url provider to create buckets on minute range on a
        // interval of 3 seconds (60/MINUTE_BUCKET_COUNT)
        fileContentUrlProvider.setBucketsPerMinute(BUCKETS_PER_MINUTE);
        store.setFileContentUrlProvider(fileContentUrlProvider);
    }

    @Test
    public void testBucketCreation() throws Exception
    {
        // create several files in a interval of ~15 seconds
        // depending when the test is started files can be created on same
        // minute or not
        File firstFile = store.createNewFile();
        for (int i = 0; i < ITERATIONS; i++)
        {
            store.createNewFile();
            Thread.sleep(3000);
        }
        File lastFile = store.createNewFile();

        // check the minute for first and last file created
        File firstFileMinute = firstFile.getParentFile().getParentFile();
        File lastFileMinute = lastFile.getParentFile().getParentFile();

        int createdBuckets;
        int firstFileMinuteBuckets = firstFileMinute.list().length;

        if (!firstFileMinute.equals(lastFileMinute))
        {
            // files are created in different minutes
            int lastFileMinutesBuckets = lastFileMinute.list().length;
            createdBuckets = firstFileMinuteBuckets + lastFileMinutesBuckets;
        }
        else
        {
            // files are created on same minute
            createdBuckets = firstFileMinuteBuckets;
        }

        // Interval of 15s + time for file creation, expecting (ITERATIONS + 1)
        // buckets
        assertTrue("Unexpected number of buckets created", createdBuckets == ITERATIONS + 1);
    }

}
