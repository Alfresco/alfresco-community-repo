/*
 * Copyright (C) 2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
