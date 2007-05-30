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
package org.alfresco.repo.node.index;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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

    /**
     * {@inheritDoc}
     * 
     * This instance modifies the ML text value to be just the default locale string.
     */
    protected void getExpectedPropertyValues(Map<QName, Serializable> checkProperties)
    {
        MLText mlTextValue = (MLText) checkProperties.get(PROP_QNAME_ML_TEXT_VALUE);
        String strValue = mlTextValue.getDefaultValue();
        checkProperties.put(PROP_QNAME_ML_TEXT_VALUE, strValue);
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
