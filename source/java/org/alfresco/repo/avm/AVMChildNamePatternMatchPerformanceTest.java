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
package org.alfresco.repo.avm;

import java.util.SortedMap;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
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
