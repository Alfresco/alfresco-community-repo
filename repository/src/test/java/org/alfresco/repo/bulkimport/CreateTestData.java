/*
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
package org.alfresco.repo.bulkimport;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.alfresco.util.GUID;

public class CreateTestData
{

    private void createFile(File sourceFile, File targetFolder) throws IOException
    {
        File f = new File(targetFolder, GUID.generate() + ".bin");
        FileUtils.copyFile(sourceFile, f);
    }

    private void createDirectory(File sourceFolder, File targetFolder) throws IOException
    {
        String s = sourceFolder.getName();
        if (s != null && !s.equals(""))
        {
            // String path = sourceFolder.getPath();
            // int i = path.lastIndexOf(File.separatorChar);
            // if(i != -1)
            // {
            // String s = path.substring(i);
            File d = new File(targetFolder, s);
            if (d.exists() || d.mkdir())
            {
                createTestData(sourceFolder, d);
            }
            else
            {
                System.err.println("Unable to create directory " + d.getAbsolutePath());
            }
        }
    }

    public void createTestData(File sourceFolder, File targetFolder) throws IOException
    {
        File[] files = sourceFolder.listFiles();
        for (File file : files)
        {
            if (file.getName().startsWith("."))
            {
                continue;
            }
            if (file.isDirectory())
            {
                createDirectory(file, targetFolder);
                // String path = file.getAbsolutePath();
                // int i = path.lastIndexOf(File.separatorChar);
                // if(i != -1)
                // {
                // String s = path.substring(i);
                // File d = new File(targetFolder, s);
                // if(d.mkdir())
                // {
                // createTestData(file, d);
                // }
                // else
                // {
                // System.err.println("Unable to create directory " + d.getAbsolutePath());
                // }
                // }
            }
            else
            {
                createFile(file, targetFolder);
            }
        }
    }

    public void execute(String sourceFolderPath, String targetFolderPath) throws IOException
    {
        File targetFolder = new File(targetFolderPath);
        File sourceFolder = new File(sourceFolderPath);

        if (!sourceFolder.isDirectory())
        {
            throw new IllegalArgumentException("source is not a folder");
        }

        if (!targetFolder.isDirectory())
        {
            throw new IllegalArgumentException("target is not a folder");
        }

        createDirectory(sourceFolder, targetFolder);
        // int i = sourceFolderPath.lastIndexOf(File.separatorChar);
        // if(i != -1)
        // {
        // String s = sourceFolderPath.substring(i);
        // File d = new File(targetFolder, s);
        // if(d.mkdir())
        // {
        // createTestData(sourceFolder, d);
        // }
        // }
    }

    public static void main(String[] args)
    {
        try
        {
            new CreateTestData().execute(args[0], args[1]);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}
