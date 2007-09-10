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
package org.alfresco.repo.node;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;

public class NodeRefPropertyMethodInterceptorTest extends BaseSpringTest
{

    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/NodeRefTestModel";

    private QName testType = QName.createQName(TEST_NAMESPACE, "testType");

    private QName categoryAspect = QName.createQName(TEST_NAMESPACE, "singleCategory");

    private QName categoriesAspect = QName.createQName(TEST_NAMESPACE, "multipleCategories");

    private QName noderefAspect = QName.createQName(TEST_NAMESPACE, "singleNodeRef");

    private QName noderefsAspect = QName.createQName(TEST_NAMESPACE, "multipleNodeRefs");

    private QName aspectCategoryProp = QName.createQName(TEST_NAMESPACE, "category");

    private QName aspectCategoriesProp = QName.createQName(TEST_NAMESPACE, "categories");

    private QName aspectNoderefProp = QName.createQName(TEST_NAMESPACE, "noderef");

    private QName aspectNoderefsProp = QName.createQName(TEST_NAMESPACE, "noderefs");

    private QName typeCategoryProp = QName.createQName(TEST_NAMESPACE, "category1");

    private QName typeCategoriesProp = QName.createQName(TEST_NAMESPACE, "categories1");

    private QName typeNoderefProp = QName.createQName(TEST_NAMESPACE, "noderef1");

    private QName typeNoderefsProp = QName.createQName(TEST_NAMESPACE, "noderefs1");

    private NodeService mlAwareNodeService;

    private NodeService nodeService;

    private NodeRef rootNodeRef;

    private AuthenticationComponent authenticationComponent;

    private DictionaryDAO dictionaryDAO;

    public NodeRefPropertyMethodInterceptorTest()
    {
        super();
    }

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        mlAwareNodeService = (NodeService) applicationContext.getBean("mlAwareNodeService");
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        dictionaryDAO = (DictionaryDAO) applicationContext.getBean("dictionaryDAO");

        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");

        authenticationComponent.setSystemUserAsCurrentUser();

        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("org/alfresco/repo/node/NodeRefTestModel.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDAO.putModel(model);

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

    }

    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            // do nothing
        }
        super.onTearDownInTransaction();
    }

    public void testOnRead()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");

        NodeRef n1 = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        // Add aspect

        mlAwareNodeService.addAspect(n1, categoryAspect, null);
        mlAwareNodeService.addAspect(n1, categoriesAspect, null);
        mlAwareNodeService.addAspect(n1, noderefAspect, null);
        mlAwareNodeService.addAspect(n1, noderefsAspect, null);

        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefsProp));

        // Set null property

        mlAwareNodeService.setProperty(n1, aspectCategoryProp, null);
        mlAwareNodeService.setProperty(n1, aspectCategoriesProp, null);
        mlAwareNodeService.setProperty(n1, aspectNoderefProp, null);
        mlAwareNodeService.setProperty(n1, aspectNoderefsProp, null);

        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefsProp));

        // Set invalid

        mlAwareNodeService.setProperty(n1, aspectCategoryProp, invalidNodeRef);
        mlAwareNodeService.setProperty(n1, aspectCategoriesProp, invalidNodeRef);
        mlAwareNodeService.setProperty(n1, aspectNoderefProp, invalidNodeRef);
        mlAwareNodeService.setProperty(n1, aspectNoderefsProp, invalidNodeRef);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());

        // Set valid node ref

        mlAwareNodeService.setProperty(n1, aspectCategoryProp, rootNodeRef);
        mlAwareNodeService.setProperty(n1, aspectCategoriesProp, rootNodeRef);
        mlAwareNodeService.setProperty(n1, aspectNoderefProp, rootNodeRef);
        mlAwareNodeService.setProperty(n1, aspectNoderefsProp, rootNodeRef);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());

        // Set valid cat

        mlAwareNodeService.setProperty(n1, aspectCategoryProp, cat);
        mlAwareNodeService.setProperty(n1, aspectCategoriesProp, cat);
        mlAwareNodeService.setProperty(n1, aspectNoderefProp, cat);
        mlAwareNodeService.setProperty(n1, aspectNoderefsProp, cat);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());

        // Set empty list

        mlAwareNodeService.setProperty(n1, aspectCategoriesProp, new ArrayList<NodeRef>());
        mlAwareNodeService.setProperty(n1, aspectNoderefsProp, new ArrayList<NodeRef>());

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());

        // Set invalid noderef in list

        ArrayList<NodeRef> cats = new ArrayList<NodeRef>();
        cats.add(invalidNodeRef);
        mlAwareNodeService.setProperty(n1, aspectCategoriesProp, cats);
        mlAwareNodeService.setProperty(n1, aspectNoderefsProp, cats);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());

        // Set valid ref in list

        cats = new ArrayList<NodeRef>();
        cats.add(rootNodeRef);
        mlAwareNodeService.setProperty(n1, aspectCategoriesProp, cats);
        mlAwareNodeService.setProperty(n1, aspectNoderefsProp, cats);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());

        // set valid cat in list

        cats = new ArrayList<NodeRef>();
        cats.add(cat);
        mlAwareNodeService.setProperty(n1, aspectCategoriesProp, cats);
        mlAwareNodeService.setProperty(n1, aspectNoderefsProp, cats);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());

        // Test list with invalid, noderef and cat

        cats = new ArrayList<NodeRef>();
        cats.add(rootNodeRef);
        cats.add(invalidNodeRef);
        cats.add(cat);
        mlAwareNodeService.setProperty(n1, aspectCategoriesProp, cats);
        mlAwareNodeService.setProperty(n1, aspectNoderefsProp, cats);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(3, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(3, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(3, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(3, ((Collection) mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(2, ((Collection) nodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(2, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());

    }

    public void testAddAspectNull()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");

        NodeRef n1 = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        // Add aspect

        nodeService.addAspect(n1, categoryAspect, null);
        nodeService.addAspect(n1, categoriesAspect, null);
        nodeService.addAspect(n1, noderefAspect, null);
        nodeService.addAspect(n1, noderefsAspect, null);

        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
    }

    public void testAddAspectNullValues()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");

        NodeRef n1 = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties1 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties2 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties3 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties4 = new HashMap<QName, Serializable>();
        properties1.put(aspectCategoryProp, null);
        properties2.put(aspectCategoriesProp, null);
        properties3.put(aspectNoderefProp, null);
        properties4.put(aspectNoderefsProp, null);

        // Add aspect

        nodeService.addAspect(n1, categoryAspect, properties1);
        nodeService.addAspect(n1, categoriesAspect, properties2);
        nodeService.addAspect(n1, noderefAspect, properties3);
        nodeService.addAspect(n1, noderefsAspect, properties4);

        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
    }

    public void testAddAspectInvalidValues()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef testInvalid = invalidNodeRef;

        NodeRef n1 = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties1 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties2 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties3 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties4 = new HashMap<QName, Serializable>();
        properties1.put(aspectCategoryProp, invalidNodeRef);
        properties2.put(aspectCategoriesProp, invalidNodeRef);
        properties3.put(aspectNoderefProp, invalidNodeRef);
        properties4.put(aspectNoderefsProp, invalidNodeRef);
        
        assertEquals(testInvalid, invalidNodeRef);

        // Add aspect

        nodeService.addAspect(n1, categoryAspect, properties1);
        nodeService.addAspect(n1, categoriesAspect, properties2);
        nodeService.addAspect(n1, noderefAspect, properties3);
        nodeService.addAspect(n1, noderefsAspect, properties4);

        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
    }

    public void testAddAspectValidNode()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");

        NodeRef n1 = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties1 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties2 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties3 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties4 = new HashMap<QName, Serializable>();
        properties1.put(aspectCategoryProp, rootNodeRef);
        properties2.put(aspectCategoriesProp, rootNodeRef);
        properties3.put(aspectNoderefProp, rootNodeRef);
        properties4.put(aspectNoderefsProp, rootNodeRef);

        // Add aspect

        nodeService.addAspect(n1, categoryAspect, properties1);
        nodeService.addAspect(n1, categoriesAspect, properties2);
        nodeService.addAspect(n1, noderefAspect, properties3);
        nodeService.addAspect(n1, noderefsAspect, properties4);

        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
    }

    public void testAddAspectValidCategory()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");

        NodeRef n1 = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties1 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties2 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties3 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties4 = new HashMap<QName, Serializable>();
        properties1.put(aspectCategoryProp, cat);
        properties2.put(aspectCategoriesProp, cat);
        properties3.put(aspectNoderefProp, cat);
        properties4.put(aspectNoderefsProp, cat);

        // Add aspect

        nodeService.addAspect(n1, categoryAspect, properties1);
        nodeService.addAspect(n1, categoriesAspect, properties2);
        nodeService.addAspect(n1, noderefAspect, properties3);
        nodeService.addAspect(n1, noderefsAspect, properties4);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNotNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
    }

    public void testAddAspectEmptyList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");

        NodeRef n1 = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties1 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties2 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties3 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties4 = new HashMap<QName, Serializable>();
        ArrayList<NodeRef> vals = new ArrayList<NodeRef>();
        properties2.put(aspectCategoriesProp, vals);
        properties4.put(aspectNoderefsProp, vals);

        // Add aspect

        nodeService.addAspect(n1, categoryAspect, properties1);
        nodeService.addAspect(n1, categoriesAspect, properties2);
        nodeService.addAspect(n1, noderefAspect, properties3);
        nodeService.addAspect(n1, noderefsAspect, properties4);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
    }

    public void testAddAspectInvalidList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");

        NodeRef n1 = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties1 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties2 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties3 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties4 = new HashMap<QName, Serializable>();
        ArrayList<NodeRef> vals = new ArrayList<NodeRef>();
        vals.add(invalidNodeRef);
        properties2.put(aspectCategoriesProp, vals);
        properties4.put(aspectNoderefsProp, vals);

        // Add aspect

        nodeService.addAspect(n1, categoryAspect, properties1);
        nodeService.addAspect(n1, categoriesAspect, properties2);
        nodeService.addAspect(n1, noderefAspect, properties3);
        nodeService.addAspect(n1, noderefsAspect, properties4);
        
        assertEquals(1, properties4.size());

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
    }

    public void testAddAspectNodeRefList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");

        NodeRef n1 = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties1 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties2 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties3 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties4 = new HashMap<QName, Serializable>();
        ArrayList<NodeRef> vals = new ArrayList<NodeRef>();
        vals.add(rootNodeRef);
        properties2.put(aspectCategoriesProp, vals);
        properties4.put(aspectNoderefsProp, vals);

        // Add aspect

        nodeService.addAspect(n1, categoryAspect, properties1);
        nodeService.addAspect(n1, categoriesAspect, properties2);
        nodeService.addAspect(n1, noderefAspect, properties3);
        nodeService.addAspect(n1, noderefsAspect, properties4);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
    }

    public void testAddAspectCatList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");

        NodeRef n1 = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties1 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties2 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties3 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties4 = new HashMap<QName, Serializable>();
        ArrayList<NodeRef> vals = new ArrayList<NodeRef>();
        vals.add(cat);
        properties2.put(aspectCategoriesProp, vals);
        properties4.put(aspectNoderefsProp, vals);

        // Add aspect

        nodeService.addAspect(n1, categoryAspect, properties1);
        nodeService.addAspect(n1, categoriesAspect, properties2);
        nodeService.addAspect(n1, noderefAspect, properties3);
        nodeService.addAspect(n1, noderefsAspect, properties4);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
    }

    public void testAddAspectMixedList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");

        NodeRef n1 = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties1 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties2 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties3 = new HashMap<QName, Serializable>();
        HashMap<QName, Serializable> properties4 = new HashMap<QName, Serializable>();
        ArrayList<NodeRef> vals = new ArrayList<NodeRef>();
        vals.add(cat);
        vals.add(rootNodeRef);
        vals.add(invalidNodeRef);
        properties2.put(aspectCategoriesProp, vals);
        properties4.put(aspectNoderefsProp, vals);

        // Add aspect

        nodeService.addAspect(n1, categoryAspect, properties1);
        nodeService.addAspect(n1, categoriesAspect, properties2);
        nodeService.addAspect(n1, noderefAspect, properties3);
        nodeService.addAspect(n1, noderefsAspect, properties4);

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(2, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(2, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(2, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(2, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
    }

    public void testCreateNodeNull()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER, null).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
    }

    public void testCreateNodeNullValues()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(aspectCategoryProp, null);
        properties.put(aspectCategoriesProp, null);
        properties.put(aspectNoderefProp, null);
        properties.put(aspectNoderefsProp, null);

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER, properties).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
    }

    public void testCreateNodeInvalidValues()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(aspectCategoryProp, invalidNodeRef);
        properties.put(aspectCategoriesProp, invalidNodeRef);
        properties.put(aspectNoderefProp, invalidNodeRef);
        properties.put(aspectNoderefsProp, invalidNodeRef);

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER, properties).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
    }

    public void testCreateNodeNodeRefValues()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(aspectCategoryProp, rootNodeRef);
        properties.put(aspectCategoriesProp, rootNodeRef);
        properties.put(aspectNoderefProp, rootNodeRef);
        properties.put(aspectNoderefsProp, rootNodeRef);

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER, properties).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
    }

    public void testCreateNodeCatValues()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(aspectCategoryProp, cat);
        properties.put(aspectCategoriesProp, cat);
        properties.put(aspectNoderefProp, cat);
        properties.put(aspectNoderefsProp, cat);

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER, properties).getChildRef();

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(aspectNoderefsProp));
        assertNotNull(nodeService.getProperty(n1, aspectCategoryProp));
        assertNotNull(nodeService.getProperty(n1, aspectCategoriesProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefProp));
        assertNotNull(nodeService.getProperty(n1, aspectNoderefsProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoryProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
    }

    public void testCreateEmptyListValues()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        ArrayList<NodeRef> val = new ArrayList<NodeRef>();

        properties.put(aspectCategoriesProp, val);
        properties.put(aspectNoderefsProp, val);

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER, properties).getChildRef();

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
    }

    public void testCreateInvalidListValues()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        ArrayList<NodeRef> val = new ArrayList<NodeRef>();
        val.add(invalidNodeRef);

        properties.put(aspectCategoriesProp, val);
        properties.put(aspectNoderefsProp, val);

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER, properties).getChildRef();

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
    }

    public void testCreateNodeRefListValues()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        ArrayList<NodeRef> val = new ArrayList<NodeRef>();
        val.add(rootNodeRef);

        properties.put(aspectCategoriesProp, val);
        properties.put(aspectNoderefsProp, val);

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER, properties).getChildRef();

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
    }

    public void testCreateCatListValues()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        ArrayList<NodeRef> val = new ArrayList<NodeRef>();
        val.add(cat);

        properties.put(aspectCategoriesProp, val);
        properties.put(aspectNoderefsProp, val);

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER, properties).getChildRef();

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
    }

    public void testCreateMixedListValues()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        ArrayList<NodeRef> val = new ArrayList<NodeRef>();
        val.add(invalidNodeRef);
        val.add(rootNodeRef);
        val.add(cat);

        properties.put(aspectCategoriesProp, val);
        properties.put(aspectNoderefsProp, val);

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER, properties).getChildRef();

        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(2, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, aspectCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, aspectNoderefsProp));
        assertEquals(2, ((Collection) mlAwareNodeService.getProperty(n1, aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(2, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(aspectCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(aspectNoderefsProp));
        assertEquals(2, ((Collection) nodeService.getProperties(n1).get(aspectNoderefsProp)).size());
    }

    public void testSetPropertyNull()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        nodeService.setProperty(n1, typeCategoryProp, null);
        nodeService.setProperty(n1, typeCategoriesProp, null);
        nodeService.setProperty(n1, typeNoderefProp, null);
        nodeService.setProperty(n1, typeNoderefsProp, null);

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));
    }

    public void testSetPropertyInvalid()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        nodeService.setProperty(n1, typeCategoryProp, invalidNodeRef);
        nodeService.setProperty(n1, typeCategoriesProp, invalidNodeRef);
        nodeService.setProperty(n1, typeNoderefProp, invalidNodeRef);
        nodeService.setProperty(n1, typeNoderefsProp, invalidNodeRef);

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));
    }

    public void testSetPropertyNodeRef()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        nodeService.setProperty(n1, typeCategoryProp, rootNodeRef);
        nodeService.setProperty(n1, typeCategoriesProp, rootNodeRef);
        nodeService.setProperty(n1, typeNoderefProp, rootNodeRef);
        nodeService.setProperty(n1, typeNoderefsProp, rootNodeRef);

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNotNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());
    }

    public void testSetPropertyCat()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        nodeService.setProperty(n1, typeCategoryProp, cat);
        nodeService.setProperty(n1, typeCategoriesProp, cat);
        nodeService.setProperty(n1, typeNoderefProp, cat);
        nodeService.setProperty(n1, typeNoderefsProp, cat);

        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNotNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNotNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());

    }

    public void testSetPropertyEmptyList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        ArrayList<NodeRef> values = new ArrayList<NodeRef>();

        nodeService.setProperty(n1, typeCategoriesProp, values);
        nodeService.setProperty(n1, typeNoderefsProp, values);

        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());
    }

    public void testSetPropertyNodeRefList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        ArrayList<NodeRef> values = new ArrayList<NodeRef>();
        values.add(rootNodeRef);

        nodeService.setProperty(n1, typeCategoriesProp, values);
        nodeService.setProperty(n1, typeNoderefsProp, values);

        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());
    }

    public void testSetPropertyCatList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        ArrayList<NodeRef> values = new ArrayList<NodeRef>();
        values.add(cat);

        nodeService.setProperty(n1, typeCategoriesProp, values);
        nodeService.setProperty(n1, typeNoderefsProp, values);

        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());
    }

    public void testSetPropertyMixedList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        ArrayList<NodeRef> values = new ArrayList<NodeRef>();
        values.add(cat);
        values.add(rootNodeRef);
        values.add(invalidNodeRef);

        nodeService.setProperty(n1, typeCategoriesProp, values);
        nodeService.setProperty(n1, typeNoderefsProp, values);

        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(2, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(2, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(2, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(2, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());
    }

    public void testSetPropertiesNull()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(typeCategoryProp, null);
        properties.put(typeCategoriesProp, null);
        properties.put(typeNoderefProp, null);
        properties.put(typeNoderefsProp, null);

        nodeService.setProperties(n1, properties);

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));
    }

    public void testSetPropertiesInvalid()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(typeCategoryProp, invalidNodeRef);
        properties.put(typeCategoriesProp, invalidNodeRef);
        properties.put(typeNoderefProp, invalidNodeRef);
        properties.put(typeNoderefsProp, invalidNodeRef);

        nodeService.setProperties(n1, properties);

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));
    }

    public void testSetPropertiesNodeRef()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(typeCategoryProp, rootNodeRef);
        properties.put(typeCategoriesProp, rootNodeRef);
        properties.put(typeNoderefProp, rootNodeRef);
        properties.put(typeNoderefsProp, rootNodeRef);

        nodeService.setProperties(n1, properties);

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNotNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());
    }

    public void testSetPropertiesCat()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(typeCategoryProp, cat);
        properties.put(typeCategoriesProp, cat);
        properties.put(typeNoderefProp, cat);
        properties.put(typeNoderefsProp, cat);

        nodeService.setProperties(n1, properties);

        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoryProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefProp));
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoryProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefProp));
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeCategoryProp));
        assertNotNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeNoderefProp));
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeCategoryProp));
        assertNotNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefProp));
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());

    }

    public void testSetPropertiesEmptyList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        ArrayList<NodeRef> values = new ArrayList<NodeRef>();

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(typeCategoriesProp, values);
        properties.put(typeNoderefsProp, values);

        nodeService.setProperties(n1, properties);

        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());
    }

    public void testSetPropertiesNodeRefList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        ArrayList<NodeRef> values = new ArrayList<NodeRef>();
        values.add(rootNodeRef);

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(typeCategoriesProp, values);
        properties.put(typeNoderefsProp, values);
        
        nodeService.setProperties(n1, properties);

        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(0, ((Collection) mlAwareNodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(0, ((Collection) nodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());
    }

    public void testSetPropertiesCatList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        ArrayList<NodeRef> values = new ArrayList<NodeRef>();
        values.add(cat);

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(typeCategoriesProp, values);
        properties.put(typeNoderefsProp, values);
        
        nodeService.setProperties(n1, properties);

        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());
    }

    public void testSetPropertiesMixedList()
    {
        NodeRef invalidNodeRef = new NodeRef(rootNodeRef.getStoreRef(), "InvalidNode");
        NodeRef cat = mlAwareNodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testType).getChildRef();

        assertNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertNull(nodeService.getProperties(n1).get(typeNoderefsProp));

        ArrayList<NodeRef> values = new ArrayList<NodeRef>();
        values.add(cat);
        values.add(rootNodeRef);
        values.add(invalidNodeRef);

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(typeCategoriesProp, values);
        properties.put(typeNoderefsProp, values);
        
        nodeService.setProperties(n1, properties);

        assertNotNull(mlAwareNodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(2, ((Collection) mlAwareNodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) mlAwareNodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(mlAwareNodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(2, ((Collection) mlAwareNodeService.getProperties(n1).get(typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperty(n1, typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperty(n1, typeNoderefsProp));
        assertEquals(2, ((Collection) nodeService.getProperty(n1, typeNoderefsProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeCategoriesProp));
        assertEquals(1, ((Collection) nodeService.getProperties(n1).get(typeCategoriesProp)).size());
        assertNotNull(nodeService.getProperties(n1).get(typeNoderefsProp));
        assertEquals(2, ((Collection) nodeService.getProperties(n1).get(typeNoderefsProp)).size());
    }

}
