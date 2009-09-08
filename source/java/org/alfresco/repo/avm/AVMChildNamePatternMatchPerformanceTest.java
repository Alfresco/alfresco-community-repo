/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
