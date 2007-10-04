/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.avm.util.BulkLoader;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.namespace.QName;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * An interactive console for the AVM repository.
 * @author britt
 */
public class AVMInterpreter
{
    /**
     * The service interface.
     */
    private AVMService fService;

    /**
     * The sync service.
     */
    private AVMSyncService fSyncService;

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
        FileSystemXmlApplicationContext context =
            new FileSystemXmlApplicationContext("config/alfresco/application-context.xml");
        AVMInterpreter console = new AVMInterpreter();
        console.setAvmService((AVMService)context.getBean("AVMService"));
        console.setAvmSyncService((AVMSyncService)context.getBean("AVMSyncService"));
        BulkLoader loader = new BulkLoader();
        loader.setAvmService((AVMService)context.getBean("AVMService"));
        console.setBulkLoader(loader);
        console.rep();
        context.close();
    }

    /**
     * Make up a new console.
     */
    public AVMInterpreter()
    {
        fIn = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * Set the AVMService.
     * @param service The AVMService instance.
     */
    public void setAvmService(AVMService service)
    {
        fService = service;
    }

    /**
     * Set the AVM sync service.
     * @param syncService
     */
    public void setAvmSyncService(AVMSyncService syncService)
    {
        fSyncService = syncService;
    }

    /**
     * Set the bulk loader.
     * @param loader
     */
    public void setBulkLoader(BulkLoader loader)
    {
        fLoader = loader;
    }

    /**
     * A Read-Eval-Print loop.
     */
    public void rep()
    {
        while (true)
        {
            System.out.print("> ");
            try
            {
                String line = fIn.readLine();
                if (line.equals("exit"))
                {
                    return;
                }
                System.out.println(interpretCommand(line, fIn));
            }
            catch (IOException ie)
            {
                ie.printStackTrace(System.err);
                System.exit(2);
            }
        }
    }

    /**
     * Interpret a single command using the BufferedReader passed in for any data needed.
     * @param line The unparsed command
     * @param in A Reader to be used for commands that need input data.
     * @return The textual output of the command.
     */
    public String interpretCommand(String line, BufferedReader in)
    {
        String[] command = line.split(",\\s+");
        if (command.length == 0)
        {
            command = new String[1];
            command[0] = line;
        }
        try
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(bout);
            if (command[0].equals("ls"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                AVMNodeDescriptor desc = fService.lookup(Integer.parseInt(command[2]),
                                                         command[1]);
                if (desc == null)
                {
                    return "Not Found.";
                }
                Map<String, AVMNodeDescriptor> listing =
                    fService.getDirectoryListing(desc, true);
                for (String name : listing.keySet())
                {
                    out.println(name + " " + listing.get(name));
                }
            }
            else if (command[0].equals("lsr"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                AVMNodeDescriptor desc = fService.lookup(Integer.parseInt(command[2]),
                                                         command[1]);
                recursiveList(out, desc, 0);
            }
            else if (command[0].equals("lsrep"))
            {
                List<AVMStoreDescriptor> repos = fService.getStores();
                for (AVMStoreDescriptor repo : repos)
                {
                    out.println(repo);
                }
            }
            else if (command[0].equals("lsver"))
            {
                if (command.length != 2)
                {
                    return "Syntax Error.";
                }
                List<VersionDescriptor> listing = fService.getStoreVersions(command[1]);
                for (VersionDescriptor desc : listing)
                {
                    out.println(desc);
                }
            }
            else if (command[0].equals("mkrep"))
            {
                if (command.length != 2)
                {
                    return "Syntax Error.";
                }
                fService.createStore(command[1]);
            }
            else if (command[0].equals("load"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                fLoader.recursiveLoad(command[1], command[2]);
            }
            else if (command[0].equals("mkdir"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                fService.createDirectory(command[1], command[2]);
            }
            else if (command[0].equals("mkbr"))
            {
                if (command.length != 5)
                {
                    return "Syntax Error.";
                }
                fService.createBranch(Integer.parseInt(command[4]), command[1], command[2], command[3]);
            }
            else if (command[0].equals("mkldir"))
            {
                if (command.length != 4)
                {
                    return "Syntax Error.";
                }
                fService.createLayeredDirectory(command[1], command[2], command[3]);
            }
            else if (command[0].equals("rename"))
            {
                if (command.length != 5)
                {
                    return "Syntax Error.";
                }
                fService.rename(command[1], command[2], command[3], command[4]);
            }
            else if (command[0].equals("cp"))
            {
                if (command.length != 5)
                {
                    return "Syntax Error.";
                }
                InputStream fin = fService.getFileInputStream(Integer.parseInt(command[2]), command[1]);
                OutputStream fout = fService.createFile(command[3], command[4]);
                byte [] buff = new byte[8192];
                int read;
                while ((read = fin.read(buff)) != -1)
                {
                    fout.write(buff, 0, read);
                }
                fin.close();
                fout.close();
            }
            else if (command[0].equals("retarget"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                fService.retargetLayeredDirectory(command[1], command[2]);
            }
            else if (command[0].equals("mkprimary"))
            {
                if (command.length != 2)
                {
                    return "Syntax Error.";
                }
                fService.makePrimary(command[1]);
            }
            else if (command[0].equals("mklfile"))
            {
                if (command.length != 4)
                {
                    return "Syntax Error.";
                }
                fService.createLayeredFile(command[1], command[2], command[3]);
            }
            else if (command[0].equals("snap"))
            {
                if (command.length != 2)
                {
                    return "Syntax Error.";
                }
                fService.createSnapshot(command[1], null, null);
            }
            else if (command[0].equals("cat"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                BufferedReader reader =
                    new BufferedReader(
                        new InputStreamReader(fService.getFileInputStream(Integer.parseInt(command[2]),
                                                                          command[1])));
                String l;
                while ((l = reader.readLine()) != null)
                {
                    out.println(l);
                }
                reader.close();
            }
            else if (command[0].equals("rm"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                fService.removeNode(command[1], command[2]);
            }
            else if (command[0].equals("rmrep"))
            {
                if (command.length != 2)
                {
                    return "Syntax Error.";
                }
                fService.purgeStore(command[1]);
            }
            else if (command[0].equals("rmver"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                fService.purgeVersion(Integer.parseInt(command[2]), command[1]);
            }
            else if (command[0].equals("write"))
            {
                if (command.length != 2)
                {
                    return "Syntax Error.";
                }
                PrintStream ps =
                    new PrintStream(fService.getFileOutputStream(command[1]));
                String l;
                while (!(l = in.readLine()).equals(""))
                {
                    ps.println(l);
                }
                ps.close();
            }
            else if (command[0].equals("create"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                PrintStream ps =
                    new PrintStream(fService.createFile(command[1], command[2]));
                String l;
                while (!(l = in.readLine()).equals(""))
                {
                    ps.println(l);
                }
                ps.close();
            }
            else if (command[0].equals("stat"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                AVMNodeDescriptor desc = fService.lookup(Integer.parseInt(command[2]), command[1]);
                out.println(desc);
                out.println("Version: " + desc.getVersionID());
                out.println("Owner: " + desc.getOwner());
                out.println("Mod Time: " + new Date(desc.getModDate()));
            }
            else if (command[0].equals("getnodeproperties"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                final Map<QName, PropertyValue> properties = fService.getNodeProperties(Integer.parseInt(command[2]), command[1]);
                for (final Map.Entry<QName, PropertyValue> p : properties.entrySet())
                {
                   out.println(p.getKey() + ": " + p.getValue());
                }
            }
            else if (command[0].equals("deletenodeproperty"))
            {
               if (command.length != 3)
               {
                  return "Syntax Error.";
               }

               fService.deleteNodeProperty(command[1], QName.createQName(command[2]));
               out.println("deleted property " + command[2] + " of " + command[1]);
            }
            else if (command[0].equals("history"))
            {
                if (command.length != 4)
                {
                    return "Syntax Error.";
                }
                AVMNodeDescriptor desc = fService.lookup(Integer.parseInt(command[2]), command[1]);
                List<AVMNodeDescriptor> history = fService.getHistory(desc, Integer.parseInt(command[3]));
                for (AVMNodeDescriptor node : history)
                {
                    out.println(node);
                    out.println("Version: " + desc.getVersionID());
                    out.println("Owner: " + desc.getOwner());
                    out.println("Mod Time: " + new Date(desc.getModDate()));
                }
            }
            /*
            else if (command[0].equals("catver"))
            {
                if (command.length != 4)
                {
                    return "Syntax Error.";
                }
                AVMNodeDescriptor desc = fService.lookup(Integer.parseInt(command[2]), command[1]);
                List<AVMNodeDescriptor> history = fService.getHistory(desc, Integer.parseInt(command[3]));
                if (history.size() == 0)
                {
                    return "No History.";
                }
                BufferedReader reader =
                    new BufferedReader(
                        new InputStreamReader(
                            fService.getFileInputStream(history.get(history.size() - 1))));
                String l;
                while ((l = reader.readLine()) != null)
                {
                    out.println(l);
                }
                reader.close();
            }
            */
            else if (command[0].equals("ca"))
            {
                if (command.length != 5)
                {
                    return "Syntax Error.";
                }
                AVMNodeDescriptor left = fService.lookup(Integer.parseInt(command[2]), command[1]);
                AVMNodeDescriptor right = fService.lookup(Integer.parseInt(command[4]), command[3]);
                AVMNodeDescriptor ca = fService.getCommonAncestor(left, right);
                out.println(ca);
            }
            else if (command[0].equals("statstore"))
            {
                if (command.length != 2)
                {
                    return "Syntax Error.";
                }
                AVMStoreDescriptor desc = fService.getStore(command[1]);
                if (desc == null)
                {
                    return "Not Found.";
                }
                out.println(desc);
                Map<QName, PropertyValue> props =
                    fService.getStoreProperties(command[1]);
                for (QName name : props.keySet())
                {
                    out.println(name + ": " + props.get(name));
                }
            }
            else if (command[0].equals("compare"))
            {
                if (command.length != 5)
                {
                    return "Syntax Error.";
                }
                List<AVMDifference> diffs = fSyncService.compare(Integer.parseInt(command[2]),
                                                                 command[1],
                                                                 Integer.parseInt(command[4]),
                                                                 command[3],
                                                                 null);
                for (AVMDifference diff : diffs)
                {
                    out.println(diff);
                }
            }
            else if (command[0].equals("update"))
            {
                if (command.length != 4)
                {
                    return "Syntax Error.";
                }
                AVMDifference diff = new AVMDifference(Integer.parseInt(command[2]), command[1],
                                                       -1, command[3], AVMDifference.NEWER);
                List<AVMDifference> diffs = new ArrayList<AVMDifference>();
                diffs.add(diff);
                fSyncService.update(diffs, null, false, false, false, false, null, null);
            }
            else if (command[0].equals("resetLayer"))
            {
                if (command.length != 2)
                {
                    return "Syntax Error.";
                }
                fSyncService.resetLayer(command[1]);
            }
            else if (command[0].equals("flatten"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                fSyncService.flatten(command[1], command[2]);
            }
            else
            {
                return "Syntax Error.";
            }
            out.flush();
            String retVal = new String(bout.toByteArray());
            out.close();
            return retVal;
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            return e.toString();
        }
    }

    private void recursiveList(PrintStream out, AVMNodeDescriptor dir, int indent)
    {
        Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(dir, true);
        for (String name : listing.keySet())
        {
            AVMNodeDescriptor child = listing.get(name);
            for (int i = 0; i < indent; i++)
            {
                out.print(' ');
            }
            out.println(name + " " + child);
            if (child.isDirectory())
            {
                recursiveList(out, child, indent + 2);
            }
        }
    }
}




