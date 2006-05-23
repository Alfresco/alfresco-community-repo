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
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.repo.avm.hibernate.HibernateHelper;
import org.alfresco.repo.avm.impl.AVMServiceImpl;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.Statistics;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import junit.framework.TestCase;

/**
 * Big test of AVM behavior.
 * @author britt
 */
public class AVMServiceTest extends TestCase
{
    /**
     * The AVMService we are testing.
     */
    private AVMService fService;
    
    /**
     * The start time of actual work for a test.
     */
    private long fStartTime;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        Configuration cfg = HibernateHelper.GetConfiguration();
        HibernateHelper.GetSessionFactory().getStatistics().setStatisticsEnabled(true);
        SchemaExport se = new SchemaExport(cfg);
        se.drop(false, true);
        AVMServiceImpl service = new AVMServiceImpl();
        service.setStorage("storage");
        service.init(true);
        fStartTime = System.currentTimeMillis();
        fService = service;
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        long now = System.currentTimeMillis();
        System.out.println("Timing: " + (now - fStartTime) + "ms");
        Statistics stats = HibernateHelper.GetSessionFactory().getStatistics();
        stats.logSummary();
        stats.clear();
        HibernateHelper.Reset();
    }
    
    /**
     * Test Nothing.  Just make sure set up works.
     */
    public void testNothing()
    {
    }
    
    /**
     * Test making a simple directory.
     */
    public void testCreateDirectory()
    {
        try
        {
            fService.createDirectory("main:/", "testdir");
            fService.createSnapshot("main");
            AVMNode node = fService.lookup(-1, "main:/").getCurrentNode();
            assertTrue(node instanceof PlainDirectoryNode);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test creating a file.
     */
    public void testCreateFile()
    {
        try
        {
            testCreateDirectory();
            fService.createFile("main:/testdir", "testfile");
            fService.createFile("main:/", "testfile2");
            fService.createSnapshot("main");
            PrintStream out = new PrintStream(fService.getFileOutputStream("main:/testdir/testfile"));
            out.println("This is testdir/testfile");
            out.close();
            out = new PrintStream(fService.getFileOutputStream("main:/testfile2"));
            out.println("This is testfile2");
            out.close();
            fService.createSnapshot("main");
            Set<Integer> versions = fService.getRepositoryVersions("main");
            for (Integer version : versions)
            {
                System.out.println("V:" + version);
                System.out.println(recursiveList("main", version));
            }
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/testdir/testfile")));
            String line = reader.readLine();
            assertEquals("This is testdir/testfile", line);
            reader.close();
            reader =
                new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/testfile2")));
            line = reader.readLine();
            assertEquals("This is testfile2", line);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test creating a branch.
     */
    public void testCreateBranch()
    {
        try
        {
            setupBasicTree();
            fService.createBranch(-1, "main:/a", "main:/d/e", "abranch");
            fService.createSnapshot("main");
            Set<Integer> versions = fService.getRepositoryVersions("main");
            for (Integer version : versions)
            {
                System.out.println("V:" + version);
                System.out.println(recursiveList("main", version));
            }
            String original = recursiveList("main:/a", -1, 0);
            original = original.substring(original.indexOf('\n'));
            String branch = recursiveList("main:/d/e/abranch", -1, 0);
            branch = branch.substring(branch.indexOf('\n'));
            assertEquals(original, branch);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test creating a layer.
     */
    public void testCreateLayer()
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/d/e", "alayer");
            fService.createSnapshot("main");
            System.out.println(recursiveList("main", -1));
            assertEquals("main:/a", fService.getIndirectionPath(-1, "main:/d/e/alayer"));
            String original = recursiveList("main:/a", -1, 0);
            original = original.substring(original.indexOf('\n'));
            String layer = recursiveList("main:/d/e/alayer", -1, 0);
            layer = original.substring(original.indexOf('\n'));
            assertEquals(original, layer);
            PrintStream out = new PrintStream(fService.getFileOutputStream("main:/d/e/alayer/b/c/foo"));
            out.println("I am main:/d/e/alayer/b/c/foo");
            out.close();
            fService.createSnapshot("main");
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/b/c/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/b/c/foo", line);
            System.out.println(recursiveList("main", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test creating a layered file.
     */
    public void testCreateLayeredFile()
    {
        try
        {
            setupBasicTree();
            fService.createLayeredFile("main:/a/b/c/foo", "main:/d", "lfoo");
            fService.createSnapshot("main");
            System.out.println(recursiveList("main", -1));
            assertEquals("main:/a/b/c/foo", fService.getIndirectionPath(-1, "main:/d/lfoo"));
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/d/lfoo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/b/c/foo", line);
            PrintStream out = new PrintStream(fService.getFileOutputStream("main:/d/lfoo"));
            out.println("I am main:/d/lfoo");
            out.close();
            fService.createSnapshot("main");
            System.out.println(recursiveList("main", -1));
            reader = 
                new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/b/c/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/b/c/foo", line);
            reader =
                new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/d/lfoo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am main:/d/lfoo", line);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test rename.
     */
    public void testRename()
    {
        try
        {
            setupBasicTree();
            fService.rename("main:/a", "b", "main:/d/e", "brenamed");
            fService.createSnapshot("main");
            System.out.println(recursiveList("main", -1));
            String original = recursiveList("main:/a/b", 0, 0);
            original = original.substring(original.indexOf('\n'));
            String renamed = recursiveList("main:/d/e/brenamed", 1, 0);
            renamed = renamed.substring(renamed.indexOf('\n'));
            assertEquals(original, renamed);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test remove.
     */
    public void testRemove()
    {
        try
        {
            setupBasicTree();
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            checkHistory(history, "main");
            System.out.println(history.get(0));
            fService.removeNode("main:/a/b/c", "foo");
            fService.createSnapshot("main");
            checkHistory(history, "main");
            System.out.println(history.get(1));
            List<FolderEntry> l = fService.getDirectoryListing(-1, "main:/a/b/c");
            assertEquals(1, l.size());
            fService.removeNode("main:/d", "e");
            fService.createSnapshot("main");
            checkHistory(history, "main");
            System.out.println(history.get(2));
            l = fService.getDirectoryListing(-1, "main:/d");
            assertEquals(0, l.size());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
   
    /**
     * Test branching from one repository to another.
     */
    public void testBranchAcross()
    {
        try
        {
            setupBasicTree();
            fService.createRepository("second");
            fService.createBranch(-1, "main:/", "second:/", "main");
            fService.createSnapshot("second");
            System.out.println(recursiveList("second", -1));
            String original = recursiveList("main:/", -1, 0);
            original = original.substring(original.indexOf('\n'));
            String branch = recursiveList("second:/main", -1, 0);
            branch = branch.substring(branch.indexOf('\n'));
            assertEquals(original, branch);
            // Now make sure nothing happens to the branched from place,
            // if the branch is modified.
            PrintStream out = 
                new PrintStream(fService.getFileOutputStream("second:/main/a/b/c/foo"));
            out.println("I am second:/main/a/b/c/foo");
            out.close();
            fService.createSnapshot("second");
            System.out.println(recursiveList("second", -1));
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/b/c/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/b/c/foo", line);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test creating a layer across repositories.
     */
    public void testLayerAcross()
    {
        try
        {
            setupBasicTree();
            fService.createRepository("second");
            fService.createLayeredDirectory("main:/", "second:/", "main");
            fService.createSnapshot("second");
            System.out.println(recursiveList("second", -1));
            String original = recursiveList("main:/", -1, 0);
            original = original.substring(original.indexOf('\n'));
            String layer = recursiveList("second:/main", -1, 0);
            layer = layer.substring(layer.indexOf('\n'));
            assertEquals(original, layer);
            // Now make sure that a copy on write will occur and
            // that the underlying stuff doesn't get changed.
            PrintStream out = new PrintStream(fService.getFileOutputStream("second:/main/a/b/c/foo"));
            out.println("I am second:/main/a/b/c/foo");
            out.close();
            fService.createSnapshot("second");
            System.out.println(recursiveList("second", -1));
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "second:/main/a/b/c/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am second:/main/a/b/c/foo", line);
            reader =
                new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/b/c/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/b/c/foo", line);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();            
        }
    }
    
    /**
     * Test rename across repositories.
     */
    public void testRenameAcross()
    {
        try
        {
            setupBasicTree();
            fService.createRepository("second");
            fService.rename("main:/a/b", "c", "second:/", "cmoved");
            ArrayList<String> toSnapshot = new ArrayList<String>();
            toSnapshot.add("main");
            toSnapshot.add("second");
            System.out.println(recursiveList("main", -1));
            System.out.println(recursiveList("second", -1));
            // Check that the moved thing has identical contents to the thing it
            // was moved from.
            String original = recursiveList("main:/a/b/c", 0, 0);
            original = original.substring(original.indexOf('\n'));
            String moved = recursiveList("second:/cmoved", -1, 0);
            moved = moved.substring(moved.indexOf('\n'));
            assertEquals(original, moved);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test COW in various circumstances.
     */
    public void testDeepCOW()
    {
        try
        {
            // Makes a layer on top of a layer on top of a plain directory.
            // Assures that the correct layers are copied when files
            // are added in the two layers.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createSnapshot("main");
            List<FolderEntry> listing = fService.getDirectoryListing(-1, "main:/a");
            assertEquals(1, listing.size());
            assertEquals("b", listing.get(0).getName());
            fService.createLayeredDirectory("main:/a", "main:/", "c");
            fService.createLayeredDirectory("main:/c", "main:/", "d");
            fService.createFile("main:/d/b", "foo.txt");
            fService.createSnapshot("main");
            System.out.println(recursiveList("main", -1));
            listing = fService.getDirectoryListing(-1, "main:/d/b");
            assertEquals(1, listing.size());
            assertEquals("foo.txt", listing.get(0).getName());
            fService.createFile("main:/c/b", "bar.txt");
            fService.createSnapshot("main");
            System.out.println(recursiveList("main", -1));
            listing = fService.getDirectoryListing(-1, "main:/c/b");
            assertEquals(1, listing.size());
            assertEquals("bar.txt", listing.get(0).getName());
            listing = fService.getDirectoryListing(-1, "main:/d/b");
            assertEquals(2, listing.size());
            assertEquals("bar.txt", listing.get(0).getName());
            assertEquals("foo.txt", listing.get(1).getName());
            fService.rename("main:/", "c", "main:/", "e");
            fService.createSnapshot("main");
            System.out.println(recursiveList("main", -1));
            listing = fService.getDirectoryListing(-1, "main:/d/b");
            assertEquals(1, listing.size());
            assertEquals("foo.txt", listing.get(0).getName());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test branching and layering interaction.
     */
    public void testBranchAndLayer()
    {
        try
        {
            // Create a simple directory hierarchy.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createFile("main:/a/b", "c.txt");
            fService.createFile("main:/a/b", "d.txt");
            fService.createFile("main:/a", "e.txt");
            fService.createSnapshot("main");
            // Make a branch off of a.
            fService.createBranch(-1, "main:/a", "main:/", "branch");
            fService.createSnapshot("main");
            // The branch should contain exactly the same things as the thing
            // it branched from.
            String original = recursiveList("main:/a", -1, 0);
            original = original.substring(original.indexOf('\n'));
            String branch = recursiveList("main:/branch", -1, 0);
            branch = branch.substring(branch.indexOf('\n'));
            assertEquals(original, branch);
            // Make a layer pointing to /branch/b
            fService.createLayeredDirectory("main:/branch/b", "main:/", "layer");
            fService.createSnapshot("main");
            // The new layer should contain exactly the same things as the thing it is layered to.
            original = recursiveList("main:/branch/b", -1, 0);
            original = original.substring(original.indexOf('\n'));
            String layer = recursiveList("main:/layer", -1, 0);
            layer = layer.substring(layer.indexOf('\n'));
            assertEquals(original, layer);
            // Make a modification in /a/b, the original branch.
            PrintStream out = new PrintStream(fService.getFileOutputStream("main:/a/b/c.txt"));
            out.println("I am c, modified in main:/a/b.");
            out.close();
            fService.createSnapshot("main");
            // The layer should still have identical content to /branch/b.
            original = recursiveList("main:/branch/b", -1, 0);
            original = original.substring(original.indexOf('\n'));
            layer = recursiveList("main:/layer", -1, 0);
            layer = layer.substring(layer.indexOf('\n'));
            assertEquals(original, layer);
            // But the layer won't have contents identical to /a/b's
            original = recursiveList("main:/a/b", -1, 0);
            original = original.substring(original.indexOf('\n'));
            assertFalse(original.equals(layer));
            // Make a modification in /branch/b
            out = new PrintStream(fService.getFileOutputStream("main:/branch/b/d.txt"));
            out.println("I am d, modified in main:/branch/b");
            out.close();
            fService.createSnapshot("main");
            // The layer contents should be identical to the latest contents of /branch/b.
            original = recursiveList("main:/branch/b", -1, 0);
            original = original.substring(original.indexOf('\n'));
            layer = recursiveList("main:/layer", -1, 0);
            layer = layer.substring(layer.indexOf('\n'));
            assertEquals(original, layer);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test basic Layering.
     */
    public void testLayering()
    {
        try
        {
            // Make some directories;
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createDirectory("main:/a/b/c", "d");
            fService.createSnapshot("main");
            // Now make some layers.  Three to be precise.
            fService.createLayeredDirectory("main:/a", "main:/", "e");
            fService.createLayeredDirectory("main:/e", "main:/", "f");
            fService.createLayeredDirectory("main:/f", "main:/", "g");
            fService.createSnapshot("main");
            // e, f, g should all have the same contents as a.
            String a = recursiveList("main:/a", -1, 0);
            a = a.substring(a.indexOf('\n'));
            String e = recursiveList("main:/e", -1, 0);
            e = e.substring(e.indexOf('\n'));
            String f = recursiveList("main:/f", -1, 0);
            f = f.substring(f.indexOf('\n'));
            String g = recursiveList("main:/g", -1, 0);
            g = g.substring(g.indexOf('\n'));
            assertEquals(a, e);
            assertEquals(a, f);
            assertEquals(a, g);
            // Now make a file in /g/b/c/d and /f/b/c/d
            fService.createFile("main:/g/b/c/d", "foo");
            fService.createFile("main:/f/b/c/d", "bar");
            fService.createSnapshot("main");
            // /g/b/c/d should contain foo and bar.
            List<FolderEntry> listing = fService.getDirectoryListing(-1, "main:/g/b/c/d");
            assertEquals(2, listing.size());
            assertEquals("bar", listing.get(0).getName());
            assertEquals("foo", listing.get(1).getName());
            // /f/b/c/d should contain just bar.
            listing = fService.getDirectoryListing(-1, "main:/f/b/c/d");
            assertEquals(1, listing.size());
            assertEquals("bar", listing.get(0).getName());
            // Now do something in the bottom layer.
            fService.createFile("main:/a/b/c", "baz");
            fService.createSnapshot("main");
            // /e/b/c should contain baz and d
            listing = fService.getDirectoryListing(-1, "main:/e/b/c");
            assertEquals(2, listing.size());
            assertEquals("baz", listing.get(0).getName());
            assertEquals("d", listing.get(1).getName());
            // Now add something in the e layer.
            fService.createFile("main:/e/b/c/d", "bing");
            fService.createSnapshot("main");
            // /f/b/c/d should now contain bar and bing.
            listing = fService.getDirectoryListing(-1, "main:/f/b/c/d");
            assertEquals(2, listing.size());
            assertEquals("bar", listing.get(0).getName());
            assertEquals("bing", listing.get(1).getName());
            System.out.println(recursiveList("main", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Test adding 100 files to each directory.
     */
    public void testAdd100()
    {
        try
        {
            String [] dirs = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" };
            for (String dir : dirs)
            {
                fService.createDirectory("main:/", dir);
                dir = "main:/" + dir;
                for (int i = 0; i < 50; i++)
                {
                    fService.createFile(dir, "file" + i);
                    System.out.println(dir + "/file" + i);
                    PrintStream out = 
                        new PrintStream(fService.getFileOutputStream(dir + "/file" + i));
                    out.println("I am " + dir + "/file" + i);
                    out.close();
                }
            }
            fService.createSnapshot("main");
            System.out.println(recursiveList("main", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Helper to write a recursive listing of a repository at a given version.
     * @param repoName The name of the repository.
     * @param version The version to look under.
     */
    private String recursiveList(String repoName, int version)
    {
        return recursiveList(repoName + ":/", version, 0);
    }
    
    /**
     * Recursive list the given path.
     * @param path The path.
     * @param version The version.
     * @param indent The current indent level.
     */
    private String recursiveList(String path, int version, int indent)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indent; i++)
        {
            builder.append(' ');
        }
        builder.append(path.substring(path.lastIndexOf('/') + 1));
        builder.append(' ');
        Lookup lookup = fService.lookup(version, path);
        AVMNode node = lookup.getCurrentNode();
        builder.append(node.toString(lookup));
        builder.append('\n');
        if (node instanceof DirectoryNode)
        {
            String basename = path.endsWith("/") ? path : path + "/";
            List<FolderEntry> listing = fService.getDirectoryListing(version, path);
            for (FolderEntry entry : listing)
            {
                builder.append(recursiveList(basename + entry.getName(), version, indent + 2));
            }
        }
        return builder.toString();
    }
    
    /**
     * Setup a basic tree.
     */
    private void setupBasicTree()
    {
        fService.createDirectory("main:/", "a");
        fService.createDirectory("main:/a", "b");
        fService.createDirectory("main:/a/b", "c");
        fService.createDirectory("main:/", "d");
        fService.createDirectory("main:/d", "e");
        fService.createDirectory("main:/d/e", "f");
        fService.createFile("main:/a/b/c", "foo");
        PrintStream out = new PrintStream(fService.getFileOutputStream("main:/a/b/c/foo"));
        out.println("I am main:/a/b/c/foo");
        out.close();
        fService.createFile("main:/a/b/c", "bar");
        out = new PrintStream(fService.getFileOutputStream("main:/a/b/c/bar"));
        out.println("I am main:/a/b/c/bar");
        out.close();
        ArrayList<String> toSnapshot = new ArrayList<String>();
        toSnapshot.add("main");
        fService.createSnapshot(toSnapshot);
    }
    
    /**
     * Check that history has not been screwed up.
     */
    private void checkHistory(TreeMap<Integer, String> history, String repName)
    {
        for (Integer i : history.keySet())
        {
            assertEquals(history.get(i), recursiveList(repName, i));
        }
        int latest = fService.getLatestVersionID(repName);
        history.put(latest - 1, recursiveList(repName, -1));
    }
}
