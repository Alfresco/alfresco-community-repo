package org.alfresco.repo.avm;

import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.GUID;

public class AVMDiffPerformanceTest extends AVMServiceTestBase
{

    public void test_10000() throws Exception
    {
        fTransactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>() {

            public Object execute() throws Throwable
            {
                try
                {
                    fService.createStore("StagingArea");
                    fService.createStore("SandBox");

                    fService.createDirectory("StagingArea:/", "www");
                    fService.createDirectory("StagingArea:/www", "test");
                    fService.createLayeredDirectory("StagingArea:/www", "SandBox:/", "www");

                    
                    for(int i = 0; i < 10000; i++)
                    {
                        String name = GUID.generate();
                        fService.createFile("SandBox:/www", name).close();
                    }
                    System.out.println("Create SandBox:/www");
                    
                    
                    for(int i = 0; i < 10000; i++)
                    {
                        String name = GUID.generate();
                        fService.createFile("SandBox:/www/test", name).close();
                    }
                    System.out.println("Create SandBox:/www/test");
                    
                    for(int i = 0; i < 10000; i++)
                    {
                        String name = GUID.generate();
                        fService.createFile("StagingArea:/www", name).close();
                    }
                    System.out.println("Create StagingArea:/www");
                    
                    for(int i = 0; i < 10000; i++)
                    {
                        String name = GUID.generate();
                        fService.createFile("StagingArea:/www/test", name).close();
                    }
                    System.out.println("Create StagingArea:/www/test");
                    
                    long start = System.nanoTime();
                    List<AVMDifference> diffs = fSyncService.compare(-1, "SandBox:/www", -1, "StagingArea:/www", null);
                    long end = System.nanoTime();
                    System.out.println("Diff in "+( (end-start)/1000000000.0f));
                    
                    assertEquals(20000, diffs.size());
                    

                }
                finally
                {
                    fService.purgeStore("StagingArea");
                    fService.purgeStore("SandBox");
                }
                return null;
            }});
        

    }
}
