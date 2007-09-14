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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.model.filefolder.loader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.remote.FileFolderRemoteClient;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.remote.LoaderRemote;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * A description of session details.
 * 
 * @author Derek Hulley
 * @since 2.2
 */
public class LoaderSession
{
    public static final ThreadGroup THREAD_GROUP = new ThreadGroup("FileFolderRemoteLoader");
    
    private String username;
    private String password;
    private String name;
    private Set<String> rmiUrls;
    private Set<StoreRef> storeRefs;
    private boolean verbose;
    private File outputFile;
    private File sourceDir;
    private int[] folderProfiles;

    private List<LoaderServerProxy> remoteServers;
    private List<NodeRef> workingRootNodeRefs;
    private File[] sourceFiles;
    private OutputStream outVerbose;
    private OutputStream outSummary;
    private OutputStream outError;
    
    /**
     * 
     */
    public LoaderSession(
            String username,
            String password,
            String name,
            Set<String> rmiUrls,
            Set<StoreRef> storeRefs,
            boolean verbose,
            File sourceDir,
            int[] folderProfiles)
    {
        this.username = username;
        this.password = password;
        this.name = name;
        this.rmiUrls = rmiUrls;
        this.storeRefs = storeRefs;
        this.verbose = verbose;
        this.sourceDir = sourceDir;
        this.folderProfiles = folderProfiles;
    }

    public String getName()
    {
        return name;
    }

    public List<LoaderServerProxy> getRemoteServers()
    {
        return remoteServers;
    }

    public List<NodeRef> getWorkingRootNodeRefs()
    {
        return workingRootNodeRefs;
    }

    public File[] getSourceFiles()
    {
        return sourceFiles;
    }

    public int[] getFolderProfiles()
    {
        return folderProfiles;
    }

    /**
     * Initialize the object before first use.
     */
    public synchronized void initialize() throws Exception
    {
        if (remoteServers != null)
        {
            throw new AlfrescoRuntimeException("The client has already been initialized");
        }
        remoteServers = LoaderSession.connect(rmiUrls, username, password);
        workingRootNodeRefs = LoaderSession.makeStores(remoteServers, storeRefs);
        LoaderSession.checkClustering(remoteServers, workingRootNodeRefs);
        
        // Create the output file, if necessary
        if (outputFile != null)
        {
            File outputDir = outputFile.getParentFile();
            if (!outputDir.exists())
            {
                outputDir.mkdirs();
            }
            if (outputFile.exists())
            {
                System.out.println("The output file " + outputFile + " already exists.");
                System.out.println("Are you sure you want to overwrite the file?");
                int in = System.in.read();
                if (in != 'Y' && in != 'y')
                {
                    throw new LoaderClientException("The output file " + outputFile + " already exists");
                }
            }
        }
        
        // Get the source files
        sourceFiles = LoaderSession.getSourceFiles(sourceDir);
        
        // Construct output and error files
        long time = System.currentTimeMillis();
        File fileVerbose = new File("./LoaderSession-" + name + "-"+ time + "-verbose.txt");
        File fileSummary = new File("./LoaderSession-" + name + "-"+ time + "-summary.txt");
        File fileError = new File("./LoaderSession-" + name + "-"+ time + "-error.txt");
        outVerbose = new BufferedOutputStream(new FileOutputStream(fileVerbose));
        outSummary = new BufferedOutputStream(new FileOutputStream(fileSummary));
        outError = new BufferedOutputStream(new FileOutputStream(fileError));
    }
    
    public synchronized void close()
    {
        try { outVerbose.close(); } catch (Throwable e) {}
        try { outSummary.close(); } catch (Throwable e) {}
        try { outError.close(); } catch (Throwable e) {}
        
        outVerbose = null;
        outSummary = null;
        outError = null;
    }
    
    /**
     * Connect to the remote servers.
     */
    private static List<LoaderServerProxy> connect(Set<String> rmiUrls, String username, String password) throws Exception
    {
        List<LoaderServerProxy> remoteServers = new ArrayList<LoaderServerProxy>(rmiUrls.size());
        for (String rmiUrl : rmiUrls)
        {
            try
            {
                // Ensure the RMI URL is consistent
                if (!rmiUrl.endsWith("/"))
                {
                    rmiUrl += "/";
                }
                // Get the FileFolderServiceTransport
                FileFolderRemoteClient fileFolderRemote = new FileFolderRemoteClient(rmiUrl);
                // Get the LoaderServiceTransport
                RmiProxyFactoryBean loaderFactory = new RmiProxyFactoryBean();
                loaderFactory.setRefreshStubOnConnectFailure(true);
                loaderFactory.setServiceInterface(LoaderRemote.class);
                loaderFactory.setServiceUrl(rmiUrl + LoaderRemote.SERVICE_NAME);
                loaderFactory.afterPropertiesSet();
                LoaderRemote loaderRemote = (LoaderRemote) loaderFactory.getObject();
                
                // Authenticate
                String ticket = loaderRemote.authenticate(username, password);
                
                // Store the service references
                LoaderServerProxy remoteServer = new LoaderServerProxy(
                        rmiUrl,
                        ticket,
                        fileFolderRemote,
                        loaderRemote);
                remoteServers.add(remoteServer);
            }
            catch (Throwable e)
            {
                // Failed to connect.
                System.err.println("\n" +
                        "ERROR: Failed to establish connection to server: \n" +
                        "   Server: " + rmiUrl + "\n" +
                        "   Error:  " + e.getMessage());
                e.printStackTrace();
            }
        }
        // Check that there is at least one server
        if (remoteServers.size() == 0)
        {
            throw new LoaderClientException("No remote servers are available");
        }
        return remoteServers;
    }
    
    private static List<NodeRef> makeStores(List<LoaderServerProxy> remoteServers, Set<StoreRef> storeRefs) throws Exception
    {
        // Take the first server and ensure that all the stores are available
        LoaderServerProxy remoteServer = remoteServers.get(0);
        
        List<NodeRef> workingRootNodeRefs = new ArrayList<NodeRef>(10);
        for (StoreRef storeRef : storeRefs)
        {
            NodeRef workingRootNodeRef = remoteServer.loaderRemote.getOrCreateWorkingRoot(remoteServer.ticket, storeRef);
            workingRootNodeRefs.add(workingRootNodeRef);
        }
        // Done
        return workingRootNodeRefs;
    }
    
    private static void checkClustering(
            List<LoaderServerProxy> remoteServers,
            List<NodeRef> workingRootNodeRefs) throws Exception
    {
        List<String> problems = new ArrayList<String>(10);
        for (LoaderServerProxy remoteServer : remoteServers)
        {
            String ticket = remoteServer.ticket;
            for (NodeRef workingRootNodeRef : workingRootNodeRefs)
            {
                try
                {
                    // Get the working root node.  If the cluster is correctly configured,
                    // it will be available on all the servers.
                    FileInfo fileInfo = remoteServer.fileFolderRemote.getFileInfo(ticket, workingRootNodeRef);
                    if (fileInfo == null)
                    {
                        problems.add("Cannot find the working root node on server: " + remoteServer.rmiUrl);
                        continue;
                    }
                    // Now look for and create a file against which to store some content
                    String sampleFilename = "InitialSample.txt";
                    String sampleContent = "Sample content";
                    NodeRef sampleNodeRef = remoteServer.fileFolderRemote.searchSimple(ticket, workingRootNodeRef, sampleFilename);
                    if (sampleNodeRef == null)
                    {
                        FileInfo sampleFileInfo = remoteServer.fileFolderRemote.create(
                                ticket, workingRootNodeRef, sampleFilename, ContentModel.TYPE_CONTENT);
                        sampleNodeRef = sampleFileInfo.getNodeRef();
                        // Write some content
                        byte[] bytes = sampleContent.getBytes("UTF-8");
                        remoteServer.fileFolderRemote.putContent(ticket, sampleNodeRef, bytes, sampleFilename);
                    }
                    // Get the content and ensure that it is the same
                    byte[] bytes = remoteServer.fileFolderRemote.getContent(ticket, sampleNodeRef);
                    if (bytes == null)
                    {
                        problems.add("Sample content was not found on server: " + remoteServer.rmiUrl);
                        continue;
                    }
                    String checkContent = new String(bytes, "UTF-8");
                    if (!checkContent.equals(sampleContent))
                    {
                        problems.add("The sample content differed from expected: " + remoteServer.rmiUrl);
                        continue;
                    }
                }
                catch (Throwable e)
                {
                    System.err.println("ERROR: Failure whilst checking server: " + remoteServer.rmiUrl);
                    e.printStackTrace();
                    problems.add(e.getMessage());
                }
            }
        }
        // Check for issues
        if (problems.size() > 0)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("\n")
              .append("The working root node references could not be found on all the remote servers.\n")
              .append("Please ensure that all the remote servers listed are active in a single cluster.");
            for (String problem : problems)
            {
                sb.append("\n")
                  .append("   ").append(problem);
            }
            throw new LoaderClientException(sb.toString());
        }
    }
    
    private static File[] getSourceFiles(File sourceDir) throws Exception
    {
        // Ensure that the source directory is present, if specified
        if (sourceDir != null)
        {
            if (!sourceDir.exists())
            {
                throw new LoaderClientException("The source directory to contain upload files is missing: " + sourceDir);
            }
            // Check that there is something in it
            File[] allFiles = sourceDir.listFiles();
            ArrayList<File> sourceFiles = new ArrayList<File>(allFiles.length);
            for (File file : allFiles)
            {
                if (file.isDirectory())
                {
                    continue;
                }
                sourceFiles.add(file);
            }
            File[] ret = new File[sourceFiles.size()];
            return sourceFiles.toArray(ret);
        }
        else
        {
            return new File[] {};
        }
    }
    
    private void writeLineEnding(OutputStream out) throws IOException
    {
        if (File.separatorChar == '/')
        {
            // It's unix
            out.write('\n');
        }
        else
        {
            // Windows
            out.write('\r');
            out.write('\n');
        }
    }
    
    public synchronized void logVerbose(String msg)
    {
        if (!verbose || outVerbose == null)
        {
            return;
        }
        try
        {
            byte[] bytes = msg.getBytes("UTF-8");
            outVerbose.write(bytes);
            writeLineEnding(outVerbose);
            outVerbose.flush();
        }
        catch (Throwable e)
        {
            System.err.println("Failed to write message to verbose file: " + e.getMessage());
        }
    }
    
    public synchronized void logSummary(String msg)
    {
        if (outSummary == null)
        {
            return;
        }
        try
        {
            byte[] bytes = msg.getBytes("UTF-8");
            outSummary.write(bytes);
            writeLineEnding(outSummary);
            outSummary.flush();
        }
        catch (Throwable e)
        {
            System.err.println("Failed to write message to summary file: " + e.getMessage());
        }
    }
    
    public synchronized void logError(String msg)
    {
        if (outSummary == null)
        {
            return;
        }
        try
        {
            byte[] bytes = msg.getBytes("UTF-8");
            outSummary.write(bytes);
            writeLineEnding(outError);
            outSummary.flush();
        }
        catch (Throwable e)
        {
            System.err.println("Failed to write message to error file: " + e.getMessage());
        }
    }
}
