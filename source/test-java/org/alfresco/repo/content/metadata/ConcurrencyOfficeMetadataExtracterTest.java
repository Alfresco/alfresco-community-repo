/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.content.metadata;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

/**
 * MNT-8978
 */
public class ConcurrencyOfficeMetadataExtracterTest
{

    private OfficeMetadataExtracter extracter = new OfficeMetadataExtracter();

    private final Date testDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime("2010-10-22").toDate();

    @Test
    public void testDateFormatting() throws Exception
    {
        Callable<Date> task = new Callable<Date>()
        {
            public Date call() throws Exception
            {
                return extracter.makeDate("2010-10-22");
            }
        };

        // pool with 5 threads
        ExecutorService exec = Executors.newFixedThreadPool(5);
        List<Future<Date>> results = new ArrayList<Future<Date>>();

        // perform 10 date conversions
        for (int i = 0; i < 10; i++)
        {
            results.add(exec.submit(task));
        }
        exec.shutdown();

        for (Future<Date> result : results)
        {
            assertEquals(testDate, result.get());
        }
    }

}
