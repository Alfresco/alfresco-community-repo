/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.repo.avm.util.RemoteBulkLoader;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncException;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Local unit tests of AVM (AVMSyncService & AVMService)
 */
public class AVMServiceLocalTest extends TestCase
{
    private static Log logger = LogFactory.getLog(AVMServiceLocalTest.class);
    
    /**
     * The AVMRemote - can be local (AVMRemoteLocal) or remote (AVMRemote)
     */
    protected static AVMRemote fService;

    /**
     * The AVMSyncService - can be local (AVMSyncService) or remote (AVMSyncServiceRemote)
     */
    protected static AVMSyncService fSyncService;
    
    /**
     * The application context.
     */
    protected static FileSystemXmlApplicationContext fContext;

    protected static NameMatcher excluder;
    
    
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            // local (embedded) test setup
            fContext = new FileSystemXmlApplicationContext("config/alfresco/application-context.xml");
            fService = (AVMRemote)fContext.getBean("avmRemote");
            fSyncService = (AVMSyncService)fContext.getBean("AVMSyncService");
            excluder = (NameMatcher) fContext.getBean("globalPathExcluder");
            
            AuthenticationService authService = (AuthenticationService)fContext.getBean("AuthenticationService");
            authService.authenticate("admin", "admin".toCharArray());
        }
        
        if (fService.getStore("main") == null)
        {
            fService.createStore("main");
        }
        if (fService.getStore("layer") == null)
        {
            fService.createStore("layer");
        }
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        fService.purgeStore("main");
        fService.purgeStore("layer");
    }
    
    public void testGetAPath() throws Exception
    {
        try
        {
            fService.createStore("test2932");
            fService.createDirectory("test2932:/", "a");
            fService.createFile("test2932:/a", "foo.txt").close();
            AVMNodeDescriptor found = fService.lookup(-1, "test2932:/a/foo.txt");
            Pair<Integer, String> path = fService.getAPath(found);
            assertEquals(path.getSecond(), "test2932:/a/foo.txt");
            explorePaths("test2932:/");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
        finally
        {
            fService.purgeStore("test2932");
        }
    }

    /**
     * Do a simple hello world test.
     */
    public void testSimple()
    {
        try
        {
            List<AVMStoreDescriptor> stores = fService.getStores();
            
            for (AVMStoreDescriptor store : stores)
            {
                if (logger.isDebugEnabled()) { logger.debug(store); }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    protected void explorePaths(String path) throws Exception
    {
        Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, path);
        for (Map.Entry<String, AVMNodeDescriptor> entry : listing.entrySet())
        {
            Pair<Integer, String> childPath = fService.getAPath(entry.getValue());
            if (logger.isDebugEnabled()) { logger.debug(childPath); }
            if (entry.getValue().isDirectory())
            {
                explorePaths(entry.getValue().getPath());
            }
        }
    }

    /**
     * Test reading and writing.
     */
    public void testReadWrite()
    {
        try
        {
            // Create a test store.
            fService.createStore("test2933");
            // Create a directory.
            fService.createDirectory("test2933:/", "a");
            // Write out a file.
            OutputStream out =
                fService.createFile("test2933:/a", "foo.txt");
            byte [] buff = "This is a plain old text file.\n".getBytes();
            out.write(buff);
            buff = "It contains text.\n".getBytes();
            out.write(buff);
            out.close();
            // Read back that file.
            InputStream in =
                fService.getFileInputStream(-1, "test2933:/a/foo.txt");
            buff = new byte[1024];
            assertEquals(49, in.read(buff));
            if (logger.isDebugEnabled()) { logger.debug(new String(buff)); }
            assertEquals(-1, in.read(buff));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
        finally
        {
            fService.purgeStore("test2933");
        }
    }

    /**
     * Another test of reading.
     */
    public void testRead()
    {
        try
        {
            fService.createStore("froo");
            // Create a file.
            byte [] buff = new byte[64];
            for (int i = 0; i < 64; i++)
            {
                buff[i] = (byte)i;
            }
            OutputStream out =
                fService.createFile("froo:/", "foo.dat");
            out.write(buff, 32, 32);
            out.close();
            // Read it back in.
            InputStream in =
                fService.getFileInputStream(-1, "froo:/foo.dat");
            buff = new byte[1024];
            assertEquals(32, in.read(buff));
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
        finally
        {
            fService.purgeStore("froo");
        }
    }

    /**
     * Test a call that should return null;
     */
    public void testErrorState()
    {
        try
        {
            assertNull(fService.lookup(-1, "main:/fizz/fazz"));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    
    /**
     * Test update to branch
     */
    public void testSimpleUpdateBR()
    {   
        try
        {
            // Create a store.
            fService.createStore("froo");
            // Create a directory.
            fService.createDirectory("froo:/", "a");
            // Create a file.
            fService.createFile("froo:/a", "foo").close();
            // Create another store.
            fService.createStore("broo");
            // Create a branch.
            fService.createBranch(-1, "froo:/a", "broo:/", "a");
            List<AVMDifference> diffs = fSyncService.compare(-1, "froo:/a", -1, "broo:/a", null);
            assertEquals(0, diffs.size());
            fService.createFile("froo:/a", "bar").close();
            diffs = fSyncService.compare(-1, "froo:/a", -1, "broo:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[froo:/a/bar[-1] > broo:/a/bar[-1]]", diffs.toString());
            // Update.
            fSyncService.update(diffs, null, false, false, false, false, "flippy", "Stuff");
            diffs = fSyncService.compare(-1, "froo:/a", -1, "broo:/a", null);
            assertEquals(0, diffs.size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
        finally
        {
            fService.purgeStore("broo");
            fService.purgeStore("froo");
        }
    }
    
    /**
     * Test update to layered directory
     */
    public void testSimpleUpdateLD() throws Exception
    {
        try
        {
            fService.createLayeredDirectory("main:/", "layer:/", "layer");
            
            // Create a directory.
            fService.createDirectory("layer:/layer", "b");
            // Create a file.
            fService.createFile("layer:/layer/b", "foo").close();
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/layer/b[-1] > main:/b[-1]]", diffs.toString());
            fService.createSnapshot("layer", null, null);
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.createSnapshot("main", null, null);
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
            fSyncService.flatten("layer:/layer", "main:/");
            recursiveList("layer");
            recursiveList("main");
            fService.createStore("layer2");
            fService.createLayeredDirectory("layer:/layer", "layer2:/", "layer");
            
            // Create a directory.
            fService.createDirectory("layer2:/layer/", "c");
            // Create a file.
            fService.createFile("layer2:/layer/c", "foo").close();
            
            fService.createSnapshot("layer2", null, null);
            diffs = fSyncService.compare(-1, "layer2:/layer", -1, "layer:/layer", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer2:/layer/c[-1] > layer:/layer/c[-1]]", diffs.toString());
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            diffs = fSyncService.compare(-1, "layer2:/layer", -1, "layer:/layer", null);
            assertEquals(0, diffs.size());
            fSyncService.flatten("layer2:/layer", "layer:/layer");
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/layer/c[-1] > main:/c[-1]]", diffs.toString());
            recursiveList("layer2");
            recursiveList("layer");
            recursiveList("main");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("layer2");
        }
    }
    
    /**
     * Test bulk update (using layered directory).
     */
    public void testBulkUpdateLD() throws Exception
    {
        try
        {
            RemoteBulkLoader loader = new RemoteBulkLoader();
            loader.setAvmRemoteService(fService);

            fService.createLayeredDirectory("main:/", "layer:/", "layer");
            loader.recursiveLoad("config/alfresco/bootstrap", "layer:/layer");
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/layer/bootstrap[-1] > main:/bootstrap[-1]]", diffs.toString());
            fService.createSnapshot("layer", null, null);
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.createSnapshot("main", null, null);
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
            fSyncService.flatten("layer:/layer", "main:/");
            recursiveList("layer");
            recursiveList("main");
            fService.createStore("layer2");
            fService.createLayeredDirectory("layer:/layer", "layer2:/", "layer");
            loader.recursiveLoad("config/alfresco/bootstrap", "layer2:/layer/bootstrap");
            fService.createSnapshot("layer2", null, null);
            diffs = fSyncService.compare(-1, "layer2:/layer", -1, "layer:/layer", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer2:/layer/bootstrap/bootstrap[-1] > layer:/layer/bootstrap/bootstrap[-1]]", diffs.toString());
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            diffs = fSyncService.compare(-1, "layer2:/layer", -1, "layer:/layer", null);
            assertEquals(0, diffs.size());
            fSyncService.flatten("layer2:/layer", "layer:/layer");
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/layer/bootstrap/bootstrap[-1] > main:/bootstrap/bootstrap[-1]]", diffs.toString());          
            recursiveList("layer2");
            recursiveList("layer");
            recursiveList("main");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("layer2");
        }
    }
    
    /**
     * Test the flatten operation, with a little bit of compare and update.
     */
    public void testFlatten() throws Exception
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            recursiveList("main");
            // Change some stuff.
            fService.createFile("main:/layer/b", "fig").close();
            fService.getFileOutputStream("main:/layer/b/c/foo").close();
            fService.createSnapshot("main", null, null);
            recursiveList("main");
            // Do a compare.
            List<AVMDifference> diffs = fSyncService.compare(-1, "main:/layer", -1, "main:/a", null);
            assertEquals(2, diffs.size());
            assertEquals("[main:/layer/b/c/foo[-1] > main:/a/b/c/foo[-1], main:/layer/b/fig[-1] > main:/a/b/fig[-1]]", diffs.toString());
            // Update.
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            recursiveList("main");
            // Flatten.
            fSyncService.flatten("main:/layer", "main:/a");
            recursiveList("main");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
        
    /**
     * Test partial flatten.
     */
    public void testPartialFlatten() throws Exception
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "layer:/", "a");
            fService.getFileOutputStream("layer:/a/b/c/foo").close();
            fService.createFile("layer:/a/b", "bing").close();
            List<AVMDifference> diffs = new ArrayList<AVMDifference>();
            diffs.add(new AVMDifference(-1, "layer:/a/b/c/foo", -1, "main:/a/b/c/foo", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fSyncService.flatten("layer:/a", "main:/a");
            AVMNodeDescriptor b = fService.lookup(-1, "layer:/a/b");
            assertTrue(b.isLayeredDirectory());
            AVMNodeDescriptor c = fService.lookup(-1, "layer:/a/b/c");
            assertTrue(c.isPlainDirectory());
            diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/a/b/bing[-1] > main:/a/b/bing[-1]]", diffs.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
    
    /**
     * Test AVMSyncService resetLayer.
     */
    public void testResetLayer() throws Exception
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createFile("main:/layer", "figs").close();
            assertFalse(recursiveContents("main:/a", -1, true).equals(recursiveContents("main:/layer", -1, true)));
            recursiveList("main");
            fSyncService.resetLayer("main:/layer");
            assertEquals(recursiveContents("main:/a", -1, true), recursiveContents("main:/layer", -1, true));
            recursiveList("main");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
    
    /**
     * Test AVMSyncService update.
     */
    public void testUpdate() throws Exception
    {
        try
        {
            setupBasicTree();
            // Try branch to branch update.
            fService.createBranch(-1, "main:/a", "main:/", "abranch");
            fService.createFile("main:/abranch", "monkey").close();
            fService.createFile("main:/abranch", "#foo").close();
            fService.createFile("main:/abranch", "figs.tmp").close();
            fService.getFileOutputStream("main:/abranch/b/c/foo").close();
            recursiveList("main");
            List<AVMDifference> cmp = fSyncService.compare(-1, "main:/abranch", -1, "main:/a", excluder);
            assertEquals(2, cmp.size());
            assertEquals("[main:/abranch/b/c/foo[-1] > main:/a/b/c/foo[-1], main:/abranch/monkey[-1] > main:/a/monkey[-1]]", cmp.toString());
            List<AVMDifference> diffs = new ArrayList<AVMDifference>();
            diffs.add(new AVMDifference(-1, "main:/abranch/monkey", -1, "main:/a/monkey", AVMDifference.NEWER));
            diffs.add(new AVMDifference(-1, "main:/abranch/b/c/foo", -1, "main:/a/b/c/foo", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.createSnapshot("main", null, null);
            recursiveList("main");
            assertEquals(fService.lookup(-1, "main:/abranch/monkey").getId(), fService.lookup(-1, "main:/a/monkey").getId());
            assertEquals(fService.lookup(-1, "main:/abranch/b/c/foo").getId(), fService.lookup(-1, "main:/a/b/c/foo").getId());
            // Try updating a deletion.
            fService.removeNode("main:/abranch", "monkey");
            recursiveList("main");
            cmp = fSyncService.compare(-1, "main:/abranch", -1, "main:/a", excluder);
            assertEquals(1, cmp.size());
            assertEquals("[main:/abranch/monkey[-1] > main:/a/monkey[-1]]", cmp.toString());
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/abranch/monkey", -1, "main:/a/monkey", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            assertEquals(0, fSyncService.compare(-1, "main:/abranch", -1, "main:/a", excluder).size());
            fService.createSnapshot("main", null, null);
            recursiveList("main");
            assertEquals(fService.lookup(-1, "main:/abranch/monkey", true).getId(), fService.lookup(-1, "main:/a/monkey", true).getId());
            // Try one that should fail.
            fService.createFile("main:/abranch", "monkey").close();
            cmp = fSyncService.compare(-1, "main:/abranch", -1, "main:/a", excluder);
            assertEquals(1, cmp.size());
            assertEquals("[main:/abranch/monkey[-1] > main:/a/monkey[-1]]", cmp.toString());
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/a/monkey", -1, "main:/abranch/monkey", AVMDifference.NEWER));
            try
            {
                fSyncService.update(diffs, null, false, false, false, false, null, null);
                fail();
            }
            catch (AVMSyncException se)
            {
                // Do nothing.
            }
            // Get synced again by doing an override older.
            recursiveList("main");
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/a/monkey", -1, "main:/abranch/monkey", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, true, null, null);
            assertEquals(0, fSyncService.compare(-1, "main:/abranch", -1, "main:/a", excluder).size());
            fService.createSnapshot("main", null, null);
            recursiveList("main");
            assertEquals(fService.lookup(-1, "main:/a/monkey", true).getId(), fService.lookup(-1, "main:/abranch/monkey", true).getId());
            // Cleanup for layered tests.
            fService.purgeStore("main");
            fService.createStore("main");
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createFile("main:/layer", "monkey").close();
            fService.getFileOutputStream("main:/layer/b/c/foo").close();
            cmp = fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder);
            assertEquals(2, cmp.size());
            assertEquals("[main:/layer/b/c/foo[-1] > main:/a/b/c/foo[-1], main:/layer/monkey[-1] > main:/a/monkey[-1]]", cmp.toString());
            recursiveList("main");
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/layer/monkey", -1, "main:/a/monkey", AVMDifference.NEWER));
            diffs.add(new AVMDifference(-1, "main:/layer/b/c/foo", -1, "main:/a/b/c/foo", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            assertEquals(0, fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder).size());
            fService.createSnapshot("main", null, null);
            recursiveList("main");
            assertEquals(fService.lookup(-1, "main:/layer/monkey").getId(), fService.lookup(-1, "main:/a/monkey").getId());
            assertEquals(fService.lookup(-1, "main:/layer/b/c/foo").getId(), fService.lookup(-1, "main:/a/b/c/foo").getId());
            // Try updating a deletion.
            fService.removeNode("main:/layer", "monkey");
            recursiveList("main");
            cmp = fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder);
            assertEquals(1, cmp.size());
            assertEquals("[main:/layer/monkey[-1] > main:/a/monkey[-1]]", cmp.toString());
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/layer/monkey", -1, "main:/a/monkey", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            assertEquals(0, fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder).size());
            fService.createSnapshot("main", null, null);
            recursiveList("main");
            assertEquals(fService.lookup(-1, "main:/layer/monkey", true).getId(), fService.lookup(-1, "main:/a/monkey", true).getId());
            // Try one that should fail.
            fService.createFile("main:/layer", "monkey").close();
            cmp = fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder);
            assertEquals(1, cmp.size());
            assertEquals("[main:/layer/monkey[-1] > main:/a/monkey[-1]]", cmp.toString());
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/a/monkey", -1, "main:/layer/monkey", AVMDifference.NEWER));
            try
            {
                fSyncService.update(diffs, null, false, false, false, false, null, null);
                fail();
            }
            catch (AVMSyncException se)
            {
                // Do nothing.
            }
            // Get synced again by doing an override older.
            recursiveList("main");
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/a/monkey", -1, "main:/layer/monkey", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, true, null, null);
            assertEquals(0, fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder).size());
            fService.createSnapshot("main", null, null);
            recursiveList("main");
            assertEquals(fService.lookup(-1, "main:/a/monkey", true).getId(), fService.lookup(-1, "main:/layer/monkey", true).getId());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
    
    /**
     * Test that an update forces a snapshot on the source.
     */
    public void testUpdateSnapshot() throws Exception
    {
        try
        {
            setupBasicTree();
            fService.createStore("branch");
            fService.createBranch(-1, "main:/", "branch:/", "branch");
            // Modify some things in the branch.
            fService.createFile("branch:/branch/a/b", "fing").close();
            fService.getFileOutputStream("branch:/branch/a/b/c/foo").close();
            fService.removeNode("branch:/branch/a/b/c", "bar");
            List<AVMDifference> diffs = fSyncService.compare(-1, "branch:/branch", -1, "main:/", null);
            assertEquals(3, diffs.size());
            assertEquals("[branch:/branch/a/b/c/bar[-1] > main:/a/b/c/bar[-1], branch:/branch/a/b/c/foo[-1] > main:/a/b/c/foo[-1], branch:/branch/a/b/fing[-1] > main:/a/b/fing[-1]]", diffs.toString());
            // Now update.
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            diffs = fSyncService.compare(-1, "branch:/branch", -1, "main:/", null);
            assertEquals(0, diffs.size());
            fService.getFileOutputStream("branch:/branch/a/b/fing").close();
            assertTrue(fService.lookup(-1, "branch:/branch/a/b/fing").getId() != fService.lookup(-1, "main:/a/b/fing").getId());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("branch");
        }
    }
    
    /**
     * Test a noodle update.
     */
    public void testNoodleUpdate() throws Exception
    {
        try
        {
            setupBasicTree();
            fService.createStore("staging");
            List<AVMDifference> diffs = fSyncService.compare(-1, "main:/", -1, "staging:/", null);
            assertEquals(2, diffs.size());
            assertEquals("[main:/a[-1] > staging:/a[-1], main:/d[-1] > staging:/d[-1]]", diffs.toString());
            List<AVMDifference> noodle = new ArrayList<AVMDifference>();
            noodle.add(new AVMDifference(-1, "main:/a/b/c/foo", -1, "staging:/a/b/c/foo", AVMDifference.NEWER));
            noodle.add(new AVMDifference(-1, "main:/d", -1, "staging:/d", AVMDifference.NEWER));
            fSyncService.update(noodle, null, false, false, false, false, null, null);
            diffs = fSyncService.compare(-1, "main:/", -1, "staging:/", null);
            assertEquals(1, diffs.size());
            assertEquals("[main:/a/b/c/bar[-1] > staging:/a/b/c/bar[-1]]", diffs.toString());
            assertEquals("main:/a/b/c/bar", diffs.get(0).getSourcePath());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("staging");
        }
    }
    

    public void testRename6() throws Exception
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "layer:/", "a");
            fService.rename("layer:/a/b", "c", "layer:/a/b", "z");
            recursiveContents("layer:/");
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(2, diffs.size());
            assertEquals("[layer:/a/b/c[-1] > main:/a/b/c[-1], layer:/a/b/z[-1] > main:/a/b/z[-1]]", diffs.toString());
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            recursiveContents("layer:/");
            recursiveContents("main:/");
            fSyncService.flatten("layer:/a", "main:/a");
            recursiveContents("layer:/");
            recursiveContents("main:/");
            fService.createFile("layer:/a/b/z", "fudge").close();
            fService.rename("layer:/a/b", "z", "layer:/a/b", "y");
            recursiveContents("layer:/");           
            diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(2, diffs.size());
            assertEquals("[layer:/a/b/y[-1] > main:/a/b/y[-1], layer:/a/b/z[-1] > main:/a/b/z[-1]]", diffs.toString());
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            recursiveContents("layer:/");
            recursiveContents("main:/");
            fSyncService.flatten("layer:/a", "main:/a");
            recursiveContents("layer:/");
            recursiveContents("main:/");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    protected void recursiveContents(String path)
    {
        String contentsStr = recursiveContents(path, -1, true);
        if (logger.isDebugEnabled())
        { 
            logger.debug(contentsStr);
        }
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

    protected void recursiveList(String store)
    {
        String list = recursiveList(store, -1, true);
        if (logger.isDebugEnabled())
        { 
            logger.debug(store+":");
            logger.debug(list);
        }
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
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(version, path);
            for (String name : listing.keySet())
            {
                if (logger.isDebugEnabled()) { logger.debug(name); }
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
        
        
        OutputStream out = fService.createFile("main:/a/b/c", "foo");
        byte [] buff = "I am main:/a/b/c/foo".getBytes();
        out.write(buff);
        out.close();
        
        /*
        fService.createFile("main:/a/b/c", "foo").close();
        ContentWriter writer = fService.getContentWriter("main:/a/b/c/foo");
        writer.setEncoding("UTF-8");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent("I am main:/a/b/c/foo");
        */
        
        out = fService.createFile("main:/a/b/c", "bar");
        buff = "I am main:/a/b/c/bar".getBytes();
        out.write(buff);
        out.close();
        
        /*
        fService.createFile("main:/a/b/c", "bar").close();
        writer = fService.getContentWriter("main:/a/b/c/bar");
        // Force a conversion
        writer.setEncoding("UTF-16");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent("I am main:/a/b/c/bar");
        */
       
        fService.createSnapshot("main", null, null);
    }
}
