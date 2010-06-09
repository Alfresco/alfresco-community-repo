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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
 
package org.alfresco.repo.avm;

import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.util.GUID;

public class AVMDiffPerformanceTest extends AVMServiceTestBase
{
    public void testSetup() throws Exception
    {
        super.testSetup();
    }
    
    public void test_1000() throws Exception
    {
        runTest(1000);
    }
    
    public void xtest_2000() throws Exception
    {
        runTest(2000);
    }
    
    /*
    public void xtest_10000() throws Exception
    {
        runTest(10000);
    }
    */

    private void runTest(final int cnt) throws Exception
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

                    long start = System.nanoTime();
                    for(int i = 0; i < cnt; i++)
                    {
                        String name = GUID.generate();
                        fService.createFile("SandBox:/www", name).close();
                    }
                    long end = System.nanoTime();
                    System.out.println("Create SandBox:/www in "+( (end-start)/1000000000.0f));
                    
                    start = System.nanoTime();
                    for(int i = 0; i < cnt; i++)
                    {
                        String name = GUID.generate();
                        fService.createFile("SandBox:/www/test", name).close();
                    }
                    end = System.nanoTime();
                    System.out.println("Create SandBox:/www/test in "+( (end-start)/1000000000.0f));
                    
                    start = System.nanoTime();
                    for(int i = 0; i < cnt; i++)
                    {
                        String name = GUID.generate();
                        fService.createFile("StagingArea:/www", name).close();
                    }
                    end = System.nanoTime();
                    System.out.println("Create StagingArea:/www in "+( (end-start)/1000000000.0f));
                    
                    start = System.nanoTime();
                    for(int i = 0; i < cnt; i++)
                    {
                        String name = GUID.generate();
                        fService.createFile("StagingArea:/www/test", name).close();
                    }
                    end = System.nanoTime();
                    System.out.println("Create StagingArea:/www/test in "+( (end-start)/1000000000.0f));
                    
                    start = System.nanoTime();
                    List<AVMDifference> diffs = fSyncService.compare(-1, "SandBox:/www", -1, "StagingArea:/www", null);
                    end = System.nanoTime();
                    System.out.println("Diff in "+( (end-start)/1000000000.0f));
                    
                    assertEquals(cnt*2, diffs.size());
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