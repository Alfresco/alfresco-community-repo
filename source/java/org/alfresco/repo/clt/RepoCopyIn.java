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
package org.alfresco.repo.clt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Simplified cp from a local filesystem to the repo.
 * @author britt
 */
public class RepoCopyIn extends CltBase 
{
    private static Object [] flagDefs = { "-r", 0, "-v", 0 };
    
    private static String USAGE = "usage: RepoCopyIn fspath repopath";
    
    private boolean fVerbose;
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.clt.CltBase#run(java.util.Map, java.util.List)
     */
    @Override
    protected void run(Map<String, List<String>> flags, List<String> args) 
    {
        if (flags.containsKey("-v"))
        {
            fVerbose = true;
        }
        else
        {
            fVerbose = false;
        }
        NodeRef root = fRepoRemote.getRoot();
        String path = args.get(1);
        while (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        Pair<NodeRef, Boolean> dst = fRepoRemote.lookup(root, path);
        if (flags.containsKey("-r"))
        {
            if (dst == null)
            {
                System.err.println(args.get(1) + " does not exist.");
                fContext.close();
                System.exit(1);
            }
            recursiveCopy(args.get(0), dst.getFirst());
            return;
        }
        File file = new File(args.get(0));
        if (!file.isFile())
        {
            System.err.println(args.get(0) + " not found, or not a file.");
            fContext.close();
            System.exit(1);
        }
        if (dst == null)
        {
            try
            {
                if (fVerbose)
                {
                    System.out.println(file.getName() + " -> " + args.get(1));
                }
                InputStream in =
                    new FileInputStream(file);
                OutputStream out = fRepoRemote.createFile(root, path);
                copyStream(in, out);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                fContext.close();
                System.exit(1);
            }
        }
        else
        {
            if (!dst.getSecond())
            {
                System.err.println("Target must be a directory.");
                fContext.close();
                System.exit(1);
            }
            try
            {
                if (fVerbose)
                {
                    System.out.println(file.getName() + " -> " + args.get(1));
                }
                InputStream in = 
                    new FileInputStream(file);
                OutputStream out =
                    fAVMRemote.createFile(args.get(1), file.getName());
                copyStream(in, out);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                fContext.close();
                System.exit(1);
            }
        }
    }

    private void recursiveCopy(String sourcePath, NodeRef dest)
    {
        File file = new File(sourcePath);
        if (fVerbose)
        {
            System.out.println(sourcePath + " -> " + dest);
        }
        if (file.isDirectory())
        {
            NodeRef dir = fRepoRemote.createDirectory(dest, file.getName());
            String [] names = file.list();
            for (String name : names)
            {
                recursiveCopy(sourcePath + File.separatorChar + name,
                              dir);                
            }
        }
        else
        {
            try
            {
                InputStream in =
                    new FileInputStream(file);
                OutputStream out = fRepoRemote.createFile(dest, file.getName());
                copyStream(in, out);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                fContext.close();
                System.exit(1);
            }
        }
    }

    public static void main(String[] args) 
    {
        RepoCopyIn me = new RepoCopyIn();
        me.exec(args, flagDefs, 2, USAGE);
    }
}
