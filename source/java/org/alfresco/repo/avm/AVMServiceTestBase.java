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

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import junit.framework.TestCase;

/**
 * Base class for AVMService tests.
 * @author britt
 */
public class AVMServiceTestBase extends TestCase
{
    /**
     * The AVMService we are testing.
     */
    protected static AVMService fService;

    /**
     * The reaper thread.
     */
    protected static OrphanReaper fReaper;
    
    /**
     * The AVMSyncService.
     */
    protected static AVMSyncService fSyncService;
    /**
     * The application context.
     */
    protected static FileSystemXmlApplicationContext fContext;
    
    /**
     * The start time of actual work for a test.
     */
    private long fStartTime;

    /**
     * Setup for AVM tests.  Note that we set the polling
     * interval for the reaper to 4 seconds so that tests will
     * finish reasonably quickly.
     */
    @Override
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            fContext = new FileSystemXmlApplicationContext("config/alfresco/application-context.xml");
            fService = (AVMService)fContext.getBean("AVMService");
            fReaper = (OrphanReaper)fContext.getBean("orphanReaper");
            fSyncService = (AVMSyncService)fContext.getBean("AVMSyncService");
        }
        fService.createAVMStore("main");
        fStartTime = System.currentTimeMillis();
    }

    /**
     * Cleanup after a test. Purge all stores. Move alf_data
     * directory aside.
     */
    @Override
    protected void tearDown() throws Exception
    {
        long now = System.currentTimeMillis();
        System.out.println("Timing: " + (now - fStartTime) + "ms");
        List<AVMStoreDescriptor> descriptors = fService.getAVMStores();
        for (AVMStoreDescriptor desc : descriptors)
        {
            fService.purgeAVMStore(desc.getName());
        }
        // fContext.close();
        // File alfData = new File("alf_data");
        // File target = new File("alf_data" + now);
        // alfData.renameTo(target);
    }
    
    /**
     * Get the recursive contents of the given path and version.
     * @param path 
     * @param version
     * @return A string representation of the contents.
     */
    protected String recursiveContents(String path, int version, boolean followLinks)
    {
        String val = recursiveList(path, version, 0, followLinks);
        return val.substring(val.indexOf('\n'));
    }

    
    /**
     * Helper to write a recursive listing of an AVMStore at a given version.
     * @param repoName The name of the AVMStore.
     * @param version The version to look under.
     */
    protected String recursiveList(String repoName, int version, boolean followLinks)
    {
        return recursiveList(repoName + ":/", version, 0, followLinks);
    }
    
    /**
     * Recursive list the given path.
     * @param path The path.
     * @param version The version.
     * @param indent The current indent level.
     */
    protected String recursiveList(String path, int version, int indent, boolean followLinks)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indent; i++)
        {
            builder.append(' ');
        }
        builder.append(path.substring(path.lastIndexOf('/') + 1));
        builder.append(' ');
        AVMNodeDescriptor desc = fService.lookup(version, path, true);
        builder.append(desc.toString());
        builder.append('\n');
        if (desc.getType() == AVMNodeType.PLAIN_DIRECTORY ||
            (desc.getType() == AVMNodeType.LAYERED_DIRECTORY && followLinks))
        {
            String basename = path.endsWith("/") ? path : path + "/";
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(version, path, true);
            for (String name : listing.keySet())
            {
                builder.append(recursiveList(basename + name, version, indent + 2, followLinks));
            }
        }
        return builder.toString();
    }
    
    /**
     * Setup a basic tree.
     */
    protected void setupBasicTree()
        throws IOException
    {
        fService.createDirectory("main:/", "a");
        fService.createDirectory("main:/a", "b");
        fService.createDirectory("main:/a/b", "c");
        fService.createDirectory("main:/", "d");
        fService.createDirectory("main:/d", "e");
        fService.createDirectory("main:/d/e", "f");
        fService.createFile("main:/a/b/c", "foo").close();
        PrintStream out = new PrintStream(fService.getFileOutputStream("main:/a/b/c/foo"));
        out.println("I am main:/a/b/c/foo");
        out.flush();
        out.close();
        fService.createFile("main:/a/b/c", "bar").close();
        out = new PrintStream(fService.getFileOutputStream("main:/a/b/c/bar"));
        out.println("I am main:/a/b/c/bar");
        out.flush();
        out.close();
        fService.createSnapshot("main", null, null);
    }
    
    /**
     * Check that history has not been screwed up.
     */
    protected void checkHistory(TreeMap<Integer, String> history, String repName)
    {
        for (Integer i : history.keySet())
        {
            assertEquals(history.get(i), recursiveList(repName, i, false));
        }
        int latest = fService.getNextVersionID(repName);
        history.put(latest - 1, recursiveList(repName, -1, false));
    }
}
