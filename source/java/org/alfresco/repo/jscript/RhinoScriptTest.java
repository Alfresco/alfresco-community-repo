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
package org.alfresco.repo.jscript;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.context.ApplicationContext;

/**
 * @author Kevin Roast
 */
public class RhinoScriptTest extends TestCase
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private ContentService contentService;
    private TemplateService templateService;
    private NodeService nodeService;
    private TransactionService transactionService;
    private ServiceRegistry serviceRegistry;
    private AuthenticationComponent authenticationComponent;
    
    /*
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        transactionService = (TransactionService)this.ctx.getBean("transactionComponent");
        contentService = (ContentService)this.ctx.getBean("contentService");
        nodeService = (NodeService)this.ctx.getBean("nodeService");
        templateService = (TemplateService)this.ctx.getBean("templateService");
        serviceRegistry = (ServiceRegistry)this.ctx.getBean("ServiceRegistry");
        
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
    
    public void testRhino()
    {
        TransactionUtil.executeInUserTransaction(
            transactionService,
            new TransactionUtil.TransactionWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    StoreRef store = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "rhino_" + System.currentTimeMillis());
                    NodeRef root = nodeService.getRootNode(store);
                    BaseNodeServiceTest.buildNodeGraph(nodeService, root);
                    
                    // check that rhino script engine is available
                    Context cx = Context.enter();
                    try
                    {
                        // The easiest way to embed Rhino is just to create a new scope this way whenever
                        // you need one. However, initStandardObjects is an expensive method to call and it
                        // allocates a fair amount of memory.
                        Scriptable scope = cx.initStandardObjects();
                        
                        Object wrappedOut = Context.javaToJS(System.out, scope);
                        ScriptableObject.putProperty(scope, "out", wrappedOut);
                        
                        // Now we can evaluate a script. Let's create a new associative array object
                        // using the object literal notation to create the members
                        Object result = cx.evaluateString(scope, "obj = {a:1, b:['x','y']}", "TestJS1", 1, null);
                        
                        Scriptable obj = (Scriptable)scope.get("obj", scope);
                        
                        // Should resolve a non-null value
                        assertNotNull(obj.get("a", obj));
                        // should resolve "obj.a == 1" - JavaScript objects come back as Number
                        assertEquals(new Double(1.0), obj.get("a", obj));
                        
                        // try another script eval
                        result = cx.evaluateString(scope, "function f(x) {return x+1} f(7)", "TestJS2", 1, null);
                        assertEquals(8.0, cx.toNumber(result));
                        
                        // wrap a simple Alfresco NodeRef object
                        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(root);
                        NodeRef ref1 = childAssocs.get(0).getChildRef();
                        Object wrappedNodeRef = Context.javaToJS(ref1, scope);
                        ScriptableObject.putProperty(scope, "rootref", wrappedNodeRef);
                        
                        // evaluate script that touches the wrapped NodeRef
                        result = cx.evaluateString(scope, "obj = rootref.getId()", "TestJS3", 1, null);
                        assertEquals(ref1.getId(), cx.toString(result));
                        
                        // wrap a script Alfresco Node object - the Node object is a wrapper like TemplateNode
                        Node node1 = new Node(root, serviceRegistry, null);
                        Object wrappedNode = Context.javaToJS(node1, scope);
                        ScriptableObject.putProperty(scope, "root", wrappedNode);
                        
                        // evaluate scripts that perform methods on the wrapped Node
                        result = cx.evaluateString(scope, TESTSCRIPT1, "TestJS4", 1, null);
                    }
                    catch (Throwable err)
                    {
                        err.printStackTrace();
                        fail(err.getMessage());
                    }
                    finally
                    {
                        cx.exit();
                    }
                    
                    return null;
                }                
            });
    }
    
    //private static final String TEMPLATE_1 = "org/alfresco/repo/template/test_template1.ftl";
    private static final String TESTSCRIPT1 =
            "var id = root.getId();\r\n" + 
            "out.println(id);\r\n" + 
            "var name = root.getName();\r\n" + 
            "out.println(name);\r\n" + 
            "var type = root.getType();\r\n" + 
            "out.println(type);\r\n" + 
            "var childList = root.getChildren();\r\n" + 
            "out.println(\"zero index node: \" + childList[0].getName());\r\n" +
            "out.println(\"properties: \" + childList[0].getProperties()[\"name\"] );";
}
