/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.template;

import java.io.InputStream;
import java.io.StringWriter;
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
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Kevin Roast
 */
@Category(OwnJVMTestsCategory.class)
public class TemplateServiceImplTest extends TestCase
{
    private static final String TEMPLATE_1 = "org/alfresco/repo/template/test_template1.ftl";
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private NodeRef root_node;

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

        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @SuppressWarnings("unchecked")
                    public Object execute() throws Exception
                    {
                        StoreRef store = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "template_" + System.currentTimeMillis());
                        root_node = nodeService.getRootNode(store);
                        BaseNodeServiceTest.buildNodeGraph(nodeService, root_node);
                        return null;
                    }
                });
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

                    // check the default template engine exists
                    assertNotNull(templateService.getTemplateProcessor("freemarker"));
                    Map model = createTemplateModel(root_node);

                    // execute on test template
                    String output = templateService.processTemplate("freemarker", TEMPLATE_1, model);
                    
                    // check template contains the expected output
                    assertTrue( (output.indexOf(root_node.getId()) != -1) );

                    return null;
                }                
            });
    }

    private Map createTemplateModel(NodeRef root)
    {
        // create test model
        Map model = new HashMap(7, 1.0f);
        model.put("root", new TemplateNode(root, serviceRegistry, null));
        return model;
    }

    public void testGetTemplateProcessor()
    {
        assertNotNull(templateService.getTemplateProcessor(null));
    }

    public void testProcessTemplate()
    {
        Map model = createTemplateModel(root_node);
        StringWriter writer = new StringWriter();
        templateService.processTemplate(TEMPLATE_1, model, writer);
        assertTrue( (writer.toString().indexOf(root_node.getId()) != -1) );

        try
        {
            templateService.processTemplate("NOT_REAL", TEMPLATE_1, model, new StringWriter());
            fail("The engine name is nonsense");
        } catch (TemplateException expected)
        {
            //
        }

        try
        {
            templateService.processTemplate("NOT_REAL", TEMPLATE_1, model, I18NUtil.getLocale());
            fail("The engine name is nonsense");
        } catch (TemplateException expected)
        {
            //
        }

        try
        {
            templateService.processTemplateString("NOT_REAL", TEMPLATE_1, model, new StringWriter());
            fail("The engine name is nonsense");
        } catch (TemplateException expected)
        {
            //
        }
    }
    
}
