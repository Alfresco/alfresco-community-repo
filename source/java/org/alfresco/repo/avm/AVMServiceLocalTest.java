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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.util.RemoteBulkLoader;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncException;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * Local unit tests of AVM (AVMSyncService & AVMService)
 */
public class AVMServiceLocalTest extends TestCase
{
    private static Log logger = LogFactory.getLog(AVMServiceLocalTest.class);
    
    /**
     * The application context.
     */
    protected static ApplicationContext fContext;
    
    /**
     * The AVMRemote - can be local (AVMRemoteLocal) or remote (AVMRemote)
     */
    protected static AVMRemote fService;
    
    /**
     * The AVMSyncService - can be local (AVMSyncService) or remote (AVMSyncServiceRemote)
     */
    protected static AVMSyncService fSyncService;
    
    protected static NameMatcher excluder;
    
    
    @Override
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            // local (embedded) test setup
            fContext = AVMTestSuite.getContext();
            fService = (AVMRemote)fContext.getBean("avmRemote");
            fSyncService = (AVMSyncService)fContext.getBean("AVMSyncService");
            excluder = (NameMatcher) fContext.getBean("globalPathExcluder");
            
            AuthenticationService authService = (AuthenticationService)fContext.getBean("AuthenticationService");
            authService.authenticate(AuthenticationUtil.getAdminUserName(), "admin".toCharArray());
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
    
    public void testSetup() throws Exception
    {
        setUp();
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
            throw e;
        }
        finally
        {
            fService.purgeStore("test2932");
        }
    }

    /**
     * Do a simple hello world test.
     */
    public void testSimple() throws Throwable
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
            throw e;
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
    public void testReadWrite() throws Throwable
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
            throw e;
        }
        finally
        {
            fService.purgeStore("test2933");
        }
    }

    /**
     * Another test of reading.
     */
    public void testRead() throws Throwable
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
            throw e;
        }
        finally
        {
            fService.purgeStore("froo");
        }
    }

    /**
     * Test a call that should return null;
     */
    public void testErrorState() throws Throwable
    {
        try
        {
            assertNull(fService.lookup(-1, "main:/fizz/fazz"));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }

    
    /**
     * Test update to branch
     */
    public void testSimpleUpdateBR() throws Throwable
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
            throw e;
        }
        finally
        {
            fService.purgeStore("broo");
            fService.purgeStore("froo");
        }
    }
    
    //
    // Test updates to layered directories
    //
    
    public void testSimpleUpdateLD1() throws Throwable
    {
        try
        {
            List<AVMDifference> diffs = fSyncService.compare(-1, "main:/", -1, "main:/", null);
            assertEquals(0, diffs.size());

            diffs = fSyncService.compare(-1, "layer:/", -1, "main:/", null);
            assertEquals(0, diffs.size());

            // create file f-a in main root dir
            fService.createFile("main:/", "f-a").close();

            diffs = fSyncService.compare(-1, "layer:/", -1, "main:/", null);
            assertEquals("[layer:/f-a[-1] < main:/f-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());

            fService.createLayeredDirectory("main:/", "layer:/", "layer");

            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());

            // create file f-b in main root dir
            fService.createFile("main:/", "f-b").close();

            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());

            // edit file f-b in layer
            fService.getFileOutputStream("layer:/layer/f-b").close();

            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals("[layer:/layer/f-b[-1] > main:/f-b[-1]]", diffs.toString());
            assertEquals(1, diffs.size());

            // update main from layer
            fSyncService.update(diffs, null, false, false, false, false, null, null);

            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
    }

    public void testSimpleUpdateLD2() throws Throwable
    {
        try
        {
            // create directories base/d-a and file f-aa in main
            fService.createDirectory("main:/", "base");
            fService.createDirectory("main:/base", "d-a");
            fService.createFile("main:/base/d-a", "f-aa").close();
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer" + ":/", -1, "main:/", null);
            assertEquals("[layer:/base[-1] < main:/base[-1]]", diffs.toString());
            assertEquals(1, diffs.size());

            fService.createLayeredDirectory("main:/base", "layer:/", "layer-to-base");

            diffs = fSyncService.compare(-1, "layer:/layer-to-base", -1, "main:/base", null);
            assertEquals(0, diffs.size());

            // edit file f-aa in main
            fService.getFileOutputStream("main:/base/d-a/f-aa").close();

            diffs = fSyncService.compare(-1, "layer:/layer-to-base", -1, "main:/base", null);
            assertEquals(0, diffs.size());

            // edit file f-aa in layer
            fService.getFileOutputStream("layer:/layer-to-base/d-a/f-aa").close();

            diffs = fSyncService.compare(-1, "layer:/layer-to-base", -1, "main:/base", null);
            assertEquals("[layer:/layer-to-base/d-a/f-aa[-1] > main:/base/d-a/f-aa[-1]]", diffs.toString());
            assertEquals(1, diffs.size());

            // update main from layer
            fSyncService.update(diffs, null, false, false, false, false, null, null);

            diffs = fSyncService.compare(-1, "layer:/layer-to-base", -1, "main:/base", null);
            assertEquals(0, diffs.size());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
    }

    public void testSimpleUpdateLD3() throws Throwable
    {
        try
        {
            fService.createDirectory("main:/", "base");
            
            fService.createLayeredDirectory("main:/base", "layer:/", "layer-to-base");
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/layer-to-base", -1, "main:/base", null);
            assertEquals(0, diffs.size());
            
            // create directory d-a and file f-aa in layer
            fService.createDirectory("layer:/layer-to-base", "d-a");
            fService.createFile("layer:/layer-to-base/d-a", "f-aa").close();

            diffs = fSyncService.compare(-1, "layer:/layer-to-base", -1, "main:/base", null);
            assertEquals("[layer:/layer-to-base/d-a[-1] > "+"main:/base/d-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());

            // update main from layer
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            
            diffs = fSyncService.compare(-1, "layer:/layer-to-base", -1, "main:/base", null);
            assertEquals(0, diffs.size());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
    } 
    
    public void testSimpleUpdateLD4() throws Exception
    {
        try
        {
            fService.createLayeredDirectory("main:/", "layer:/", "layer");
            
            // create directory b and file foo in layer
            fService.createDirectory("layer:/layer", "b");
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
            
            fService.createStore("layer2");
            fService.createLayeredDirectory("layer:/layer", "layer2:/", "layer");
            
            // create directory c and file foo in layer2
            fService.createDirectory("layer2:/layer/", "c");
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
            
            recursiveList("main");
            recursiveList("layer");
            recursiveList("layer2");
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
    
    public void testSimpleUpdateLD5() throws Exception
    {
        Logger lg1 = null;
        Level l1 = null;
        
        Logger lg2 = null;
        Level l2 = null;
        
        try
        {
            // TEMP
            lg1 = Logger.getLogger("org.alfresco.repo.avm.AVMServiceLocalTest");
            l1 = lg1.getLevel();
            lg1.setLevel(Level.DEBUG);
            
            lg2 = Logger.getLogger("org.alfresco.repo.avm.OrphanReaper");
            l2 = lg2.getLevel();
            lg2.setLevel(Level.DEBUG);
            
            logger.debug("start: testSimpleUpdateLD5");
            
            logger.debug("created 2 stores: main, layer");
            
            recursiveList("main");
            recursiveList("layer");
            
            fService.createLayeredDirectory("main:/", "layer:/", "layer");
            
            logger.debug("created layered dir: layer:/layer -> main:/");
            
            recursiveList("main");
            recursiveList("layer");
            
            fService.createDirectory("layer:/layer", "b");
            
            logger.debug("created dir in layer: layer:/layer/b");
            
            recursiveList("main");
            recursiveList("layer");
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/layer/b[-1] > main:/b[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            
            logger.debug("updated: created directory: main:/b");
            
            recursiveList("main");
            recursiveList("layer");
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
            
            fSyncService.flatten("layer:/layer", "main:/");
            
            logger.debug("flattened: created directory: main:/b");
            
            recursiveList("main");
            recursiveList("layer");
            
            fService.createFile("layer:/layer/b", "foo").close();
            
            logger.debug("created file: layer:/layer/b/foo");
            
            recursiveList("main");
            recursiveList("layer");
            
            fService.createFile("layer:/layer/b", "bar").close();
            
            logger.debug("created file: layer:/layer/b/bar");
            
            recursiveList("main");
            recursiveList("layer");
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(2, diffs.size());
            assertEquals("[layer:/layer/b/bar[-1] > main:/b/bar[-1], layer:/layer/b/foo[-1] > main:/b/foo[-1]]", diffs.toString());
            
            // submit only first diff
            List<AVMDifference> selected = new ArrayList<AVMDifference>(1);
            selected.add(diffs.get(1));
            
            assertEquals("[layer:/layer/b/foo[-1] > main:/b/foo[-1]]", selected.toString());
            
            fSyncService.update(selected, null, false, false, false, false, null, null);
            
            logger.debug("updated: created file: main:/b/foo");
            
            recursiveList("main");
            recursiveList("layer");
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/layer/b/bar[-1] > main:/b/bar[-1]]", diffs.toString());
            
            fSyncService.flatten("layer:/layer", "main:/");
            
            logger.debug("flattened: created file: main:/b/foo");
            
            recursiveList("main");
            recursiveList("layer");
            
            fService.removeNode("layer:/layer", "b");
            
            logger.debug("removed dir in layer: layer:/layer/b");
            
            recursiveList("main");
            recursiveList("layer");
            
            fService.createSnapshot("layer", null, null);
            
            logger.debug("snapshot: layer");
            
            recursiveList("main");
            recursiveList("layer");
            
            // ETWOTWO-1266
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/layer/b[-1] > main:/b[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            
            logger.debug("updated: deleted dir: main:/b");
            
            recursiveList("main");
            recursiveList("layer");
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
            
            fSyncService.flatten("layer:/layer", "main:/");
            
            logger.debug("flattened: deleted dir: main:/b");
            
            recursiveList("main");
            recursiveList("layer");
            
            logger.debug("finish: testSimpleUpdateLD5");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            lg1.setLevel(l1);
            lg2.setLevel(l2);
        }
    }
    
    public void testSimpleUpdateLD6() throws Exception
    {
        try
        {
            logger.debug("created 2 stores: main, layer");
            
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            
            logger.debug("created dirs: main:/a, main:/a/b");
            
            fService.createLayeredDirectory("main:/", "layer:/", "layer");
            
            logger.debug("created layered dir: layer:/layer -> main:/");
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
            
            fService.createDirectory("layer:/layer/a", "xyz");
            fService.createFile("layer:/layer/a/xyz", "index.html").close();
            
            logger.debug("created: layer:/layer/a/xyz, layer:/layer/a/xyz/index.html");
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/layer/a/xyz[-1] > main:/a/xyz[-1]]", diffs.toString());
            
            fService.createStore("main--workflow1");
            
            logger.debug("created store: main--workflow1");
            
            fService.createLayeredDirectory("main:/a", "main--workflow1:/", "a");
            
            logger.debug("created layered dir: main--workflow1:/a -> main:/a");
            
            diffs = fSyncService.compare(-1, "main--workflow1:/a", -1, "main:/a", null);
            assertEquals(0, diffs.size());
            
            diffs = new ArrayList<AVMDifference>(1);
            diffs.add(new AVMDifference(-1, "layer:/layer/a/xyz/index.html", -1, "main--workflow1:/a/xyz/index.html", AVMDifference.NEWER));
            diffs.add(new AVMDifference(-1, "layer:/layer/a/xyz", -1, "main--workflow1:/a/xyz", AVMDifference.NEWER));
            
            // ETHREEOH-2057
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            
            logger.debug("updated: added: main--workflow1:/a/xyz,  main--workflow1:/a/xyz/index.html");
            
            diffs = fSyncService.compare(-1, "layer:/layer/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/layer/a/xyz[-1] > main:/a/xyz[-1]]", diffs.toString());
            
            diffs = fSyncService.compare(-1, "main--workflow1:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[main--workflow1:/a/xyz[-1] > main:/a/xyz[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, true, true, "two", "two");
            fSyncService.flatten("main--workflow1:/a", "main:/a");
            
            logger.debug("updated & flattened");
            
            diffs = fSyncService.compare(-1, "main--workflow1:/a", -1, "main:/a", null);
            assertEquals(0, diffs.size());
            
            diffs = fSyncService.compare(-1, "main--workflow1:/a", -1, "layer:/layer/a", null);
            assertEquals(0, diffs.size());
            
            recursiveList("main");
            recursiveList("layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            if (fService.getStore("main--workflow1") != null) { fService.purgeStore("main--workflow1"); }
        }
    }
    
    public void testDeleteLD1() throws Exception
    {
        try
        {
            logger.debug("created 2 stores: main, layer");
            
            fService.createDirectory("main:/", "a");
            
            OutputStream o = fService.createFile("main:/a", "foo");
            PrintStream out = new PrintStream(o);
            out.println("I am main:/a/foo");
            out.close();
            
            logger.debug("created file in main: main:/a/foo");
            
            fService.createLayeredDirectory("main:/a", "layer:/", "a");
            
            logger.debug("created layered dir: layer:/a -> main:/a");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "layer:/a/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/foo", line);
            
            out = new PrintStream(fService.getFileOutputStream("layer:/a/foo"));
            out.println("I am layer:/a/foo");
            out.close();
            
            logger.debug("update file in layer: layer:/a/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "layer:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am layer:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/foo", line);
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/a/foo[-1] > main:/a/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            
            diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(0, diffs.size());
            
            fSyncService.flatten("layer:/a", "main:/a");
            
            logger.debug("updated & flattened: updated file: main:/a/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "layer:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am layer:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am layer:/a/foo", line);
            
            fService.removeNode("main:/", "a");
            fService.createSnapshot("main", null, null);
            
            logger.debug("remove directory in main & snapshot: main:/a");
            
            assertNotNull(fService.lookup(-1, "layer:/a"));
            assertNull(fService.lookup(-1, "layer:/a/foo"));
            
            try 
            {
                fService.getFileInputStream(-1, "layer:/a/foo");
                fail();
            }
            catch (AVMNotFoundException nfe)
            {
                // expected
            }
            
            try 
            {
                fService.getFileOutputStream("layer:/a/foo");
                fail();
            }
            catch (AVMNotFoundException nfe)
            {
                // expected
            }
            
            recursiveList("main");
            recursiveList("layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
    
    public void testDeleteLD2() throws Exception
    {
        try
        {
            logger.debug("created 2 stores: main, layer");
            
            fService.createDirectory("main:/", "a");
            
            OutputStream o = fService.createFile("main:/a", "foo");
            PrintStream out = new PrintStream(o);
            out.println("I am main:/a/foo");
            out.close();
            
            logger.debug("created file in main: main:/a/foo");
            
            fService.createLayeredDirectory("main:/a", "layer:/", "a");
            
            logger.debug("created layered dir: layer:/a -> main:/a");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "layer:/a/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/foo", line);
            
            out = new PrintStream(fService.getFileOutputStream("layer:/a/foo"));
            out.println("I am layer:/a/foo");
            out.close();
            
            logger.debug("update file in layer: layer:/a/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "layer:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am layer:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/foo", line);
            
            fService.removeNode("main:/", "a");
            
            logger.debug("remove directory in main: main:/a");
            
            fService.createSnapshot("main", null, null);
            
            logger.debug("snapshot: main:/a");
            
            assertNull(fService.lookup(-1, "main:/a"));
            assertNull(fService.lookup(-1, "main:/a/foo"));
            
            try 
            {
                fService.getFileInputStream(-1, "main:/a/foo");
                fail();
            }
            catch (AVMNotFoundException nfe)
            {
                // expected
            }
            
            assertNotNull(fService.lookup(-1, "layer:/a"));
            assertNotNull(fService.lookup(-1, "layer:/a/foo"));
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "layer:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am layer:/a/foo", line);
            
            out = new PrintStream(fService.getFileOutputStream("layer:/a/foo"));
            out.println("I am layer:/a/foo V2");
            out.close();
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/a[-1] > main:/a[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            
            diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(0, diffs.size());
            
            logger.debug("updated: updated dir & file: main:/a/foo");
            
            fSyncService.flatten("layer:/a", "main:/a");
            
            logger.debug("flattened: layer:/a -> main:/a");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "layer:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am layer:/a/foo V2", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am layer:/a/foo V2", line);
            
            recursiveList("main");
            recursiveList("layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
    
    /**
     * Test bulk update (using layered directory).
     */
    public void testBulkUpdateLD() throws Exception
    {
        //String LOAD_DIR = "config/alfresco/bootstrap";
        String LOAD_DIR = "source/java/org/alfresco/repo/avm/actions";
        
        String[] split = LOAD_DIR.split("/");
        String DIR = split[split.length-1];
        
        try
        {
            RemoteBulkLoader loader = new RemoteBulkLoader();
            loader.setAvmRemoteService(fService);

            fService.createLayeredDirectory("main:/", "layer:/", "layer");
            
            loader.recursiveLoad(LOAD_DIR, "layer:/layer");
            
            
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/layer/"+DIR+"[-1] > main:/"+DIR+"[-1]]", diffs.toString());
            
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
            
            loader.recursiveLoad(LOAD_DIR, "layer2:/layer/"+DIR);
            
            fService.createSnapshot("layer2", null, null);
            diffs = fSyncService.compare(-1, "layer2:/layer", -1, "layer:/layer", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer2:/layer/"+DIR+"/"+DIR+"[-1] > layer:/layer/"+DIR+"/"+DIR+"[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            diffs = fSyncService.compare(-1, "layer2:/layer", -1, "layer:/layer", null);
            assertEquals(0, diffs.size());
            
            fSyncService.flatten("layer2:/layer", "layer:/layer");
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            assertEquals("[layer:/layer/"+DIR+"/"+DIR+"[-1] > main:/"+DIR+"/"+DIR+"[-1]]", diffs.toString());
            
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
            if (fService.getStore("layer2") != null) { fService.purgeStore("layer2"); }
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
            // Compare again.
            diffs = fSyncService.compare(-1, "main:/layer", -1, "main:/a", null);
            assertEquals(0, diffs.size());
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
    
    public void testRename1() throws Exception
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
    
    public void testRename2() throws Exception
    {
        String fileLower = "foo";
        String fileUpper = "FOO";
        
        try
        {
            logger.debug("created 2 stores: main, layer");

            fService.createDirectory("main:/", "a");
            fService.createFile("main:/a", fileLower);
            
            logger.debug("created: main:/a/"+fileLower);

            AVMNodeDescriptor desc = fService.lookup(-1, "main:/a/"+fileLower);
            assertNotNull(desc);
            assertEquals("main:/a/"+fileLower, desc.getPath());
            
            fService.createLayeredDirectory("main:/a", "layer:/", "a");
            
            logger.debug("created: layer:/a/"+fileLower+" -> main:/a/"+fileLower);

            assertNotNull(fService.lookup(-1, "layer:/a/"+fileLower));
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(0, diffs.size());
            
            fService.rename("layer:/a/", fileLower, "layer:/a", fileUpper);
            
            logger.debug("rename: layer:/a/"+fileLower+" -> layer:/a/"+fileUpper);

            diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals("[layer:/a/"+fileUpper+"[-1] > main:/a/"+fileUpper+"[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            
            logger.debug("update: layer:/a/"+fileUpper+" -> main:/a/"+fileUpper);

            diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(0, diffs.size());
            
            fSyncService.flatten("layer:/a", "main:/a");
            
            logger.debug("flatten: layer:/a -> main:/a");
            
            desc = fService.lookup(-1, "main:/a/"+fileLower);
            assertNotNull(desc);
            assertEquals("main:/a/"+fileUpper, desc.getPath());
            
            desc = fService.lookup(-1, "main:/a/"+fileUpper);
            assertNotNull(desc);
            assertEquals("main:/a/"+fileUpper, desc.getPath());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }
    
    public void testRename3() throws Exception
    {
        try
        {
            logger.debug("created 2 stores: main, layer");
            
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            
            logger.debug("created: main:/a/b/c");
            
            fService.createLayeredDirectory("main:/a", "layer:/", "a");
            
            logger.debug("created: layer:/a -> main:/a");
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(0, diffs.size());
            
            AVMNodeDescriptor desc = fService.lookup(-1, "main:/a/b");
            assertNotNull(desc);
            assertEquals("main:/a/b", desc.getPath());
            
            desc = fService.lookup(-1, "main:/a/B");
            assertNotNull(desc);
            assertEquals("main:/a/b", desc.getPath());
            
            desc = fService.lookup(-1, "layer:/a/b");
            assertNotNull(desc);
            assertEquals("layer:/a/b", desc.getPath());
            
            fService.rename("layer:/a/", "b", "layer:/a", "B");
            
            logger.debug("rename: layer:/a/b -> layer:/a/B");
            
            desc = fService.lookup(-1, "main:/a/b");
            assertNotNull(desc);
            assertEquals("main:/a/b", desc.getPath());
            
            desc = fService.lookup(-1, "layer:/a/B");
            assertNotNull(desc);
            assertEquals("layer:/a/B", desc.getPath());
            
            desc = fService.lookup(-1, "layer:/a/b");
            assertNotNull(desc);
            assertEquals("layer:/a/B", desc.getPath());
            
            diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals("[layer:/a/B[-1] > main:/a/B[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            
            logger.debug("update: layer:/a/B -> main:/a/B");
            
            diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(0, diffs.size());
            
            fSyncService.flatten("layer:/a", "main:/a");
            
            logger.debug("flatten: layer:/a -> main:/a");
            
            desc = fService.lookup(-1, "main:/a/b");
            assertNotNull(desc);
            assertEquals("main:/a/B", desc.getPath());
            
            desc = fService.lookup(-1, "main:/a/B");
            assertNotNull(desc);
            assertEquals("main:/a/B", desc.getPath());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }
    
    
    /**
     * Test file properties update ...
     */
    public void testUpdateFileTitleAndDescription() throws Exception
    {
        try
        {
            fService.createLayeredDirectory("main:/", "layer:/", "layer");
            fService.createDirectory("layer:/layer", "b");
            fService.createFile("layer:/layer/b", "foo").close();
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals("[layer:/layer/b[-1] > main:/b[-1]]", diffs.toString());
            
            fService.createSnapshot("layer", null, null);
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.createSnapshot("main", null, null);
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
            
            fSyncService.flatten("layer:/layer", "main:/");
            
            assertEquals(0, fService.getNodeProperties(-1, "main:/b/foo").size());
            assertEquals(0, fService.getNodeProperties(-1, "layer:/layer/b/foo").size());
            
            Map<QName, PropertyValue> properties = new HashMap<QName, PropertyValue>();
            properties.put(ContentModel.PROP_TITLE, new PropertyValue(DataTypeDefinition.TEXT, "foo title"));
            properties.put(ContentModel.PROP_DESCRIPTION, new PropertyValue(DataTypeDefinition.TEXT, "foo descrip"));
            fService.setNodeProperties("layer:/layer/b/foo", properties);
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals("[layer:/layer/b/foo[-1] > main:/b/foo[-1]]", diffs.toString());
            
            fService.createSnapshot("layer", null, null);
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.createSnapshot("main", null, null);
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
            
            fSyncService.flatten("layer:/layer", "main:/");
            
            assertEquals(2, fService.getNodeProperties(-1, "main:/b/foo").size());
            
            assertEquals("foo title", fService.getNodeProperty(-1, "main:/b/foo", ContentModel.PROP_TITLE).getStringValue());
            assertEquals("foo descrip", fService.getNodeProperty(-1, "main:/b/foo", ContentModel.PROP_DESCRIPTION).getStringValue());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
    
    /**
     * Test directory properties update ...
     */
    public void testUpdateDirectoryTitleAndDescription() throws Exception
    {
        try
        {
            fService.createLayeredDirectory("main:/", "layer:/", "layer");
            fService.createDirectory("layer:/layer", "b");
            fService.createFile("layer:/layer/b", "foo").close();
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals("[layer:/layer/b[-1] > main:/b[-1]]", diffs.toString());
            
            fService.createSnapshot("layer", null, null);
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.createSnapshot("main", null, null);
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
            
            fSyncService.flatten("layer:/layer", "main:/");
            
            assertEquals(0, fService.getNodeProperties(-1, "main:/b").size());
            assertEquals(0, fService.getNodeProperties(-1, "layer:/layer/b").size());
            
            Map<QName, PropertyValue> properties = new HashMap<QName, PropertyValue>();
            properties.put(ContentModel.PROP_TITLE, new PropertyValue(DataTypeDefinition.TEXT, "b title"));
            properties.put(ContentModel.PROP_DESCRIPTION, new PropertyValue(DataTypeDefinition.TEXT, "b descrip"));
            fService.setNodeProperties("layer:/layer/b", properties);
            
            assertEquals(0, fService.getNodeProperties(-1, "main:/b").size());
            assertEquals(2, fService.getNodeProperties(-1, "layer:/layer/b").size());
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals("[layer:/layer/b[-1] > main:/b[-1]]", diffs.toString());
            
            fService.createSnapshot("layer", null, null);
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.createSnapshot("main", null, null);
            
            assertEquals(2, fService.getNodeProperties(-1, "main:/b").size());
            assertEquals(2, fService.getNodeProperties(-1, "layer:/layer/b").size());
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
            
            fSyncService.flatten("layer:/layer", "main:/");
            
            assertEquals(2, fService.getNodeProperties(-1, "main:/b").size());
            assertEquals(2, fService.getNodeProperties(-1, "layer:/layer/b").size());
            
            assertEquals("b title", fService.getNodeProperty(-1, "main:/b", ContentModel.PROP_TITLE).getStringValue());
            assertEquals("b descrip", fService.getNodeProperty(-1, "main:/b", ContentModel.PROP_DESCRIPTION).getStringValue());
            
            fService.setNodeProperty("layer:/layer/b", ContentModel.PROP_TITLE, new PropertyValue(DataTypeDefinition.TEXT, "b title2"));
            fService.setNodeProperty("layer:/layer/b", ContentModel.PROP_DESCRIPTION, new PropertyValue(DataTypeDefinition.TEXT, "b descrip2"));
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals("[layer:/layer/b[-1] > main:/b[-1]]", diffs.toString());
            
            fService.createSnapshot("layer", null, null);
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.createSnapshot("main", null, null);
            
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
            
            fSyncService.flatten("layer:/layer", "main:/");
            
            assertEquals(2, fService.getNodeProperties(-1, "main:/b").size());
            assertEquals(2, fService.getNodeProperties(-1, "layer:/layer/b").size());
            
            assertEquals("b title2", fService.getNodeProperty(-1, "main:/b", ContentModel.PROP_TITLE).getStringValue());
            assertEquals("b descrip2", fService.getNodeProperty(-1, "main:/b", ContentModel.PROP_DESCRIPTION).getStringValue());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
        
    public void testSimpleUpdateLF1() throws Exception
    {
        try
        {
            List<VersionDescriptor> snapshots = fService.getStoreVersions("main");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            snapshots = fService.getStoreVersions("layer");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            fService.createDirectory("main:/", "a");
            fService.createDirectory("layer:/", "a");
            
            logger.debug("created 2 plain dirs: main:/a, layer:/a");
            
            fService.createFile("main:/a", "foo");
            
            assertEquals(1, fService.lookup(-1, "main:/a/foo").getVersionID());
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("main:/a/foo"));
            out.println("I am main:/a/foo");
            out.close();
            
            AVMNodeDescriptor node = fService.lookup(-1, "main:/a/foo");
            assertEquals(1, node.getVersionID());
            List<AVMNodeDescriptor> history = fService.getHistory(node, -1);
            assertEquals(0, history.size());
            
            fService.createSnapshot("main", null, null);
            
            snapshots = fService.getStoreVersions("main");
            assertEquals(2, snapshots.size());
            assertEquals(1, snapshots.get(snapshots.size()-1).getVersionID());
            
            snapshots = fService.getStoreVersions("layer");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            assertEquals(1, fService.lookup(-1, "main:/a/foo").getVersionID());
            assertEquals(1, fService.lookup(1, "main:/a/foo").getVersionID());
            
            logger.debug("created plain file: main:/a/foo");
            
            fService.createLayeredFile("main:/a/foo", "layer:/a", "foo");
            
            assertEquals(1, fService.lookup(-1, "layer:/a/foo").getVersionID());
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "layer:/a/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/foo", line);
            
            node = fService.lookup(-1, "layer:/a/foo");
            assertEquals(1, node.getVersionID());
            
            history = fService.getHistory(node, -1);
            assertEquals(0, history.size());
            
            fService.createSnapshot("layer", null, null);
            
            snapshots = fService.getStoreVersions("main");
            assertEquals(2, snapshots.size());
            assertEquals(1, snapshots.get(snapshots.size()-1).getVersionID());
            
            snapshots = fService.getStoreVersions("layer");
            assertEquals(2, snapshots.size());
            assertEquals(1, snapshots.get(snapshots.size()-1).getVersionID());
            
            assertEquals(1, fService.lookup(-1, "layer:/a/foo").getVersionID());
            assertEquals(1, fService.lookup(1, "layer:/a/foo").getVersionID());
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(0, diffs.size());
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "layer:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/foo", line);
            
            logger.debug("created layered file: layer:/a/foo -> main:/a/foo");
            
            out = new PrintStream(fService.getFileOutputStream("layer:/a/foo"));
            out.println("I am layer:/a/foo");
            out.close();
            
            logger.debug("modified file: layer:/a/foo");
            
            assertEquals(2, fService.lookup(-1, "layer:/a/foo").getVersionID());
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/foo", line);
            
            diffs = fSyncService.compare(-1, "layer:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            
            // TODO - review behaviour
            assertEquals("[layer:/a/foo[-1] > main:/a/foo[-1]]", diffs.toString());
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            
            // update will implicitly snapshot (src and dst)
            snapshots = fService.getStoreVersions("main");
            assertEquals(3, snapshots.size());
            assertEquals(2, snapshots.get(snapshots.size()-1).getVersionID());
            
            snapshots = fService.getStoreVersions("layer");
            assertEquals(3, snapshots.size());
            assertEquals(2, snapshots.get(snapshots.size()-1).getVersionID());
            
            node = fService.lookup(-1, "layer:/a/foo");
            assertEquals(2, node.getVersionID());
            history = fService.getHistory(node, -1);
            
            assertEquals(1, history.size());
            assertEquals(1, history.get(0).getVersionID());
            
            assertEquals(1, fService.lookup(1, "layer:/a/foo").getVersionID());
            assertEquals(2, fService.lookup(2, "layer:/a/foo").getVersionID());
            
            logger.debug("submitted/updated file: layer:/a/foo -> main:/a/foo");
            
            fSyncService.flatten("layer:/a", "main:/a");
            
            logger.debug("flatten dir: layer:/a -> main:/a");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "layer:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am layer:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am layer:/a/foo", line);
            
            snapshots = fService.getStoreVersions("main");
            assertEquals(3, snapshots.size());
            assertEquals(2, snapshots.get(snapshots.size()-1).getVersionID());
            
            snapshots = fService.getStoreVersions("layer");
            assertEquals(3, snapshots.size());
            assertEquals(2, snapshots.get(snapshots.size()-1).getVersionID());
            
            recursiveList("main");
            recursiveList("layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
    
    public void testSimpleUpdateLF2() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            fService.createStore("mainB--layer");
            
            List<VersionDescriptor> snapshots = fService.getStoreVersions("mainA");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            snapshots = fService.getStoreVersions("mainB");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            snapshots = fService.getStoreVersions("mainB--layer");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            logger.debug("created 3 stores: mainA, mainB, mainB-layer");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainB:/", "a");
            
            logger.debug("created 2 plain dirs: mainA:/a, mainB:/a");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            logger.debug("created layered dir: mainB--layer:/a -> mainB:/a");
            
            // note: unlike WCM, edit staging directly (ie. don't bother with mainA--layer for now)
            fService.createFile("mainA:/a", "foo");
            
            assertEquals(1, fService.lookup(-1, "mainA:/a/foo").getVersionID());
            assertNull(fService.lookup(-1, "mainB:/a/foo"));
            assertNull(fService.lookup(-1, "mainB--layer:/a/foo"));
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainA:/a/foo"));
            out.println("I am mainA:/a/foo");
            out.close();
            
            logger.debug("created plain file: mainA:/a/foo");
            
            fService.createSnapshot("mainA", null, null);
            
            assertEquals(1, fService.lookup(-1, "mainA:/a/foo").getVersionID());
            assertNull(fService.lookup(-1, "mainB:/a/foo"));
            assertNull(fService.lookup(-1, "mainB--layer:/a/foo"));
            
            snapshots = fService.getStoreVersions("mainA");
            assertEquals(2, snapshots.size());
            assertEquals(1, snapshots.get(snapshots.size()-1).getVersionID());
            
            logger.debug("created snapshot: mainA");
            
            // note: WCM does not expose layered file (between web project staging sandboxes)
            fService.createLayeredFile("mainA:/a/foo", "mainB:/a", "foo");
            
            assertEquals(1, fService.lookup(-1, "mainA:/a/foo").getVersionID());
            assertEquals(1, fService.lookup(-1, "mainB:/a/foo").getVersionID());
            assertEquals(1, fService.lookup(-1, "mainB--layer:/a/foo").getVersionID());
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            logger.debug("created layered file: mainB:/a/foo -> mainA:/a/foo");
            
            // modify file in user's sandbox
            out = new PrintStream(fService.getFileOutputStream("mainB--layer:/a/foo"));
            out.println("I am mainB--layer:/a/foo");
            out.close();
            
            assertEquals(1, fService.lookup(-1, "mainA:/a/foo").getVersionID());
            assertEquals(1, fService.lookup(-1, "mainB:/a/foo").getVersionID());
            assertEquals(2, fService.lookup(-1, "mainB--layer:/a/foo").getVersionID());
            
            logger.debug("modified file: mainB--layer:/a/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            
            // TODO - review behaviour
            assertEquals("[mainB--layer:/a/foo[-1] > mainB:/a/foo[-1]]", diffs.toString());
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            assertEquals(1, fService.lookup(-1, "mainA:/a/foo").getVersionID());
            assertEquals(2, fService.lookup(-1, "mainB:/a/foo").getVersionID());
            assertEquals(2, fService.lookup(-1, "mainB--layer:/a/foo").getVersionID());
            
            snapshots = fService.getStoreVersions("mainB--layer");
            assertEquals(2, snapshots.size());
            assertEquals(1, snapshots.get(snapshots.size()-1).getVersionID());
            
            snapshots = fService.getStoreVersions("mainB");
            assertEquals(3, snapshots.size());
            assertEquals(2, snapshots.get(snapshots.size()-1).getVersionID());
            
            logger.debug("submit/update file: mainB--layer:/a/foo -> mainB:/a/foo");
            
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("flatten dir: mainB--layer:/a/foo -> mainB:/a/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
        }
    }
    
    public void testSimpleUpdateLF3() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            fService.createStore("mainB--layer");
            
            List<VersionDescriptor> snapshots = fService.getStoreVersions("mainA");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            snapshots = fService.getStoreVersions("mainB");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            snapshots = fService.getStoreVersions("mainB--layer");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            logger.debug("created 3 stores: mainA, mainB, mainB-layer");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainB:/", "a");
            
            logger.debug("created 2 plain dirs: mainA:/a, mainB:/a");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            logger.debug("created layered dir: mainB--layer:/a -> mainB:/a");
            
            // note: unlike WCM, edit staging directly (ie. don't bother with mainA--layer for now)
            fService.createFile("mainA:/a", "foo");
            
            assertEquals(1, fService.lookup(-1, "mainA:/a/foo").getVersionID());
            assertNull(fService.lookup(-1, "mainB:/a/foo"));
            assertNull(fService.lookup(-1, "mainB--layer:/a/foo"));
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainA:/a/foo"));
            out.println("I am mainA:/a/foo");
            out.close();
            
            logger.debug("created plain file: mainA:/a/foo");
            
            fService.createSnapshot("mainA", null, null);
            
            assertEquals(1, fService.lookup(-1, "mainA:/a/foo").getVersionID());
            assertNull(fService.lookup(-1, "mainB:/a/foo"));
            assertNull(fService.lookup(-1, "mainB--layer:/a/foo"));
            
            snapshots = fService.getStoreVersions("mainA");
            assertEquals(2, snapshots.size());
            assertEquals(1, snapshots.get(snapshots.size()-1).getVersionID());
            
            logger.debug("created snapshot: mainA");
            
            // note: WCM does not expose layered file (between web project staging sandboxes)
            fService.createLayeredFile("mainA:/a/foo", "mainB:/a", "foo");
            
            assertEquals(1, fService.lookup(-1, "mainA:/a/foo").getVersionID());
            assertEquals(1, fService.lookup(-1, "mainB:/a/foo").getVersionID());
            
            AVMNodeDescriptor foo = fService.lookup(-1, "mainB--layer:/a/foo");
            assertEquals(1, foo.getVersionID());
            assertTrue(foo.isLayeredFile());
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            logger.debug("created layered file: mainB:/a/foo -> mainA:/a/foo");
            
            // add dir in user's sandbox
            fService.createDirectory("mainB--layer:/a", "b");
            
            assertEquals(1, fService.lookup(-1, "mainA:/a/foo").getVersionID());
            assertEquals(1, fService.lookup(-1, "mainB:/a/foo").getVersionID());
            assertEquals(1, fService.lookup(-1, "mainB--layer:/a/b").getVersionID());
            
            foo = fService.lookup(-1, "mainB--layer:/a/foo");
            assertEquals(1, foo.getVersionID());
            assertTrue(foo.isLayeredFile());
            
            logger.debug("created dir: mainB--layer:/a/b");
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            
            foo = fService.lookup(-1, "mainB--layer:/a/foo");
            assertTrue(foo.isLayeredFile());
            
            assertEquals("[mainB--layer:/a/b[-1] > mainB:/a/b[-1]]", diffs.toString());
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            
            foo = fService.lookup(-1, "mainB--layer:/a/foo");
            assertTrue(foo.isLayeredFile());
            
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            foo = fService.lookup(-1, "mainB--layer:/a/foo");
            assertTrue(foo.isLayeredFile());
            
            assertEquals(1, fService.lookup(-1, "mainA:/a/foo").getVersionID());
            assertEquals(2, fService.lookup(-1, "mainB:/a/foo").getVersionID());
            assertEquals(2, fService.lookup(-1, "mainB--layer:/a/foo").getVersionID());
            
            snapshots = fService.getStoreVersions("mainB--layer");
            assertEquals(2, snapshots.size());
            assertEquals(1, snapshots.get(snapshots.size()-1).getVersionID());
            
            snapshots = fService.getStoreVersions("mainB");
            assertEquals(3, snapshots.size());
            assertEquals(2, snapshots.get(snapshots.size()-1).getVersionID());
            
            logger.debug("submitted dir: mainB--layer:/a/b -> mainB:/a/b");
            
            foo = fService.lookup(-1, "mainB--layer:/a/foo");
            assertEquals(2, foo.getVersionID());
            assertTrue(foo.isLayeredFile());
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
        }
    }
    
    public void testLayeredFolder1() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainA:/a", "b");
            
            fService.createDirectory("mainB:/", "a");
            
            fService.createStore("mainB--layer");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            // note: short-cut - created directly in "staging" area (don't bother with sandbox mainA--layer for now)
            fService.createFile("mainA:/a/b", "foo");
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainA:/a/b/foo"));
            out.println("I am mainA:/a/b/foo");
            out.close();
            
            logger.debug("created file: mainA:/a/b/foo");
            
            // create equivalent of WCM layered folder between web project staging sandboxes (mainB:/a/b pointing to mainA:/a/b)
            fService.createLayeredDirectory("mainA:/a/b", "mainB:/a", "b");
            
            fService.createSnapshot("mainA", null, null);
            fService.createSnapshot("mainB", null, null);
            
            assertTrue(fService.lookup(-1, "mainB--layer:/a/b").isLayeredDirectory());
            
            logger.debug("created layered directory: mainB:/a/b -> mainA:/a/b");
            
            fService.createDirectory("mainB--layer:/a", "c");
            
            fService.createSnapshot("mainB--layer", null, null);
            
            logger.debug("created dir: mainB--layer:/a/c");
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/c[-1] > mainB:/a/c[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated: created dir: mainB:/a/c");
            
            assertTrue(fService.lookup(-1, "mainB--layer:/a/b").isLayeredDirectory());
            
            fService.createDirectory("mainB--layer:/a/b", "c");
            
            assertTrue(fService.lookup(-1, "mainB--layer:/a/b").isLayeredDirectory());
            
            fService.createSnapshot("mainB--layer", null, null);
            
            logger.debug("created dir: mainB--layer:/a/b/c");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/c[-1] > mainB:/a/b/c[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated: created dir: mainB:/a/b/c");
            
            assertTrue(fService.lookup(-1, "mainB--layer:/a/b").isLayeredDirectory());
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
        }
    }
    
    public void testLayeredFolder2() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainA:/a", "b");
            
            fService.createDirectory("mainB:/", "a");
            
            fService.createStore("mainB--layer");
            
            List<VersionDescriptor> snapshots = fService.getStoreVersions("mainA");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            snapshots = fService.getStoreVersions("mainB");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            snapshots = fService.getStoreVersions("mainB--layer");
            assertEquals(1, snapshots.size());
            assertEquals(0, snapshots.get(0).getVersionID());
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            // note: short-cut - created directly in "staging" area (don't bother with sandbox mainA--layer for now)
            fService.createFile("mainA:/a/b", "foo");
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainA:/a/b/foo"));
            out.println("I am mainA:/a/b/foo");
            out.close();
            
            logger.debug("created file: mainA:/a/b/foo");
            
            // create equivalent of WCM layered folder between web project staging sandboxes (mainB:/a/b pointing to mainA:/a/b)
            fService.createLayeredDirectory("mainA:/a/b", "mainB:/a", "b");
            
            logger.debug("created layered directory: mainB:/a/b -> mainA:/a/b");
            
            fService.createFile("mainB--layer:/a/b", "bar");
            
            out = new PrintStream(fService.getFileOutputStream("mainB--layer:/a/b/bar"));
            out.println("I am mainB--layer:/a/b/bar");
            out.close();
            
            logger.debug("created file: mainB--layer:/a/b/bar");
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/bar[-1] > mainB:/a/b/bar[-1]]", diffs.toString());
            
            snapshots = fService.getStoreVersions("mainB");
            assertEquals(1, snapshots.size());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            
            snapshots = fService.getStoreVersions("mainB");
            assertEquals(3, snapshots.size());
            
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated: created file: mainB:/a/b/bar");
            
            snapshots = fService.getStoreVersions("mainB");
            assertEquals(3, snapshots.size());
            assertEquals(2, snapshots.get(snapshots.size()-1).getVersionID());
            
            // note: short-cut - created directly in "staging" area (don't bother with sandbox mainA--layer for now)
            fService.createFile("mainA:/a/b", "baz");
            
            out = new PrintStream(fService.getFileOutputStream("mainA:/a/b/baz"));
            out.println("I am mainA:/a/b/baz");
            out.close();
            
            logger.debug("created file: mainA:/a/b/baz");
            
            fService.createSnapshot("mainB", "two", "two");
            
            logger.debug("snapshot: mainB");
            
            snapshots = fService.getStoreVersions("mainB");
            assertEquals(4, snapshots.size());
            assertEquals(3, snapshots.get(snapshots.size()-1).getVersionID());
            
            // ETHREEOH-3340
            diffs = fSyncService.compare(2, "mainB:/a", 3, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB:/a/b/baz[2] < mainB:/a/b/baz[3]]", diffs.toString());
            
            logger.debug("list mainB [2]");
            
            recursiveList("mainB", 2);
            
            logger.debug("list mainB [3]");
            
            recursiveList("mainB", 3);
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
        }
    }
    
    public void testLayeredFolder3() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            fService.createStore("mainB--layer");
            
            logger.debug("created stores: mainA, mainB and mainB--layer");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainA:/a", "b");
            fService.createDirectory("mainB:/", "a");
            
            logger.debug("created directories: mainA:/a/b and mainB:/a");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            logger.debug("created layered directory: mainB--layer:/a -> mainB:/a");
            
            // create equivalent of WCM layered folder between web project staging sandboxes (mainB:/a/b pointing to mainA:/a/b)
            fService.createLayeredDirectory("mainA:/a/b", "mainB:/a", "b");
            
            logger.debug("created layered directory: mainB:/a/b -> mainA:/a/b");
            
            fService.createDirectory("mainB--layer:/a/b", "c");
            
            logger.debug("created directory: mainB--layer:/a/b/c");
            
            fService.createFile("mainB--layer:/a/b/c", "foo");
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainB--layer:/a/b/c/foo"));
            out.println("I am mainB--layer:/a/b/c/foo");
            out.close();
            
            logger.debug("created file: mainB--layer:/a/b/c/foo");
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/c[-1] > mainB:/a/b/c[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            
            logger.debug("updated: mainB--layer:/a/b/c (including 'foo') to mainB:/a/b/c");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(0, diffs.size());
            
            diffs = fSyncService.compare(-1, "mainB:/a", -1, "mainA:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB:/a/b/c[-1] > mainA:/a/b/c[-1]]", diffs.toString());
            
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("flattened: mainB--layer:/a to mainB:/a");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(0, diffs.size());
            
            diffs = fSyncService.compare(-1, "mainB:/a", -1, "mainA:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB:/a/b/c[-1] > mainA:/a/b/c[-1]]", diffs.toString());
            
            // ETHREEOH-3643
            out = new PrintStream(fService.getFileOutputStream("mainB--layer:/a/b/c/foo"));
            out.println("I am mainB--layer:/a/b/c/foo V2");
            out.close();
            
            logger.debug("updated file: mainB--layer:/a/b/c/foo");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/c/foo[-1] > mainB:/a/b/c/foo[-1]]", diffs.toString());
            
            logger.debug("updated: mainB:/a/b/c/foo");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/c/foo[-1] > mainB:/a/b/c/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "two", "two");
            
            logger.debug("updated: mainB--layer:/a/b/c/foo to mainB:/a/b/c/foo");
            
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("flattened: mainB--layer:/a to mainB:/a");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(0, diffs.size());
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
        }
    }
    
    public void testLayeredFolder4() throws Exception
    {
        Logger lg1 = null;
        Level l1 = null;
        
        Logger lg2 = null;
        Level l2 = null;
        
        try
        {
            // TEMP
            lg1 = Logger.getLogger("org.alfresco.repo.avm.AVMServiceLocalTest");
            l1 = lg1.getLevel();
            lg1.setLevel(Level.DEBUG);
            
            lg2 = Logger.getLogger("org.alfresco.repo.avm.OrphanReaper");
            l2 = lg2.getLevel();
            lg2.setLevel(Level.DEBUG);
            
            logger.debug("start: testLayeredFolder4");
            
            fService.createStore("mainA");
            fService.createStore("mainB");
            fService.createStore("mainB--layer");
            
            logger.debug("created stores: mainA, mainB and mainB--layer");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainA:/a", "b");
            fService.createDirectory("mainA:/a/b", "c");
            fService.createDirectory("mainB:/", "a");
            
            logger.debug("created directories: mainA:/a/b/c and mainB:/a");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            logger.debug("created layered directory: mainB--layer:/a -> mainB:/a");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            
            // create equivalent of WCM layered folder between web project staging sandboxes (mainB:/a/b pointing to mainA:/a/b)
            fService.createLayeredDirectory("mainA:/a/b", "mainB:/a", "b");
            
            logger.debug("created layered directory: mainB:/a/b -> mainA:/a/b");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            
            fService.createDirectory("mainB--layer:/a/b/c", "d");
            
            logger.debug("created directory: mainB--layer:/a/b/c/d");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/c/d[-1] > mainB:/a/b/c/d[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            
            logger.debug("updated: mainB--layer:/a/b/c/d to mainB:/a/b/c/d");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("flattened: mainB--layer:/a to mainB:/a");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            
            fService.createFile("mainB--layer:/a/b/c/d", "foo");
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainB--layer:/a/b/c/d/foo"));
            out.println("I am mainB--layer:/a/b/c/d/foo");
            out.close();
            
            logger.debug("created file: mainB--layer:/a/b/c/foo");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            
            fService.createStore("mainB--workflow1");
            
            logger.debug("created store: mainB--workflow1");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            recursiveList("mainB--workflow1");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--workflow1:/", "a");
            
            logger.debug("created layered dir: mainB--workflow1:/a -> mainB:/a");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            recursiveList("mainB--workflow1");
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a", -1, "mainB:/a", null);
            assertEquals(0, diffs.size());
            
            diffs = new ArrayList<AVMDifference>(1);
            diffs.add(new AVMDifference(-1, "mainB--layer:/a/b/c/d/foo", -1, "mainB--workflow1:/a/b/c/d/foo", AVMDifference.NEWER));
            
            assertNotNull(fService.lookup(-1, "mainB--workflow1:/a/b/c/d"));
            
            // ETHREEOH-3763
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            
            logger.debug("updated: added file: mainB--workflow1:/a/b/c/d/foo");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            recursiveList("mainB--workflow1");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals("[mainB--layer:/a/b/c/d/foo[-1] > mainB:/a/b/c/d/foo[-1]]", diffs.toString());
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a", -1, "mainB:/a", null);
            assertEquals("[mainB--workflow1:/a/b/c/d/foo[-1] > mainB:/a/b/c/d/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, true, true, "two", "two");
            
            logger.debug("updated: added file: mainB:/a/b/c/d/foo");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            recursiveList("mainB--workflow1");
            
            fSyncService.flatten("mainB--workflow1:/a", "mainB:/a");
            
            logger.debug("flattened: added file: mainB:/a/b/c/d/foo");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            recursiveList("mainB--workflow1");
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a", -1, "mainB:/a", null);
            assertEquals(0, diffs.size());
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a", -1, "mainB--layer:/a", null);
            assertEquals(0, diffs.size());
            
            logger.debug("finish: testLayeredFolder4");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
            if (fService.getStore("mainB--workflow1") != null) { fService.purgeStore("mainB--workflow1"); }
            
            lg1.setLevel(l1);
            lg2.setLevel(l2);
        }
    }
    
    public void testLayeredFolderDelete1() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainA:/a", "b");
            
            fService.createDirectory("mainB:/", "a");
            
            fService.createStore("mainB--layer");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            // note: short-cut - created directly in "staging" area (don't bother with sandbox mainA--layer for now)
            fService.createFile("mainA:/a/b", "foo");
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainA:/a/b/foo"));
            out.println("I am mainA:/a/b/foo");
            out.close();
            
            logger.debug("created file: mainA:/a/b/foo");
            
            // create equivalent of WCM layered folder between web project staging sandboxes (mainB:/a/b pointing to ,mainA:/a/b)
            fService.createLayeredDirectory("mainA:/a/b", "mainB:/a", "b");
            
            fService.createSnapshot("mainA", null, null);
            fService.createSnapshot("mainB", null, null);
            
            logger.debug("created layered directory: mainB:/a/b -> mainA:/a/b");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/b/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            out = new PrintStream(fService.getFileOutputStream("mainB--layer:/a/b/foo"));
            out.println("I am mainB--layer:/a/b/foo");
            out.close();
            
            fService.createSnapshot("mainB--layer", null, null);
            
            logger.debug("updated file: mainB--layer:/a/b/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo", line);
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/foo[-1] > mainB:/a/b/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated: created file: mainB:/a/b/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo", line);
            
            // delete file - note: short-cut - removed directly from "staging" area (don't bother with sandbox mainA--layer for now)
            fService.removeNode("mainA:/a/b", "foo");
            fService.createSnapshot("mainA", null, null);
            
            logger.debug("removed file & snapshot: mainA:/a/b/foo");
            
            // ETHREEOH-2297
            fService.removeNode("mainB--layer:/a/b", "foo");
            fService.createSnapshot("mainB--layer", null, null);
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/foo[-1] > mainB:/a/b/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            fService.createSnapshot("mainB", null, null);
            
            logger.debug("updated: removed file: mainB:/a/b/foo");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
        }
    }
    
    public void testLayeredFolderDelete2() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainA:/a", "b");
            
            fService.createDirectory("mainB:/", "a");
            
            fService.createStore("mainB--layer");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            // note: short-cut - created directly in "staging" area (don't bother with sandbox mainA--layer for now)
            fService.createFile("mainA:/a/b", "foo");
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainA:/a/b/foo"));
            out.println("I am mainA:/a/b/foo");
            out.close();
            
            logger.debug("created file: mainA:/a/b/foo");
            
            // create equivalent of WCM layered folder between web project staging sandboxes (mainB:/a/b pointing to ,mainA:/a/b)
            fService.createLayeredDirectory("mainA:/a/b", "mainB:/a", "b");
            
            fService.createSnapshot("mainA", null, null);
            fService.createSnapshot("mainB", null, null);
            
            logger.debug("created layered directory: mainB:/a/b -> mainA:/a/b");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/b/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            out = new PrintStream(fService.getFileOutputStream("mainB--layer:/a/b/foo"));
            out.println("I am mainB--layer:/a/b/foo");
            out.close();
            
            fService.createSnapshot("mainB--layer", null, null);
            
            logger.debug("updated file: mainB--layer:/a/b/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo", line);
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/foo[-1] > mainB:/a/b/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated: created file: mainB:/a/b/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo", line);
            
            // delete folder - note: short-cut - remove directly from "staging" area (don't bother with sandbox mainA--layer for now)
            fService.removeNode("mainA:/a", "b");
            fService.createSnapshot("mainA", null, null);
            
            logger.debug("removed folder & snapshot: mainA:/a/b");
            
            fService.removeNode("mainB--layer:/a/b", "foo");
            fService.createSnapshot("mainB--layer", null, null);
            
            logger.debug("removed file & snapshot: mainB--layer:/a/b/foo");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/foo[-1] > mainB:/a/b/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated & flattened: removed file: mainB:/a/b/foo");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
        }
    }
    
    public void testLayeredFolderDelete3() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainA:/a", "b");
            
            fService.createDirectory("mainB:/", "a");
            
            fService.createStore("mainB--layer");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            // note: short-cut - created directly in "staging" area (don't bother with sandbox mainA--layer for now)
            fService.createFile("mainA:/a/b", "foo");
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainA:/a/b/foo"));
            out.println("I am mainA:/a/b/foo");
            out.close();
            
            logger.debug("created file: mainA:/a/b/foo");
            
            // create equivalent of WCM layered folder between web project staging sandboxes (mainB:/a/b pointing to ,mainA:/a/b)
            fService.createLayeredDirectory("mainA:/a/b", "mainB:/a", "b");
            
            fService.createSnapshot("mainA", null, null);
            fService.createSnapshot("mainB", null, null);
            
            logger.debug("created layered directory: mainB:/a/b -> mainA:/a/b");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/b/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            out = new PrintStream(fService.getFileOutputStream("mainB--layer:/a/b/foo"));
            out.println("I am mainB--layer:/a/b/foo");
            out.close();
            
            logger.debug("updated file: mainB--layer:/a/b/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo", line);
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/foo[-1] > mainB:/a/b/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated: created file: mainB:/a/b/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo", line);
            
            // delete folder - note: short-cut - remove directly from "staging" area (don't bother with sandbox mainA--layer for now)
            fService.removeNode("mainA:/a", "b");
            fService.createSnapshot("mainA", null, null);
            
            logger.debug("removed folder & snapshot: mainA:/a/b");
            
            fService.createFile("mainB--layer:/a/b", "bar");
            
            out = new PrintStream(fService.getFileOutputStream("mainB--layer:/a/b/bar"));
            out.println("I am mainB--layer:/a/b/bar");
            out.close();
            
            logger.debug("created file: mainB--layer:/a/b/bar");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/bar[-1] > mainB:/a/b/bar[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "two", "two");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated & flattened: created file: mainB:/a/b/bar");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
        }
    }
    
    public void testLayeredFolderDelete4() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainA:/a", "b");
            
            fService.createDirectory("mainB:/", "a");
            
            fService.createStore("mainB--layer");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            // note: short-cut - created directly in "staging" area (don't bother with sandbox mainA--layer for now)
            fService.createFile("mainA:/a/b", "foo");
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainA:/a/b/foo"));
            out.println("I am mainA:/a/b/foo");
            out.close();
            
            logger.debug("created file: mainA:/a/b/foo");
            
            // create equivalent of WCM layered folder between web project staging sandboxes (mainB:/a/b pointing to ,mainA:/a/b)
            fService.createLayeredDirectory("mainA:/a/b", "mainB:/a", "b");
            
            fService.createSnapshot("mainA", null, null);
            fService.createSnapshot("mainB", null, null);
            
            logger.debug("created layered directory: mainB:/a/b -> mainA:/a/b");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/b/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            out = new PrintStream(fService.getFileOutputStream("mainB--layer:/a/b/foo"));
            out.println("I am mainB--layer:/a/b/foo");
            out.close();
            
            logger.debug("updated file: mainB--layer:/a/b/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo", line);
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/foo[-1] > mainB:/a/b/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated & flattened: updated file: mainB:/a/b/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo", line);
            
            // delete folder - note: short-cut - remove directly from "staging" area (don't bother with sandbox mainA--layer for now)
            fService.removeNode("mainA:/a", "b");
            fService.createSnapshot("mainA", null, null);
            
            logger.debug("removed folder & snapshot: mainA:/a/b");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo", line);
            
            out = new PrintStream(fService.getFileOutputStream("mainB--layer:/a/b/foo"));
            out.println("I am mainB--layer:/a/b/foo V2");
            out.close();
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo V2", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo", line);
            
            logger.debug("updated file: mainB--layer:/a/b/foo");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(1, diffs.size());
            assertEquals("[mainB--layer:/a/b/foo[-1] > mainB:/a/b/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "two", "two");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated & flattened: updated file: mainB:/a/b/foo");
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainB--layer:/a/b/foo V2", line);
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
        }
    }
    
    public void testLayeredFileDeleteFile1() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            fService.createStore("mainB--layer");
            
            logger.debug("created 3 stores: mainA, mainB, mainB--layer");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainB:/", "a");
            
            logger.debug("created 2 plain dirs: mainA:/a and mainB:/a");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            logger.debug("created layered dir: mainB--layer:/a -> mainB:/a");
            
            // note: short-cut - created directly in "staging" area (don't bother with sandbox mainA--layer for now)
            fService.createFile("mainA:/a", "foo");
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainA:/a/foo"));
            out.println("I am mainA:/a/foo");
            out.close();
            
            logger.debug("created file: mainA:/a/foo");
            
            // create equivalent of WCM layered folder between web project staging sandboxes (mainB:/a/b pointing to ,mainA:/a/b)
            fService.createLayeredFile("mainA:/a/foo", "mainB:/a", "foo");
            
            logger.debug("created layered file: mainB:/a/foo -> mainA:/a/foo");
            
            fService.createSnapshot("mainA", null, null);
            fService.createSnapshot("mainB", null, null);
            
            logger.debug("created 2 snapshots: mainA and mainB");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(0, diffs.size());
            
            fService.removeNode("mainB--layer:/a", "foo");
            fService.createSnapshot("mainB--layer", null, null);
            
            logger.debug("removed file & snapshot: mainB--layer:/a/foo");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals("[mainB--layer:/a/foo[-1] > mainB:/a/foo[-1]]", diffs.toString());
            
            /*
            // For testing ALF-4800 (testLayeredFileDelete1 / testLayeredFileDelete2 - error in dispatchUpdate)
            // In addition to sleep below, increase frequency of OrphanReaper (eg. set startDelay/repeatInterval to 2000/500 msecs)
            logger.debug("start sleep");
            Thread.sleep(30000);
            logger.debug("finish sleep");
            */
            
            // ETHREEOH-2844
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated & flattened: removed file: mainB:/a/foo");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
        }
    }
    
    public void testLayeredFileDeleteFile2() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            fService.createStore("mainB--layer");
            
            logger.debug("created 3 stores: mainA, mainB, mainB--layer");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainB:/", "a");
            
            logger.debug("created 2 plain dirs: mainA:/a and mainB:/a");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            logger.debug("created layered dir: mainB--layer:/a -> mainB:/a");
            
            // note: short-cut - created directly in "staging" area (don't bother with sandbox mainA--layer for now)
            fService.createFile("mainA:/a", "foo");
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainA:/a/foo"));
            out.println("I am mainA:/a/foo");
            out.close();
            
            logger.debug("created file: mainA:/a/foo");
            
            // create equivalent of WCM layered folder between web project staging sandboxes (mainB:/a/b pointing to ,mainA:/a/b)
            fService.createLayeredFile("mainA:/a/foo", "mainB:/a", "foo");
            
            logger.debug("created layered file: mainB:/a/foo -> mainA:/a/foo");
            
            fService.createSnapshot("mainA", null, null);
            fService.createSnapshot("mainB", null, null);
            
            logger.debug("created 2 snapshots: mainA and mainB");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/foo", line);
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(0, diffs.size());
            
            // note: short-cut - removed directly from "staging" area (don't bother with sandbox mainA--layer for now)
            fService.removeNode("mainA:/a", "foo");
            fService.createSnapshot("mainA", null, null);
            
            logger.debug("removed file & snapshot: mainA:/a/foo");
            
            try
            {
                fService.getFileInputStream(-1, "mainA:/a/foo");
                fail("Unexpected");
            }
            catch (AVMNotFoundException nfe)
            {
                // expected
            }
            
            try
            {
                fService.getFileInputStream(-1, "mainB:/a/foo");
                fail("Unexpected");
            }
            catch (AVMNotFoundException nfe)
            {
                // expected
            }
            
            try
            {
                fService.getFileInputStream(-1, "mainB--layer:/a/foo");
                fail("Unexpected");
            }
            catch (AVMNotFoundException nfe)
            {
                // expected
            }
            
            fService.removeNode("mainB--layer:/a", "foo");
            fService.createSnapshot("mainB--layer", null, null);
            
            logger.debug("removed file & snapshot: mainB--layer:/a/foo");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals("[mainB--layer:/a/foo[-1] > mainB:/a/foo[-1]]", diffs.toString());
            
            /*
            // For testing ALF-4800 (testLayeredFileDelete1 / testLayeredFileDelete2 - error in dispatchUpdate)
            // In addition to sleep below, increase frequency of OrphanReaper (eg. set startDelay/repeatInterval to 2000/500 msecs)
            logger.debug("start sleep");
            Thread.sleep(30000);
            logger.debug("finish sleep");
            */
            
            // ETHREEOH-2829
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("updated & flattened: removed file: mainB:/a/foo");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
        }
    }
    
    public void testLayeredFileDeleteFile3() throws Exception
    {
        try
        {
            fService.createStore("mainB");
            fService.createStore("mainB--layer");
            
            logger.debug("created 2 stores: mainB, mainB--layer");
            
            fService.createDirectory("mainB:/", "a");
            
            logger.debug("created plain dir: mainB:/a");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            logger.debug("created layered dir: mainB--layer:/a -> mainB:/a");
            
            // create equivalent of WCM layered file between web project staging sandboxes (mainB:/a/b/foo pointing to mainA:/a/b/foo)
            fService.createLayeredFile("mainA:/a/foo", "mainB:/a", "foo"); // note: unbacked/stale here ... even store does not exist !!
            
            logger.debug("created layered file: mainB:/a/foo -> mainA:/a/foo");
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals(0, diffs.size());
            
            // create file
            fService.createFile("mainB--layer:/a", "bar");
            
            logger.debug("created file: mainB--layer:/a/bar");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals("[mainB--layer:/a/bar[-1] > mainB:/a/bar[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            
            logger.debug("updated: created file: mainB:/a/bar");
            
            fSyncService.flatten("mainB--layer:/a", "mainB:/a");
            
            logger.debug("flattened: created file: mainB:/a/bar");
            
            // delete layered file (from mainB--layer)
            fService.removeNode("mainB--layer:/a", "foo");
            fService.createSnapshot("mainB--layer", null, null);
            
            logger.debug("removed file & snapshot: mainB--layer:/a/foo");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a", -1, "mainB:/a", null);
            assertEquals("[mainB--layer:/a/foo[-1] > mainB:/a/foo[-1]]", diffs.toString());
            
            fService.createStore("mainB--workflow1");
            
            logger.debug("created store: mainB--workflow1");
            
            recursiveList("mainB--workflow1");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--workflow1:/", "a");
            
            logger.debug("created layered dir: mainB--workflow1:/a -> mainB:/a");
            
            recursiveList("mainB");
            recursiveList("mainB--layer");
            recursiveList("mainB--workflow1");
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a", -1, "mainB:/a", null);
            assertEquals(0, diffs.size());
            
            diffs = new ArrayList<AVMDifference>(1);
            diffs.add(new AVMDifference(-1, "mainB--layer:/a/foo", -1, "mainB--workflow1:/a/foo", AVMDifference.NEWER));
            
            // ETHREEOH-2868
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            
            logger.debug("updated: removed file: mainB--workflow1:/a/foo");
            
            recursiveList("mainB");
            recursiveList("mainB--layer");
            recursiveList("mainB--workflow1");
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a", -1, "mainB:/a", null);
            assertEquals("[mainB--workflow1:/a/foo[-1] > mainB:/a/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, true, true, "one", "one");
            fSyncService.flatten("mainB--workflow1:/a", "mainB:/a");
            
            logger.debug("updated & flattened: removed file: mainB:/a/foo");
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a", -1, "mainB:/a", null);
            assertEquals(0, diffs.size());
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a", -1, "mainB--layer:/a", null);
            assertEquals(0, diffs.size());
            
            fSyncService.update(diffs, null, true, true, false, false, null, null);
            fSyncService.flatten("mainB--layer:/a", "mainB--workflow1:/a");
            
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
            if (fService.getStore("mainB--workflow1") != null) { fService.purgeStore("mainB--workflow1"); }
        }
    }
    
    public void testLayeredFileDeleteFile4() throws Exception
    {
        try
        {
            fService.createStore("mainA");
            fService.createStore("mainB");
            fService.createStore("mainB--layer");
            
            logger.debug("created 3 stores: mainA, mainB, mainB--layer");
            
            fService.createDirectory("mainA:/", "a");
            fService.createDirectory("mainB:/", "a");
            
            logger.debug("created 2 plain dirs: mainA:/a and mainB:/a");
            
            fService.createSnapshot("mainA", null, null);
            fService.createSnapshot("mainB", null, null);
            
            logger.debug("created 2 snapshots: mainA and mainB");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--layer:/", "a");
            
            logger.debug("created layered dir: mainB--layer:/a -> mainB:/a");
            
            // note: short-cut - created directly in "staging" areas (don't bother with sandbox mainA--layer or mainB--layer for now)
            fService.createDirectory("mainA:/a", "b");
            fService.createDirectory("mainB:/a", "b");
            
            logger.debug("created directories: mainA:/a/b &  mainB:/a/b");
            
            fService.createFile("mainA:/a/b", "foo");
            
            PrintStream out = new PrintStream(fService.getFileOutputStream("mainA:/a/b/foo"));
            out.println("I am mainA:/a/b/foo");
            out.close();
            
            logger.debug("created file: mainA:/a/b/foo");
            
            // create equivalent of WCM layered file between web project staging sandboxes (mainB:/a/b/foo pointing to mainA:/a/b/foo)
            fService.createLayeredFile("mainA:/a/b/foo", "mainB:/a/b", "foo");
            
            logger.debug("created layered file: mainB:/a/b/foo -> mainA:/a/b/foo");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB--layer:/a/b/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainB:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "mainA:/a/b/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am mainA:/a/b/foo", line);
            
            List<AVMDifference> diffs = fSyncService.compare(-1, "mainB--layer:/a/b", -1, "mainB:/a/b", null);
            assertEquals(0, diffs.size());
            
            // create file
            fService.createFile("mainB--layer:/a/b", "bar");
            
            logger.debug("created file: mainB--layer:/a/b/bar");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a/b", -1, "mainB:/a/b", null);
            assertEquals("[mainB--layer:/a/b/bar[-1] > mainB:/a/b/bar[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, "one", "one");
            fSyncService.flatten("mainB--layer:/a/b", "mainB:/a/b");
            
            logger.debug("updated & flattened: created file: mainB:/a/b/bar");
            
            // delete layered file (from mainB--layer)
            fService.removeNode("mainB--layer:/a/b", "foo");
            fService.createSnapshot("mainB--layer", null, null);
            
            logger.debug("removed file & snapshot: mainB--layer:/a/b/foo");
            
            diffs = fSyncService.compare(-1, "mainB--layer:/a/b", -1, "mainB:/a/b", null);
            assertEquals("[mainB--layer:/a/b/foo[-1] > mainB:/a/b/foo[-1]]", diffs.toString());
            
            fService.createStore("mainB--workflow1");
            
            fService.createLayeredDirectory("mainB:/a", "mainB--workflow1:/", "a");
            
            logger.debug("created layered dir: mainB--workflow1:/a -> mainB:/a");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            recursiveList("mainB--workflow1");
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a/b", -1, "mainB:/a/b", null);
            assertEquals(0, diffs.size());
            
            diffs = new ArrayList<AVMDifference>(1);
            diffs.add(new AVMDifference(-1, "mainB--layer:/a/b/foo", -1, "mainB--workflow1:/a/b/foo", AVMDifference.NEWER));
            
            // ETHREEOH-2868
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            
            logger.debug("updated: removed file: mainB--workflow1:/a/b/foo");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
            recursiveList("mainB--workflow1");
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a/b", -1, "mainB:/a/b", null);
            assertEquals("[mainB--workflow1:/a/b/foo[-1] > mainB:/a/b/foo[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, true, true, "one", "one");
            fSyncService.flatten("mainB--workflow1:/a/b", "mainB:/a/b");
            
            logger.debug("updated & flattened: removed file: mainB:/a/b/foo");
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a/b", -1, "mainB:/a/b", null);
            assertEquals(0, diffs.size());
            
            diffs = fSyncService.compare(-1, "mainB--workflow1:/a/b", -1, "mainB--layer:/a/b", null);
            assertEquals(0, diffs.size());
            
            fSyncService.update(diffs, null, true, true, false, false, null, null);
            fSyncService.flatten("mainB--layer:/a/b", "mainB--workflow1:/a/b");
            
            recursiveList("mainA");
            recursiveList("mainB");
            recursiveList("mainB--layer");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            fService.purgeStore("mainA");
            fService.purgeStore("mainB");
            fService.purgeStore("mainB--layer");
            if (fService.getStore("mainB--workflow1") != null) { fService.purgeStore("mainB--workflow1"); }
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
        recursiveList(store, -1);
    }
    
    protected void recursiveList(String store, int version)
    {
        String list = recursiveList(store, version, true);
        if (logger.isDebugEnabled())
        { 
            logger.debug("\n\n"+store+":"+"\n"+list+"\n");
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
        
        List<AVMNodeDescriptor> ancs = fService.getHistory(desc, -1);
        for (AVMNodeDescriptor anc : ancs)
        {
            builder.append("--->").append(anc.toString());
        }
        
        builder.append('\n');
        if (desc.getType() == AVMNodeType.PLAIN_DIRECTORY ||
            (desc.getType() == AVMNodeType.LAYERED_DIRECTORY && followLinks))
        {
            String basename = path.endsWith("/") ? path : path + "/";
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(version, path);
            for (String name : listing.keySet())
            {
                if (logger.isTraceEnabled()) { logger.trace(name); }
                builder.append(recursiveList(basename + name, version, indent + 2, followLinks));
            }
            List<String> deletedList = fService.getDeleted(version, path);
            for (String name : deletedList)
            {
                if (logger.isTraceEnabled()) { logger.trace(name); }
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
