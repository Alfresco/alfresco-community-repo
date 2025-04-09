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
package org.alfresco.repo.template;

import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.model.ContentModel;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;

/**
 * @author Kevin Roast
 */
public class TemplateServiceImplTest extends BaseSpringTest
{
    private static final String TEMPLATE_1 = "org/alfresco/repo/template/test_template1.ftl";
    private NodeRef root_node;

    private TemplateService templateService;
    private NodeService nodeService;
    private TransactionService transactionService;
    private ServiceRegistry serviceRegistry;
    private AuthenticationComponent authenticationComponent;

    @Before
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        templateService = (TemplateService) applicationContext.getBean("templateService");
        serviceRegistry = (ServiceRegistry) applicationContext.getBean("ServiceRegistry");

        this.authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        this.authenticationComponent.setSystemUserAsCurrentUser();

        DictionaryDAO dictionaryDao = (DictionaryDAO) applicationContext.getBean("dictionaryDAO");

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
        BaseNodeServiceTest.loadModel(applicationContext);

        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Object>() {
                    @SuppressWarnings("unchecked")
                    public Object execute() throws Exception
                    {
                        StoreRef store = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "template_" + System.currentTimeMillis());
                        root_node = nodeService.getRootNode(store);
                        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(11);
                        properties.put(ContentModel.PROP_NAME, (Serializable) "subFolder");
                        NodeRef subFolderRef = nodeService.createNode(
                                root_node,
                                ContentModel.ASSOC_CHILDREN,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName("subFolder")),
                                ContentModel.TYPE_FOLDER,
                                properties).getChildRef();
                        properties.put(ContentModel.PROP_NAME, (Serializable) "subSubFolder");
                        NodeRef subSubFolderRef = nodeService.createNode(
                                subFolderRef,
                                ContentModel.ASSOC_CONTAINS,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName("subSubFolder")),
                                ContentModel.TYPE_FOLDER,
                                properties).getChildRef();
                        properties.put(ContentModel.PROP_NAME, (Serializable) "subSubSubFolder");
                        nodeService.createNode(
                                subSubFolderRef,
                                ContentModel.ASSOC_CONTAINS,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName("subSubSubFolder")),
                                ContentModel.TYPE_FOLDER,
                                properties);
                        BaseNodeServiceTest.buildNodeGraph(nodeService, root_node);
                        return null;
                    }
                });
    }

    @After
    public void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.tearDown();
    }

    @Test
    public void testTemplates()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Object>() {
                    @SuppressWarnings("unchecked")
                    public Object execute() throws Exception
                    {

                        // check the default template engine exists
                        assertNotNull(templateService.getTemplateProcessor("freemarker"));
                        Map model = createTemplateModel(root_node);

                        // execute on test template
                        String output = templateService.processTemplate("freemarker", TEMPLATE_1, model);

                        // check template contains the expected output
                        assertTrue("Cannot find root-node-id", (output.indexOf(root_node.getId()) != -1));
                        assertTrue("Cannot resolve subFolder properly", (output.indexOf("root.childByNamePath[\"subFolder\"].name=subFolder") != -1));
                        assertTrue("Cannot resolve subSubFolder properly", (output.indexOf("root.childByNamePath[\"subFolder/subSubFolder\"].name=subSubFolder") != -1));
                        assertTrue("Cannot resolve subSubSubFolder properly", (output.indexOf("root.childByNamePath[\"subFolder/subSubFolder/subSubSubFolder\"].name=subSubSubFolder") != -1));
                        assertTrue("Cannot resolve subSubSubFolder with enhancement properly", (output.indexOf("root.childByNamePath[\"subFolder\"].childByNamePath[\"subSubFolder/subSubSubFolder\"].name=subSubSubFolder") != -1));

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

    @Test
    public void testGetTemplateProcessor()
    {
        assertNotNull(templateService.getTemplateProcessor(null));
    }

    @Test
    public void testProcessTemplate()
    {
        Map model = createTemplateModel(root_node);
        StringWriter writer = new StringWriter();
        templateService.processTemplate(TEMPLATE_1, model, writer);
        assertTrue((writer.toString().indexOf(root_node.getId()) != -1));

        try
        {
            templateService.processTemplate("NOT_REAL", TEMPLATE_1, model, new StringWriter());
            fail("The engine name is nonsense");
        }
        catch (TemplateException expected)
        {
            //
        }

        try
        {
            templateService.processTemplate("NOT_REAL", TEMPLATE_1, model, I18NUtil.getLocale());
            fail("The engine name is nonsense");
        }
        catch (TemplateException expected)
        {
            //
        }

        try
        {
            templateService.processTemplateString("NOT_REAL", TEMPLATE_1, model, new StringWriter());
            fail("The engine name is nonsense");
        }
        catch (TemplateException expected)
        {
            //
        }
    }

}
