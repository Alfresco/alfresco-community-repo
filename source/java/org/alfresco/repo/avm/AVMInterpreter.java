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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.repo.avm.util.BulkLoader;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.wcm.util.WCMUtil;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * An interactive console for the AVM repository.
 * 
 * @author britt
 * @author Gavin Cornwell
 * @author janv
 */
public class AVMInterpreter
{
    private static final Pattern collectionPattern = Pattern.compile("^\\[(.*)\\]$");
    private static final Pattern nodeRefPattern = Pattern.compile("^\\w+://\\w+\\w+$");
    private static final Pattern integerPattern = Pattern.compile("^\\d+$");
    private static final Pattern dateTimePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");

    /**
     * The service interface.
     */
    private AVMService fService;

    /**
     * The sync service.
     */
    private AVMSyncService fSyncService;

    /**
     * The locking service.
     */
    private AVMLockingService fLockingService;

    /**
     * The permission service.
     */
    private PermissionService fPermissionService;

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
        console.setAvmLockingService((AVMLockingService)context.getBean("AVMLockingService"));
        console.setPermissionService((PermissionService)context.getBean("PermissionService"));
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
     * Set the AVM locking service.
     * @param lockService
     */
    public void setAvmLockingService(AVMLockingService lockService)
    {
        fLockingService = lockService;
    }
    
    /**
     * Set the PermissionService.
     * @param service The PermissionService instance.
     */
    public void setPermissionService(PermissionService service)
    {
        fPermissionService = service;
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
                if (command.length < 2)
                {
                    return "Syntax Error.";
                }
                int version = (command.length == 2) ? -1 : Integer.parseInt(command[2]);
                AVMNodeDescriptor desc = fService.lookup(version, command[1]);
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
                if (command.length < 2)
                {
                    return "Syntax Error.";
                }
                int version = (command.length == 2) ? -1 : Integer.parseInt(command[2]);
                AVMNodeDescriptor desc = fService.lookup(version, command[1]);
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
                if ((command.length < 2) || (command.length > 4))
                {
                    return "Syntax Error.";
                }
                
                List<VersionDescriptor> listing = null;
                String storeName = command[1];
                if (command.length == 2)
                {
                    listing = fService.getStoreVersions(storeName);
                }
                else
                {
                    Date fromDate = ISO8601DateFormat.parse(command[2]);
                    Date toDate = new Date();
                    if (command.length == 4)
                    {
                        toDate = ISO8601DateFormat.parse(command[3]);
                    }
                    listing = fService.getStoreVersions(storeName, fromDate, toDate);
                }
                
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
            else if (command[0].equals("setopacity"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                boolean isOpaque = new Boolean(command[2]);
                fService.setOpacity(command[1], isOpaque);
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
                if ((command.length < 2) || (command.length > 4))
                {
                    return "Syntax Error.";
                }
                
                String tag = (command.length > 2) ? command[2] : null;
                String description = (command.length > 3) ? command[3] : null;
                
                fService.createSnapshot(command[1], tag, description);
            }
            else if (command[0].equals("cat"))
            {
                if (command.length < 2)
                {
                    return "Syntax Error.";
                }
                int version = (command.length == 2) ? -1 : Integer.parseInt(command[2]);
                BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(fService.getFileInputStream(version, command[1])));
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
                
                String storeName = command[1];
                int ver =Integer.parseInt(command[2]);
                String wpStoreId = WCMUtil.getWebProject(fService, storeName);
                if ((wpStoreId != null) && (ver <= 2))
                {
                    return "WCM store - cannot delete versions 0-2";
                }
                fService.purgeVersion(ver, storeName);
            }
            else if (command[0].equals("rmvers"))
            {
                if (command.length != 4)
                {
                    return "Syntax Error.";
                }
                String storeName = command[1];
                String wpStoreId = WCMUtil.getWebProject(fService, storeName);
                
                Date fromDate = ISO8601DateFormat.parse(command[2]);
                Date toDate = ISO8601DateFormat.parse(command[3]);
                
                List<VersionDescriptor> listing = fService.getStoreVersions(storeName, fromDate, toDate);
                for (VersionDescriptor desc : listing)
                {
                    int ver = desc.getVersionID();
                    if ((wpStoreId != null) && (ver <= 2))
                    {
                        return "WCM store - cannot delete versions 0-2";
                    }
                    fService.purgeVersion(ver, storeName);
                }
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
                if (command.length < 2)
                {
                    return "Syntax Error.";
                }
                int version = (command.length == 2) ? -1 : Integer.parseInt(command[2]);
                AVMNodeDescriptor desc = fService.lookup(version, command[1]);
                out.println(desc);
                out.println("Version: " + desc.getVersionID());
                out.println("Owner: " + desc.getOwner());
                out.println("Mod Time: " + new Date(desc.getModDate()));
            }
            else if (command[0].equals("getnodeproperties"))
            {
                if (command.length < 2)
                {
                    return "Syntax Error.";
                }
                int version = (command.length == 2) ? -1 : Integer.parseInt(command[2]);
                final Map<QName, PropertyValue> properties = fService.getNodeProperties(version, command[1]);
                for (final Map.Entry<QName, PropertyValue> p : properties.entrySet())
                {
                    out.println(p.getKey() + ": " + p.getValue());
                }
            }
            else if (command[0].equals("setnodepermission"))
            {
                if (command.length != 4)
                {
                    return "Syntax Error.";
                }
                
                fPermissionService.setPermission(
                        AVMNodeConverter.ToNodeRef(-1, command[1]), command[2], command[3], true);
            }
            else if (command[0].equals("clearnodepermission"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                
                fPermissionService.clearPermission(
                        AVMNodeConverter.ToNodeRef(-1, command[1]), command[2]);
            }
            else if (command[0].equals("descnode"))
            {
                if (command.length < 2)
                {
                    return "Syntax Error.";
                }

                String path = command[1];
                int version = (command.length == 2) ? -1 : Integer.parseInt(command[2]);
                AVMNodeDescriptor nodeDesc = fService.lookup(version, path);
                if (nodeDesc == null)
                {
                    return "Path Not Found.";
                }

                out.println(nodeDesc.toString());
                out.println("isDirectory: " + nodeDesc.isDirectory());
                out.println("isFile: " + nodeDesc.isFile());
                out.println("isPrimary: " + nodeDesc.isPrimary());
                out.println("isOpaque: " + nodeDesc.getOpacity());
                out.println("creator: " + nodeDesc.getCreator());
                out.println("owner: " + nodeDesc.getOwner());
                out.println("lastModifier: " + nodeDesc.getLastModifier());
                out.println("created: " + new Date(nodeDesc.getCreateDate()));
                out.println("modified: " + new Date(nodeDesc.getModDate()));
                out.println("lastAccess: " + new Date(nodeDesc.getAccessDate()));

                // get lock information
                String lockPath = path.substring(path.indexOf("/"));
                String store = path.substring(0, path.indexOf(":"));
                String mainStore = store;
                if (store.indexOf("--") != -1)
                {
                    mainStore = store.substring(0, store.indexOf("--"));
                }

                try
                {
                    String lockOwner = fLockingService.getLockOwner(mainStore, lockPath);
                    if (lockOwner != null)
                    {
                        out.println("lock: " + lockOwner);
                    }
                    else
                    {
                        out.println("No locks found");
                    }
                }
                catch (AVMNotFoundException avmerr)
                {
                    out.println("No locks found");
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
                    out.println("Version: " + node.getVersionID());
                    out.println("Owner: " + node.getOwner());
                    out.println("Mod Time: " + new Date(node.getModDate()));
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
            else if (command[0].equals("getnodeaspects"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }

                final Set<QName> aspects = fService.getAspects(Integer.parseInt(command[2]), command[1]);
                for (final QName qn : aspects)
                {
                    out.println(qn.toString());
                }
            }
            else if (command[0].equals("addnodeaspect"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }

                fService.addAspect(command[1], QName.createQName(command[2]));
            }
            else if (command[0].equals("deletenodeaspect"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }

                fService.removeAspect(command[1], QName.createQName(command[2]));
            }
            else if (command[0].equals("setnodeproperty"))
            {
                if (command.length < 4)
                {
                    return "Syntax Error.";
                }

                QName valueQName = QName.createQName(command[2]);

                String propertyValue = "";

                // If multiple values are specified then concatenate the values
                if (command.length > 4)
                {
                    StringBuffer sb = new StringBuffer();
                    for (int i=3; i<command.length; i++)
                    {
                        sb.append(command[i]);
                    }
                    propertyValue = sb.toString();
                }
                else
                {
                    propertyValue = command[3];
                }

                // Pass setNodeProperty() the serializable value

                Serializable serializableValue = convertValueFromSring(propertyValue);
                QName valueTypeQName = getValueTypeQName(propertyValue);

                fService.setNodeProperty(command[1], valueQName, new PropertyValue(valueTypeQName, serializableValue));

                out.println("set property " + command[2] + " of " + command[1]);
            }
            else if (command[0].equals("setstoreproperty"))
            {
                if (command.length < 4)
                {
                    return "Syntax Error.";
                }

                QName valueQName = QName.createQName(command[2]);

                String propertyValue = "";

                // If multiple values are specified then concatenate the values
                if (command.length > 4)
                {
                    StringBuffer sb = new StringBuffer();
                    for (int i=3; i<command.length; i++)
                    {
                        sb.append(command[i]);
                    }
                    propertyValue = sb.toString();
                }
                else
                {
                    propertyValue = command[3];
                }

                Serializable serializableValue = convertValueFromSring(propertyValue);
                QName valueTypeQName = getValueTypeQName(propertyValue);

                fService.setStoreProperty(command[1], valueQName, new PropertyValue(valueTypeQName, serializableValue));

                out.println("set property " + command[2] + " of " + command[1]);
            }
            else if (command[0].equals("setstorepermission"))
            {
                if (command.length != 4)
                {
                    return "Syntax Error.";
                }
                
                fPermissionService.setPermission(
                        new StoreRef(StoreRef.PROTOCOL_AVM, command[1]), command[2], command[3], true);
            }
            else if (command[0].equals("clearstorepermission"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.";
                }
                
                fPermissionService.clearPermission(
                        new StoreRef(StoreRef.PROTOCOL_AVM, command[1]), command[2]);
            }
            // unknown or invalid command specified
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

    private static Serializable convertValueFromSring(String sValue)
    {
        Serializable retValue = "";

        CharSequence seq = sValue.subSequence(0, sValue.length());

        if (collectionPattern.matcher(seq).matches())
        {
            String[] elements = getCSVArray(sValue.substring(1, sValue.length()-1));

            // Should this be an ArrayList or a HashSet?
            Collection<Serializable> propValues = new HashSet<Serializable>(elements.length);
            for (int i=0; i<elements.length; i++)
            {
                // Add each value in turn unless it is empty
                if (!"".equals(elements[i]))
                {
                    propValues.add(convertValueFromSring(elements[i]));
                }
            }
            retValue = (Serializable)propValues;
        }
        else if (nodeRefPattern.matcher(seq).matches())
        {
            retValue = new NodeRef(sValue);
        }
        else if (integerPattern.matcher(seq).matches())
        {
            retValue = new Integer(sValue);
        }
        else if (dateTimePattern.matcher(seq).matches())
        {
            // TODO: Support timestamps
            /*
            DateFormat df = DateFormat.getDateTimeInstance(dateStyle, timeStyle)df.pa
            Calendar cal = Calendar.getInstance();
            retValue = new NodeRef(dateTimePattern.matcher(seq).group(1));
             */
        } else
        {
            retValue = sValue;
        }
        return retValue;
    }

    private static QName getValueTypeQName(String sValue)
    {
        QName typeQName = null;

        CharSequence seq = sValue.subSequence(0, sValue.length());

        if (collectionPattern.matcher(seq).matches())
        {
            String[] elements = getCSVArray(sValue.substring(1, sValue.length()-1));

            if (elements[0] != "")
            {
                typeQName = getValueTypeQName(elements[0]);
            }
        }
        else if (nodeRefPattern.matcher(seq).matches())
        {
            typeQName = DataTypeDefinition.NODE_REF;
        }
        else if (integerPattern.matcher(seq).matches())
        {
            typeQName = DataTypeDefinition.INT;
        }
        else if (dateTimePattern.matcher(seq).matches())
        {
            // TODO: Support timestamps
            /*
            DateFormat df = DateFormat.getDateTimeInstance(dateStyle, timeStyle)df.pa
            Calendar cal = Calendar.getInstance();
            retValue = new NodeRef(dateTimePattern.matcher(seq).group(1));
             */
            typeQName = DataTypeDefinition.DATETIME;
        } else
        {
            typeQName = DataTypeDefinition.TEXT;
        }
        return typeQName;
    }

    private static String[] getCSVArray(String valueString)
    {
        String[] elements = valueString.split(",");

        if (elements.length == 0)
        {
            elements = new String[1];
            elements[0] = valueString;
        }
        return elements;
    }
}