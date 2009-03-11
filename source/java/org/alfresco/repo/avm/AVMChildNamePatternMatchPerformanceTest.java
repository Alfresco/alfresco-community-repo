package org.alfresco.repo.avm;

import java.util.List;
import java.util.SortedMap;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.GUID;

public class AVMChildNamePatternMatchPerformanceTest extends AVMServiceTestBase
{

    public void test_1000() throws Exception
    {
        fTransactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {

            public Object execute() throws Throwable
            {
                try
                {
                    fService.createStore("StagingArea");
                    fService.createStore("SandBox");

                    fService.createDirectory("StagingArea:/", "www");
                    fService.createLayeredDirectory("StagingArea:/www", "SandBox:/", "www");

                    for (int i = 0; i < 500; i++)
                    {
                        String name = GUID.generate();
                        if (i % 100 == 0)
                        {
                            name = "A" + name;
                        }
                        if(name.startsWith("a"))
                        {
                            name = "G"+name;
                        }
                        fService.createFile("SandBox:/www", name).close();
                    }
                    System.out.println("Create SandBox:/www");

                   
                    for (int i = 0; i < 500; i++)
                    {
                        String name = GUID.generate();
                        if (i % 100 == 0)
                        {
                            name = "A" + name;
                        }
                        if(name.startsWith("a"))
                        {
                            name = "G"+name;
                        }
                        fService.createFile("StagingArea:/www", name).close();
                    }
                    System.out.println("Create StagingArea:/www");

                    long start = System.nanoTime();
                    AVMNodeDescriptor dir = fService.lookup(-1, "SandBox:/www");
                    SortedMap<String, AVMNodeDescriptor> result = fService.getDirectoryListing(dir, "A*");
                    assertEquals(10, result.size());
                    long end = System.nanoTime();
                    System.out.println("Pattern in " + ((end - start) / 1000000000.0f));
                }
                finally
                {
                    fService.purgeStore("StagingArea");
                    fService.purgeStore("SandBox");
                }
                return null;
            }
        });

    }
}
