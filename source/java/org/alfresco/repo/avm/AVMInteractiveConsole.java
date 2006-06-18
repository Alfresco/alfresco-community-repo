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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.avm.util.BulkLoader;

/**
 * An interactive console for the AVM repository.
 * @author britt
 */
public class AVMInteractiveConsole
{
    /**
     * The service interface.
     */
    private AVMService fService;

    /**
     * The Orphan Cleaner Upper.
     */
    private OrphanReaper fReaper;

    /**
     * The reader for interaction.
     */
    private BufferedReader fIn;

    /**
     * The Bulk Loader.
     */
    private BulkLoader fLoader;

    /**
     * Main entry point.  
     * Syntax: AVMInteractiveConsole storage (new|old).
     */
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("Usage: AVMInteractiveConsole storage (new|old)");
            System.exit(1);
        }
        AVMInteractiveConsole console = new AVMInteractiveConsole(args[0], args[1].equals("new"));
        console.rep();
    }

    /**
     * Make up a new console.
     * @param storage Where The backing store goes.
     * @param createNew Whether to create a new SuperRepository.
     */
    public AVMInteractiveConsole(String storage, boolean createNew)
    {
        AVMServiceImpl service = new AVMServiceImpl();
        service.setStorage(storage);
        service.init(createNew);
        fService = service;
        fReaper = new OrphanReaper();
        fReaper.init();
        fLoader = new BulkLoader(fService);
        fIn = new BufferedReader(new InputStreamReader(System.in));
    }
    
    /**
     * A Read-Eval-Print loop.
     */
    public void rep()
    {
        boolean done = false;
        while (!done)
        {
            String command[] = null;
            try
            {
                String line = fIn.readLine();
                command = line.split("\\s+");
                if (command.length == 0)
                {
                    command = new String[1];
                    command[0] = line;
                }
            }
            catch (IOException ie)
            {
                ie.printStackTrace(System.err);
                System.exit(2);
            }
            if (command.length < 1)
            {
                continue;
            }
            try
            {
                if (command[0].equals("ls"))
                {
                    if (command.length != 3)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    AVMNodeDescriptor desc = fService.lookup(Integer.parseInt(command[2]),
                                                             command[1]);
                    Map<String, AVMNodeDescriptor> listing =
                        fService.getDirectoryListing(desc);
                    for (String name : listing.keySet())
                    {
                        System.out.println(name + " " + listing.get(name));
                    }
                }
                else if (command[0].equals("lsrep"))
                {
                    List<String> repos = fService.getRepositoryNames();
                    for (String name : repos)
                    {
                        System.out.println(name);
                    }
                }
                else if (command[0].equals("lsver"))
                {
                    if (command.length != 2)
                    {
                        System.err.println("Syntax Error.");
                        continue;
                    }
                    List<VersionDescriptor> listing = fService.getRepositoryVersions(command[1]);
                    for (VersionDescriptor desc : listing)
                    {
                        System.out.println(desc);
                    }
                }
                else if (command[0].equals("mkrep"))
                {
                    if (command.length != 2)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    fService.createRepository(command[1]);
                }
                else if (command[0].equals("load"))
                {
                    if (command.length != 3)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    fLoader.recursiveLoad(command[1], command[2]);
                }
                else if (command[0].equals("mkdir"))
                {
                    if (command.length != 3)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    fService.createDirectory(command[1], command[2]);
                }
                else if (command[0].equals("mkldir"))
                {
                    if (command.length != 4)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    fService.createLayeredDirectory(command[1], command[2], command[3]);
                }
                else if (command[0].equals("mklfile"))
                {
                    if (command.length != 4)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    fService.createLayeredFile(command[1], command[2], command[3]);
                }
                else if (command[0].equals("snap"))
                {
                    if (command.length != 2)
                    {
                        System.err.println("Syntax Error");
                        continue;
                    }
                    fService.createSnapshot(command[1]);
                }
                else if (command[0].equals("cat"))
                {
                    if (command.length != 3)
                    {
                        System.err.println("Syntax Error");
                        continue;
                    }
                    BufferedReader reader = 
                        new BufferedReader(
                            new InputStreamReader(fService.getFileInputStream(Integer.parseInt(command[2]), 
                                                                              command[1])));
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        System.out.println(line);
                    }
                    reader.close();
                }
                else if (command[0].equals("rm"))
                {
                    if (command.length != 3)
                    {
                        System.err.println("Syntax Error.");
                        continue;
                    }
                    fService.removeNode(command[1], command[2]);
                }
                else if (command[0].equals("rmrep"))
                {
                    if (command.length != 2)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    fService.purgeRepository(command[1]);
                }
                else if (command[0].equals("rmver"))
                {
                    if (command.length != 3)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    fService.purgeVersion(Integer.parseInt(command[2]), command[1]);
                }
                else if (command[0].equals("write"))
                {
                    if (command.length != 2)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    PrintStream out = 
                        new PrintStream(fService.getFileOutputStream(command[1]));
                    String line;
                    while (!(line = fIn.readLine()).equals(""))
                    {
                        out.println(line);
                    }
                    out.close();
                }
                else if (command[0].equals("create"))
                {
                    if (command.length != 3)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    PrintStream out = 
                        new PrintStream(fService.createFile(command[1], command[2]));
                    String line;
                    while (!(line = fIn.readLine()).equals(""))
                    {
                        out.println(line);
                    }
                    out.close();
                }
                else if (command[0].equals("stat"))
                {
                    if (command.length != 3)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    AVMNodeDescriptor desc = fService.lookup(Integer.parseInt(command[2]), command[1]);
                    System.out.println(desc);
                    System.out.println("Version: " + desc.getVersionID());
                    System.out.println("Owner: " + desc.getOwner());
                    System.out.println("Mod Time: " + new Date(desc.getModDate()));
                }
                else if (command[0].equals("history"))
                {
                    if (command.length != 4)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    AVMNodeDescriptor desc = fService.lookup(Integer.parseInt(command[2]), command[1]);
                    List<AVMNodeDescriptor> history = fService.getHistory(desc, Integer.parseInt(command[3]));
                    for (AVMNodeDescriptor node : history)
                    {
                        System.out.println(node);
                        System.out.println("Version: " + desc.getVersionID());
                        System.out.println("Owner: " + desc.getOwner());
                        System.out.println("Mod Time: " + new Date(desc.getModDate()));
                    }
                }
                else if (command[0].equals("catver"))
                {
                    if (command.length != 4)
                    {
                        System.err.println("Syntax error.");
                        continue;
                    }
                    AVMNodeDescriptor desc = fService.lookup(Integer.parseInt(command[2]), command[1]);
                    List<AVMNodeDescriptor> history = fService.getHistory(desc, Integer.parseInt(command[3]));
                    if (history.size() == 0)
                    {
                        System.err.println("No history found.");
                        continue;
                    }
                    BufferedReader reader =
                        new BufferedReader(
                            new InputStreamReader(
                                fService.getFileInputStream(history.get(history.size() - 1))));
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        System.out.println(line);
                    }
                    reader.close();
                }
                else if (command[0].equals("exit"))
                {
                    done = true;
                }
                else
                {
                    System.err.println("Syntax error.");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
            }
        }
        fReaper.shutDown();
    }
}
                    


                
