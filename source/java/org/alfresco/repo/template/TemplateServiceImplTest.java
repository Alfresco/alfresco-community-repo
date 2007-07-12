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
package org.alfresco.repo.template;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
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
        
        transactionService = (TransactionService)ctx.getBean("transactionComponent");
        nodeService = (NodeService)ctx.getBean("nodeService");
        templateService = (TemplateService)ctx.getBean("templateService");
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
    
    public void testTemplates()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Object>()
            {
                @SuppressWarnings("unchecked")
                public Object execute() throws Exception
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
