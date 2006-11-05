/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

package org.alfresco.repo.avm;

import java.io.PrintStream;

/**
 * Performance test(s).
 * @author britt
 */
public class AVMServicePerfTest extends AVMServiceTestBase
{
    /**
     * Test adding 100 files to each directory.
     */
    public void testAdd100a()
    {
        try
        {
            String [] dirs = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" };
            for (String dir : dirs)
            {
                fService.createDirectory("main:/", dir);
                String ndir = "main:/" + dir;
                fService.createDirectory(ndir, dir);
                ndir = ndir + "/" + dir;
                for (int i = 0; i < 100; i++)
                {
                    PrintStream out = new PrintStream(fService.createFile(ndir, "file" + i));
                    out.println("I am " + ndir + "/file" + i);
                    System.out.println(ndir + "/file" + i);
                    out.close();
                }
                fService.createSnapshot("main", null, null);
            }
            //            System.out.println(recursiveList("main", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test adding 100 files to each directory.
     */
    public void testAdd100b()
    {
        try
        {
            String [] dirs = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" };
            for (String dir : dirs)
            {
                fService.createDirectory("main:/", dir);
                String ndir = "main:/" + dir;
                fService.createDirectory(ndir, dir);
                ndir = ndir + "/" + dir;
                for (int i = 0; i < 100; i++)
                {
                    PrintStream out = new PrintStream(fService.createFile(ndir, "file" + i));
                    out.println("I am " + ndir + "/file" + i);
                    System.out.println(ndir + "/file" + i);
                    out.close();
                }
                fService.createSnapshot("main", null, null);
            }
            //            System.out.println(recursiveList("main", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test adding 100 files to each directory.
     */
    public void testAdd100c()
    {
        try
        {
            String [] dirs = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" };
            for (String dir : dirs)
            {
                fService.createDirectory("main:/", dir);
                String ndir = "main:/" + dir;
                fService.createDirectory(ndir, dir);
                ndir = ndir + "/" + dir;
                for (int i = 0; i < 100; i++)
                {
                    PrintStream out = new PrintStream(fService.createFile(ndir, "file" + i));
                    out.println("I am " + ndir + "/file" + i);
                    System.out.println(ndir + "/file" + i);
                    out.close();
                }
                fService.createSnapshot("main", null, null);
            }
            //            System.out.println(recursiveList("main", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test adding 100 files to each directory.
     */
    public void testAdd100d()
    {
        try
        {
            String [] dirs = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" };
            for (String dir : dirs)
            {
                fService.createDirectory("main:/", dir);
                String ndir = "main:/" + dir;
                fService.createDirectory(ndir, dir);
                ndir = ndir + "/" + dir;
                for (int i = 0; i < 100; i++)
                {
                    PrintStream out = new PrintStream(fService.createFile(ndir, "file" + i));
                    out.println("I am " + ndir + "/file" + i);
                    out.close();
                    System.out.println(ndir + "/file" + i);
                }
                fService.createSnapshot("main", null, null);
            }
            //            System.out.println(recursiveList("main", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
}
