/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.node.index;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.perf.PerformanceMonitor;

/**
 * Checks that the indexing of the node hierarchy is working
 * 
 * @see org.alfresco.repo.node.index.NodeIndexer
 * 
 * @author Derek Hulley
 */
public class NodeIndexerTest extends BaseNodeServiceTest
{
    private SearchService searchService;
    private static StoreRef localStoreRef;
    private static NodeRef localRootNode;

    @Override
    protected NodeService getNodeService()
    {
        return (NodeService) applicationContext.getBean("NodeService");
    }
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        searchService = (SearchService) applicationContext.getBean("searchService");

        if (localStoreRef == null)
        {
            localStoreRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_Persisted" + System.currentTimeMillis());
            localRootNode = nodeService.getRootNode(localStoreRef);
        }
    }

    public void testCommitQueryData() throws Exception
    {
        rootNodeRef = localRootNode;
        buildNodeGraph();
        setComplete();
    }

    public void testQuery() throws Exception
    {
        rootNodeRef = localRootNode;
        ResultSet results = searchService.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"" + BaseNodeServiceTest.TEST_PREFIX + ":root_p_n1\"", null, null);
        assertEquals(1, results.length());
        results.close();
    }

    public void testLikeAndContains() throws Exception
    {
        rootNodeRef = localRootNode;
        
        DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
        namespacePrefixResolver.registerNamespace(NamespaceService.SYSTEM_MODEL_PREFIX, NamespaceService.SYSTEM_MODEL_1_0_URI);
        namespacePrefixResolver.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
        namespacePrefixResolver.registerNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
   
        PerformanceMonitor selectNodesPerf = new PerformanceMonitor(getClass().getSimpleName(), "selectNodes");
        PerformanceMonitor selectPropertiesPerf = new PerformanceMonitor(getClass().getSimpleName(), "selectProperties");
        
        List<NodeRef> answer;
        
        selectNodesPerf.start();
        answer =  searchService.selectNodes(rootNodeRef, "//*[like(@test:animal, 'm_nkey')]", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        selectNodesPerf.stop();
        
        selectNodesPerf.start();
        answer =  searchService.selectNodes(rootNodeRef, "//*[like(@test:animal, 'm%key')]", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        selectNodesPerf.stop();
        
        selectNodesPerf.start();
        answer =  searchService.selectNodes(rootNodeRef, "//*[like(@test:animal, 'monk__')]", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        selectNodesPerf.stop();
        
        selectNodesPerf.start();
        answer =  searchService.selectNodes(rootNodeRef, "//*[like(@test:animal, 'monk%')]", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        selectNodesPerf.stop();
        
        selectNodesPerf.start();
        answer =  searchService.selectNodes(rootNodeRef, "//*[like(@test:animal, 'monk\\%')]", null, namespacePrefixResolver, false);
        assertEquals(0, answer.size());
        selectNodesPerf.stop();
        
        selectNodesPerf.start();
        answer =  searchService.selectNodes(rootNodeRef, "//*[contains('monkey')]", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        selectNodesPerf.stop();
        
        selectPropertiesPerf.start();
        List<Serializable> result =  searchService.selectProperties(rootNodeRef, "//@*[contains('monkey')]", null, namespacePrefixResolver, false);
        assertEquals(2, result.size());
        selectPropertiesPerf.stop();
        
        selectNodesPerf.start();
        answer =  searchService.selectNodes(rootNodeRef, "//*[contains('mon?ey')]", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        selectNodesPerf.stop();
        
        selectPropertiesPerf.start();
        result =  searchService.selectProperties(rootNodeRef, "//@*[contains('mon?ey')]", null, namespacePrefixResolver, false);
        assertEquals(2, result.size());
        selectPropertiesPerf.stop();
        
        selectNodesPerf.start();
        answer =  searchService.selectNodes(rootNodeRef, "//*[contains('m*y')]", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        selectNodesPerf.stop();
        
        selectPropertiesPerf.start();
        result =  searchService.selectProperties(rootNodeRef, "//@*[contains('mon*')]", null, namespacePrefixResolver, false);
        assertEquals(2, result.size());
        selectPropertiesPerf.stop();
        
        selectNodesPerf.start();
        answer =  searchService.selectNodes(rootNodeRef, "//*[contains('*nkey')]", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        selectNodesPerf.stop();
        
        selectPropertiesPerf.start();
        result =  searchService.selectProperties(rootNodeRef, "//@*[contains('?onkey')]", null, namespacePrefixResolver, false);
        assertEquals(2, result.size());
        selectPropertiesPerf.stop();
    }
}
