/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.transfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.transfer.NodeCrawler;
import org.alfresco.service.cmr.transfer.NodeCrawlerFactory;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.testing.category.LuceneTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit test for classes related to the {@link NodeCrawler} interface
 * 
 * @author Brian Remmington
 */
@Category({BaseSpringTestsCategory.class})
public class NodeCrawlerTest extends BaseAlfrescoSpringTest
{
    private ServiceRegistry serviceRegistry;
    private NodeRef companyHome;
    private NodeCrawlerFactory nodeCrawlerFactory;

    /**
     * Called during the transaction setup
     */
    @SuppressWarnings("deprecation")
    @Before
    public void before() throws Exception
    {
        super.before();

        // Get the required services
        this.nodeService = (NodeService) applicationContext.getBean("NodeService");
        this.serviceRegistry = (ServiceRegistry) applicationContext.getBean("ServiceRegistry");
        this.nodeCrawlerFactory = (NodeCrawlerFactory) applicationContext.getBean("NodeCrawlerFactory");
        Repository repositoryHelper = (Repository) applicationContext.getBean("repositoryHelper");
        this.companyHome = repositoryHelper.getCompanyHome();
    }

    @Test
    public void testContentClassFilter() throws Exception
    {
        NodeRef node1 = makeNode(companyHome, ContentModel.TYPE_BASE);
        NodeRef node2 = makeNode(companyHome, ContentModel.TYPE_CONTENT);
        NodeRef node3 = makeNode(companyHome, ContentModel.TYPE_FOLDER);
        NodeRef node4 = makeNode(companyHome, ContentModel.TYPE_CONTENT);
        NodeRef node5 = makeNode(companyHome, ContentModel.TYPE_THUMBNAIL);

        nodeService.addAspect(node4, ContentModel.ASPECT_REFERENCEABLE, null);

        ContentClassFilter contentFilter = new ContentClassFilter();
        contentFilter.setServiceRegistry(serviceRegistry);
        contentFilter.setContentClasses(ContentModel.TYPE_BASE);

        assertTrue(contentFilter.accept(node1));
        assertTrue(contentFilter.accept(node2));
        assertTrue(contentFilter.accept(node3));

        contentFilter.setDirectOnly(true);
        assertTrue(contentFilter.accept(node1));
        assertFalse(contentFilter.accept(node2));
        assertFalse(contentFilter.accept(node3));

        contentFilter.setContentClasses(ContentModel.TYPE_BASE, ContentModel.ASPECT_REFERENCEABLE);
        assertTrue(contentFilter.accept(node4));

        contentFilter.setExclude(true);
        contentFilter.setDirectOnly(false);
        contentFilter.setContentClasses(ContentModel.TYPE_CONTENT);
        assertTrue(contentFilter.accept(node1));
        assertFalse(contentFilter.accept(node2));
        assertTrue(contentFilter.accept(node3));
        assertFalse(contentFilter.accept(node5));

        contentFilter.setDirectOnly(true);
        assertTrue(contentFilter.accept(node1));
        assertFalse(contentFilter.accept(node2));
        assertTrue(contentFilter.accept(node3));
        assertTrue(contentFilter.accept(node5));

    }

    @Test
    public void testChildAssociationFinder()
    {
        makeNode(companyHome, ContentModel.TYPE_BASE);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        NodeRef node8 = makeNode(companyHome, ContentModel.TYPE_FOLDER);
        makeNode(node8, ContentModel.TYPE_FOLDER);
        NodeRef node10 = makeNode(node8, ContentModel.TYPE_FOLDER);
        makeNode(node10, ContentModel.TYPE_FOLDER);
        NodeRef node12 = makeNode(node10, ContentModel.TYPE_FOLDER);
        NodeRef node13 = makeNode(node12, ContentModel.TYPE_FOLDER);
        makeNode(node13, ContentModel.TYPE_CONTENT);
        NodeRef node15 = makeNode(node13, ContentModel.TYPE_THUMBNAIL);

        nodeService.addAspect(node8, RenditionModel.ASPECT_RENDITIONED, null);
        nodeService.addChild(node8, node15, RenditionModel.ASSOC_RENDITION, QName.createQName(
                NamespaceService.APP_MODEL_1_0_URI, "temp"));

        ChildAssociatedNodeFinder nodeFinder = new ChildAssociatedNodeFinder();
        nodeFinder.setServiceRegistry(serviceRegistry);
        nodeFinder.setAssociationTypes(ContentModel.ASSOC_CONTAINS);

        Set<NodeRef> results = nodeFinder.findFrom(node8);
        assertEquals(2, results.size());

        nodeFinder.setAssociationTypes(RenditionModel.ASSOC_RENDITION);
        results = nodeFinder.findFrom(node8);
        assertEquals(1, results.size());
        assertEquals(node15, new ArrayList<NodeRef>(results).get(0));
    }

    @Test
    public void testCrawler()
    {
        NodeRef node8 = makeNode(companyHome, ContentModel.TYPE_FOLDER);
        NodeRef node9 = makeNode(node8, ContentModel.TYPE_FOLDER);
        NodeRef node10 = makeNode(node8, ContentModel.TYPE_FOLDER);
        NodeRef node11 = makeNode(node10, ContentModel.TYPE_FOLDER);
        NodeRef node12 = makeNode(node10, ContentModel.TYPE_FOLDER);
        NodeRef node13 = makeNode(node12, ContentModel.TYPE_FOLDER);

        makeNode(node10, ContentModel.TYPE_BASE);
        makeNode(node13, ContentModel.TYPE_CONTENT);
        makeNode(node10, ContentModel.TYPE_CONTENT);
        makeNode(node11, ContentModel.TYPE_CONTENT);
        makeNode(node8, ContentModel.TYPE_CONTENT);
        makeNode(node8, ContentModel.TYPE_CONTENT);
        makeNode(node9, ContentModel.TYPE_CONTENT);
        makeNode(node13, ContentModel.TYPE_CONTENT);
        NodeRef node15 = makeNode(node13, ContentModel.TYPE_THUMBNAIL);

        nodeService.addAspect(node8, RenditionModel.ASPECT_RENDITIONED, null);
        nodeService.addChild(node8, node15, RenditionModel.ASSOC_RENDITION, QName.createQName(
                NamespaceService.APP_MODEL_1_0_URI, "temp"));

        NodeCrawler crawler = nodeCrawlerFactory.getNodeCrawler();
        crawler.setNodeFinders(new ChildAssociatedNodeFinder(ContentModel.ASSOC_CONTAINS));

        Set<NodeRef> crawledNodes = crawler.crawl(node8);
        assertEquals(15, crawledNodes.size());

        crawler.setNodeFilters(new ContentClassFilter(ContentModel.TYPE_FOLDER));
        crawledNodes = crawler.crawl(node8);
        assertEquals(6, crawledNodes.size());
    }

    /**
     * @param parent NodeRef
     * @param nodeType QName
     * @return
     */
    private NodeRef makeNode(NodeRef parent, QName nodeType)
    {
        String uuid = GUID.generate();
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, uuid);
        ChildAssociationRef assoc = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, QName.createQName(
                NamespaceService.APP_MODEL_1_0_URI, uuid), nodeType, props);
        return assoc.getChildRef();
    }

}
