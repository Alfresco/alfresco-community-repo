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
