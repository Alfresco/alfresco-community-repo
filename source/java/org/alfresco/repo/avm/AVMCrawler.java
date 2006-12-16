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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.repository.ContentIOException;


/**
 * This is another tester designed to emulate more typical use patterns.
 * @author britt
 */
class AVMCrawler implements Runnable
{
    /**
     * The AVMService to use.
     */
    private AVMService fService;

    /**
     * The Operation count.
     */
    private int fOpCount;

    /**
     * Whether we are done.
     */
    private boolean fDone;

    /**
     * Whether an error has occurred.
     */
    private boolean fError;

    /**
     * Random number generator.
     */
    private Random fRandom;

    /**
     * Make up a new one.
     * @param service The AVMService.
     */
    public AVMCrawler(AVMService service)
    {
        fService = service;
        fOpCount = 0;
        fDone = false;
        fError = false;
        fRandom = new Random();
    }

    /**
     * Tell this thread it is done.
     */
    public void setDone()
    {
        fDone = true;
    }

    /**
     * Is this thread in an error state.
     */
    public boolean getError()
    {
        return fError;
    }
    
    /**
     * Implementation of run.
     */
    public void run()
    {
        try
        {
            while (!fDone)
            {
                doCrawl();
            }
        }
        catch (Exception e)
        {
            fError = true;
        }
    }

    /**
     * Do one crawl.
     */
    public void doCrawl()
    {
        try
        {
            List<AVMStoreDescriptor> reps = fService.getStores();
            fOpCount++;
            AVMStoreDescriptor repDesc = reps.get(fRandom.nextInt(reps.size()));
            Map<String, AVMNodeDescriptor> rootListing = fService.getDirectoryListing(-1, repDesc.getName() + ":/");
            fOpCount++;
            // Get all the directories in the root.
            List<AVMNodeDescriptor> dirs = new ArrayList<AVMNodeDescriptor>();
            for (AVMNodeDescriptor desc : rootListing.values())
            {
                if (desc.isDirectory())
                {
                    dirs.add(desc);
                }
            }
            AVMNodeDescriptor dir = dirs.get(fRandom.nextInt(dirs.size()));
            int depth = 1;
            while (dir != null)
            {
                Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, dir.getPath());
                fOpCount++;
                List<AVMNodeDescriptor> files = new ArrayList<AVMNodeDescriptor>();
                dirs = new ArrayList<AVMNodeDescriptor>();
                for (AVMNodeDescriptor desc : listing.values())
                {
                    if (desc.isDirectory())
                    {
                        dirs.add(desc);
                    }
                    else
                    {
                        files.add(desc);
                    }
                }
                // Read some files if there are any.
                if (files.size() > 0)
                {
                    for (int i = 0; i < 6; i++)
                    {
                        BufferedReader
                            reader = new BufferedReader
                            (new InputStreamReader
                             (fService.getFileInputStream(-1, files.get(fRandom.nextInt(files.size())).getPath())));
                        fOpCount++;
                        String line = reader.readLine();
                        System.out.println(line);
                        reader.close();
                    }
                    // Modify some files.
                    for (int i = 0; i < 2; i++)
                    {
                        String path = files.get(fRandom.nextInt(files.size())).getPath();
                        PrintStream out = new PrintStream(fService.getFileOutputStream(path));
                        out.println("I am " + path);
                        out.close();
                        fOpCount++;
                    }
                }
                if (fRandom.nextInt(depth) < depth - 1)
                {
                    // Create some files.
                    for (int i = 0; i < 1; i++)
                    {
                        String name = randomName();
                        fService.createFile(dir.getPath(), name, 
                            new ByteArrayInputStream(("I am " + name).getBytes()));
                        fOpCount++;
                    }
                }
                // 1 in 100 times create a directory.
                if (fRandom.nextInt(100) == 0)
                {
                    String name = randomName();
                    fService.createDirectory(dir.getPath(), name);
                    fOpCount++;
                }
                if (listing.size() > 0)
                {
                    // 1 in 100 times remove something
                    if (fRandom.nextInt(100) == 0)
                    {
                        List<String> names = new ArrayList<String>(listing.keySet());
                        fService.removeNode(dir.getPath(), 
                                            names.get(fRandom.nextInt(names.size())));
                        fOpCount++;
                    }
                }
                if (dirs.size() > 0)
                {
                    dir = dirs.get(fRandom.nextInt(dirs.size()));
                }
                else
                {
                    dir = null;
                }
                depth++;
            }
            if (fRandom.nextInt(16) == 0)
            {
                fService.createSnapshot(repDesc.getName(), null, null);
                fOpCount++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            if (e instanceof AVMException)
            {
                return;
            }
            if (e instanceof ContentIOException)
            {
                return;
            }
            throw new AVMException("Failure", e);
        }
    }
    
    /**
     * Get a random two character string.
     * @return A random name.
     */
    private String randomName()
    {
        char [] chars = new char[2];
        chars[0] = (char)('a' + fRandom.nextInt(26));
        chars[1] = (char)('a' + fRandom.nextInt(26));
        return new String(chars);
    }
    
    /**
     * Get the operation count.
     */
    public int getOpCount()
    {
        return fOpCount;
    }
}

                
