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
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.hibernate.HibernateException;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * This is a Runnable which randomly performs operations on an AVM Repository.
 * It's purpose is to act as a single thread in a multithreaded stress tester.
 * @author britt
 */
class AVMTester implements Runnable
{
    // Operation codes.
    private static final int CREATE_FILE = 0;
    private static final int CREATE_DIR = 1;
    private static final int RENAME = 2;
    private static final int CREATE_LAYERED_DIR = 3;
    private static final int CREATE_LAYERED_FILE = 4;
    private static final int REMOVE_NODE = 5;
    private static final int MODIFY_FILE = 6;
    private static final int READ_FILE = 7;
    private static final int SNAPSHOT = 8;
    
    private List<String> fAllPaths;
    private List<String> fAllDirectories;
    private List<String> fAllFiles;
    
    private static boolean fgFrozen = false;
    private static int fgOpCount = 0;
    
    /**
     * The operation table.
     */
    private int [] fOpTable;
    
    /**
     * The number of operations to perform.
     */
    private int fOpCount;
    
    /**
     * The AVMService instance.
     */
    private AVMService fService;
    
    /**
     * The random number generators.
     */
    private static Random fgRandom = new Random();
    
    /**
     * Names for nodes.
     */
    private String[] fNames;
    
    /**
     * Flag for whether this thread errored out.
     */
    private boolean fError;
    
    /**
     * Flag for whether this thread should exit.
     */
    private boolean fExit;
    
    /**
     * Initialize this with the relative frequencies of differents operations.
     * @param createFile
     * @param createDir
     * @param rename
     * @param createLayeredDir
     * @param createLayeredFile
     * @param removeNode
     * @param modifyFile
     * @param readFile
     * @param snapshot
     * @param opCount The number of operations to perform.
     * @param service The instance of AVMService.
     */
    public AVMTester(int createFile,
                     int createDir,
                     int rename,
                     int createLayeredDir,
                     int createLayeredFile,
                     int removeNode,
                     int modifyFile,
                     int readFile,
                     int snapshot,
                     int opCount,
                     AVMService service)
    {
        fError = false;
        fExit = false;
        fService = service;
        fOpCount = opCount;
        int count = createFile + createDir + rename + createLayeredDir +
                    createLayeredFile + removeNode + modifyFile + readFile +
                    snapshot;
        fOpTable = new int[count];
        int off = 0;
        for (int i = 0; i < createFile; i++)
        {
            fOpTable[off + i] = CREATE_FILE;
        }
        off += createFile;
        for (int i = 0; i < createDir; i++)
        {
            fOpTable[off + i] = CREATE_DIR;
        }
        off += createDir;        
        for (int i = 0; i < rename; i++)
        {
            fOpTable[off + i] = RENAME;
        }
        off += rename; 
        for (int i = 0; i < createLayeredDir; i++)
        {
            fOpTable[off + i] = CREATE_LAYERED_DIR;
        }
        off += createLayeredDir;        
        for (int i = 0; i < createLayeredFile; i++)
        {
            fOpTable[off + i] = CREATE_LAYERED_FILE;
        }
        off += createLayeredFile;        
        for (int i = 0; i < removeNode; i++)
        {
            fOpTable[off + i] = REMOVE_NODE;
        }
        off += removeNode;
        for (int i = 0; i < modifyFile; i++)
        {
            fOpTable[off + i] = MODIFY_FILE;
        }
        off += modifyFile;
        for (int i = 0; i < readFile; i++)
        {
            fOpTable[off + i] = READ_FILE;
        }
        off += readFile;
        for (int i = 0; i < snapshot; i++)
        {
            fOpTable[off + i] = SNAPSHOT;
        }
        off += snapshot;
        // Generate a bunch of names.
        String [] letters = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
                              "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
                              "u", "v", "w", "x", "y", "z" };
        fNames = new String[26 * 26];
        for (int i = 0; i < 26; i++)
        {
            for (int j = 0; j < 26; j++)
            {
                fNames[i * 26 + j] = letters[i] + letters[j];
            }
        }
    }
    
    /**
     * It's off.
     */
    public void run()
    {
        try
        {
            long threadID = Thread.currentThread().getId();
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < fOpCount; i++)
            {
                if (fgFrozen)
                {
                    Thread.sleep(3600000);
                }
                if (fExit)
                {
                    return;
                }
                System.out.print(threadID + ":" + i + ":");
                int which = fgRandom.nextInt(fOpTable.length);
                switch (fOpTable[which])
                {
                    case CREATE_FILE :
                        createFile();
                        break;
                    case CREATE_DIR :
                        createDirectory();
                        break;
                    case RENAME :
                        rename();
                        break;
                    case CREATE_LAYERED_DIR :
                        createLayeredDir();
                        break;
                    case CREATE_LAYERED_FILE :
                        createLayeredFile();
                        break;
                    case REMOVE_NODE :
                        removeNode();
                        break;
                    case MODIFY_FILE :
                        modifyFile();
                        break;
                    case READ_FILE :
                        readFile();
                        break;
                    case SNAPSHOT :
                        snapshot();
                        break;
                }   
                IncCount();
            }
            System.out.println(fAllPaths.size() + " fses in " + (System.currentTimeMillis() - startTime) +
                                "ms");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fgFrozen = true;
            fError = true;
        }
    }
    
    private void createFile() 
    {
        String name = "PF" + fNames[fgRandom.nextInt(26 * 26)];
        String path = randomDirectory();
        try
        {
            System.out.println("create " + path + " " + name);
            PrintStream out = new PrintStream(fService.createFile(path, name));
            out.println(path + "/" + name);
            out.close();
            addFile(appendPath(path, name));
        }
        catch (Exception e)
        {
            handleException(e);
        }
    }
    
    private void createDirectory()
    {
        String name = "PD" + fNames[fgRandom.nextInt(26 * 26)];
        String path = randomDirectory();
        try
        {
            System.out.println("mkdir " + path + " " + name);
            fService.createDirectory(path, name);
            addDirectory(appendPath(path, name));
        }
        catch (Exception e)
        {
            handleException(e);
        }
    }
    
    private void rename()
    {
        String name = fNames[fgRandom.nextInt(26 * 26)];
        String path = randomPath();
        AVMNodeDescriptor desc = fService.lookup(-1, path);
        if (desc == null)
        {
            return;
        }
        if (path.equals("main:/"))
        {
            return;
        }
        int lastSlash = path.lastIndexOf('/');
        String srcPath = path.substring(0, lastSlash);
        if (srcPath.equals("main:"))
        {
            srcPath = srcPath + "/";
        }
        String srcName = path.substring(lastSlash + 1);
        String dstPath = randomDirectory();
        try
        {
            System.out.println("rename " + srcPath + " " + srcName + " " + dstPath + " " + name);
            fService.rename(srcPath, srcName, dstPath, name);
            removePath(path);
            if (desc.isDirectory())
            {
                addDirectory(appendPath(dstPath, name));
            }
            else
            {
                addFile(appendPath(dstPath, name));
            }
        }
        catch (Exception e)
        {
            handleException(e);
        }
    }      
    
    private void createLayeredDir() 
    {
        String name = "LD" + fNames[fgRandom.nextInt(26 * 26)];
        String path = randomDirectory();
        String target = randomDirectory();
        try
        {
            System.out.println("mklayereddir " + path + " " + name + " " + target);
            fService.createLayeredDirectory(target, path, name);
            addDirectory(appendPath(path, name));
        }
        catch (Exception e)
        {
            handleException(e);
        }
    }
    
    private void createLayeredFile() 
    {
        String name = "LF" + fNames[fgRandom.nextInt(26 * 26)];
        String path = randomDirectory();
        String target = randomFile();
        try
        {
            System.out.println("createlayered " + path + " " + name + " " + target);
            fService.createLayeredFile(target, path, name);
            addFile(appendPath(path, name));
        }
        catch (Exception e)
        {
            handleException(e);
        }
    }
    
    private void removeNode()
    {
        String target = randomPath();
        int lastSlash = target.lastIndexOf('/');
        String path = target.substring(0, lastSlash);
        if (path.equals("main:"))
        {
            path = path + "/";
        }
        String name = target.substring(lastSlash + 1);
        try
        {
            System.out.println("remove " + target);
            fService.removeNode(path, name);
            removePath(target);
        }
        catch (Exception e)
        {
            handleException(e);
        }
    }
    
    private void modifyFile()
    {
        String path = randomFile();
        try
        {
            System.out.println("modify " + path);
            PrintStream out = 
                new PrintStream(fService.getFileOutputStream(path));
            out.println("I am " + path);
            out.close();
        }
        catch (Exception e)
        {
            handleException(e);
        }
    }
    
    private void readFile()
    {
        String path = randomFile();
        try
        {
            System.out.println("read " + path);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, path)));
            String line = reader.readLine();
            System.out.println(line);
            reader.close();
        }
        catch (Exception e)
        {
            handleException(e);
        }
    }       
    
    public void refresh()
    {
        System.out.println("refresh");
        fAllPaths = new ArrayList<String>();
        fAllDirectories = new ArrayList<String>();
        fAllFiles = new ArrayList<String>();
        fAllPaths.add("main:/");
        fAllDirectories.add("main:/");
        Set<Long> visited = new HashSet<Long>();
        AVMNodeDescriptor root = fService.getAVMStoreRoot(-1, "main");
        recursiveRefresh(root, visited);
    }

    private void recursiveRefresh(AVMNodeDescriptor dir, Set<Long> visited)
    {
        try
        {
            String baseName = dir.getPath().endsWith("/") ? dir.getPath() : dir.getPath() + "/";
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(dir);
            for (String name : listing.keySet())
            {
                String path = baseName + name;
                AVMNodeDescriptor desc = listing.get(name);
                switch (desc.getType())
                {
                    case AVMNodeType.LAYERED_DIRECTORY :
                    case AVMNodeType.PLAIN_DIRECTORY :
                    {
                        if (visited.contains(desc.getId()))
                        {
                            continue;
                        }
                        visited.add(desc.getId());
                        fAllPaths.add(path);
                        fAllDirectories.add(path);
                        recursiveRefresh(desc, visited);
                        break;
                    }
                    case AVMNodeType.LAYERED_FILE :
                    case AVMNodeType.PLAIN_FILE :
                    {
                        fAllPaths.add(path);
                        fAllFiles.add(path);
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            handleException(e);
        }
    }

    private void snapshot() 
    {
        System.out.println("snapshot");
        try
        {
            fService.createSnapshot("main", null, null);
        }
        catch (Exception e)
        {
            handleException(e);
        }
    }
    
    public boolean getError()
    {
        return fError;
    }
    
    public void setExit()
    {
        fExit = true;
    }
    
    private void addDirectory(String path)
    {
        fAllDirectories.add(path);
        fAllPaths.add(path);
    }
    
    private void addFile(String path)
    {
        fAllFiles.add(path);
        fAllPaths.add(path);
    }
    
    private void removePath(String path)
    {
        List<String> allPaths = new ArrayList<String>();
        List<String> allDirectories = new ArrayList<String>();
        List<String> allFiles = new ArrayList<String>();
        for (String p : fAllPaths)
        {
            if (p.indexOf(path) != 0)
            {
                allPaths.add(p);
            }
        }
        for (String p : fAllDirectories)
        {
            if (p.indexOf(path) != 0)
            {
                allDirectories.add(p);
            }
        }
        for (String p : fAllFiles)
        {
            if (p.indexOf(path) != 0)
            {
                allFiles.add(p);
            }
        }
        fAllPaths = allPaths;
        fAllDirectories = allDirectories;
        fAllFiles = allFiles;
    }
    
    private String appendPath(String path, String name)
    {
        return path.endsWith("/") ? path + name : path + "/" + name;
    }
    
    private String randomDirectory()
    {
        return fAllDirectories.get(fgRandom.nextInt(fAllDirectories.size()));
    }

    private String randomFile()
    {
        return fAllFiles.get(fgRandom.nextInt(fAllFiles.size()));
    }

    private String randomPath()
    {
        return fAllPaths.get(fgRandom.nextInt(fAllPaths.size()));
    }
    
    private static synchronized void IncCount()
    {
        ++fgOpCount;
    }
    
    public static synchronized int GetCount()
    {
        return fgOpCount;
    }
    
    private void handleException(Exception e)
    {
        e.printStackTrace(System.err);
        if (e instanceof AVMException ||
            e instanceof AlfrescoRuntimeException ||
            e instanceof ConcurrencyFailureException ||
            e instanceof HibernateException ||
            e instanceof InvalidNodeRefException)
        {
            return;
        }
        throw new AVMException("Naughty Exception.", e);
    }
}
