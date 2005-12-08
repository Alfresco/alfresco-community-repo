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
package org.alfresco.repo.template;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Kevin Roast
 */
public class TemplateServiceImplTest extends TestCase
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
    
    public void testTemplates()
    {
        TransactionUtil.executeInUserTransaction(
            transactionService,
            new TransactionUtil.TransactionWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    StoreRef store = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "template_" + System.currentTimeMillis());
                    NodeRef root = nodeService.getRootNode(store);
                    BaseNodeServiceTest.buildNodeGraph(nodeService, root);
                    
                    // check the default template engine exists
                    assertNotNull(templateService.getTemplateProcessor("freemarker"));
                    
                    // create test model
                    Map model = new HashMap(7, 1.0f);
                    
                    model.put("root", new TemplateNode(root, serviceRegistry, null));
                    
                    // execute on test template
                    String output = templateService.processTemplate("freemarker", TEMPLATE_1, model);
                    
                    // check template contains the expected output
                    assertTrue( (output.indexOf(root.getId()) != -1) );
                    
                    System.out.print(output);
                    
                    return null;
                }                
            });
    }
    
    private static final String TEMPLATE_1 = "org/alfresco/repo/template/test_template1.ftl";
}
