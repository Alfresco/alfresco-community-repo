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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.transaction.UserTransaction;

import org.alfresco.config.JNDIConstants;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.avm.actions.AVMRevertListAction;
import org.alfresco.repo.avm.actions.AVMRevertToVersionAction;
import org.alfresco.repo.avm.actions.AVMUndoSandboxListAction;
import org.alfresco.repo.avm.actions.SimpleAVMPromoteAction;
import org.alfresco.repo.avm.actions.SimpleAVMSubmitAction;
import org.alfresco.repo.avm.util.BulkLoader;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.search.IndexMode;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.AVMLuceneIndexer;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMCycleException;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.LayeringDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.avm.deploy.DeploymentReport;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.service.cmr.avm.locking.AVMLockingException;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.remote.RepoRemote;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CrossRepositoryCopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.Pair;

/**
 * Big test of AVM behavior.
 * 
 * @author britt
 */
public class AVMServiceTest extends AVMServiceTestBase
{
    /**
     * Test version by date lookup.
     */
    public void testVersionByDate()
    {
        try
        {
            ArrayList<Long> times = new ArrayList<Long>();
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            loader.recursiveLoad("source/java/org/alfresco/repo/avm", "main:/");
            times.add(System.currentTimeMillis());
            assertEquals(1, fService.createSnapshot("main", null, null).get("main").intValue());
            loader.recursiveLoad("source/java/org/alfresco/repo/action", "main:/");
            times.add(System.currentTimeMillis());
            assertEquals(2, fService.createSnapshot("main", null, null).get("main").intValue());
            loader.recursiveLoad("source/java/org/alfresco/repo/audit", "main:/");
            times.add(System.currentTimeMillis());
            assertEquals(3, fService.createSnapshot("main", null, null).get("main").intValue());
            assertEquals(1, fService.getStoreVersions("main", null, new Date(times.get(0))).size());
            assertEquals(3, fService.getStoreVersions("main", new Date(times.get(0)), null).size());
            assertEquals(2, fService.getStoreVersions("main", new Date(times.get(1)),
                    new Date(System.currentTimeMillis())).size());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test properties.
     */
    public void testProperties()
    {
        try
        {
            setupBasicTree();
            
            StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");
            SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
            ResultSet   results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@{silly.uri}SillyProperty")+":\"Silly\"");
            assertEquals(0, results.length());
            results.close();
            
            QName name = QName.createQName("silly.uri", "SillyProperty");
            PropertyValue value = new PropertyValue(name, "Silly Property Value");
            fService.setNodeProperty("main:/a/b/c/foo", name, value);
            fService.createSnapshot("main", null, null);
            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@{silly.uri}SillyProperty")+":\"Silly\"");
            assertEquals(1, results.length());
            results.close();
            PropertyValue returned = fService.getNodeProperty(-1, "main:/a/b/c/foo", name);
            assertEquals(value.toString(), returned.toString());
            Map<QName, PropertyValue> props = fService.getNodeProperties(-1, "main:/a/b/c/foo");
            assertEquals(1, props.size());
            assertEquals(value.toString(), props.get(name).toString());
            
            
            props = new HashMap<QName, PropertyValue>();
            QName n1 = QName.createQName("silly.uri", "Prop1");
            PropertyValue p1 = new PropertyValue(null, new Date(System.currentTimeMillis()));
            props.put(n1, p1);
            QName n2 = QName.createQName("silly.uri", "Prop2");
            PropertyValue p2 = new PropertyValue(null, "A String Property.");
            props.put(n2, p2);
            QName n3 = QName.createQName("silly.uri", "Prop3");
            PropertyValue p3 = new PropertyValue(null, 42);
            props.put(n3, p3);
            fService.setNodeProperties("main:/a/b/c/bar", props);
            fService.createSnapshot("main", null, null);
            props = fService.getNodeProperties(-1, "main:/a/b/c/bar");
            assertEquals(3, props.size());
            assertEquals(p1.toString(), props.get(n1).toString());
            assertEquals(p2.toString(), props.get(n2).toString());
            assertEquals(p3.toString(), props.get(n3).toString());
            
            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@{silly.uri}Prop1")+":\"" + props.get(n1).getStringValue() +"\"");
            assertEquals(1, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@{silly.uri}Prop2")+":\"" + props.get(n2).getStringValue() +"\"");
            assertEquals(1, results.length());
            results.close();
            
            fService.deleteNodeProperty("main:/a/b/c/bar", n1);
            fService.createSnapshot("main", null, null);
            
            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@{silly.uri}Prop1")+":\"" + props.get(n1).getStringValue() +"\"");
            assertEquals(0, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@{silly.uri}Prop2")+":\"" + props.get(n2).getStringValue() +"\"");
            assertEquals(1, results.length());
            results.close();
            
            props = fService.getNodeProperties(-1, "main:/a/b/c/bar");
            assertEquals(2, props.size());
            assertEquals(p2.toString(), props.get(n2).toString());
            assertEquals(p3.toString(), props.get(n3).toString());
            fService.deleteNodeProperties("main:/a/b/c/bar");
            fService.createSnapshot("main", null, null);
            
            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@{silly.uri}Prop1")+":\"" + p1.getStringValue() +"\"");
            assertEquals(0, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@{silly.uri}Prop2")+":\"" + props.get(n2).getStringValue() +"\"");
            assertEquals(0, results.length());
            results.close();
            
            props = fService.getNodeProperties(-1, "main:/a/b/c/bar");
            assertEquals(0, props.size());
            fService.removeNode("main:/a/b/c/foo");
            fService.setNodeProperty("main:/a/b/c/foo", QName.createQName("silly.uri", "Prop1"), new PropertyValue(
                    null, 42));
            assertEquals(1, fService.getNodeProperties(-1, "main:/a/b/c/foo").size());
            fService.createSnapshot("main", null, null);
            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@{silly.uri}Prop1")+":\"" + p1.getStringValue() +"\"");
            assertEquals(0, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@{silly.uri}Prop2")+":\"" + p2.getStringValue() +"\"");
            assertEquals(0, results.length());
            results.close();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test getStoreVersionRootPaths().
     */
    public void testGetStoreVersionRootPaths()
    {
        try
        {
            setupBasicTree();
            AVMNodeDescriptor desc = fService.lookup(-1, "main:/a/b/c/foo");
            List<String> paths = fService.getPathsInStoreVersion(desc, "main", 1);
            assertEquals(1, paths.size());
            assertEquals("main:/a/b/c/foo", paths.get(0));
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            fService.createSnapshot("main", null, null);
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            fService.createSnapshot("main", null, null);
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            fService.createSnapshot("main", null, null);
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            fService.createSnapshot("main", null, null);
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            fService.createSnapshot("main", null, null);
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            fService.createSnapshot("main", null, null);
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            fService.createSnapshot("main", null, null);
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            fService.createSnapshot("main", null, null);
            paths = fService.getPathsInStoreVersion(desc, "main", 1);
            assertEquals(1, paths.size());
            assertEquals("main:/a/b/c/foo", paths.get(0));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * Minimal testing of Locking Aware service.
     */
    public void testLockingAwareService()
    {
        AVMService oldService = fService;
        fService = (AVMService)fContext.getBean("AVMLockingAwareService");
        AuthenticationService authService = (AuthenticationService)fContext.getBean("AuthenticationService");
        try
        {
            fService.setStoreProperty("main", QName.createQName(null, ".dns.main"), 
                    new PropertyValue(QName.createQName(null, "silly"), "Nothing."));
            fService.createStore("test");
            fService.setStoreProperty("test", QName.createQName(null, ".dns.test.main"), 
                    new PropertyValue(QName.createQName(null, "silly"), "Nothing."));
            setupBasicTree0();
            authService.authenticateAsGuest();
            assertEquals(0, fLockingService.getUsersLocks("admin").size());
            List<AVMDifference> diffs = fSyncService.compare(-1, "main:/", -1, "test:/", null);
            fSyncService.update(diffs, null, false, false, false, false, null, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
        finally
        {
            fService = oldService;
            fLockingService.removeWebProject("main");
            authService.authenticate("admin", "admin".toCharArray());
        }
    }
    
    /**
     * Test async indexing.
     * @throws Exception
     */
    public void testAsyncIndex() throws Exception
    {
        // Make sure the slate is clean ...
        UserTransaction tx = fTransactionService.getUserTransaction();
        tx.begin();
        if(fService.getStore("avmAsynchronousTest") != null)
        {
            fService.purgeStore("avmAsynchronousTest");
        }
        StoreRef storeRef = AVMNodeConverter.ToStoreRef("avmAsynchronousTest");
        Indexer indexer = fIndexerAndSearcher.getIndexer(storeRef);
        if(indexer instanceof AVMLuceneIndexer)
        {
            AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer)indexer;
            avmIndexer.deleteIndex("avmAsynchronousTest", IndexMode.SYNCHRONOUS);
        }
        tx.commit();
      
        // TODO: Suspend and resume indexing in case we are really unlucky and hit an index before we expect it.
        
        SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
        ResultSet results;
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(0, results.length());
        results.close();
        
        fService.createStore("avmAsynchronousTest");
        fService.createSnapshot("avmAsynchronousTest", null, null);
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(1, results.length());
        results.close();
        
        fService.createDirectory("avmAsynchronousTest:/", "a");
        fService.createDirectory("avmAsynchronousTest:/a", "b");
        fService.createDirectory("avmAsynchronousTest:/a/b", "c");
        fService.createSnapshot("avmAsynchronousTest", null, null);
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(1, results.length());
        results.close();
        
        Thread.sleep(180000);
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(4, results.length());
        results.close();
        
        fService.purgeStore("avmAsynchronousTest");
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(0, results.length());
        results.close();
        
        fService.createStore("avmAsynchronousTest");
        fService.createSnapshot("avmAsynchronousTest", null, null);
        fService.createDirectory("avmAsynchronousTest:/", "a");
        fService.createDirectory("avmAsynchronousTest:/a", "b");
        fService.createDirectory("avmAsynchronousTest:/a/b", "c");
        fService.createSnapshot("avmAsynchronousTest", null, null);
        fService.purgeStore("avmAsynchronousTest");
        fService.createStore("avmAsynchronousTest");
        fService.createSnapshot("avmAsynchronousTest", null, null);
        fService.createDirectory("avmAsynchronousTest:/", "a");
        fService.createDirectory("avmAsynchronousTest:/a", "b");
        fService.createDirectory("avmAsynchronousTest:/a/b", "c");
        fService.createSnapshot("avmAsynchronousTest", null, null);
        
        Thread.sleep(180000);
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(4, results.length());
        results.close();
    }
    
    public void testForceCopyDeleted()
    {
        try
        {
            setupBasicTree();
            fService.removeNode("main:/a/b/c/foo");
            fService.createSnapshot("main", null, null);
            AVMNodeDescriptor desc = fService.forceCopy("main:/a/b/c/foo");
            assertTrue(desc.isDeleted());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    public void testLayerSnapshots()
    {
        try
        {
            // Layers are not yet indexed
            setupBasicTree();
            assertEquals(1, fService.createSnapshot("main", null, null).get("main").intValue());
            runQueriesAgainstBasicTree("main");
            fService.createStore("layer");
            fService.createLayeredDirectory("main:/a", "layer:/", "a");
            fService.createSnapshot("layer", null, null);
            runQueriesAgainstBasicTree("main");
            fService.createFile("main:/a", "Xander");
            fService.createSnapshot("layer", null, null);
            assertEquals(2, fService.lookup(2, "layer:/a").getIndirectionVersion());
            assertEquals(fService.lookup(2, "main:/a/Xander").getId(), fService.lookup(2, "layer:/a/Xander").getId());
            assertNull(fService.lookup(1, "layer:/a/Xander"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    public void testBranchLayerSnapshot()
    {
        try
        {
            // layers are not ye indexed
            setupBasicTree();
            fService.createStore("layer");
            fService.createDirectory("layer:/", "root");
            fService.createLayeredDirectory("main:/a", "layer:/root", "layer");
            fService.createSnapshot("layer", null, null);
            fService.createFile("main:/a", "dummy").close();
            fService.createFile("layer:/root/layer", "pygmy");
            fService.createSnapshot("layer", null, null);
            fService.createStore("branch");
            fService.createBranch(1, "layer:/root", "branch:/", "branch");
            fService.createSnapshot("branch", null, null);
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            assertEquals(fService.lookup(1, "main:/a/b/c/foo").getId(), fService.lookup(1,
                    "branch:/branch/layer/b/c/foo").getId());
            assertEquals(fService.lookup(-1, "main:/a/b/c/foo").getId(), fService.lookup(-1,
                    "branch:/branch/layer/b/c/foo").getId());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test Deployment.
     */
    public void testDeployment()
    {
        try
        {
            DeploymentService depService = (DeploymentService) fContext.getBean("DeploymentService");
            setupBasicTree();
            fService.addAspect("main:/a", ContentModel.ASPECT_REFERENCEABLE);
            fService.addAspect("main:/a/b", ContentModel.ASPECT_REFERENCEABLE);
            fService.addAspect("main:/a/b/c", ContentModel.ASPECT_REFERENCEABLE);
            fService.addAspect("main:/a/b/c/foo", ContentModel.ASPECT_REFERENCEABLE);
            fService.addAspect("main:/a/b/c/bar", ContentModel.ASPECT_REFERENCEABLE);
            // TestDeploymentCallback callback = new TestDeploymentCallback();
            fService.createStore("target");
            DeploymentReport report = depService.deployDifference(-1, "main:/a", "localhost", 50500, "admin", "admin",
                    "target:/a", false, false, false, null);
            runQueriesAgainstBasicTreeWithAOnly("target");
            System.out.println(report);
            assertEquals(fService.lookup(-1, "main:/a/b/c/foo").getGuid(), fService.lookup(-1, "target:/a/b/c/foo")
                    .getGuid());
            assertEquals(fService.lookup(-1, "main:/a/b/c/bar").getGuid(), fService.lookup(-1, "target:/a/b/c/bar")
                    .getGuid());
            ContentData srcCD = fService.getContentDataForRead(-1, "main:/a/b/c/foo");
            ContentData dstCD = fService.getContentDataForRead(-1, "target:/a/b/c/foo");
            assertEquals(srcCD.getMimetype(), dstCD.getMimetype());
            fService.createFile("main:/a/b", "biz").close();
            report = depService.deployDifference(-1, "main:/a", "localhost", 50500, "admin", "admin", "target:/a",
                    false, false, true, null);
            // Nothing was done
            runQueriesAgainstBasicTreeWithAOnly("target");
            System.out.println(report);
            System.out.println(recursiveList("target", -1, true));
            assertNull(fService.lookup(-1, "target:/a/b/biz"));
            report = depService.deployDifference(-1, "main:/a", "localhost", 50500, "admin", "admin", "target:/a",
                    false, false, false, null);
            System.out.println(report);
            runQueriesForCreateAndDeploy("target");
            assertEquals(fService.lookup(-1, "main:/a/b/biz").getGuid(), fService.lookup(-1, "target:/a/b/biz")
                    .getGuid());
            fService.removeNode("main:/a/b/c/foo");
            report = depService.deployDifference(-1, "main:/a", "localhost", 50500, "admin", "admin", "target:/a",
                    false, true, false, null);
            runQueriesForCreateAndDeploy("target");
            System.out.println(report);
            assertNotNull(fService.lookup(-1, "target:/a/b/c/foo"));
            report = depService.deployDifference(-1, "main:/a", "localhost", 50500, "admin", "admin", "target:/a",
                    false, false, false, null);
            System.out.println(report);
            runQueriesForRemoveAndDelpoy("target");
            assertNull(fService.lookup(-1, "target:/a/b/c/foo"));
            fService.removeNode("main:/a/b/c/bar");
            fService.createDirectory("main:/a/b/c", "bar");
            report = depService.deployDifference(-1, "main:/a", "localhost", 50500, "admin", "admin", "target:/a",
                    false, false, false, null);
            System.out.println(report);
            runQueriesForRemoveFileAndCreateDirectoryAndDeploy("target");
            report = depService.deployDifference(-1, "main:/a", "localhost", 50500, "admin", "admin", "target:/a",
                    false, false, false, null);
            depService.deployDifference(-1, "main:/a", "localhost", 50500, "admin", "admin", "target2:/wiggly/diggly",
                    true, false, false, null);
            System.out.println(report);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    protected void runQueriesForCreateAndDeploy(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);

        // Text index
        SearchService searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef(store), true);
        ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"I am main\"");
        assertEquals(2, results.length());
        results.close();

        // Basic properties

        // Note "a" is a stop word and therefore not findable ...

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":\"foo\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":foo");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":biz");
        assertEquals(1, results.length());
        results.close();

        // TODO: Fix auth in AVMDiskDriver and more??

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_CREATOR)
                + ":admin");
        assertEquals(7, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_MODIFIER)
                + ":admin");
        assertEquals(7, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_OWNER)
                + ":admin");
        assertEquals(7, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NODE_UUID)
                + ":unknown");
        assertEquals(7, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                + ContentModel.PROP_STORE_PROTOCOL)
                + ":avm");
        assertEquals(7, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                + ContentModel.PROP_STORE_IDENTIFIER)
                + ":" + store);
        assertEquals(7, results.length());
        results.close();

        // Basic paths

        results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/biz\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/foo\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/bar\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(7, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
        assertEquals(5, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
        assertEquals(0, results.length());
        results.close();
    }

    protected void runQueriesForRemoveAndDelpoy(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);

        // Text index
        SearchService searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef(store), true);
        ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"I am main\"");
        assertEquals(1, results.length());
        results.close();

        // Basic properties

        // Note "a" is a stop word and therefore not findable ...

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":\"foo\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":foo");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":biz");
        assertEquals(1, results.length());
        results.close();

        // TODO: Fix auth in AVMDiskDriver and more??

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_CREATOR)
                + ":admin");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_MODIFIER)
                + ":admin");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_OWNER)
                + ":admin");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NODE_UUID)
                + ":unknown");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                + ContentModel.PROP_STORE_PROTOCOL)
                + ":avm");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                + ContentModel.PROP_STORE_IDENTIFIER)
                + ":" + store);
        assertEquals(6, results.length());
        results.close();

        // Basic paths

        results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/biz\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/foo\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/bar\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
        assertEquals(5, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
        assertEquals(5, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
        assertEquals(4, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
        assertEquals(0, results.length());
        results.close();
    }

    protected void runQueriesForRemoveFileAndCreateDirectoryAndDeploy(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);

        // Text index
        SearchService searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef(store), true);
        ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"I am main\"");
        assertEquals(0, results.length());
        results.close();

        // Basic properties

        // Note "a" is a stop word and therefore not findable ...

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":\"foo\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":\"bar\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":foo");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":biz");
        assertEquals(1, results.length());
        results.close();

        // TODO: Fix auth in AVMDiskDriver and more??

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_CREATOR)
                + ":admin");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_MODIFIER)
                + ":admin");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_OWNER)
                + ":admin");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NODE_UUID)
                + ":unknown");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                + ContentModel.PROP_STORE_PROTOCOL)
                + ":avm");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                + ContentModel.PROP_STORE_IDENTIFIER)
                + ":" + store);
        assertEquals(6, results.length());
        results.close();

        // Basic paths

        results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/biz\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/foo\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/bar\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
        assertEquals(0, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(6, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
        assertEquals(5, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
        assertEquals(5, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
        assertEquals(4, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
        assertEquals(0, results.length());
        results.close();
    }

    /**
     * Test of GUIDs on AVM Nodes.
     */
    public void testGuids()
    {
        try
        {
            setupBasicTree();
            System.out.println(fService.lookup(-1, "main:/a/b/c/foo").getGuid());
            String guid = GUID.generate();
            fService.setGuid("main:/a/b/c/foo", guid);
            assertEquals(guid, fService.lookup(-1, "main:/a/b/c/foo").getGuid());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test the revert to version action.
     */
    public void testRevertToVersionAction()
    {
        try
        {
            setupBasicTree();
            ContentWriter writer = fService.getContentWriter("main:/a/b/c/foo");
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.putContent("I am main:/a/b/c/foo V1");
            fService.createSnapshot("main", "v1", null);
            writer = fService.getContentWriter("main:/a/b/c/foo");
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.putContent("I am main:/a/b/c/foo V2");
            fService.createSnapshot("main", "v2", null);
            writer = fService.getContentWriter("main:/a/b/c/foo");
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.putContent("I am main:/a/b/c/foo V3");
            fService.createSnapshot("main", "v3", null);
            writer = fService.getContentWriter("main:/a/b/c/foo");
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.putContent("I am main:/a/b/c/foo V4");
            fService.createSnapshot("main", "v4", null);
            writer = fService.getContentWriter("main:/a/b/c/foo");
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.putContent("I am main:/a/b/c/foo V5");
            fService.createSnapshot("main", "v5", null);
            writer = fService.getContentWriter("main:/a/b/c/foo");
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.putContent("I am main:/a/b/c/foo HEAD");
            StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");
            SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
            ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"HEAD\"");
            assertEquals(0, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", "TEXT:\"V5\"");
            assertEquals(1, results.length());
            results.close();

            AVMNodeDescriptor desc = fService.lookup(-1, "main:/a/b/c/foo");
            List<AVMNodeDescriptor> history = fService.getHistory(desc, 100);
            AVMNodeDescriptor toRevert = history.get(3);
            final ActionImpl action = new ActionImpl(null, GUID.generate(), AVMRevertToVersionAction.NAME);
            action.setParameterValue(AVMRevertToVersionAction.TOREVERT, toRevert);
            final AVMRevertToVersionAction revert = (AVMRevertToVersionAction) fContext
                    .getBean("avm-revert-to-version");
            class TxnWork implements TransactionUtil.TransactionWork<Object>
            {
                public Object doWork() throws Exception
                {
                    revert.execute(action, AVMNodeConverter.ToNodeRef(-1, "main:/a/b/c/foo"));
                    return null;
                }
            }
            ;
            TransactionUtil.executeInUserTransaction((TransactionService) fContext.getBean("transactionComponent"),
                    new TxnWork());
            results = searchService.query(storeRef, "lucene", "TEXT:\"HEAD\"");
            assertEquals(0, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", "TEXT:\"V5\"");
            assertEquals(1, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", "TEXT:\"V2\"");
            assertEquals(0, results.length());
            results.close();

            fService.createSnapshot("main", "reverted", null);

            results = searchService.query(storeRef, "lucene", "TEXT:\"HEAD\"");
            assertEquals(0, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", "TEXT:\"V5\"");
            assertEquals(0, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", "TEXT:\"V2\"");
            assertEquals(1, results.length());
            results.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test version numbering.
     */
    public void testVersionNumbering()
    {
        try
        {
            setupBasicTree();
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            AVMNodeDescriptor desc = fService.lookup(-1, "main:/a/b/c/foo");
            assertEquals(2, desc.getVersionID());
            desc = fService.lookup(-1, "main:/a/b");
            assertEquals(2, desc.getVersionID());
            fService.createSnapshot("main", null, null);
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            fService.getFileOutputStream("main:/layer/b/c/bar").close();
            desc = fService.lookup(-1, "main:/layer/b/c/bar");
            assertEquals(2, desc.getVersionID());
            desc = fService.lookup(-1, "main:/layer/b/c");
            assertEquals(3, desc.getVersionID());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test relinking of nodes to history.
     */
    public void testHistoryRelink()
    {
        try
        {
            setupBasicTree();
            fService.createStore("branch");
            fService.createBranch(-1, "main:/a", "branch:/", "a");
            fService.removeNode("branch:/a/b/c/foo");
            List<AVMDifference> diffs = fSyncService.compare(-1, "branch:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            assertEquals(AVMDifference.NEWER, diffs.get(0).getDifferenceCode());
            fService.createFile("branch:/a/b/c", "foo").close();
            diffs = fSyncService.compare(-1, "branch:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            assertEquals(AVMDifference.NEWER, diffs.get(0).getDifferenceCode());
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.removeNode("branch:/a/b/c/bar");
            fService.createFile("branch:/a/b/c", "pismo").close();
            fService.rename("branch:/a/b/c", "pismo", "branch:/a/b/c", "bar");
            diffs = fSyncService.compare(-1, "branch:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            assertEquals(AVMDifference.NEWER, diffs.get(0).getDifferenceCode());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test renaming a store.
     */
    public void testRenameStore()
    {
        try
        {
            setupBasicTree();
            assertNotNull(fService.lookup(-1, "main:/a/b"));
            fService.renameStore("main", "foo");
            assertNotNull(fService.getStore("foo"));
            assertNotNull(fService.lookup(-1, "foo:/a/b"));
            runQueriesAgainstBasicTree("foo");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test copy.
     */
    public void testCopy()
    {
        try
        {
            setupBasicTree();
            // Copy a file.
            fService.copy(-1, "main:/a/b/c/foo", "main:/d", "fooCopy");
            AVMNodeDescriptor desc = fService.lookup(-1, "main:/d/fooCopy");
            assertTrue(desc.isFile());
            fService.createSnapshot("main", null, null);
            runQueriesAgainstBasicTreePlusFileCopy("main");

            // Copy a whole tree
            fService.copy(-1, "main:/a", "main:/d/e", "aCopy");
            desc = fService.lookup(-1, "main:/d/e/aCopy");
            assertTrue(desc.isDirectory());
            desc = fService.lookup(-1, "main:/a/b/c/bar");
            AVMNodeDescriptor desc2 = fService.lookup(-1, "main:/d/e/aCopy/b/c/bar");
            assertTrue(desc2.isFile());
            assertEquals(desc.getLength(), desc2.getLength());
            fService.createSnapshot("main", null, null);
            runQueriesAgainstBasicTreePlusFileCopyAndDirectoryCopy("main");

            // Check that it rejects infinite copies.
            try
            {
                fService.copy(-1, "main:/", "main://d/e", "illegal");
                fail();
            }
            catch (AVMException ae)
            {
                // This is a success.
            }

            StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");
            SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
            ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"tutorial\"");
            assertEquals(0, results.length());
            results.close();

            CrossRepositoryCopyService copyService = (CrossRepositoryCopyService) fContext
                    .getBean("CrossRepositoryCopyService");
            RepoRemote remoteService = (RepoRemote) fContext.getBean("RepoRemoteService");
            Pair<NodeRef, Boolean> toCopy = remoteService.lookup(remoteService.getRoot(), "Guest Home");
            copyService.copy(toCopy.getFirst(), AVMNodeConverter.ToNodeRef(-1, "main:/"), "Guest Home");
            desc = fService.lookup(-1, "main:/Guest Home");
            assertTrue(desc.isDirectory());
            System.out.println(this.recursiveList("main", -1, true));

            fService.createSnapshot("main", null, null);
            searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
            results = searchService.query(storeRef, "lucene", "TEXT:\"tutorial\"");
            assertEquals(1, results.length());
            results.close();

            StoreRef spacesStore = new StoreRef("workspace", "SpacesStore");

            searchService = fIndexerAndSearcher.getSearcher(spacesStore, true);
            results = searchService.query(spacesStore, "lucene", "PATH:\"//.\"");
            int count = results.length();
            results.close();

            copyService.copy(AVMNodeConverter.ToNodeRef(-1, "main:/a"), toCopy.getFirst(), "a");
            Pair<NodeRef, Boolean> theCopy = remoteService.lookup(toCopy.getFirst(), "a");
            assertTrue(theCopy.getSecond());

            results = searchService.query(spacesStore, "lucene", "PATH:\"//.\"");
            assertEquals(count + 5, results.length());
            results.close();

            results = searchService.query(spacesStore, "lucene", "PATH:\"//.\"");
            assertEquals(count + 5, results.length());
            results.close();

            results = searchService.query(spacesStore, "lucene", "@cm\\:name:foo");
            assertEquals(1, results.length());
            results.close();

            remoteService.removeNode(theCopy.getFirst());

            results = searchService.query(spacesStore, "lucene", "PATH:\"//.\"");
            assertEquals(count, results.length());
            results.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    protected void runQueriesAgainstBasicTreePlusFileCopy(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);

        // Text index
        SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
        ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"I am main\"");
        assertEquals(3, results.length());
        results.close();

        // Basic properties

        // Note "a" is a stop word and therefore not findable ...

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":\"foo\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":foo");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":\"fooCopy\"");
        assertEquals(1, results.length());
        results.close();

        // TODO: Fix auth in AVMDiskDriver and more??

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_CREATOR)
                + ":admin");
        assertEquals(10, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_MODIFIER)
                + ":admin");
        assertEquals(10, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_OWNER)
                + ":admin");
        assertEquals(10, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NODE_UUID)
                + ":unknown");
        assertEquals(10, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                + ContentModel.PROP_STORE_PROTOCOL)
                + ":avm");
        assertEquals(10, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                + ContentModel.PROP_STORE_IDENTIFIER)
                + ":" + store);
        assertEquals(10, results.length());
        results.close();

        // Basic paths

        results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/foo\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/bar\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/fooCopy\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(10, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
        assertEquals(9, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
        assertEquals(5, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
        assertEquals(4, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
        assertEquals(3, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
        assertEquals(0, results.length());
        results.close();
    }

    protected void runQueriesAgainstBasicTreePlusFileCopyAndDirectoryCopy(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);

        // Text index
        SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
        ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"I am main\"");
        assertEquals(5, results.length());
        results.close();

        // Basic properties

        // Note "a" is a stop word and therefore not findable ...

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":\"foo\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":foo");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                + ":\"fooCopy\"");
        assertEquals(1, results.length());
        results.close();

        // TODO: Fix auth in AVMDiskDriver and more??

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_CREATOR)
                + ":admin");
        assertEquals(15, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_MODIFIER)
                + ":admin");
        assertEquals(15, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_OWNER)
                + ":admin");
        assertEquals(15, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NODE_UUID)
                + ":unknown");
        assertEquals(15, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                + ContentModel.PROP_STORE_PROTOCOL)
                + ":avm");
        assertEquals(15, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                + ContentModel.PROP_STORE_IDENTIFIER)
                + ":" + store);
        assertEquals(15, results.length());
        results.close();

        // Basic paths

        results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/foo\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/bar\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/fooCopy\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/aCopy\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/aCopy/b\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/aCopy/b/c\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/aCopy/b/c/foo\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/aCopy/b/c/bar\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(15, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
        assertEquals(14, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
        assertEquals(5, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
        assertEquals(4, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
        assertEquals(4, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
        assertEquals(3, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
        assertEquals(3, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
        assertEquals(3, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
        assertEquals(1, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*/*\"");
        assertEquals(2, results.length());
        results.close();

        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*/*/*\"");
        assertEquals(0, results.length());
        results.close();
    }

    /**
     * Test cyclic lookup behavior.
     */
    public void testCyclicLookup()
    {
        try
        {
            fService.createDirectory("main:/", "a");
            fService.createFile("main:/a", "foo").close();
            for (int i = 0; i < 1000; i++)
            {
                fService.lookup(-1, "main:/a/bar");
            }
            fService.lookup(-1, "main:/a/foo");
            fService.createLayeredDirectory("main:/c", "main:/", "b");
            fService.createLayeredDirectory("main:/b", "main:/", "c");
            try
            {
                fService.lookup(-1, "main:/b/bar");
                fail();
            }
            catch (AVMCycleException ce)
            {
                // Do nothing; this means success.
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test getting all paths for a node.
     */
    public void testGetPaths()
    {
        try
        {
            setupBasicTree();
            fService.createBranch(-1, "main:/a", "main:/", "abranch");
            fService.createSnapshot("main", null, null);
            fService.createBranch(-1, "main:/a/b", "main:/", "bbranch");
            List<Pair<Integer, String>> paths = fService.getPaths(fService.lookup(-1, "main:/a/b/c/foo"));
            for (Pair<Integer, String> path : paths)
            {
                System.out.println(path.getFirst() + " " + path.getSecond());
            }
            paths = fService.getHeadPaths(fService.lookup(-1, "main:/a/b/c/foo"));
            System.out.println("------------------------------");
            for (Pair<Integer, String> path : paths)
            {
                System.out.println(path.getFirst() + " " + path.getSecond());
            }
            paths = fService.getPathsInStoreHead(fService.lookup(-1, "main:/a/b/c/foo"), "main");
            System.out.println("------------------------------");
            for (Pair<Integer, String> path : paths)
            {
                System.out.println(path.getFirst() + " " + path.getSecond());
            }
            assertNotNull(fService.getAPath(fService.lookup(-1, "main:/a/b/c/foo")));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test partial flatten.
     */
    public void testPartialFlatten()
    {
        try
        {
            setupBasicTree();
            fService.createStore("layer");
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
            assertEquals(1, fSyncService.compare(-1, "layer:/a", -1, "main:/a", null).size());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test getIndirection.
     */
    public void testGetIndirection()
    {
        try
        {
            setupBasicTree();
            fService.createStore("layer");
            fService.createLayeredDirectory("main:/a", "layer:/", "layer");
            assertEquals("main:/a", fService.getIndirectionPath(-1, "layer:/layer"));
            assertEquals("main:/a/b", fService.getIndirectionPath(-1, "layer:/layer/b"));
            assertEquals("main:/a/b/c", fService.getIndirectionPath(-1, "layer:/layer/b/c"));
            assertEquals("main:/a/b/c/foo", fService.getIndirectionPath(-1, "layer:/layer/b/c/foo"));
            fService.createLayeredDirectory("main:/d", "layer:/layer/b", "dlayer");
            assertEquals("main:/d", fService.getIndirectionPath(-1, "layer:/layer/b/dlayer"));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test the revert list action.
     */
    public void testRevertListAction()
    {
        try
        {
            setupBasicTree();
            fService.createStore("area");
            fService.createLayeredDirectory("main:/a", "area:/", "a");
            fService.getFileOutputStream("area:/a/b/c/foo").close();
            List<AVMDifference> diffs = fSyncService.compare(-1, "area:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.getFileOutputStream("area:/a/b/c/bar").close();
            diffs = fSyncService.compare(-1, "area:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            final ActionImpl action = new ActionImpl(null, GUID.generate(), AVMRevertListAction.NAME);
            List<String> paths = new ArrayList<String>();
            paths.add("area:/a/b");
            action.setParameterValue(AVMRevertListAction.PARAM_VERSION, fService.getLatestSnapshotID("area"));
            action.setParameterValue(AVMRevertListAction.PARAM_NODE_LIST, (Serializable) paths);
            final AVMRevertListAction revert = (AVMRevertListAction) fContext.getBean("avm-revert-list");
            class TxnWork implements TransactionUtil.TransactionWork<Object>
            {
                public Object doWork() throws Exception
                {
                    revert.execute(action, null);
                    return null;
                }
            }
            ;
            TransactionUtil.executeInUserTransaction((TransactionService) fContext.getBean("transactionComponent"),
                    new TxnWork());
            diffs = fSyncService.compare(-1, "area:/a", -1, "main:/a", null);
            assertEquals(0, diffs.size());
            System.out.println(recursiveList("area", -1, true));
            System.out.println(recursiveList("main", -1, true));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test the undo list action.
     */
    public void testUndoListAction()
    {
        try
        {
            setupBasicTree();
            fService.createStore("area");
            fService.createLayeredDirectory("main:/a", "area:/", "a");
            fService.getFileOutputStream("area:/a/b/c/foo").close();
            List<AVMDifference> diffs = fSyncService.compare(-1, "area:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.getFileOutputStream("area:/a/b/c/bar").close();
            diffs = fSyncService.compare(-1, "area:/a", -1, "main:/a", null);
            assertEquals(1, diffs.size());
            final ActionImpl action = new ActionImpl(null, GUID.generate(), AVMUndoSandboxListAction.NAME);
            List<Pair<Integer, String>> versionPaths = new ArrayList<Pair<Integer, String>>();
            versionPaths.add(new Pair<Integer, String>(-1, "area:/a/b/c/bar"));
            action.setParameterValue(AVMUndoSandboxListAction.PARAM_NODE_LIST, (Serializable) versionPaths);
            final AVMUndoSandboxListAction revert = (AVMUndoSandboxListAction) fContext.getBean("avm-undo-list");
            class TxnWork implements TransactionUtil.TransactionWork<Object>
            {
                public Object doWork() throws Exception
                {
                    revert.execute(action, null);
                    return null;
                }
            }
            ;
            TransactionUtil.executeInUserTransaction((TransactionService) fContext.getBean("transactionComponent"),
                    new TxnWork());
            diffs = fSyncService.compare(-1, "area:/a", -1, "main:/a", null);
            assertEquals(0, diffs.size());
            System.out.println(recursiveList("area", -1, true));
            System.out.println(recursiveList("main", -1, true));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test the promote action.
     */
    public void testPromoteAction()
    {
        try
        {
            setupBasicTree();
            fService.createDirectory("main:/", JNDIConstants.DIR_DEFAULT_WWW);
            fService.rename("main:/", "a", "main:/" + JNDIConstants.DIR_DEFAULT_WWW, "a");
            fService.rename("main:/", "d", "main:/" + JNDIConstants.DIR_DEFAULT_WWW, "d");
            fService.createSnapshot("main", null, null);
            fService.createStore("source");
            fService.createLayeredDirectory("main:/" + JNDIConstants.DIR_DEFAULT_WWW, "source:/",
                    JNDIConstants.DIR_DEFAULT_WWW);
            fService.getFileOutputStream("source:/" + JNDIConstants.DIR_DEFAULT_WWW + "/a/b/c/foo").close();
            final ActionImpl action = new ActionImpl(AVMNodeConverter.ToNodeRef(-1, "source:/"
                    + JNDIConstants.DIR_DEFAULT_WWW + "/a"), GUID.generate(), SimpleAVMPromoteAction.NAME);
            action.setParameterValue(SimpleAVMPromoteAction.PARAM_TARGET_STORE, "main");
            final SimpleAVMPromoteAction promote = (SimpleAVMPromoteAction) fContext.getBean("simple-avm-promote");
            class TxnWork implements TransactionUtil.TransactionWork<Object>
            {
                public Object doWork() throws Exception
                {
                    promote.execute(action, AVMNodeConverter.ToNodeRef(-1, "source:/"
                            + JNDIConstants.DIR_DEFAULT_WWW + "/a"));
                    return null;
                }
            }
            ;
            TransactionUtil.executeInUserTransaction((TransactionService) fContext.getBean("transactionComponent"),
                    new TxnWork());
            assertEquals(0, fSyncService.compare(-1, "source:/" + JNDIConstants.DIR_DEFAULT_WWW, -1,
                    "main:/" + JNDIConstants.DIR_DEFAULT_WWW, null).size());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test a noodle update.
     */
    public void testNoodleUpdate()
    {
        try
        {
            setupBasicTree();
            fService.createStore("staging");
            List<AVMDifference> diffs = fSyncService.compare(-1, "main:/", -1, "staging:/", null);
            assertEquals(2, diffs.size());
            List<AVMDifference> noodle = new ArrayList<AVMDifference>();
            noodle.add(new AVMDifference(-1, "main:/a/b/c/foo", -1, "staging:/a/b/c/foo", AVMDifference.NEWER));
            noodle.add(new AVMDifference(-1, "main:/d", -1, "staging:/d", AVMDifference.NEWER));
            fSyncService.update(noodle, null, false, false, false, false, null, null);
            diffs = fSyncService.compare(-1, "main:/", -1, "staging:/", null);
            assertEquals(1, diffs.size());
            assertEquals("main:/a/b/c/bar", diffs.get(0).getSourcePath());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test the SimpleAVMSubmitAction.
     */
    public void testSubmitAction()
    {
        try
        {
            fService.createStore("foo-staging");
            fService.createDirectory("foo-staging:/", JNDIConstants.DIR_DEFAULT_WWW);
            fService.createDirectory("foo-staging:/" + JNDIConstants.DIR_DEFAULT_WWW, "a");
            fService.createDirectory("foo-staging:/" + JNDIConstants.DIR_DEFAULT_WWW + "/a", "b");
            fService.createDirectory("foo-staging:/" + JNDIConstants.DIR_DEFAULT_WWW + "/a/b", "c");
            fService.createFile("foo-staging:/" + JNDIConstants.DIR_DEFAULT_WWW + "/a/b/c", "foo").close();
            fService.createFile("foo-staging:/" + JNDIConstants.DIR_DEFAULT_WWW + "/a/b/c", "bar").close();
            fService.createStore("area");
            fService.setStoreProperty("area", QName.createQName(null, ".website.name"), new PropertyValue(null, "foo"));
            fService.createLayeredDirectory("foo-staging:/" + JNDIConstants.DIR_DEFAULT_WWW, "area:/",
                    JNDIConstants.DIR_DEFAULT_WWW);
            fService.createFile("area:/" + JNDIConstants.DIR_DEFAULT_WWW, "figs").close();
            fService.getFileOutputStream("area:/" + JNDIConstants.DIR_DEFAULT_WWW + "/a/b/c/foo").close();
            fService.removeNode("area:/" + JNDIConstants.DIR_DEFAULT_WWW + "/a/b/c/bar");
            List<AVMDifference> diffs = fSyncService.compare(-1, "area:/" + JNDIConstants.DIR_DEFAULT_WWW, -1,
                    "foo-staging:/" + JNDIConstants.DIR_DEFAULT_WWW, null);
            assertEquals(3, diffs.size());
            final SimpleAVMSubmitAction action = (SimpleAVMSubmitAction) fContext.getBean("simple-avm-submit");
            class TxnWork implements TransactionUtil.TransactionWork<Object>
            {
                public Object doWork() throws Exception
                {
                    action.execute(null, AVMNodeConverter.ToNodeRef(-1, "area:/" + JNDIConstants.DIR_DEFAULT_WWW));
                    return null;
                }
            }
            ;
            TxnWork worker = new TxnWork();
            TransactionUtil.executeInUserTransaction((TransactionService) fContext.getBean("transactionComponent"),
                    worker);
            diffs = fSyncService.compare(-1, "area:/" + JNDIConstants.DIR_DEFAULT_WWW, -1, "foo-staging:/"
                    + JNDIConstants.DIR_DEFAULT_WWW, null);

            assertEquals(0, diffs.size());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test one argument remove.
     */
    public void testOneArgRemove()
    {
        try
        {
            setupBasicTree();
            fService.removeNode("main:/a/b/c/foo/");
            fService.removeNode("main://d");
            fService.createSnapshot("main", null, null);

            StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");

            // Text index
            SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
            ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"I am main\"");
            assertEquals(1, results.length());
            results.close();

            // Basic properties

            // Note "a" is a stop word and therefore not findable ...

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                    + ":\"foo\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                    + ":foo");
            assertEquals(0, results.length());
            results.close();

            // TODO: Fix auth in AVMDiskDriver and more??

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_CREATOR)
                    + ":admin");
            if (results.length() == 6)
            {
                for (ResultSetRow row : results)
                {
                    System.out.println(row.getNodeRef());
                }
            }
            assertEquals(5, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser
                    .escape("@" + ContentModel.PROP_MODIFIER)
                    + ":admin");
            assertEquals(5, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_OWNER)
                    + ":admin");
            assertEquals(5, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                    + ContentModel.PROP_NODE_UUID)
                    + ":unknown");
            assertEquals(5, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                    + ContentModel.PROP_STORE_PROTOCOL)
                    + ":avm");
            assertEquals(5, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                    + ContentModel.PROP_STORE_IDENTIFIER)
                    + ":" + "main");
            assertEquals(5, results.length());
            results.close();

            // Basic paths

            results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/foo\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/bar\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
            assertEquals(5, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
            assertEquals(4, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
            assertEquals(4, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
            assertEquals(3, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
            assertEquals(0, results.length());
            results.close();

            try
            {
                fService.removeNode("main://");
                fail();
            }
            catch (AVMException e)
            {
                // Do nothing.
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Test that non head version sources are update correctly.
     */
    public void testVersionUpdate()
    {
        try
        {
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            fService.createStore("source");
            fService.createStore("dest");
            loader.recursiveLoad("config/alfresco/bootstrap", "source:/");
            int version1 = fService.createSnapshot("source", null, null).get("source");
            loader.recursiveLoad("config/alfresco/extension", "source:/");
            int version2 = fService.createSnapshot("source", null, null).get("source");
            List<AVMDifference> diffs = fSyncService.compare(version1, "source:/", -1, "dest:/", null);
            fService.createSnapshot("dest", null, null);
            assertEquals(1, diffs.size());
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            diffs = fSyncService.compare(version1, "source:/", -1, "dest:/", null);
            assertEquals(0, diffs.size());
            diffs = fSyncService.compare(version2, "source:/", -1, "dest:/", null);
            assertEquals(1, diffs.size());
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.createSnapshot("dest", null, null);
            diffs = fSyncService.compare(version2, "source:/", -1, "dest:/", null);
            assertEquals(0, diffs.size());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test that an update forces a snapshot on the source.
     */
    public void testUpdateSnapshot()
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
            // Now update.
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            diffs = fSyncService.compare(-1, "branch:/branch", -1, "main:/", null);
            assertEquals(0, diffs.size());
            fService.getFileOutputStream("branch:/branch/a/b/fing").close();
            assertTrue(fService.lookup(-1, "branch:/branch/a/b/fing").getId() != fService.lookup(-1, "main:/a/b/fing")
                    .getId());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test that branching forces a snapshot on the source repository.
     */
    public void testBranchSnapshot()
    {
        try
        {
            setupBasicTree();
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            fService.createBranch(-1, "main:/a", "main:/", "abranch");
            assertEquals(fService.lookup(-1, "main:/a/b/c/foo").getId(), fService.lookup(-1, "main:/abranch/b/c/foo")
                    .getId());
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            assertTrue(fService.lookup(-1, "main:/a/b/c/foo").getId() != fService.lookup(-1, "main:/abranch/b/c/foo")
                    .getId());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test bulk update.
     */
    public void testBulkUpdate()
    {
        try
        {
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            fService.createStore("layer");
            fService.createLayeredDirectory("main:/", "layer:/", "layer");
            loader.recursiveLoad("config/alfresco/bootstrap", "layer:/layer");
            List<AVMDifference> diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            fService.createSnapshot("layer", null, null);
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.createSnapshot("main", null, null);
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(0, diffs.size());
            fSyncService.flatten("layer:/layer", "main:/");
            System.out.println("Layer:");
            System.out.println(recursiveList("layer", -1, true));
            System.out.println("Main:");
            System.out.println(recursiveList("main", -1, true));
            fService.createStore("layer2");
            fService.createLayeredDirectory("layer:/layer", "layer2:/", "layer");
            loader.recursiveLoad("config/alfresco/bootstrap", "layer2:/layer/bootstrap");
            fService.createSnapshot("layer2", null, null);
            diffs = fSyncService.compare(-1, "layer2:/layer", -1, "layer:/layer", null);
            assertEquals(1, diffs.size());
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            diffs = fSyncService.compare(-1, "layer2:/layer", -1, "layer:/layer", null);
            assertEquals(0, diffs.size());
            fSyncService.flatten("layer2:/layer", "layer:/layer");
            diffs = fSyncService.compare(-1, "layer:/layer", -1, "main:/", null);
            assertEquals(1, diffs.size());
            System.out.println("Layer2:");
            System.out.println(recursiveList("layer2", -1, true));
            System.out.println("Layer:");
            System.out.println(recursiveList("layer", -1, true));
            System.out.println("Main:");
            System.out.println(recursiveList("main", -1, true));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test the flatten operation, with a little bit of compare and update.
     */
    public void testFlatten()
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            // Change some stuff.
            fService.createFile("main:/layer/b", "fig").close();
            fService.getFileOutputStream("main:/layer/b/c/foo").close();
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            // Do a compare.
            List<AVMDifference> diffs = fSyncService.compare(-1, "main:/layer", -1, "main:/a", null);
            for (AVMDifference diff : diffs)
            {
                System.out.println(diff);
            }
            // Update.
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            System.out.println(recursiveList("main", -1, true));
            // Flatten.
            fSyncService.flatten("main:/layer", "main:/a");
            System.out.println(recursiveList("main", -1, true));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test of Descriptor indirection field.
     */
    public void testDescriptorIndirection()
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createFile("main:/layer/b/c", "bambino").close();
            AVMNodeDescriptor desc = fService.lookup(-1, "main:/layer");
            assertEquals("main:/a", desc.getIndirection());
            Map<String, AVMNodeDescriptor> list = fService.getDirectoryListing(-1, "main:/");
            assertEquals("main:/a", list.get("layer").getIndirection());
            desc = fService.lookup(-1, "main:/layer/b");
            assertEquals("main:/a/b", desc.getIndirection());
            list = fService.getDirectoryListing(-1, "main:/layer");
            assertEquals("main:/a/b", list.get("b").getIndirection());
            list = fService.getDirectoryListingDirect(-1, "main:/layer");
            assertEquals("main:/a/b", list.get("b").getIndirection());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test AVMSyncService update.
     */
    public void testUpdate()
    {
        try
        {
            NameMatcher excluder = (NameMatcher) fContext.getBean("globalPathExcluder");
            setupBasicTree();
            // Try branch to branch update.
            fService.createBranch(-1, "main:/a", "main:/", "abranch");
            fService.createFile("main:/abranch", "monkey").close();
            fService.createFile("main:/abranch", "#foo").close();
            fService.createFile("main:/abranch", "figs.tmp").close();
            fService.getFileOutputStream("main:/abranch/b/c/foo").close();
            System.out.println(recursiveList("main", -1, true));
            List<AVMDifference> cmp = fSyncService.compare(-1, "main:/abranch", -1, "main:/a", excluder);
            for (AVMDifference diff : cmp)
            {
                System.out.println(diff);
            }
            assertEquals(2, cmp.size());
            List<AVMDifference> diffs = new ArrayList<AVMDifference>();
            diffs.add(new AVMDifference(-1, "main:/abranch/monkey", -1, "main:/a/monkey", AVMDifference.NEWER));
            diffs.add(new AVMDifference(-1, "main:/abranch/b/c/foo", -1, "main:/a/b/c/foo", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            assertEquals(fService.lookup(-1, "main:/abranch/monkey").getId(), fService.lookup(-1, "main:/a/monkey")
                    .getId());
            assertEquals(fService.lookup(-1, "main:/abranch/b/c/foo").getId(), fService.lookup(-1, "main:/a/b/c/foo")
                    .getId());
            // Try updating a deletion.
            fService.removeNode("main:/abranch", "monkey");
            System.out.println(recursiveList("main", -1, true));
            cmp = fSyncService.compare(-1, "main:/abranch", -1, "main:/a", excluder);
            for (AVMDifference diff : cmp)
            {
                System.out.println(diff);
            }
            assertEquals(1, cmp.size());
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/abranch/monkey", -1, "main:/a/monkey", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            assertEquals(0, fSyncService.compare(-1, "main:/abranch", -1, "main:/a", excluder).size());
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            assertEquals(fService.lookup(-1, "main:/abranch/monkey", true).getId(), fService.lookup(-1,
                    "main:/a/monkey", true).getId());
            // Try one that should fail.
            fService.createFile("main:/abranch", "monkey").close();
            cmp = fSyncService.compare(-1, "main:/abranch", -1, "main:/a", excluder);
            for (AVMDifference diff : cmp)
            {
                System.out.println(diff);
            }
            assertEquals(1, cmp.size());
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
            System.out.println(recursiveList("main", -1, true));
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/a/monkey", -1, "main:/abranch/monkey", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, true, null, null);
            assertEquals(0, fSyncService.compare(-1, "main:/abranch", -1, "main:/a", excluder).size());
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            assertEquals(fService.lookup(-1, "main:/a/monkey", true).getId(), fService.lookup(-1,
                    "main:/abranch/monkey", true).getId());
            // Cleanup for layered tests.
            fService.purgeStore("main");
            fService.createStore("main");
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createFile("main:/layer", "monkey").close();
            fService.getFileOutputStream("main:/layer/b/c/foo").close();
            cmp = fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder);
            for (AVMDifference diff : cmp)
            {
                System.out.println(diff);
            }
            assertEquals(2, cmp.size());
            System.out.println(recursiveList("main", -1, true));
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/layer/monkey", -1, "main:/a/monkey", AVMDifference.NEWER));
            diffs.add(new AVMDifference(-1, "main:/layer/b/c/foo", -1, "main:/a/b/c/foo", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            assertEquals(0, fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder).size());
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            assertEquals(fService.lookup(-1, "main:/layer/monkey").getId(), fService.lookup(-1, "main:/a/monkey")
                    .getId());
            assertEquals(fService.lookup(-1, "main:/layer/b/c/foo").getId(), fService.lookup(-1, "main:/a/b/c/foo")
                    .getId());
            // Try updating a deletion.
            fService.removeNode("main:/layer", "monkey");
            System.out.println(recursiveList("main", -1, true));
            cmp = fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder);
            for (AVMDifference diff : cmp)
            {
                System.out.println(diff);
            }
            assertEquals(1, cmp.size());
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/layer/monkey", -1, "main:/a/monkey", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            assertEquals(0, fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder).size());
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            assertEquals(fService.lookup(-1, "main:/layer/monkey", true).getId(), fService.lookup(-1, "main:/a/monkey",
                    true).getId());
            // Try one that should fail.
            fService.createFile("main:/layer", "monkey").close();
            cmp = fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder);
            for (AVMDifference diff : cmp)
            {
                System.out.println(diff);
            }
            assertEquals(1, cmp.size());
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
            System.out.println(recursiveList("main", -1, true));
            diffs.clear();
            diffs.add(new AVMDifference(-1, "main:/a/monkey", -1, "main:/layer/monkey", AVMDifference.NEWER));
            fSyncService.update(diffs, null, false, false, false, true, null, null);
            assertEquals(0, fSyncService.compare(-1, "main:/layer", -1, "main:/a", excluder).size());
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            assertEquals(fService.lookup(-1, "main:/a/monkey", true).getId(), fService.lookup(-1, "main:/layer/monkey",
                    true).getId());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test link AVMService call.
     */
    public void testLink()
    {
        try
        {
            setupBasicTree();
            // Just try linking /a/b/c/foo into /a/b
            fService.link("main:/a/b", "foo", fService.lookup(-1, "main:/a/b/c/foo"));
            assertEquals(fService.lookup(-1, "main:/a/b/c/foo").getId(), fService.lookup(-1, "main:/a/b/foo").getId());
            // Try linking /a/b/c/bar to /a/b/foo. It should fail.
            System.out.println(recursiveList("main", -1, true));
            try
            {
                fService.link("main:/a/b", "foo", fService.lookup(-1, "main:/a/b/c/bar"));
                fail();
            }
            catch (AVMExistsException e)
            {
                // Do nothing. It's OK.
            }
            // Delete /a/b/foo, and link /a/b/c/foo into /a/b. This checks that
            // a deleted node is no impediment.
            fService.removeNode("main:/a/b", "foo");
            fService.link("main:/a/b", "foo", fService.lookup(-1, "main:/a/b/c/foo"));
            assertEquals(fService.lookup(-1, "main:/a/b/c/foo").getId(), fService.lookup(-1, "main:/a/b/foo").getId());
            // Delete /a/b/foo again in prep for layer tests.
            fService.removeNode("main:/a/b", "foo");
            System.out.println(recursiveList("main", -1, true));
            fService.createSnapshot("main", null, null);
            // Create a layer do a link from /layer/b/c/bar to /layer/b
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.link("main:/layer/b", "bar", fService.lookup(-1, "main:/layer/b/c/bar"));
            assertEquals(fService.lookup(-1, "main:/layer/b/c/bar").getId(), fService.lookup(-1, "main:/layer/b/bar")
                    .getId());
            System.out.println(recursiveList("main", -1, true));
            // Now link /layer/b/c/foo into /layer/b.
            fService.link("main:/layer/b", "foo", fService.lookup(-1, "main:/layer/b/c/foo"));
            assertEquals(fService.lookup(-1, "main:/layer/b/c/foo").getId(), fService.lookup(-1, "main:/layer/b/foo")
                    .getId());
            // Make sure that the underlying layer is not mucked up.
            assertTrue(fService.lookup(-1, "main:/a/b/foo", true).isDeleted());
            System.out.println(recursiveList("main", -1, true));
            // Try to link /layer/b/c/bar to /layer/b/c. It should fail.
            try
            {
                fService.link("main:/layer/b", "bar", fService.lookup(-1, "main:/layer/b/c/bar"));
                fail();
            }
            catch (AVMExistsException e)
            {
                // Do nothing.
            }
            // Try to link /layer/b to /frinx. It should fail.
            try
            {
                fService.link("main:/", "frinx", fService.lookup(-1, "main:/layer/b"));
                fail();
            }
            catch (AVMBadArgumentException ba)
            {
                // Do nothing.
            }
            // Delete /layer/b/bar and redo. It should work.
            fService.removeNode("main:/layer/b", "bar");
            fService.link("main:/layer/b", "bar", fService.lookup(-1, "main:/layer/b/c/bar"));
            assertEquals(fService.lookup(-1, "main:/layer/b/c/bar").getId(), fService.lookup(-1, "main:/layer/b/bar")
                    .getId());
            System.out.println(recursiveList("main", -1, true));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test goofy paths.
     */
    public void testGoofyPaths()
    {
        try
        {
            setupBasicTree();
            fService.getFileInputStream(-1, "main://a/b/c/foo").close();
            fService.getDirectoryListing(-1, "main:/a/");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test getting deleted names.
     */
    public void testGetDeleted()
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            List<String> deleted = fService.getDeleted(-1, "main:/layer/b/c");
            assertEquals(0, deleted.size());
            fService.removeNode("main:/a/b/c", "foo");
            fService.createSnapshot("main", null, null);
            deleted = fService.getDeleted(-1, "main:/a/b/c");
            assertEquals(0, deleted.size());
            fService.removeNode("main:/layer/b/c", "bar");
            fService.createSnapshot("main", null, null);
            deleted = fService.getDeleted(-1, "main:/layer/b/c");
            assertEquals(1, deleted.size());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test directly contained listing.
     */
    public void testListingDirect()
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListingDirect(-1, "main:/layer");
            assertEquals(0, listing.size());
            listing = fService.getDirectoryListingDirect(-1, "main:/layer/b");
            assertEquals(0, listing.size());
            fService.createFile("main:/layer/b/c", "sigmoid").close();
            fService.createSnapshot("main", null, null);
            listing = fService.getDirectoryListingDirect(-1, "main:/layer");
            assertEquals(1, listing.size());
            fService.createFile("main:/layer", "lepton");
            fService.createSnapshot("main", null, null);
            listing = fService.getDirectoryListingDirect(-1, "main:/layer");
            assertEquals(2, listing.size());
            listing = fService.getDirectoryListingDirect(-1, "main:/layer/b/c");
            assertEquals(1, listing.size());
            listing = fService.getDirectoryListingDirect(-1, "main:/a/b/c");
            assertEquals(2, listing.size());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test layering info.
     */
    public void testLayeringInfo()
    {
        try
        {
            setupBasicTree();
            fService.createStore("layer");
            fService.createLayeredDirectory("main:/a", "layer:/", "alayer");
            fService.createSnapshot("layer", null, null);
            LayeringDescriptor info = fService.getLayeringInfo(-1, "layer:/alayer");
            assertFalse(info.isBackground());
            assertEquals("layer", info.getPathAVMStore().getName());
            assertEquals("layer", info.getNativeAVMStore().getName());
            info = fService.getLayeringInfo(-1, "layer:/alayer/b/c");
            assertTrue(info.isBackground());
            assertEquals("layer", info.getPathAVMStore().getName());
            assertEquals("main", info.getNativeAVMStore().getName());
            fService.createFile("layer:/alayer/b", "figs").close();
            fService.createSnapshot("layer", null, null);
            info = fService.getLayeringInfo(-1, "layer:/alayer/b/figs");
            assertFalse(info.isBackground());
            assertEquals("layer", info.getPathAVMStore().getName());
            assertEquals("layer", info.getNativeAVMStore().getName());
            info = fService.getLayeringInfo(-1, "layer:/alayer/b/c");
            assertTrue(info.isBackground());
            assertEquals("layer", info.getPathAVMStore().getName());
            assertEquals("main", info.getNativeAVMStore().getName());
            fService.createLayeredDirectory("layer:/alayer/b", "layer:/", "blayer");
            fService.createSnapshot("layer", null, null);
            System.err.println(recursiveList("main", -1, true));
            System.err.println(recursiveList("layer", -1, true));
            info = fService.getLayeringInfo(-1, "layer:/blayer/c");
            assertEquals("main", info.getNativeAVMStore().getName());
            info = fService.getLayeringInfo(-1, "layer:/blayer/figs");
            assertEquals("layer", info.getNativeAVMStore().getName());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Another test of renaming in a layer.
     */
    public void testRenameLayer2()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            // Set up a basic hierarchy.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a", "c");
            fService.createFile("main:/a/b", "foo", new ByteArrayInputStream("I am foo.".getBytes()));
            fService.createFile("main:/a/c", "bar", new ByteArrayInputStream("I am bar.".getBytes()));
            fService.createSnapshot("main", null, null);
            // History is unchanged.
            checkHistory(history, "main");
            // Make a layer to a.
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // History is unchanged.
            checkHistory(history, "main");
            // /a and /layer should have identical contents.
            assertEquals(recursiveContents("main:/a", -1, true), recursiveContents("main:/layer", -1, true));
            // Now rename /layer/c/bar to /layer/b/bar
            fService.rename("main:/layer/c", "bar", "main:/layer/b", "bar");
            fService.createSnapshot("main", null, null);
            // History is unchanged.
            checkHistory(history, "main");
            // /layer/c should be empty.
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/layer/c");
            assertEquals(0, listing.size());
            // /layer/b should contain fao and bar
            listing = fService.getDirectoryListing(-1, "main:/layer/b");
            assertEquals(2, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("bar", list.get(0));
            assertEquals("foo", list.get(1));
            // /a/b should contain foo.
            listing = fService.getDirectoryListing(-1, "main:/a/b");
            assertEquals(1, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("foo", list.get(0));
            // /a/c should contain bar.
            listing = fService.getDirectoryListing(-1, "main:/a/c");
            assertEquals(1, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("bar", list.get(0));
            // Now make a file in /a/b
            fService.createFile("main:/a/b", "baz").close();
            fService.createSnapshot("main", null, null);
            // History is unchanged.
            checkHistory(history, "main");
            // /a/b should contain baz and foo.
            listing = fService.getDirectoryListing(-1, "main:/a/b");
            assertEquals(2, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("baz", list.get(0));
            assertEquals("foo", list.get(1));
            // /layer/b should contain foo, bar, and baz.
            listing = fService.getDirectoryListing(-1, "main:/layer/b");
            System.out.println(recursiveList("main", -1, true));
            assertEquals(3, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("bar", list.get(0));
            assertEquals("baz", list.get(1));
            assertEquals("foo", list.get(2));
            // Remove baz from /layer/b
            fService.removeNode("main:/layer/b", "baz");
            fService.createSnapshot("main", null, null);
            // History is unchanged.
            checkHistory(history, "main");
            System.out.println(recursiveList("main", -1, true));
            // /layer/b should have bar and foo.
            listing = fService.getDirectoryListing(-1, "main:/layer/b");
            assertEquals(2, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("bar", list.get(0));
            assertEquals("foo", list.get(1));
            // /a/b should contain baz and foo as before.
            listing = fService.getDirectoryListing(-1, "main:/a/b");
            assertEquals(2, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("baz", list.get(0));
            assertEquals("foo", list.get(1));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Yet another test around rename in layers.
     */
    public void testRenameLayer3()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            // Set up a handy hierarchy.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createFile("main:/a/b", "foo").close();
            fService.createFile("main:/a/b", "bar").close();
            fService.createDirectory("main:/", "c");
            fService.createDirectory("main:/c", "d");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make a layer over /a
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Move /c/d to /layer
            fService.rename("main:/c", "d", "main:/layer", "d");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Now make a file in /layer/d
            fService.createFile("main:/layer/d", "baz").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make /a/d/figs and see the wackiness.
            fService.createDirectory("main:/a", "d");
            fService.createFile("main:/a/d", "figs").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer/d should no contain baz and figs.
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/layer/d");
            assertEquals(2, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("baz", list.get(0));
            assertEquals("figs", list.get(1));
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test the uncover operation.
     */
    public void testUncover()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            // Set up a handy hierarchy.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a/", "b");
            fService.createFile("main:/a/b", "foo").close();
            fService.createFile("main:/a/b", "bar").close();
            fService.createDirectory("main:/", "c");
            fService.createDirectory("main:/c", "d");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make a layer over /a
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Move /c/d to /layer
            fService.rename("main:/c", "d", "main:/layer", "d");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make a file in /layer/d
            fService.createFile("main:/layer/d", "baz").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make /a/d/figs and see the wackiness.
            fService.createDirectory("main:/a", "d");
            fService.createFile("main:/a/d", "figs").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer/d should now contain baz and figs.
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/layer/d");
            assertEquals(2, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("baz", list.get(0));
            assertEquals("figs", list.get(1));
            // Rename /layer/d to /layer/e and uncover /layer/d
            fService.rename("main:/layer", "d", "main:/layer", "e");
            fService.uncover("main:/layer", "d");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer/d contains figs.
            listing = fService.getDirectoryListing(-1, "main:/layer/d");
            assertEquals(1, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("figs", list.get(0));
            // /layer/e contains baz.
            listing = fService.getDirectoryListing(-1, "main:/layer/e");
            assertEquals(1, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("baz", list.get(0));
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Another test of renaming in a layer.
     */
    public void testRenameLayer4()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            // Set up a handy hierarchy.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createFile("main:/a/b", "foo").close();
            fService.createFile("main:/a/b", "bar").close();
            fService.createDirectory("main:/", "c");
            fService.createDirectory("main:/c", "d");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make a layer over /a
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Move /layer/b to /b
            fService.rename("main:/layer", "b", "main:/", "b");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Add something to /a/b and it should show up in /b.
            fService.createFile("main:/a/b", "baz").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /b should have foo and bar and baz.
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/b");
            assertEquals(3, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("bar", list.get(0));
            assertEquals("baz", list.get(1));
            assertEquals("foo", list.get(2));
            // Add something to /a and it will show up in /layer.
            fService.createFile("main:/a", "figs").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer should have figs in it.
            listing = fService.getDirectoryListing(-1, "main:/layer");
            assertEquals(1, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("figs", list.get(0));
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test branching within branches.
     */
    public void testBranchesInBranches()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            // Set up a hierarchy.
            setupBasicTree();
            // History unchanged.
            checkHistory(history, "main");
            // Make a branch from /a
            fService.createBranch(-1, "main:/a", "main:/", "abranch");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make a branch in something that has been branched.
            fService.createBranch(-1, "main:/a/b", "main:/a", "bbranch");
            fService.createSnapshot("main", null, null);
            // History unchanged
            checkHistory(history, "main");
            // Everything under /abranch should be identical in this version
            // and the previous.
            int version = fService.getNextVersionID("main");
            assertEquals(recursiveContents("main:/abranch", version - 1, true), recursiveContents("main:/abranch",
                    version - 2, true));
            // Make a branch within a branch.
            fService.createBranch(-1, "main:/abranch/b/c", "main:/abranch/b", "cbranch");
            fService.createSnapshot("main", null, null);
            // History unchanged
            checkHistory(history, "main");
            // Everything under /a should be unchanged between this version and the last.
            version = fService.getNextVersionID("main");
            assertEquals(recursiveContents("main:/a", version - 1, true), recursiveContents("main:/a", version - 2,
                    true));
            // Make a branch to something outside of a branch inside a branch.
            fService.createBranch(-1, "main:/d", "main:/abranch", "dbranch");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make something ind /abranch/dbranch.
            fService.createFile("main:/abranch/dbranch/e/f", "baz").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // d should not have changed since the previous version.
            version = fService.getNextVersionID("main");
            assertEquals(recursiveContents("main:/d", version - 1, true), recursiveContents("main:/d", version - 2,
                    true));
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test layers inside of layers.
     */
    public void testLayersInLayers()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            setupBasicTree();
            // History unchanged.
            checkHistory(history, "main");
            // Create a layer to /a
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make a layer inside of a layer pointing to d.
            fService.createLayeredDirectory("main:/d", "main:/layer", "under");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Create a file in /layer/under/e/f.
            fService.createFile("main:/layer/under/e/f", "baz").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Create a file in /d/e.
            fService.createFile("main:/d/e", "bow").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer/under/e should contain bow and f.
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/layer/under/e");
            assertEquals(2, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("bow", list.get(0));
            assertEquals("f", list.get(1));
            // Put a new set of dirs in to be made into a layering under d.
            fService.createDirectory("main:/", "g");
            fService.createDirectory("main:/g", "h");
            fService.createDirectory("main:/g/h", "i");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make a layer in /d to /g.
            fService.createLayeredDirectory("main:/g", "main:/d", "gover");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /d/gover should be identical to /layer/under/gover
            assertEquals(recursiveContents("main:/d/gover", -1, true), recursiveContents("main:/layer/under/gover", -1,
                    true));
            // Create a file in /layer/under/gover/h/i
            fService.createFile("main:/layer/under/gover/h/i", "moo").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /d should be unchanged before this version and the last
            // and /g should be unchanged between this version and the last.
            int version = fService.getNextVersionID("main");
            // TODO Need an equivalent test that won't mind the version number change
            // assertEquals(recursiveContents("main:/d", version - 1, true),
            // recursiveContents("main:/d", version - 2, true));
            // assertEquals(recursiveContents("main:/g", version - 1, true),
            // recursiveContents("main:/g", version - 2, true));
            // Add a file through /d/gover/h/i
            fService.createFile("main:/d/gover/h/i", "cow").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /g should not have changed since its last version.
            version = fService.getNextVersionID("main");
            assertEquals(recursiveContents("main:/g", version - 1, true), recursiveContents("main:/g", version - 2,
                    true));
            // /layer/under/gover/h/i shows both moo and cow.
            listing = fService.getDirectoryListing(-1, "main:/layer/under/gover/h/i");
            assertEquals(2, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("cow", list.get(0));
            assertEquals("moo", list.get(1));
            // Rename /layer/under/gover to /layer/b/gover and see what happens.
            fService.rename("main:/layer/under", "gover", "main:/layer/b", "gover");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // moo should be in /layer/b/gover/h/i
            listing = fService.getDirectoryListing(-1, "main:/layer/b/gover/h/i");
            assertEquals(1, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("moo", list.get(0));
            // Add a new file to /layer/b/gover/h/i
            fService.createFile("main:/layer/b/gover/h/i", "oink").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer/b/gover/h/i should contain moo, oink.
            listing = fService.getDirectoryListing(-1, "main:/layer/b/gover/h/i");
            assertEquals(2, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("moo", list.get(0));
            assertEquals("oink", list.get(1));
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test behavior when one branches a layer.
     */
    public void testLayerAndBranch()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            // Create a basic tree.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createFile("main:/a/b/c", "foo").close();
            fService.createFile("main:/a/b/c", "bar").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Create a layer over /a
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /a and /layer should have identical contents.
            assertEquals(recursiveContents("main:/a", -1, true), recursiveContents("main:/layer", -1, true));
            // Make a modification in /layer
            fService.createFile("main:/layer/b", "baz").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Branch off layer.
            fService.createBranch(-1, "main:/layer", "main:/", "branch");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer/b and /branch/b should have identical contents.
            assertEquals(recursiveContents("main:/layer/b", -1, true), recursiveContents("main:/branch/b", -1, true));
            // Create /branch/b/c/foo
            fService.createFile("main:/branch/b/c", "baz").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer should not have changed.
            int version = fService.getNextVersionID("main");
            assertEquals(recursiveContents("main:/layer", version - 1, true), recursiveContents("main:/layer",
                    version - 2, true));
            // Change something in /layer
            fService.createFile("main:/layer/b/c", "fig").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /branch should not have changed.
            version = fService.getNextVersionID("main");
            assertEquals(recursiveContents("main:/branch", version - 1, true), recursiveContents("main:/branch",
                    version - 2, true));
            // Create another layer on /a
            fService.createLayeredDirectory("main:/a", "main:/", "layer2");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Branch from /layer2/b.
            fService.createBranch(-1, "main:/layer2/b", "main:/", "branch2");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Create something in the branch.
            fService.createFile("main:/branch2", "goofy").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer2 should be unchanged.
            version = fService.getNextVersionID("main");
            assertEquals(recursiveContents("main:/layer2", version - 1, true), recursiveContents("main:/layer2",
                    version - 2, true));
            // Remove something from /layer2
            fService.removeNode("main:/layer2/b/c", "foo");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /branch2 is unchanged.
            version = fService.getNextVersionID("main");
            assertEquals(recursiveContents("main:/branch2", version - 1, true), recursiveContents("main:/branch2",
                    version - 2, true));
            // /a is unchanged.
            assertEquals(recursiveContents("main:/a", version - 1, true), recursiveContents("main:/a", version - 2,
                    true));
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test scenario in which something is renamed from inside one independent layer to another.
     */
    public void testRenameLayerToLayer()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            // Set up two trees
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createFile("main:/a/b/c", "foo").close();
            fService.createFile("main:/a/b/c", "bar").close();
            fService.createDirectory("main:/", "d");
            fService.createDirectory("main:/d", "e");
            fService.createDirectory("main:/d/e", "f");
            fService.createFile("main:/d/e/f", "moo").close();
            fService.createFile("main:/d/e/f", "cow").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make a layer to /a and a layer to /d
            fService.createLayeredDirectory("main:/a", "main:/", "la");
            fService.createLayeredDirectory("main:/d", "main:/", "ld");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Move /la/b/c to /ld/e/c.
            fService.rename("main:/la/b", "c", "main:/ld/e", "c");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Create file /ld/e/c/baz.
            fService.createFile("main:/ld/e/c", "baz").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Here's the thing we'd like to assert.
            assertEquals("main:/a/b/c", fService.lookup(-1, "main:/ld/e/c").getIndirection());
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test Nothing. Just make sure set up works.
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
            fService.createSnapshot("main", null, null);
            assertEquals(AVMNodeType.PLAIN_DIRECTORY, fService.lookup(-1, "main:/").getType());

            StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");

            // Text index
            SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
            ResultSet results = searchService.query(storeRef, "lucene", "@cm\\:name:testdir");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
            assertEquals(2, results.length());
            results.close();

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
            fService.createFile("main:/testdir", "testfile").close();
            fService.createFile("main:/", "testfile2").close();
            fService.createSnapshot("main", null, null);

            StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");

            // Text index
            SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
            ResultSet results = searchService.query(storeRef, "lucene", "@cm\\:name:testfile");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "@cm\\:name:testfile2");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "TEXT:\"This is testdir/testfile\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "TEXT:\"This is testfile2\"");
            assertEquals(0, results.length());
            results.close();

            ContentWriter writer = fService.getContentWriter("main:/testdir/testfile");
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            PrintStream out = new PrintStream(writer.getContentOutputStream());
            out.println("This is testdir/testfile");
            out.close();

            out = new PrintStream(fService.getFileOutputStream("main:/testfile2"));

            writer = fService.getContentWriter("main:/testfile2");
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            out = new PrintStream(writer.getContentOutputStream());
            out.println("This is testfile2");
            out.close();
            fService.createSnapshot("main", null, null);

            results = searchService.query(storeRef, "lucene", "TEXT:\"This is testdir/testfile\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "TEXT:\"This is testfile2\"");
            assertEquals(1, results.length());
            results.close();

            List<VersionDescriptor> versions = fService.getStoreVersions("main");
            for (VersionDescriptor version : versions)
            {
                System.out.println("V:" + version.getVersionID());
                System.out.println(recursiveList("main", version.getVersionID(), true));
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1,
                    "main:/testdir/testfile")));
            String line = reader.readLine();
            assertEquals("This is testdir/testfile", line);
            reader.close();
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/testfile2")));
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
            fService.createSnapshot("main", null, null);
            List<VersionDescriptor> versions = fService.getStoreVersions("main");
            for (VersionDescriptor version : versions)
            {
                System.out.println("V:" + version.getVersionID());
                System.out.println(recursiveList("main", version.getVersionID(), true));
            }
            String original = recursiveList("main:/a", -1, 0, true);
            original = original.substring(original.indexOf('\n'));
            String branch = recursiveList("main:/d/e/abranch", -1, 0, true);
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
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            assertEquals("main:/a", fService.getIndirectionPath(-1, "main:/d/e/alayer"));
            assertEquals(recursiveContents("main:/a", -1, true), recursiveContents("main:/d/e/alayer", -1, true));
            PrintStream out = new PrintStream(fService.getFileOutputStream("main:/d/e/alayer/b/c/foo"));
            out.println("I am main:/d/e/alayer/b/c/foo");
            out.close();
            fService.createSnapshot("main", null, null);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1,
                    "main:/a/b/c/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/b/c/foo", line);
            System.out.println(recursiveList("main", -1, true));
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
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            assertEquals("main:/a/b/c/foo", fService.lookup(-1, "main:/d/lfoo").getIndirection());
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1,
                    "main:/d/lfoo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/b/c/foo", line);
            PrintStream out = new PrintStream(fService.getFileOutputStream("main:/d/lfoo"));
            out.println("I am main:/d/lfoo");
            out.close();
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/b/c/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/b/c/foo", line);
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/d/lfoo")));
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
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            assertEquals(recursiveContents("main:/a/b", 1, true), recursiveContents("main:/d/e/brenamed", 2, true));

            // Test inedx update

            StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");

            // Text index
            SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
            ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"I am main\"");
            assertEquals(2, results.length());
            results.close();

            // Basic properties

            // Note "a" is a stop word and therefore not findable ...

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                    + ":\"foo\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                    + ":foo");
            assertEquals(1, results.length());
            results.close();

            // TODO: Fix auth in AVMDiskDriver and more??

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_CREATOR)
                    + ":admin");

            assertEquals(9, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser
                    .escape("@" + ContentModel.PROP_MODIFIER)
                    + ":admin");
            assertEquals(9, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_OWNER)
                    + ":admin");
            assertEquals(9, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                    + ContentModel.PROP_NODE_UUID)
                    + ":unknown");
            assertEquals(9, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                    + ContentModel.PROP_STORE_PROTOCOL)
                    + ":avm");
            assertEquals(9, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                    + ContentModel.PROP_STORE_IDENTIFIER)
                    + ":main" );
            assertEquals(9, results.length());
            results.close();

            // Basic paths

            results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
            assertEquals(0, results.length());
            results.close();
            
            
            results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/brenamed\"");
            assertEquals(1, results.length());
            results.close();
            
            results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/brenamed/c\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/brenamed/c/foo\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/brenamed/c/bar\"");
            assertEquals(1, results.length());
            results.close();

            
            results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
            assertEquals(9, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
            assertEquals(8, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
            assertEquals(2, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
            assertEquals(2, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
            assertEquals(2, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
            assertEquals(2, results.length());
            results.close();
            
            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*/*\"");
            assertEquals(0, results.length());
            results.close();

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
            fService.createSnapshot("main", null, null);
            checkHistory(history, "main");
            System.out.println(history.get(1));
            Map<String, AVMNodeDescriptor> l = fService.getDirectoryListing(-1, "main:/a/b/c");
            assertEquals(1, l.size());
            fService.removeNode("main:/d", "e");
            fService.createSnapshot("main", null, null);
            checkHistory(history, "main");
            System.out.println(history.get(2));
            l = fService.getDirectoryListing(-1, "main:/d");
            assertEquals(0, l.size());
            
//          Text index
            StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");
            SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
            ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"I am main\"");
            assertEquals(1, results.length());
            results.close();

            // Basic properties

            // Note "a" is a stop word and therefore not findable ...

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                    + ":\"foo\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_NAME)
                    + ":foo");
            assertEquals(0, results.length());
            results.close();

            // TODO: Fix auth in AVMDiskDriver and more??

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_CREATOR)
                    + ":admin");
            assertEquals(6, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser
                    .escape("@" + ContentModel.PROP_MODIFIER)
                    + ":admin");
            assertEquals(6, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@" + ContentModel.PROP_OWNER)
                    + ":admin");
            assertEquals(6, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                    + ContentModel.PROP_NODE_UUID)
                    + ":unknown");
            assertEquals(6, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                    + ContentModel.PROP_STORE_PROTOCOL)
                    + ":avm");
            assertEquals(6, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"
                    + ContentModel.PROP_STORE_IDENTIFIER)
                    + ":" + "main");
            assertEquals(6, results.length());
            results.close();

            // Basic paths

            results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/foo\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/bar\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
            assertEquals(0, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
            assertEquals(6, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
            assertEquals(5, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
            assertEquals(4, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
            assertEquals(3, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
            assertEquals(2, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
            assertEquals(1, results.length());
            results.close();

            results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
            assertEquals(0, results.length());
            results.close();
            
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test branching from one AVMStore to another.
     */
    public void testBranchAcross()
    {
        try
        {
            setupBasicTree();
            fService.createStore("second");
            List<AVMStoreDescriptor> repos = fService.getStores();
            assertEquals(2, repos.size());
            System.out.println(repos.get(0));
            System.out.println(repos.get(1));
            fService.createBranch(-1, "main:/", "second:/", "main");
            fService.createSnapshot("second", null, null);
            System.out.println(recursiveList("second", -1, true));
            assertEquals(recursiveContents("main:/", -1, true), recursiveContents("second:/main", -1, true));
            // Now make sure nothing happens to the branched from place,
            // if the branch is modified.
            PrintStream out = new PrintStream(fService.getFileOutputStream("second:/main/a/b/c/foo"));
            out.println("I am second:/main/a/b/c/foo");
            out.close();
            fService.createSnapshot("second", null, null);
            System.out.println(recursiveList("second", -1, true));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1,
                    "main:/a/b/c/foo")));
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
     * Test creating a layer across AVMStores.
     */
    public void testLayerAcross()
    {
        try
        {
            setupBasicTree();
            fService.createStore("second");
            fService.createLayeredDirectory("main:/", "second:/", "main");
            fService.createSnapshot("second", null, null);
            System.out.println(recursiveList("second", -1, true));
            assertEquals(recursiveContents("main:/", -1, true), recursiveContents("second:/main", -1, true));
            // Now make sure that a copy on write will occur and
            // that the underlying stuff doesn't get changed.
            PrintStream out = new PrintStream(fService.getFileOutputStream("second:/main/a/b/c/foo"));
            out.println("I am second:/main/a/b/c/foo");
            out.close();
            fService.createSnapshot("second", null, null);
            System.out.println(recursiveList("second", -1, true));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1,
                    "second:/main/a/b/c/foo")));
            String line = reader.readLine();
            reader.close();
            assertEquals("I am second:/main/a/b/c/foo", line);
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/a/b/c/foo")));
            line = reader.readLine();
            reader.close();
            assertEquals("I am main:/a/b/c/foo", line);
            fService.purgeStore("second");
            fService.purgeVersion(1, "main");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test rename across AVMStores.
     */
    public void testRenameAcross()
    {
        try
        {
            setupBasicTree();
            fService.createStore("second");
            fService.rename("main:/a/b", "c", "second:/", "cmoved");
            ArrayList<String> toSnapshot = new ArrayList<String>();
            toSnapshot.add("main");
            toSnapshot.add("second");
            System.out.println(recursiveList("main", -1, true));
            System.out.println(recursiveList("second", -1, true));
            // Check that the moved thing has identical contents to the thing it
            // was moved from.
            assertEquals(recursiveContents("main:/a/b/c", 1, true), recursiveContents("second:/cmoved", -1, true));
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
            fService.createSnapshot("main", null, null);
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/a");
            assertEquals(1, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("b", list.get(0));
            fService.createLayeredDirectory("main:/a", "main:/", "c");
            fService.createLayeredDirectory("main:/c", "main:/", "d");
            fService.createFile("main:/d/b", "foo.txt").close();
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            listing = fService.getDirectoryListing(-1, "main:/d/b");
            assertEquals(1, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("foo.txt", list.get(0));
            fService.createFile("main:/c/b", "bar.txt").close();
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            listing = fService.getDirectoryListing(-1, "main:/c/b");
            assertEquals(1, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("bar.txt", list.get(0));
            listing = fService.getDirectoryListing(-1, "main:/d/b");
            assertEquals(2, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("bar.txt", list.get(0));
            assertEquals("foo.txt", list.get(1));
            AVMNodeDescriptor[] arrayListing = fService.getDirectoryListingArray(-1, "main:/d/b", false);
            assertEquals("bar.txt", arrayListing[0].getName());
            assertEquals("foo.txt", arrayListing[1].getName());
            fService.rename("main:/", "c", "main:/", "e");
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            listing = fService.getDirectoryListing(-1, "main:/d/b");
            assertEquals(1, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("foo.txt", list.get(0));
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
            fService.createFile("main:/a/b", "c.txt").close();
            fService.createFile("main:/a/b", "d.txt").close();
            fService.createFile("main:/a", "e.txt").close();
            fService.createSnapshot("main", null, null);
            // Make a branch off of a.
            fService.createBranch(-1, "main:/a", "main:/", "branch");
            fService.createSnapshot("main", null, null);
            // The branch should contain exactly the same things as the thing
            // it branched from.
            assertEquals(recursiveContents("main:/a", -1, true), recursiveContents("main:/branch", -1, true));
            // Make a layer pointing to /branch/b
            fService.createLayeredDirectory("main:/branch/b", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // The new layer should contain exactly the same things as the thing it is layered to.
            assertEquals(recursiveContents("main:/branch/b", -1, true), recursiveContents("main:/layer", -1, true));
            // Make a modification in /a/b, the original branch.
            PrintStream out = new PrintStream(fService.getFileOutputStream("main:/a/b/c.txt"));
            out.println("I am c, modified in main:/a/b.");
            out.close();
            fService.createSnapshot("main", null, null);
            // The layer should still have identical content to /branch/b.
            assertEquals(recursiveContents("main:/branch/b", -1, true), recursiveContents("main:/layer", -1, true));
            // But the layer won't have contents identical to /a/b's
            assertFalse(recursiveContents("main:/a/b", -1, true).equals(recursiveContents("main:/layer", -1, true)));
            // Make a modification in /branch/b
            out = new PrintStream(fService.getFileOutputStream("main:/branch/b/d.txt"));
            out.println("I am d, modified in main:/branch/b");
            out.close();
            fService.createSnapshot("main", null, null);
            // The layer contents should be identical to the latest contents of /branch/b.
            assertEquals(recursiveContents("main:/branch/b", -1, true), recursiveContents("main:/layer", -1, true));
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
            fService.createSnapshot("main", null, null);
            // Now make some layers. Three to be precise.
            fService.createLayeredDirectory("main:/a", "main:/", "e");
            fService.createLayeredDirectory("main:/e", "main:/", "f");
            fService.createLayeredDirectory("main:/f", "main:/", "g");
            fService.createSnapshot("main", null, null);
            // e, f, g should all have the same contents as a.
            String a = recursiveContents("main:/a", -1, true);
            String e = recursiveContents("main:/e", -1, true);
            String f = recursiveContents("main:/f", -1, true);
            String g = recursiveContents("main:/g", -1, true);
            assertEquals(a, e);
            assertEquals(a, f);
            assertEquals(a, g);
            // Now make a file in /g/b/c/d and /f/b/c/d
            fService.createFile("main:/g/b/c/d", "foo").close();
            fService.createFile("main:/f/b/c/d", "bar").close();
            fService.createSnapshot("main", null, null);
            // /g/b/c/d should contain foo and bar.
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/g/b/c/d");
            assertEquals(2, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("bar", list.get(0));
            assertEquals("foo", list.get(1));
            // /f/b/c/d should contain just bar.
            listing = fService.getDirectoryListing(-1, "main:/f/b/c/d");
            assertEquals(1, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("bar", list.get(0));
            // Now do something in the bottom layer.
            fService.createFile("main:/a/b/c", "baz").close();
            fService.createSnapshot("main", null, null);
            // /e/b/c should contain baz and d
            listing = fService.getDirectoryListing(-1, "main:/e/b/c");
            assertEquals(2, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("baz", list.get(0));
            assertEquals("d", list.get(1));
            // Now add something in the e layer.
            fService.createFile("main:/e/b/c/d", "bing").close();
            fService.createSnapshot("main", null, null);
            // /f/b/c/d should now contain bar and bing.
            listing = fService.getDirectoryListing(-1, "main:/f/b/c/d");
            assertEquals(2, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("bar", list.get(0));
            assertEquals("bing", list.get(1));
            System.out.println(recursiveList("main", -1, true));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test rename within a layer.
     */
    public void testRenameInLayer()
    {
        try
        {
            // Setup a base hierarchy.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createDirectory("main:/a", "d");
            fService.createSnapshot("main", null, null);
            // Now make a layer to a.
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // /layer should have the same contents as /a at this point.
            assertEquals(recursiveContents("main:/a", -1, true), recursiveContents("main:/layer", -1, true));
            // Now we will rename /layer/d to /layer/moved
            fService.rename("main:/layer", "d", "main:/layer", "moved");
            fService.createSnapshot("main", null, null);
            // /layer should contain b and moved
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/layer");
            assertEquals(2, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("b", list.get(0));
            assertEquals("moved", list.get(1));
            // Now rename moved back to d.
            fService.rename("main:/layer", "moved", "main:/layer", "d");
            fService.createSnapshot("main", null, null);
            // /layer should contain b and d.
            listing = fService.getDirectoryListing(-1, "main:/layer");
            assertEquals(2, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("b", list.get(0));
            assertEquals("d", list.get(1));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test behavior of multiply layers not in register.
     */
    public void testMultiLayerUnregistered()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            setupBasicTree();
            // History unchanged.
            checkHistory(history, "main");
            // Create layered directory /d/e/f/ to /a
            fService.createLayeredDirectory("main:/a", "main:/d/e/f", "l0");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Create layered directory /d/l1 to /d/e/f.
            fService.createLayeredDirectory("main:/d/e/f", "main:/d", "l1");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Create layered directory /l2 to /d
            fService.createLayeredDirectory("main:/d", "main:/", "l2");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Create /l2/l1/l0/a/foo.
            fService.createFile("main:/l2/l1/l0/b", "foo").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /l2/l1/l0 should now point at /d/l1/l0
            assertEquals("main:/d/l1/l0", fService.lookup(-1, "main:/l2/l1/l0").getIndirection());
            // /l2/l1/l0/b should now point at /d/l1/l0/b
            assertEquals("main:/d/l1/l0/b", fService.lookup(-1, "main:/l2/l1/l0/b").getIndirection());
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test makePrimary.
     */
    public void testMakePrimary()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            setupBasicTree();
            // History unchanged.
            checkHistory(history, "main");
            // Make a layer to /a
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make /layer/b/c primary.
            fService.makePrimary("main:/layer/b/c");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Rename /layer/b/c to /layer/c
            fService.rename("main:/layer/b", "c", "main:/layer", "c");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /a/b/c should have identical contents to /layer/c
            assertEquals(recursiveContents("main:/a/b/c", -1, true), recursiveContents("main:/layer/c", -1, true));
            // Create /layer2 to /a.
            fService.createLayeredDirectory("main:/a", "main:/", "layer2");
            // Make a file down in /layer2/b/c
            fService.createFile("main:/layer2/b/c", "baz").close();
            // make /layer2/b/c primary.
            fService.makePrimary("main:/layer2/b/c");
            // Rename /layer2/b/c to /layer2/c
            fService.rename("main:/layer2/b", "c", "main:/layer2", "c");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer2/c should contain foo bar and baz.
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/layer2/c");
            assertEquals(3, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("bar", list.get(0));
            assertEquals("baz", list.get(1));
            assertEquals("foo", list.get(2));
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test retargeting a directory.
     */
    public void testRetarget()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            setupBasicTree();
            // History unchanged.
            checkHistory(history, "main");
            // Make a layer to /a
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Retarget /layer/b/c to /d.
            fService.retargetLayeredDirectory("main:/layer/b/c", "main:/d");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer/b/c should contain e.
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/layer/b/c");
            assertEquals(1, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("e", list.get(0));
            // Rename /layer/b/c to /layer/c
            fService.rename("main:/layer/b", "c", "main:/layer", "c");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /d should have identical contents to /layer/c
            assertEquals(recursiveContents("main:/d", -1, true), recursiveContents("main:/layer/c", -1, true));
            // Create /layer2 to /a.
            fService.createLayeredDirectory("main:/a", "main:/", "layer2");
            // Make a file down in /layer2/b/c
            fService.createFile("main:/layer2/b/c", "baz").close();
            // make /layer2/b/c primary.
            fService.retargetLayeredDirectory("main:/layer2/b/c", "main:/d");
            // Rename /layer2/b/c to /layer2/c
            fService.rename("main:/layer2/b", "c", "main:/layer2", "c");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer2/c should have baz and e in it.
            listing = fService.getDirectoryListing(-1, "main:/layer2/c");
            assertEquals(2, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("baz", list.get(0));
            assertEquals("e", list.get(1));
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test rename between branches.
     */
    public void testRenameBranchToBranch()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            // Set up two trees
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createFile("main:/a/b/c", "foo").close();
            fService.createFile("main:/a/b/c", "bar").close();
            fService.createDirectory("main:/", "d");
            fService.createDirectory("main:/d", "e");
            fService.createDirectory("main:/d/e", "f");
            fService.createFile("main:/d/e/f", "moo").close();
            fService.createFile("main:/d/e/f", "cow").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make branches.
            fService.createBranch(-1, "main:/a/b", "main:/", "abranch");
            fService.createBranch(-1, "main:/d/e", "main:/", "dbranch");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Move /abranch/c/foo /dbranch/foo
            fService.rename("main:/abranch/c", "foo", "main:/dbranch", "foo");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Confirm that /a and /d are unchanged.
            int version = fService.getNextVersionID("main");
            assertEquals(recursiveContents("main:/a", version - 1, true), recursiveContents("main:/a", version - 2,
                    true));
            assertEquals(recursiveContents("main:/d", version - 1, true), recursiveContents("main:/d", version - 2,
                    true));
            // Move /dbranch/f to /abranch/c/f
            fService.rename("main:/dbranch", "f", "main:/abranch/c", "f");
            fService.createSnapshot("main", null, null);
            // Confirm that /a and /d are unchanged.
            version = fService.getNextVersionID("main");
            assertEquals(recursiveContents("main:/a", version - 1, true), recursiveContents("main:/a", version - 2,
                    true));
            assertEquals(recursiveContents("main:/d", version - 1, true), recursiveContents("main:/d", version - 2,
                    true));
            // History unchanged.
            checkHistory(history, "main");
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test a branch being created in a layer.
     */
    public void testBranchIntoLayer()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            // Set up two trees
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createFile("main:/a/b/c", "foo").close();
            fService.createFile("main:/a/b/c", "bar").close();
            fService.createDirectory("main:/", "d");
            fService.createDirectory("main:/d", "e");
            fService.createDirectory("main:/d/e", "f");
            fService.createFile("main:/d/e/f", "moo").close();
            fService.createFile("main:/d/e/f", "cow").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Create a layer to /a
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Now create a branch from /d in /layer/a/b.
            fService.createBranch(-1, "main:/d", "main:/layer/b", "branch");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Now modify /layer/b/branch/e/f/moo.
            PrintStream out = new PrintStream(fService.getFileOutputStream("main:/layer/b/branch/e/f/moo"));
            out.println("moo modified.");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer/b/branch/e/f should contain moo and cow.
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/layer/b/branch/e/f");
            assertEquals(2, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("cow", list.get(0));
            assertEquals("moo", list.get(1));
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test renaming into a layer.
     */
    public void testRenameIntoLayer()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            // Set up two trees
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createFile("main:/a/b/c", "foo").close();
            fService.createFile("main:/a/b/c", "bar").close();
            fService.createDirectory("main:/", "d");
            fService.createDirectory("main:/d", "e");
            fService.createDirectory("main:/d/e", "f");
            fService.createFile("main:/d/e/f", "moo").close();
            fService.createFile("main:/d/e/f", "cow").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Create a layer to /a
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Now rename /d into /layer/a/b.
            fService.rename("main:/", "d", "main:/layer/b", "d");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Now modify /layer/b/branch/e/f/moo.
            PrintStream out = new PrintStream(fService.getFileOutputStream("main:/layer/b/d/e/f/moo"));
            out.println("moo modified.");
            out.close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer/b/branch/e/f should contain moo and cow.
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/layer/b/d/e/f");
            assertEquals(2, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("cow", list.get(0));
            assertEquals("moo", list.get(1));
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test proper indirection behavior.
     */
    public void testIndirectionBehavior()
    {
        try
        {
            TreeMap<Integer, String> history = new TreeMap<Integer, String>();
            // Setup the stage.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createDirectory("main:/a/b/c", "d");
            fService.createDirectory("main:/a/b/c/d", "e");
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            fService.createDirectory("main:/", "f");
            fService.createDirectory("main:/f", "g");
            fService.createDirectory("main:/f/g", "h");
            fService.createLayeredDirectory("main:/f", "main:/", "flayer");
            fService.createLayeredDirectory("main:/flayer", "main:/layer/b/c", "fover");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            fService.createDirectory("main:/", "i");
            fService.createDirectory("main:/i", "j");
            fService.createDirectory("main:/i/j", "k");
            fService.createLayeredDirectory("main:/i", "main:/f/g/h", "iover");
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            fService.createFile("main:/layer/b/c/fover/g/h/iover/j/k", "foo").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // Make a file in /i/j/k
            fService.createFile("main:/i/j/k", "pismo").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer/b/c/fover/g/h/iover/j/k should contain pismo and foo.
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1,
                    "main:/layer/b/c/fover/g/h/iover/j/k");
            assertEquals(2, listing.size());
            List<String> list = new ArrayList<String>(listing.keySet());
            assertEquals("foo", list.get(0));
            assertEquals("pismo", list.get(1));
            // Make a file in /flayer/g/h/iover/j/k
            fService.createFile("main:/flayer/g/h/iover/j/k", "zuma").close();
            fService.createSnapshot("main", null, null);
            // History unchanged.
            checkHistory(history, "main");
            // /layer/b/c/fover/g/h/iover/j/k should contain foo, pismo, and zuma.
            listing = fService.getDirectoryListing(-1, "main:/layer/b/c/fover/g/h/iover/j/k");
            assertEquals(3, listing.size());
            list = new ArrayList<String>(listing.keySet());
            assertEquals("foo", list.get(0));
            assertEquals("pismo", list.get(1));
            assertEquals("zuma", list.get(2));
            for (String val : history.values())
            {
                System.out.println(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test reading of versioned content via a layer.
     */
    public void testVersionedRead()
    {
        try
        {
            PrintStream out = new PrintStream(fService.createFile("main:/", "foo"));
            out.print("version1");
            out.close();
            fService.createLayeredFile("main:/foo", "main:/", "afoo");
            fService.createSnapshot("main", null, null);
            assertEquals(8, fService.lookup(-1, "main:/foo").getLength());
            out = new PrintStream(fService.getFileOutputStream("main:/foo"));
            out.print("version2");
            out.close();
            fService.createSnapshot("main", null, null);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(1,
                    "main:/afoo")));
            assertEquals("version1", reader.readLine());
            reader.close();
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(2, "main:/afoo")));
            assertEquals("version2", reader.readLine());
            reader.close();
            reader = new BufferedReader(new InputStreamReader(fService.getFileInputStream(-1, "main:/afoo")));
            assertEquals("version2", reader.readLine());
            reader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test rename of an overlayed directory contained in an overlayed directory.
     */
    public void testRenameLayerInLayer()
    {
        try
        {
            // Make some directories.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createSnapshot("main", null, null);
            // Make a layer to /a.
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            // Force a copy on write in the layer.
            fService.createFile("main:/layer/b/c", "foo").close();
            fService.createSnapshot("main", null, null);
            assertEquals("main:/a/b/c", fService.lookup(-1, "main:/layer/b/c").getIndirection());
            // Now rename.
            fService.rename("main:/layer/b", "c", "main:/layer/b", "d");
            fService.createSnapshot("main", null, null);
            assertEquals("main:/a/b/d", fService.lookup(-1, "main:/layer/b/d").getIndirection());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Yet another rename from layer to layer test.
     */
    public void testAnotherRename()
    {
        try
        {
            // Make two directory trees.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createDirectory("main:/", "d");
            fService.createDirectory("main:/d", "e");
            fService.createDirectory("main:/d/e", "f");
            fService.createSnapshot("main", null, null);
            runQueriesForInitialRenameStructure("main");
            // Make a layer over each.
            fService.createLayeredDirectory("main:/a", "main:/", "la");
            fService.createLayeredDirectory("main:/d", "main:/", "ld");
            fService.createSnapshot("main", null, null);
            // TODO: Sort out paths to layers ....
            //runQueriesForInitialRenameStructureAndInitialLayers("main");
            // rename from down in one layer to another.
            fService.rename("main:/ld/e", "f", "main:/la/b", "f");
            fService.createSnapshot("main", null, null);
            AVMNodeDescriptor desc = fService.lookup(-1, "main:/la/b/f");
            assertTrue(desc.isPrimary());
            assertEquals("main:/d/e/f", desc.getIndirection());
            // Now rename in in the layer.
            fService.rename("main:/la/b", "f", "main:/la/b/c", "f");
            fService.createSnapshot("main", null, null);
            desc = fService.lookup(-1, "main:/la/b/c/f");
            assertTrue(desc.isPrimary());
            assertEquals("main:/d/e/f", desc.getIndirection());
            // Now create a directory in the layered f.
            fService.createDirectory("main:/la/b/c/f", "dir");
            fService.createSnapshot("main", null, null);
            desc = fService.lookup(-1, "main:/la/b/c/f/dir");
            assertFalse(desc.isPrimary());
            assertEquals("main:/d/e/f/dir", desc.getIndirection());
            // Now rename that and see where it points.
            fService.rename("main:/la/b/c/f", "dir", "main:/la/b", "dir");
            fService.createSnapshot("main", null, null);
            desc = fService.lookup(-1, "main:/la/b/dir");
            assertFalse(desc.isPrimary());
            assertEquals("main:/a/b/dir", desc.getIndirection());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    
    protected void runQueriesForInitialRenameStructure(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        
      
        SearchService searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef(store), true);
        
        // Note "a" is a stop word and therefore not findable ...
        
        ResultSet   results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_CREATOR)+":admin");
        assertEquals(7, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_MODIFIER)+":admin");
        assertEquals(7, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_OWNER)+":admin");
        assertEquals(7, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_NODE_UUID)+":unknown");
        assertEquals(7, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_STORE_PROTOCOL)+":avm");
        assertEquals(7, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_STORE_IDENTIFIER)+":"+store);
        assertEquals(7, results.length());
        results.close();
        
        // Basic paths
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(7, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
        assertEquals(6, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
        assertEquals(3, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
        assertEquals(0, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
        assertEquals(0, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
        assertEquals(0, results.length());
        results.close();
    }
    
    
    protected void runQueriesForInitialRenameStructureAndInitialLayers(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        
      
        SearchService searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef(store), true);
        
        // Note "a" is a stop word and therefore not findable ...
        
        ResultSet   results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_CREATOR)+":admin");
        assertEquals(9, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_MODIFIER)+":admin");
        assertEquals(7, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_OWNER)+":admin");
        assertEquals(7, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_NODE_UUID)+":unknown");
        assertEquals(7, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_STORE_PROTOCOL)+":avm");
        assertEquals(7, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_STORE_IDENTIFIER)+":"+store);
        assertEquals(7, results.length());
        results.close();
        
        // Basic paths
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(7, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
        assertEquals(6, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
        assertEquals(3, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
        assertEquals(0, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
        assertEquals(0, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
        assertEquals(0, results.length());
        results.close();
    }
    
    /**
     * Test rename behavior of an overlayed file withing a layer.
     */
    public void testFileRenameLayer()
    {
        try
        {
            // Make a set of directories.
            fService.createDirectory("main:/", "a");
            fService.createDirectory("main:/a", "b");
            fService.createDirectory("main:/a/b", "c");
            fService.createFile("main:/", "foo").close();
            fService.createSnapshot("main", null, null);
            // Make a layer.
            fService.createLayeredDirectory("main:/a", "main:/", "la");
            fService.createSnapshot("main", null, null);
            // Make a layered file.
            fService.createLayeredFile("main:/foo", "main:/la/b", "foo");
            fService.createSnapshot("main", null, null);
            AVMNodeDescriptor desc = fService.lookup(-1, "main:/la/b/foo");
            assertEquals("main:/foo", desc.getIndirection());
            // Now rename it. It should still point at the same place.
            fService.rename("main:/la/b", "foo", "main:/la", "foo");
            fService.createSnapshot("main", null, null);
            desc = fService.lookup(-1, "main:/la/foo");
            assertEquals("main:/foo", desc.getIndirection());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * The random access.
     */
    /*
     * public void testRandomAccess() { try { setupBasicTree(); RandomAccessFile file = fService.getRandomAccess(1,
     * "main:/a/b/c/foo", "r"); byte [] buff = new byte[256]; assertTrue(file.read(buff) >= 20); file.close(); file =
     * fService.getRandomAccess(-1, "main:/a/b/c/bar", "rw"); for (int i = 0; i < 256; i++) { buff[i] = (byte)i; }
     * file.write(buff); file.close(); fService.createSnapshot("main", null, null); } catch (Exception e) {
     * e.printStackTrace(System.err); fail(); } }
     */

    /**
     * Test COW during long operations.
     */
    public void testCOWLongOps()
    {
        try
        {
            setupBasicTree();
            // Create a layer to a.
            fService.createLayeredDirectory("main:/a", "main:/d/e/f", "layer");
            // Create a layer to /d
            fService.createLayeredDirectory("main:/d", "main:/", "l2");
            // Force a copy on write on l2
            fService.createFile("main:/l2", "baz").close();
            // Force a copy on write on /d/e/f/layer/b/c
            fService.createFile("main:/d/e/f/layer/b/c", "fink").close();
            // Create /l2/e/f/layer/b/c/nottle
            fService.createFile("main:/l2/e/f/layer/b/c", "nottle").close();
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
            assertFalse(fService.lookup(-1, "main:/d/e/f/layer/b/c").getId() == fService.lookup(-1,
                    "main:/l2/e/f/layer/b/c").getId());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test new lookup methods.
     */
    public void testLookup()
    {
        try
        {
            setupBasicTree();
            AVMNodeDescriptor desc = fService.getStoreRoot(-1, "main");
            assertNotNull(desc);
            System.out.println(desc.toString());
            AVMNodeDescriptor child = fService.lookup(desc, "a");
            assertNotNull(child);
            System.out.println(child.toString());
            child = fService.lookup(child, "b");
            assertNotNull(child);
            System.out.println(child.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }


    /**
     * Test AVMStore functions.
     */
    public void testAVMStore()
    {
        try
        {
            // First check that we get the right error when we try to create an
            // AVMStore that exists.
            try
            {
                fService.createStore("main");
                fail();
            }
            catch (AVMExistsException ae)
            {
                // Do nothing.
            }
            // Now make sure getRepository() works.
            AVMStoreDescriptor desc = fService.getStore("main");
            assertNotNull(desc);
            System.out.println(desc);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test opacity and manipulations.
     */
    public void testOpacity()
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createSnapshot("main", null, null);
            fService.createFile("main:/layer/b/c", "baz").close();
            fService.createFile("main:/layer/b/c", "fig").close();
            fService.createSnapshot("main", null, null);
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/layer/b/c");
            assertEquals(4, listing.size());
            System.out.println(recursiveList("main", -1, true));
            // Setting the opacity of layer to true will make no difference to what we see through
            // main:/layer/b/c.
            fService.setOpacity("main:/layer", true);
            fService.createSnapshot("main", null, null);
            assertTrue(fService.lookup(-1, "main:/layer").getOpacity());
            assertEquals(4, fService.getDirectoryListing(-1, "main:/layer/b/c").size());
            System.out.println(recursiveList("main", -1, true));
            // If main:/layer/b/c is opaque, however, we'll only see two items in it.
            fService.setOpacity("main:/layer", false);
            fService.setOpacity("main:/layer/b/c", true);
            fService.createSnapshot("main", null, null);
            assertFalse(fService.lookup(-1, "main:/layer").getOpacity());
            assertTrue(fService.lookup(-1, "main:/layer/b/c").getOpacity());
            assertEquals(2, fService.getDirectoryListing(-1, "main:/layer/b/c").size());
            System.out.println(recursiveList("main", -1, true));
            // Gratuitous test of retarget.
            fService.retargetLayeredDirectory("main:/layer", "main:/d");
            fService.setOpacity("main:/layer/b/c", false);
            fService.createSnapshot("main", null, null);
            assertFalse(fService.lookup(-1, "main:/layer/b/c").getOpacity());
            assertEquals(2, fService.getDirectoryListing(-1, "main:/layer/b/c").size());
            // This is just testing that opacity setting works on latent
            // layered directories.
            fService.setOpacity("main:/layer/e/f", true);
            System.out.println(recursiveList("main", -1, true));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test common ancestor.
     */
    public void testCommonAncestor()
    {
        try
        {
            setupBasicTree();
            fService.createBranch(-1, "main:/a", "main:/", "branch");
            fService.createSnapshot("main", null, null);
            AVMNodeDescriptor ancestor = fService.lookup(-1, "main:/a/b/c/foo");
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            fService.getFileOutputStream("main:/branch/b/c/foo").close();
            fService.createSnapshot("main", null, null);
            AVMNodeDescriptor main = fService.lookup(-1, "main:/a/b/c/foo");
            AVMNodeDescriptor branch = fService.lookup(-1, "main:/branch/b/c/foo");
            AVMNodeDescriptor ca = fService.getCommonAncestor(main, branch);
            assertEquals(ancestor, ca);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }


    /**
     * Test properties on stores.
     */
    public void testStoreProperties()
    {
        try
        {
            QName name = QName.createQName("silly.uri", "SillyProperty");
            PropertyValue value = new PropertyValue(name, "Silly Property Value");
            fService.setStoreProperty("main", name, value);
            PropertyValue found = fService.getStoreProperty("main", name);
            assertEquals(value.toString(), found.toString());
            Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>();
            QName n1 = QName.createQName("silly.uri", "Prop1");
            PropertyValue p1 = new PropertyValue(null, new Date(System.currentTimeMillis()));
            props.put(n1, p1);
            QName n2 = QName.createQName("silly.uri", "Prop2");
            PropertyValue p2 = new PropertyValue(null, "A String Property.");
            props.put(n2, p2);
            QName n3 = QName.createQName("silly.uri", "Prop3");
            PropertyValue p3 = new PropertyValue(null, 42);
            props.put(n3, p3);
            fService.setStoreProperties("main", props);
            props = fService.getStoreProperties("main");
            assertEquals(6, props.size());
            assertEquals(p1.toString(), props.get(n1).toString());
            assertEquals(p2.toString(), props.get(n2).toString());
            assertEquals(p3.toString(), props.get(n3).toString());
            fService.deleteStoreProperty("main", name);
            props = fService.getStoreProperties("main");
            assertEquals(5, props.size());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test Aspect Name storage.
     */
    public void testAspectNames()
    {
        try
        {
            setupBasicTree();
            
            StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");
            SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
            ResultSet   results = searchService.query(storeRef, "lucene", "ASPECT:\"" + ContentModel.ASPECT_TITLED.toString()  +"\"");
            assertEquals(0, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", "ASPECT:\"" + ContentModel.ASPECT_AUDITABLE.toString()  +"\"");
            assertEquals(0, results.length());
            results.close();
            
            fService.addAspect("main:/a/b/c/foo", ContentModel.ASPECT_TITLED);
            fService.addAspect("main:/a/b/c/foo", ContentModel.ASPECT_AUDITABLE);
            fService.createSnapshot("main", null, null);
            
            results = searchService.query(storeRef, "lucene", "ASPECT:\"" + ContentModel.ASPECT_TITLED.toString()  +"\"");
            assertEquals(1, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", "ASPECT:\"" + ContentModel.ASPECT_AUDITABLE.toString()  +"\"");
            assertEquals(1, results.length());
            results.close();
            
            fService.removeNode("main:/a/b/c/bar");
            fService.addAspect("main:/a/b/c/bar", ContentModel.ASPECT_TITLED);
            Set<QName> names = fService.getAspects(-1, "main:/a/b/c/foo");
            assertEquals(2, names.size());
            assertTrue(fService.hasAspect(-1, "main:/a/b/c/foo", ContentModel.ASPECT_TITLED));
            assertFalse(fService.hasAspect(-1, "main:/a/b/c/foo", ContentModel.ASPECT_AUTHOR));
            assertTrue(fService.hasAspect(-1, "main:/a/b/c/foo", ContentModel.ASPECT_TITLED));
            fService.removeAspect("main:/a/b/c/foo", ContentModel.ASPECT_TITLED);
            fService.createSnapshot("main", null, null);
            
            results = searchService.query(storeRef, "lucene", "ASPECT:\"" + ContentModel.ASPECT_TITLED.toString()  +"\"");
            assertEquals(0, results.length());
            results.close();
            results = searchService.query(storeRef, "lucene", "ASPECT:\"" + ContentModel.ASPECT_AUDITABLE.toString()  +"\"");
            assertEquals(1, results.length());
            results.close();
            
            
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            assertFalse(fService.hasAspect(-1, "main:/a/b/c/foo", ContentModel.ASPECT_TITLED));
            assertTrue(fService.hasAspect(-1, "main:/a/b/c/foo", ContentModel.ASPECT_AUDITABLE));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test case insensitivity.
     */
    public void testCaseInsensitive()
    {
        try
        {
            setupBasicTree();
            try
            {
                fService.createFile("main:/a/b/c", "Foo").close();
                fail();
            }
            catch (AVMExistsException e)
            {
                // Do nothing.
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test ACLs.
     */
    public void testACLs()
    {
        try
        {
            setupBasicTree();
            PermissionService perm = (PermissionService) fContext.getBean("PermissionService");
            // AuthenticationService ac = (AuthenticationService)fContext.getBean("AuthenticationService");
            // ac.authenticate("admin", "admin".toCharArray());
            perm.setPermission(AVMNodeConverter.ToNodeRef(-1, "main:/a/b/c/foo"),
                    PermissionService.ADMINISTRATOR_AUTHORITY, PermissionService.ALL_PERMISSIONS, true);
            fService.createSnapshot("main", null, null);
            fService.getFileOutputStream("main:/a/b/c/foo").close();
            Set<AccessPermission> perms = perm.getPermissions(AVMNodeConverter.ToNodeRef(-1, "main:/a/b/c/foo"));
            for (AccessPermission permission : perms)
            {
                System.out.println(permission);
            }
            assertTrue(perms.size() > 0);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test FileFolderService with AVM.
     */
    public void testFileFolderService()
    {
        try
        {
            setupBasicTree();
            FileFolderService ffs = (FileFolderService) fContext.getBean("FileFolderService");
            // AuthenticationComponent ac = (AuthenticationComponent)fContext.getBean("authenticationComponent");
            // ac.authenticate("admin", "admin".toCharArray());
            assertTrue(ffs.create(AVMNodeConverter.ToNodeRef(-1, "main:/a/b/c/"), "banana",
                    WCMModel.TYPE_AVM_PLAIN_CONTENT) != null);
            assertTrue(ffs.create(AVMNodeConverter.ToNodeRef(-1, "main://"), "banana", WCMModel.TYPE_AVM_PLAIN_CONTENT) != null);
            assertTrue(ffs.create(AVMNodeConverter.ToNodeRef(-1, "main:/a/b/c"), "apples",
                    WCMModel.TYPE_AVM_PLAIN_FOLDER) != null);
            NodeService ns = (NodeService) fContext.getBean("NodeService");
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
            properties.put(WCMModel.PROP_AVM_DIR_INDIRECTION, AVMNodeConverter.ToNodeRef(-1, "main:/a"));
            assertTrue(ns.createNode(AVMNodeConverter.ToNodeRef(-1, "main:/"), ContentModel.ASSOC_CONTAINS, QName
                    .createQName(NamespaceService.APP_MODEL_1_0_URI, "layer"), WCMModel.TYPE_AVM_LAYERED_FOLDER,
                    properties) != null);
            assertTrue(ns.getProperty(AVMNodeConverter.ToNodeRef(-1, "main:/layer"), WCMModel.PROP_AVM_DIR_INDIRECTION) != null);
            properties.clear();
            properties.put(WCMModel.PROP_AVM_FILE_INDIRECTION, AVMNodeConverter.ToNodeRef(-1, "main:/a/b/c/foo"));
            assertTrue(ns.createNode(AVMNodeConverter.ToNodeRef(-1, "main:/"), ContentModel.ASSOC_CONTAINS, QName
                    .createQName(NamespaceService.APP_MODEL_1_0_URI, "foo"), WCMModel.TYPE_AVM_LAYERED_CONTENT,
                    properties) != null);
            assertTrue(ns.getProperty(AVMNodeConverter.ToNodeRef(-1, "main:/foo"), WCMModel.PROP_AVM_FILE_INDIRECTION) != null);
            fService.createSnapshot("main", null, null);
            System.out.println(recursiveList("main", -1, true));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test overwriting without snapshots in between.
     */
    public void testOverwrite()
    {
        try
        {
            setupBasicTree();
            class TxnCallback implements RetryingTransactionHelper.RetryingTransactionCallback
            {
                public Object execute()
                {
                    try
                    {
                        AVMService service = (AVMService) fContext.getBean("avmService");
                        service.createLayeredDirectory("main:/a", "main:/", "layer");
                        // Modify something in an ordinary directory 3 times.
                        service.getFileOutputStream("main:/a/b/c/foo").close();
                        service.getFileOutputStream("main:/a/b/c/foo").close();
                        service.getFileOutputStream("main:/a/b/c/foo").close();
                        service.createFile("main:/a/b/c", "pint").close();
                        service.createFile("main:/a/b/c", "quart").close();
                        // Modify another file in the same directory.
                        service.getFileOutputStream("main:/a/b/c/bar").close();
                        service.getFileOutputStream("main:/a/b/c/bar").close();
                        service.lookup(-1, "main:/a/b/c");
                        service.createFile("main:/a/b/c", "figment").close();
                        // Repeat in a layer.
                        service.getFileOutputStream("main:/layer/b/c/foo").close();
                        service.getFileOutputStream("main:/layer/b/c/foo").close();
                        service.getFileOutputStream("main:/layer/b/c/foo").close();
                        service.createFile("main:/layer/b/c", "gallon").close();
                        service.createFile("main:/layer/b/c", "dram").close();
                        service.getFileOutputStream("main:/layer/b/c/bar").close();
                        service.getFileOutputStream("main:/layer/b/c/bar").close();
                        try
                        {
                            service.lookup(-1, "main:/a/b/c/froo");
                        }
                        catch (AVMException ae)
                        {
                            // Do nothing.
                        }
                        service.createDirectory("main:/a/b/c", "froo");
                        service.createFile("main:/a/b/c/froo", "franistan").close();
                        try
                        {
                            service.lookup(-1, "main:/layer/b/c/groo");
                        }
                        catch (AVMException ae)
                        {
                            // Do nothing.
                        }
                        service.createDirectory("main:/layer/b/c", "groo");
                        service.createFile("main:/layer/b/c/groo", "granistan").close();
                        return null;
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        fail();
                        return null;
                    }
                }
            }
            RetryingTransactionHelper helper = (RetryingTransactionHelper) fContext
                    .getBean("retryingTransactionHelper");
            helper.doInTransaction(new TxnCallback(), false);
            assertNotNull(fService.lookup(-1, "main:/layer/b/c/groo"));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test creating a file over a ghost.
     */
    public void testCreateOverDeleted()
    {
        try
        {
            setupBasicTree();
            fService.removeNode("main:/a/b/c", "foo");
            fService.createFile("main:/a/b/c", "foo").close();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test lookup and listing of deleted files.
     */
    public void testDeleted()
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            // Delete something in regular directory.
            fService.removeNode("main:/a/b/c", "foo");
            AVMNodeDescriptor desc = fService.lookup(-1, "main:/a/b/c/foo", true);
            assertTrue(desc.isDeleted());
            assertTrue(desc.isDeletedFile());
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/a/b/c", true);
            assertEquals(2, listing.size());
            assertTrue(listing.get("foo").isDeleted());
            AVMNodeDescriptor dir = fService.lookup(-1, "main:/a/b/c", true);
            desc = fService.lookup(dir, "foo", true);
            assertTrue(desc.isDeleted());
            listing = fService.getDirectoryListing(dir, true);
            assertEquals(2, listing.size());
            assertTrue(listing.get("foo").isDeleted());
            desc = fService.lookup(-1, "main:/layer/b/c/foo", true);
            assertTrue(desc.isDeleted());
            listing = fService.getDirectoryListing(-1, "main:/layer/b/c", true);
            assertEquals(2, listing.size());
            assertTrue(listing.get("foo").isDeleted());
            dir = fService.lookup(-1, "main:/layer/b/c", true);
            listing = fService.getDirectoryListing(dir, true);
            assertEquals(2, listing.size());
            assertTrue(listing.get("foo").isDeleted());
            // Delete something in a layer.
            fService.removeNode("main:/layer/b/c", "bar");
            desc = fService.lookup(-1, "main:/layer/b/c/bar", true);
            assertTrue(desc.isDeleted());
            assertTrue(desc.isDeletedFile());
            listing = fService.getDirectoryListing(-1, "main:/layer/b/c", true);
            assertEquals(2, listing.size());
            assertTrue(listing.get("foo").isDeleted());
            assertTrue(listing.get("bar").isDeleted());
            listing = fService.getDirectoryListingDirect(-1, "main:/layer/b/c", true);
            assertEquals(1, listing.size());
            assertTrue(listing.get("bar").isDeleted());
            dir = fService.lookup(-1, "main:/layer/b/c", true);
            desc = fService.lookup(dir, "bar", true);
            assertTrue(desc.isDeleted());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test Store property querying.
     */
    public void testStorePropertyQuerying()
    {
        try
        {
            fService.setStoreProperty("main", QName.createQName(null, ".dns.alice--preview"), new PropertyValue(null,
                    "alice-preview"));
            fService.setStoreProperty("main", QName.createQName("", ".other.property"), new PropertyValue(null,
                    "other value"));
            Map<QName, PropertyValue> result = fService.queryStorePropertyKey("main", QName.createQName("", ".dns.%"));
            assertEquals(1, result.size());
            fService.createStore("second");
            fService.setStoreProperty("second", QName.createQName("", ".dns.alice"), new PropertyValue(null,
                    "alice-space"));
            Map<String, Map<QName, PropertyValue>> matches = fService.queryStoresPropertyKeys(QName.createQName("",
                    ".dns.%"));
            assertEquals(2, matches.size());
            assertEquals(1, matches.get("main").size());
            assertEquals(1, matches.get("second").size());
            assertEquals("alice-preview", matches.get("main").get(QName.createQName(null, ".dns.alice--preview"))
                    .getStringValue());
            assertEquals("alice-space", matches.get("second").get(QName.createQName(null, ".dns.alice"))
                    .getStringValue());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test AVMSyncService resetLayer.
     */
    public void testResetLayer()
    {
        try
        {
            setupBasicTree();
            fService.createLayeredDirectory("main:/a", "main:/", "layer");
            fService.createFile("main:/layer", "figs").close();
            assertFalse(recursiveContents("main:/a", -1, true).equals(recursiveContents("main:/layer", -1, true)));
            System.out.println(recursiveList("main", -1, true));
            fSyncService.resetLayer("main:/layer");
            assertEquals(recursiveContents("main:/a", -1, true), recursiveContents("main:/layer", -1, true));
            System.out.println(recursiveList("main", -1, true));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
}
