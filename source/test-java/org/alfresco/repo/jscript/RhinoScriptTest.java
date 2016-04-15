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
package org.alfresco.repo.jscript;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.experimental.categories.Category;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.context.ApplicationContext;

/**
 * @author Kevin Roast
 */
@Category(OwnJVMTestsCategory.class)
public class RhinoScriptTest extends TestCase
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private ContentService contentService;
    private NodeService nodeService;
    private TransactionService transactionService;
    private ServiceRegistry serviceRegistry;
    private AuthenticationComponent authenticationComponent;
    private ScriptService scriptService;
    
    /*
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        transactionService = (TransactionService)ctx.getBean("transactionComponent");
        contentService = (ContentService)ctx.getBean("contentService");
        nodeService = (NodeService)ctx.getBean("nodeService");
        scriptService = (ScriptService)ctx.getBean("scriptService");
        serviceRegistry = (ServiceRegistry)ctx.getBean("ServiceRegistry");
        
        this.authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        DictionaryDAO dictionaryDao = (DictionaryDAO)ctx.getBean("dictionaryDAO");
        
        // load the system model
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("alfresco/model/contentModel.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        
        // load the test model
        modelStream = cl.getResourceAsStream("org/alfresco/repo/node/BaseNodeServiceTest_model.xml");
        assertNotNull(modelStream);
        model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        
        DictionaryComponent dictionary = new DictionaryComponent();
        dictionary.setDictionaryDAO(dictionaryDao);
        BaseNodeServiceTest.loadModel(ctx);
    }

    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    public void testRhinoIntegration()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    // check that rhino script engine is available
                    Context cx = Context.enter();
                    try
                    {
                        // The easiest way to embed Rhino is just to create a new scope this way whenever
                        // you need one. However, initStandardObjects is an expensive method to call and it
                        // allocates a fair amount of memory.
                        Scriptable scope = cx.initStandardObjects();
                        
                        // Now we can evaluate a script. Let's create a new associative array object
                        // using the object literal notation to create the members
                        Object result = cx.evaluateString(scope, "obj = {a:1, b:['x','y']}", "TestJS1", 1, null);
                        
                        Scriptable obj = (Scriptable)scope.get("obj", scope);
                        
                        // Should resolve a non-null value
                        assertNotNull(obj.get("a", obj));
                        
                        // should resolve "obj.a == 1" - JavaScript objects come back as Number
                        assertEquals(new Double(1.0), obj.get("a", obj));
                        
                        // try another script eval - this time a function call returning a result
                        result = cx.evaluateString(scope, "function f(x) {return x+1} f(7)", "TestJS2", 1, null);
                        assertEquals(8.0, Context.toNumber(result));
                    }
                    finally
                    {
                        Context.exit();
                    }
                    
                    return null;
                }                
            });
    }
    
    public void testJSObjectWrapping()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    StoreRef store = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "rhino_" + System.currentTimeMillis());
                    NodeRef root = nodeService.getRootNode(store);
                    BaseNodeServiceTest.buildNodeGraph(nodeService, root);
                    
                    Context cx = Context.enter();
                    try
                    {
                        Scriptable scope = cx.initStandardObjects();
                        
                        // add logger object so we can perform output from within scripts
                        ScriptableObject.putProperty(scope, "logger", new ScriptLogger());
                        
                        // wrap a simple NodeRef Java object
                        // we can use Java style method calls within the script to access it's properties
                        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(root);
                        NodeRef ref1 = childAssocs.get(0).getChildRef();
                        Object wrappedNodeRef = Context.javaToJS(ref1, scope);
                        ScriptableObject.putProperty(scope, "rootref", wrappedNodeRef);
                        
                        // evaluate script that touches the wrapped NodeRef
                        Object result = cx.evaluateString(scope, "obj = rootref.getId()", "TestJS3", 1, null);
                        assertEquals(ref1.getId(), Context.toString(result));
                        
                        // wrap a scriptable Alfresco Node object - the Node object is a wrapper like TemplateNode
                        ScriptNode node1 = new ScriptNode(root, serviceRegistry, scope);
                        Object wrappedNode = Context.javaToJS(node1, scope);
                        ScriptableObject.putProperty(scope, "root", wrappedNode);
                        
                        // evaluate scripts that perform methods on the wrapped Node
                        result = cx.evaluateString(scope, TESTSCRIPT1, "TestJS4", 1, null);
                    }
                    finally
                    {
                        Context.exit();
                    }
                    
                    return null;
                }                
            });
    }
    
    public void testScriptService()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    StoreRef store = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "rhino_" + System.currentTimeMillis());
                    NodeRef root = nodeService.getRootNode(store);
                    BaseNodeServiceTest.buildNodeGraph(nodeService, root);
                    
                    try
                    {
                        Map<String, Object> model = new HashMap<String, Object>();
                        model.put("out", System.out);
                        
                        // create an Alfresco scriptable Node object
                        // the Node object is a wrapper similar to the TemplateNode concept
                        ScriptNode rootNode = new ScriptNode(root, serviceRegistry, null);
                        model.put("root", rootNode);
                        
                        // execute test scripts using the various entry points of the ScriptService
                        
                        // test executing a script file via classpath lookup
                        Object result = scriptService.executeScript(TESTSCRIPT_CLASSPATH1, model);
                        System.out.println("Result from TESTSCRIPT_CLASSPATH1: " + result.toString());
                        assertTrue((Boolean)result);    // we know the result is a boolean
                        
                        // test executing a script embedded inside Node content
                        ChildAssociationRef childRef = nodeService.createNode(
                                root,
                                BaseNodeServiceTest.ASSOC_TYPE_QNAME_TEST_CHILDREN,
                                QName.createQName(BaseNodeServiceTest.NAMESPACE, "script_content"),
                                BaseNodeServiceTest.TYPE_QNAME_TEST_CONTENT,
                                null);
                        NodeRef contentNodeRef = childRef.getChildRef();
                        ContentWriter writer = contentService.getWriter(
                                contentNodeRef,
                                BaseNodeServiceTest.PROP_QNAME_TEST_CONTENT,
                                true);
                        writer.setMimetype("application/x-javascript");
                        writer.putContent(TESTSCRIPT1);
                        scriptService.executeScript(contentNodeRef, BaseNodeServiceTest.PROP_QNAME_TEST_CONTENT, model);
                        
                        // test executing a script directly as a String
                        scriptService.executeScriptString("javascript", TESTSCRIPT1, model);
                    }
                    finally
                    {
                    }
                    
                    return null;
                }                
            });
    }
    
    public void testScriptActions()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    StoreRef store = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "rhino_" + System.currentTimeMillis());
                    NodeRef root = nodeService.getRootNode(store);
                    
                    try
                    {
                        // create a content object
                        ChildAssociationRef childRef = nodeService.createNode(
                                root,
                                BaseNodeServiceTest.ASSOC_TYPE_QNAME_TEST_CHILDREN,
                                QName.createQName(BaseNodeServiceTest.NAMESPACE, "script_content"),
                                BaseNodeServiceTest.TYPE_QNAME_TEST_CONTENT,
                                null);
                        NodeRef contentNodeRef = childRef.getChildRef();
                        ContentWriter writer = contentService.getWriter(
                                contentNodeRef,
                                BaseNodeServiceTest.PROP_QNAME_TEST_CONTENT,
                                true);
                        writer.setMimetype("application/x-javascript");
                        writer.putContent(TESTSCRIPT1);

                        
                        // create an Alfresco scriptable Node object
                        // the Node object is a wrapper similar to the TemplateNode concept
                        Map<String, Object> model = new HashMap<String, Object>();
                        model.put("doc", new ScriptNode(childRef.getChildRef(), serviceRegistry, null));
                        model.put("root", new ScriptNode(root, serviceRegistry, null));
                        
                        // execute to add aspect via action
                        Object result = scriptService.executeScript(TESTSCRIPT_CLASSPATH2, model);
                        System.out.println("Result from TESTSCRIPT_CLASSPATH2: " + result.toString());
                        assertTrue((Boolean)result);    // we know the result is a boolean

                        // ensure aspect has been added via script
                        assertTrue(nodeService.hasAspect(childRef.getChildRef(), ContentModel.ASPECT_LOCKABLE));
                    }
                    finally
                    {
                    }
                    
                    return null;
                }                
            });
    }

    
    public void xtestScriptActionsMail()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    StoreRef store = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "rhino_" + System.currentTimeMillis());
                    NodeRef root = nodeService.getRootNode(store);
                    
                    try
                    {
                        // create a content object
                        ChildAssociationRef childRef = nodeService.createNode(
                                root,
                                BaseNodeServiceTest.ASSOC_TYPE_QNAME_TEST_CHILDREN,
                                QName.createQName(BaseNodeServiceTest.NAMESPACE, "script_content"),
                                BaseNodeServiceTest.TYPE_QNAME_TEST_CONTENT,
                                null);
                        NodeRef contentNodeRef = childRef.getChildRef();
                        ContentWriter writer = contentService.getWriter(
                                contentNodeRef,
                                BaseNodeServiceTest.PROP_QNAME_TEST_CONTENT,
                                true);
                        writer.setMimetype("application/x-javascript");
                        writer.putContent(TESTSCRIPT1);
                        
                        // create an Alfresco scriptable Node object
                        // the Node object is a wrapper similar to the TemplateNode concept
                        Map<String, Object> model = new HashMap<String, Object>();
                        model.put("doc", new ScriptNode(childRef.getChildRef(), serviceRegistry, null));
                        model.put("root", new ScriptNode(root, serviceRegistry, null));
                        
                        // execute to add aspect via action
                        Object result = scriptService.executeScript(TESTSCRIPT_CLASSPATH3, model);
                        System.out.println("Result from TESTSCRIPT_CLASSPATH3: " + result.toString());
                        assertTrue((Boolean)result);    // we know the result is a boolean
                    }
                    finally
                    {
                    }
                    
                    return null;
                }                
            });
    }
    
    private static final String TESTSCRIPT_CLASSPATH1 = "org/alfresco/repo/jscript/test_script1.js";
    private static final String TESTSCRIPT_CLASSPATH2 = "org/alfresco/repo/jscript/test_script2.js";
    private static final String TESTSCRIPT_CLASSPATH3 = "org/alfresco/repo/jscript/test_script3.js";
    
    private static final String TESTSCRIPT1 =
            "var id = root.id;\r\n" + 
            "logger.log(id);\r\n" + 
            "var name = root.name;\r\n" + 
            "logger.log(name);\r\n" + 
            "var type = root.type;\r\n" + 
            "logger.log(type);\r\n" + 
            "var childList = root.children;\r\n" + 
            "logger.log(\"zero index node name: \" + childList[0].name);\r\n" +
            "logger.log(\"name property access: \" + childList[0].properties.name);\r\n" +
            "var childByNameNode = root.childByNamePath(\"/\" + childList[0].name);\r\n" +
            "logger.log(\"child by name path: \" + childByNameNode.name);\r\n" +
            "var xpathResults = root.childrenByXPath(\"/*\");\r\n" +
            "logger.log(\"children of root from xpath: \" + xpathResults.length);\r\n";
}
