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
package org.alfresco.repo.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.repo.domain.dialect.PostgreSQLDialect;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.ISO9075;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @see org.alfresco.repo.search.SearcherComponent
 *
 * @author Derek Hulley
 */
public class SearcherComponentTest extends BaseSpringTest
{
    //private static String COMPLEX_LOCAL_NAME = " `¬¦!\"£$%^&*()-_=+\t\n\\\u0000[]{};'#:@~,./<>?\\|\u0123\u4567\u8900\uabcd\uefff_xT65A_";
    // \u0123 and \u8900 removed

    //private static String COMPLEX_LOCAL_NAME = "\u0020\u0060\u00ac\u00a6\u0021\"\u00a3\u0024\u0025\u005e\u0026\u002a\u0028\u0029\u002d\u005f\u003d\u002b\t\n\\\u0000\u005b\u005d\u007b\u007d\u003b\u0027\u0023\u003a\u0040\u007e\u002c\u002e\u002f\u003c\u003e\u003f\\u007c\u4567\uabcd\uefff\u005f\u0078\u0054\u0036\u0035\u0041\u005f";

    private static String COMPLEX_LOCAL_NAME = "\u0020\u0060\u00ac\u00a6\u0021\"\u00a3\u0024\u0025\u005e\u0026\u002a\u0028\u0029\u002d\u005f\u003d\u002b\t\n\\\u0000\u005b\u005d\u007b\u007d\u003b\u0027\u0023\u003a\u0040\u007e\u002c\u002e\u002f\u003c\u003e\u003f\\u007c\u005f\u0078\u0054\u0036\u0035\u0041\u005f";

    private static String COMPLEX_LOCAL_NAME_NO_U0000 = "\u0020\u0060\u00ac\u00a6\u0021\"\u00a3\u0024\u0025\u005e\u0026\u002a\u0028\u0029\u002d\u005f\u003d\u002b\t\n\\\u005b\u005d\u007b\u007d\u003b\u0027\u0023\u003a\u0040\u007e\u002c\u002e\u002f\u003c\u003e\u003f\\u007c\u005f\u0078\u0054\u0036\u0035\u0041\u005f";


    private ServiceRegistry serviceRegistry;

    private TransactionService transactionService;

    private DictionaryService dictionaryService;

    private SearcherComponent searcher;

    private NodeService nodeService;

    private AuthenticationComponent authenticationComponent;

    private NodeRef rootNodeRef;

    private UserTransaction txn;

    // TODO: pending replacement
    private Dialect dialect;

    @Before
    public void setUp() throws Exception
    {
        dialect = (Dialect) applicationContext.getBean("dialect");
        if (dialect instanceof PostgreSQLDialect)
        {
            // Note: PostgreSQL does not support \u0000 char embedded in a string
            // http://archives.postgresql.org/pgsql-jdbc/2007-02/msg00115.php
            COMPLEX_LOCAL_NAME = COMPLEX_LOCAL_NAME_NO_U0000;
        }

        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        dictionaryService = BaseNodeServiceTest.loadModel(applicationContext);
        nodeService = serviceRegistry.getNodeService();

        this.authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        this.authenticationComponent.setSystemUserAsCurrentUser();

        // get the indexer and searcher factory
        IndexerAndSearcher indexerAndSearcher = (IndexerAndSearcher) applicationContext.getBean("indexerAndSearcherFactory");
        searcher = new SearcherComponent();
        searcher.setIndexerAndSearcherFactory(indexerAndSearcher);
        // create a test workspace
        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName()
                + "_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        // begin a transaction
        txn = transactionService.getUserTransaction();
        txn.begin();
    }

    @After
    public void tearDown() throws Exception
    {
        if (txn.getStatus() == Status.STATUS_ACTIVE)
        {
            txn.rollback();
        }
        authenticationComponent.clearCurrentSecurityContext();
        super.tearDown();
    }

    @Test
    public void testNodeXPath() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = BaseNodeServiceTest.buildNodeGraph(nodeService, rootNodeRef);

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(QName.createQName(BaseNodeServiceTest.NAMESPACE, COMPLEX_LOCAL_NAME), "monkey");
        QName qnamerequiringescaping = QName.createQName(BaseNodeServiceTest.NAMESPACE, COMPLEX_LOCAL_NAME);
        nodeService.createNode(rootNodeRef, BaseNodeServiceTest.ASSOC_TYPE_QNAME_TEST_CHILDREN, qnamerequiringescaping,
                ContentModel.TYPE_CONTAINER);
        QName qname = QName.createQName(BaseNodeServiceTest.NAMESPACE, "n2_p_n4");

        NodeServiceXPath xpath;
        String xpathStr;
        QueryParameterDefImpl paramDef;
        List list;

        DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
        namespacePrefixResolver.registerNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        // create the document navigator
        DocumentNavigator documentNavigator = new DocumentNavigator(dictionaryService, nodeService, searcher,
                namespacePrefixResolver, false, false);

        xpath = new NodeServiceXPath("//.[@test:animal='monkey']", documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(1, list.size());

        xpath = new NodeServiceXPath("*", documentNavigator, null);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(3, list.size());

        xpath = new NodeServiceXPath("*/*", documentNavigator, null);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(5, list.size());

        xpath = new NodeServiceXPath("*/*/*", documentNavigator, null);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(3, list.size());

        xpath = new NodeServiceXPath("*/*/*/*", documentNavigator, null);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(2, list.size());

        xpath = new NodeServiceXPath("*/*/*/*/..", documentNavigator, null);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(2, list.size());

        xpath = new NodeServiceXPath("*//.", documentNavigator, null);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(13, list.size());

        xpathStr = "test:root_p_n1";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(1, list.size());

        xpathStr = "*//.[@test:animal]";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(1, list.size());

        xpathStr = "*//.[@test:animal='monkey']";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(1, list.size());

        xpathStr = "//.[@test:animal='monkey']";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(1, list.size());

        paramDef = new QueryParameterDefImpl(QName.createQName("test:test", namespacePrefixResolver), dictionaryService
                .getDataType(DataTypeDefinition.TEXT), true, "monkey");
        xpathStr = "//.[@test:animal=$test:test]";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, new QueryParameterDefinition[] { paramDef });
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(1, list.size());

        xpath = new NodeServiceXPath(".", documentNavigator, null);
        list = xpath.selectNodes(assocRefs.get(qname));
        assertEquals(1, list.size());

        xpath = new NodeServiceXPath("/test:" + ISO9075.encode(COMPLEX_LOCAL_NAME), documentNavigator, null);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(1, list.size());

        xpath = new NodeServiceXPath("//test:" + ISO9075.encode(COMPLEX_LOCAL_NAME), documentNavigator, null);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(1, list.size());

        xpath = new NodeServiceXPath("..", documentNavigator, null);
        list = xpath.selectNodes(assocRefs.get(qname));
        assertEquals(1, list.size());

        // follow all parent links now
        documentNavigator.setFollowAllParentLinks(true);

        xpath = new NodeServiceXPath("..", documentNavigator, null);
        list = xpath.selectNodes(assocRefs.get(qname));
        assertEquals(2, list.size());

        xpathStr = "//@test:animal";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(assocRefs.get(qname));
        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof DocumentNavigator.Property);

        xpathStr = "//@test:reference";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(assocRefs.get(qname));
        assertEquals(1, list.size());

        // stop following parent links
        documentNavigator.setFollowAllParentLinks(false);

        xpathStr = "deref(/test:root_p_n1/test:n1_p_n3/@test:reference, '*')";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(assocRefs.get(qname));
        assertEquals(1, list.size());

        xpathStr = "deref(/test:root_p_n1/test:n1_p_n3/@test:reference, 'test:root_p_n1')";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(assocRefs.get(qname));
        assertEquals(0, list.size());

        xpathStr = "deref(/test:root_p_n1/test:n1_p_n3/@test:reference, 'test:root_p_n2')";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(assocRefs.get(qname));
        assertEquals(1, list.size());

        // test 'subtypeOf' function
        paramDef = new QueryParameterDefImpl(QName.createQName("test:type", namespacePrefixResolver), dictionaryService
                .getDataType(DataTypeDefinition.QNAME), true, BaseNodeServiceTest.TYPE_QNAME_TEST_CONTENT
                .toPrefixString(namespacePrefixResolver));
        xpathStr = "//.[subtypeOf($test:type)]";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, new QueryParameterDefinition[] { paramDef });
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(assocRefs.get(qname));
        assertEquals(3, list.size()); // 2 distinct paths to node n8, which is of type content

        xpath = new NodeServiceXPath("/", documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(assocRefs.get(qname));
        assertEquals(1, list.size());

        xpathStr = "test:root_p_n1 | test:root_p_n2";
        xpath = new NodeServiceXPath(xpathStr, documentNavigator, null);
        xpath.addNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);
        list = xpath.selectNodes(new ChildAssociationRef(null, null, null, rootNodeRef));
        assertEquals(2, list.size());
    }

    @Test
    public void testSelectAPI() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = BaseNodeServiceTest.buildNodeGraph(nodeService, rootNodeRef);
        NodeRef n6Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n3_p_n6")).getChildRef();

        DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
        namespacePrefixResolver.registerNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);

        List<NodeRef> answer = searcher.selectNodes(rootNodeRef, "/test:root_p_n1/test:n1_p_n3/*", null,
                namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        assertTrue(answer.contains(n6Ref));

        // List<ChildAssocRef>
        answer = searcher.selectNodes(rootNodeRef, "*", null, namespacePrefixResolver, false);
        assertEquals(2, answer.size());

        List<Serializable> attributes = searcher.selectProperties(rootNodeRef, "//@test:animal", null,
                namespacePrefixResolver, false);
        assertEquals(1, attributes.size());

        NodeRef n1 = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root_p_n1")).getChildRef();
        NodeRef n2 = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root_p_n2")).getChildRef();

        answer = searcher.selectNodes(rootNodeRef, "test:root_p_n1 | test:root_p_n2", null, namespacePrefixResolver, false);
        assertEquals(2, answer.size());
        assertTrue(answer.contains(n1));
        assertTrue(answer.contains(n2));

        NodeRef n3 = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n1_p_n3")).getChildRef();

        answer = searcher.selectNodes(rootNodeRef, "//@test:animal", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        assertTrue(answer.contains(n3));

        answer = searcher.selectNodes(rootNodeRef, "*//.[@test:animal]", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        assertTrue(answer.contains(n3));

        answer = searcher.selectNodes(rootNodeRef, "*//.[@test:animal='monkey']", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        assertTrue(answer.contains(n3));

        answer = searcher.selectNodes(rootNodeRef, "//.[@test:animal='monkey']", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        assertTrue(answer.contains(n3));

        answer = searcher.selectNodes(rootNodeRef, "*//.[@test:animal='monkey']", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        assertTrue(answer.contains(n3));

        // "like" query is not translated to DB query
//        answer = searcher.selectNodes(rootNodeRef, "//*[like(@test:animal, 'monk*')]", null, namespacePrefixResolver, false);
//        assertEquals(1, answer.size());
//        assertTrue(answer.contains(n3));

        answer = searcher.selectNodes(rootNodeRef, "//@*", null, namespacePrefixResolver, false);
        assertEquals(9, answer.size());

        QName qname = QName.createQName(BaseNodeServiceTest.NAMESPACE, "my@test_with_at_sign");

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(QName.createQName(BaseNodeServiceTest.NAMESPACE, "mytest"), "my@test_value_with_at_sign");

        ChildAssociationRef assoc = nodeService.createNode(n1, BaseNodeServiceTest.ASSOC_TYPE_QNAME_TEST_CHILDREN, qname, ContentModel.TYPE_CONTAINER, properties);
        NodeRef n4 = assoc.getChildRef();

        StringBuffer path = new StringBuffer().append("test:root_p_n1/").append(ISO9075.getXPathName(qname, namespacePrefixResolver));
        answer = searcher.selectNodes(rootNodeRef, path.toString(), null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        assertTrue(answer.contains(n4));

        String xpathQuery = "//*[@test:mytest='my@test_value_with_at_sign']";
        answer = searcher.selectNodes(rootNodeRef, xpathQuery, null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        assertTrue(answer.contains(n4));
    }

    /**
     * Tests the <b>like</b> and <b>contains</b> functions (FTS functions) within a currently executing transaction
     */
    public void xtestLikeAndContains() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = BaseNodeServiceTest.buildNodeGraph(nodeService, rootNodeRef);

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(QName.createQName(BaseNodeServiceTest.NAMESPACE, COMPLEX_LOCAL_NAME), "monkey");
        QName qnamerequiringescaping = QName.createQName(BaseNodeServiceTest.NAMESPACE, COMPLEX_LOCAL_NAME);
        nodeService.createNode(rootNodeRef, BaseNodeServiceTest.ASSOC_TYPE_QNAME_TEST_CHILDREN, qnamerequiringescaping,
                ContentModel.TYPE_CONTAINER, properties);

        // commit the node graph
        txn.commit();

        txn = transactionService.getUserTransaction();
        txn.begin();

        DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
        namespacePrefixResolver.registerNamespace(BaseNodeServiceTest.TEST_PREFIX, BaseNodeServiceTest.NAMESPACE);

        List<NodeRef> answer = searcher.selectNodes(rootNodeRef, "//*[like(@test:animal, 'm__k%', false)]", null,
                namespacePrefixResolver, false);
        assertEquals(1, answer.size());

        System.out.println("Encoded = "+ISO9075.encode(COMPLEX_LOCAL_NAME));
        String roundTrip = ISO9075.decode(ISO9075.encode(COMPLEX_LOCAL_NAME));
        for(int i = 0; i < COMPLEX_LOCAL_NAME.length() && 1 < roundTrip.length(); i++)
        {
            System.out.println("Char at "+i+" = "+Integer.toHexString(COMPLEX_LOCAL_NAME.charAt(i))+ "   ...    "+Integer.toHexString(roundTrip.charAt(i)));
        }

        assertEquals( COMPLEX_LOCAL_NAME, roundTrip);

        answer = searcher.selectNodes(rootNodeRef, "//*[like(@test:"
                + ISO9075.encode(COMPLEX_LOCAL_NAME) + ", 'm__k%', false)]", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());

        answer = searcher.selectNodes(rootNodeRef, "//*[like(@test:animal, 'M__K%', false)]", null,
                namespacePrefixResolver, false);
        assertEquals(1, answer.size());

        answer = searcher.selectNodes(rootNodeRef, "//*[like(@test:"
                + ISO9075.encode(COMPLEX_LOCAL_NAME) + ", 'M__K%', false)]", null, namespacePrefixResolver, false);
        assertEquals(1, answer.size());

        answer = searcher.selectNodes(rootNodeRef, "//*[like(@test:UPPERANIMAL, 'm__k%', false)]", null,
                namespacePrefixResolver, false);
        assertEquals(1, answer.size());

        answer = searcher.selectNodes(rootNodeRef, "//*[like(@test:UPPERANIMAL, 'M__K%', false)]", null,
                namespacePrefixResolver, false);
        assertEquals(1, answer.size());

        answer = searcher.selectNodes(rootNodeRef, "//*[like(@test:UPPERANIMAL, 'M__K%', true)]", null,
                namespacePrefixResolver, false);
        assertEquals(1, answer.size());

        answer = searcher.selectNodes(rootNodeRef, "//*[contains('monkey')]", null, namespacePrefixResolver, false);
        assertEquals(2, answer.size());

        answer = searcher.selectNodes(rootNodeRef, "//*[contains('MONKEY')]", null, namespacePrefixResolver, false);
        assertEquals(2, answer.size());

        answer = searcher.selectNodes(rootNodeRef, "//*[contains(lower-case('MONKEY'))]", null,
                namespacePrefixResolver, false);
        assertEquals(2, answer.size());

        // select the monkey node in the second level
        QueryParameterDefinition[] paramDefs = new QueryParameterDefinition[2];
        paramDefs[0] = new QueryParameterDefImpl(QName.createQName("test:animal", namespacePrefixResolver),
                dictionaryService.getDataType(DataTypeDefinition.TEXT), true, "monkey%");
        paramDefs[1] = new QueryParameterDefImpl(QName.createQName("test:type", namespacePrefixResolver),
                dictionaryService.getDataType(DataTypeDefinition.TEXT), true,
                BaseNodeServiceTest.TYPE_QNAME_TEST_CONTENT.toString());
        answer = searcher.selectNodes(rootNodeRef,
                "./*/*[like(@test:animal, $test:animal, false) or subtypeOf($test:type)]", paramDefs,
                namespacePrefixResolver, false);
        assertEquals(1, answer.size());

        // select the monkey node again, but use the first level as the starting poing
        NodeRef n1Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root_p_n1")).getChildRef();
        NodeRef n3Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n1_p_n3")).getChildRef();
        // first time go too deep
        answer = searcher.selectNodes(n1Ref, "./*/*[like(@test:animal, $test:animal, false) or subtypeOf($test:type)]",
                paramDefs, namespacePrefixResolver, false);
        assertEquals(0, answer.size());
        // second time get it right
        answer = searcher.selectNodes(n1Ref, "./*[like(@test:animal, $test:animal, false) or subtypeOf($test:type)]",
                paramDefs, namespacePrefixResolver, false);
        assertEquals(1, answer.size());
        assertFalse("Incorrect result: search root node pulled back", answer.contains(n1Ref));
        assertTrue("Incorrect result: incorrect node retrieved", answer.contains(n3Ref));
    }

    public static void main(String[] args)
    {
        String escape = "\\\t\n\"";
        StringBuilder builder = new StringBuilder();
        builder.append("\"");
        for (int i = 0; i < COMPLEX_LOCAL_NAME.length(); i++)
        {
            if (escape.indexOf(COMPLEX_LOCAL_NAME.charAt(i)) != -1)
            {
                builder.append(COMPLEX_LOCAL_NAME.charAt(i));
            }
            else
            {
                String part = Integer.toHexString(COMPLEX_LOCAL_NAME.charAt(i));
                builder.append("\\u");
                if (part.length() == 0)
                {
                    builder.append("000");
                }
                if (part.length() == 1)
                {
                    builder.append("000");
                }
                if (part.length() == 2)
                {
                    builder.append("00");
                }
                if (part.length() == 3)
                {
                    builder.append("0");
                }

                builder.append(part);
                System.out.println(COMPLEX_LOCAL_NAME.charAt(i) + " = " + part);
            }

        }
        builder.append("\"");
        System.out.println(builder.toString());
    }
}