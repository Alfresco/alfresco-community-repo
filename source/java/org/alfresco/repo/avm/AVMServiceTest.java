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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.avm.hibernate.HibernateHelper;
import org.alfresco.repo.avm.impl.AVMServiceImpl;
import org.hibernate.cfg.Configuration;
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

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        Configuration cfg = HibernateHelper.GetConfiguration();
        SchemaExport se = new SchemaExport(cfg);
        se.drop(false, true);
        AVMServiceImpl service = new AVMServiceImpl();
        service.setStorage("storage");
        service.init(true);
        fService = service;
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
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
            fService.createFile("main:testdir", "testfile");
            fService.createFile("main:/", "testfile2");
            fService.createSnapshot("main");
            PrintStream out = new PrintStream(fService.getFileOutputStream("main:testdir/testfile"));
            out.println("This is testdir/testfile");
            out.close();
            out = new PrintStream(fService.getFileOutputStream("main:testfile2"));
            out.println("This is testfile2");
            out.close();
            fService.createSnapshot("main");
            Set<Integer> versions = fService.getRepositoryVersions("main");
            for (Integer version : versions)
            {
                System.out.println("V:" + version);
                System.out.println(recursiveList("main", version));
            }
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
            fService.createBranch(-1, "main:a", "main:d/e", "abranch");
            fService.createSnapshot("main");
            Set<Integer> versions = fService.getRepositoryVersions("main");
            for (Integer version : versions)
            {
                System.out.println("V:" + version);
                System.out.println(recursiveList("main", version));
            }
            List<FolderEntry> original = fService.getDirectoryListing(-1, "main:a");
            List<FolderEntry> branch = fService.getDirectoryListing(-1, "main:d/e/abranch");
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
            fService.createLayeredDirectory("main:a", "main:d/e", "alayer");
            fService.createSnapshot("main");
            System.out.println(recursiveList("main", -1));
            List<FolderEntry> original = fService.getDirectoryListing(-1, "main:a");
            List<FolderEntry> layer = fService.getDirectoryListing(-1, "main:d/e/alayer");
            assertEquals(original, layer);
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
        builder.append(path);
        builder.append(' ');
        Lookup lookup = fService.lookup(version, path);
        AVMNode node = lookup.getCurrentNode();
        builder.append(node.toString(lookup));
        builder.append('\n');
        if (node instanceof DirectoryNode)
        {
            String basename = path.endsWith("/") ? path.substring(0, path.length() - 1) : path + "/";
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
        fService.createDirectory("main:a", "b");
        fService.createDirectory("main:a/b", "c");
        fService.createDirectory("main:/", "d");
        fService.createDirectory("main:d", "e");
        fService.createDirectory("main:d/e", "f");
        fService.createFile("main:a/b/c", "foo");
        PrintStream out = new PrintStream(fService.getFileOutputStream("main:a/b/c/foo"));
        out.println("I am main:a/b/c/foo");
        out.close();
        fService.createFile("main:a/b/c", "bar");
        out = new PrintStream(fService.getFileOutputStream("main:a/b/c/bar"));
        out.println("I am main:a/b/c/bar");
        out.close();
        ArrayList<String> toSnapshot = new ArrayList<String>();
        toSnapshot.add("main");
        fService.createSnapshot(toSnapshot);
    }
}
