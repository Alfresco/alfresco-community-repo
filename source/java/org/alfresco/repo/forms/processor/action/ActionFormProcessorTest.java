/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.FormService;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.repo.forms.processor.AbstractFormProcessor;
import org.alfresco.repo.forms.processor.node.FormFieldConstants;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test class for the {@link ActionFormProcessor}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class ActionFormProcessorTest
{
    private static final ApplicationContext testContext = ApplicationContextHelper.getApplicationContext();
    
    // injected services
    private static ContentService CONTENT_SERVICE;
    private static FormService FORM_SERVICE;
    private static NamespaceService NAMESPACE_SERVICE;
    private static NodeService NODE_SERVICE;
    private static Repository REPOSITORY_HELPER;
    private static RetryingTransactionHelper TRANSACTION_HELPER;
    
    private NodeRef testNode;
    private List<NodeRef> testNodesToBeTidiedUp;
    
    @BeforeClass public static void initTestsContext() throws Exception
    {
        CONTENT_SERVICE = (ContentService)testContext.getBean("ContentService");
        FORM_SERVICE = (FormService)testContext.getBean("FormService");
        NAMESPACE_SERVICE = (NamespaceService)testContext.getBean("NamespaceService");
        NODE_SERVICE = (NodeService)testContext.getBean("NodeService");
        REPOSITORY_HELPER = (Repository)testContext.getBean("repositoryHelper");
        TRANSACTION_HELPER = (RetryingTransactionHelper)testContext.getBean("retryingTransactionHelper");
        
        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }
    
    /**
     * Create some content that can have actions run on it.
     */
    @Before public void createTestObjects() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Create some content which we will run actions on.
                NodeRef companyHome = REPOSITORY_HELPER.getCompanyHome();
                testNode = createNode(companyHome,
                                      "testDoc" + ActionFormProcessorTest.class.getSimpleName() + ".txt",
                                      ContentModel.TYPE_CONTENT);
                ContentWriter writer = CONTENT_SERVICE.getWriter(testNode, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.setEncoding("UTF-8");
                writer.putContent("Irrelevant content");
                
                return null;
            }
        });
        
        testNodesToBeTidiedUp = new ArrayList<NodeRef>();
        testNodesToBeTidiedUp.add(testNode);
    }
    
    
    /**
     * Create a node of the specified content type, under the specified parent node with the specified cm:name.
     */
    private NodeRef createNode(NodeRef parentNode, String name, QName type)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, name);
        QName docContentQName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, name);
        NodeRef node = NODE_SERVICE.createNode(parentNode,
                    ContentModel.ASSOC_CONTAINS,
                    docContentQName,
                    type,
                    props).getChildRef();
        return node;
    }
    
    /**
     * This method deletes any nodes which were created during test execution.
     */
    @After public void tidyUpTestNodes() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                    
                    for (NodeRef node : testNodesToBeTidiedUp)
                    {
                        if (NODE_SERVICE.exists(node)) NODE_SERVICE.deleteNode(node);
                    }
                    
                    return null;
                }
            });
    }
    
    @Test(expected=FormNotFoundException.class) public void requestFormForNonExistentAction() throws Exception
    {
        FORM_SERVICE.getForm(new Item(ActionFormProcessor.ITEM_KIND, "noSuchActionBean"));
    }
    
    
    @Test public void generateDefaultFormForParameterlessAction() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    Form form = FORM_SERVICE.getForm(new Item(ActionFormProcessor.ITEM_KIND, "extract-metadata"));
                    
                    // check a form got returned
                    assertNotNull("Expecting form to be present", form);
                    
                    // get the fields into a Map
                    Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
                    
                    assertEquals("Wrong number of fieldDefs", 1, fieldDefs.size());
                    
                    Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
                    for (FieldDefinition fieldDef : fieldDefs)
                    {
                        fieldDefMap.put(fieldDef.getName(), fieldDef);
                    }
                    
                    validateExecuteAsynchronouslyField(fieldDefMap);
                    
                    return null;
                }
            });
    }
    
    @Test public void generateDefaultFormForActionWithNodeRefParam() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    Form form = FORM_SERVICE.getForm(new Item(ActionFormProcessor.ITEM_KIND, "script"));
                    
                    // check a form got returned
                    assertNotNull("Expecting form to be present", form);
                    
                    // get the fields into a Map
                    Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
                    
                    assertEquals("Wrong number of fieldDefs", 2, fieldDefs.size());
                    Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
                    for (FieldDefinition fieldDef : fieldDefs)
                    {
                        fieldDefMap.put(fieldDef.getName(), fieldDef);
                    }
                    
                    // First of all, we'll check the fields that come from the Action class.
                    validateExecuteAsynchronouslyField(fieldDefMap);
                    
                    // One defined parameter for this action.
                    PropertyFieldDefinition scriptRef = (PropertyFieldDefinition)fieldDefMap.get("script-ref");
                    assertNotNull("'script-ref' field defn was missing.", scriptRef);
                    assertEquals("script-ref", scriptRef.getName());
                    assertEquals("Script", scriptRef.getLabel());
                    assertEquals("script-ref", scriptRef.getDescription());
                    assertEquals("text", scriptRef.getDataType());
                    assertTrue(scriptRef.isMandatory());
                    List<FieldConstraint> constraints = scriptRef.getConstraints();
                    assertEquals(1, constraints.size());
                    assertEquals("LIST", constraints.get(0).getType());
                    
                    return null;
                }
            });
    }

    private void validateExecuteAsynchronouslyField(Map<String, FieldDefinition> fieldDefMap)
    {
        // executeAsynchronously
        PropertyFieldDefinition execAsync = (PropertyFieldDefinition)fieldDefMap.get("executeAsynchronously");
        assertNotNull("'executeAsynchronously' field defn was missing.", execAsync);
        assertEquals("'executeAsynchronously' name was wrong", "executeAsynchronously", execAsync.getName());
        assertEquals("'executeAsynchronously' label was wrong", "executeAsynchronously", execAsync.getLabel());
        assertNull("'executeAsynchronously' description was wrong", execAsync.getDescription());
        assertEquals("'executeAsynchronously' datatype was wrong", "boolean", execAsync.getDataType());
    }
    
    
    @Test public void generateFormWithSelectedFields() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    // request a certain set of fields
                    List<String> fields = new ArrayList<String>();
                    fields.add(MoveActionExecuter.PARAM_DESTINATION_FOLDER);
                    fields.add(ActionFormProcessor.EXECUTE_ASYNCHRONOUSLY);
                    
                    Form form = FORM_SERVICE.getForm(new Item(ActionFormProcessor.ITEM_KIND, "move"), fields);
                    
                    // check a form got returned
                    assertNotNull("Expecting form to be present", form);
                    
                    // get the fields into a Map
                    Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
                    Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
                    for (FieldDefinition fieldDef : fieldDefs)
                    {
                        fieldDefMap.put(fieldDef.getName(), fieldDef);
                    }
                    
                    // check there are 2 field
                    assertEquals(2, fieldDefMap.size());
                    
                    // check the 2 fields are the correct ones!
                    AssociationFieldDefinition destFolderField = (AssociationFieldDefinition)fieldDefMap.get(MoveActionExecuter.PARAM_DESTINATION_FOLDER);
                    assertNotNull(destFolderField);
                    PropertyFieldDefinition execAsyncField = (PropertyFieldDefinition)fieldDefMap.get(ActionFormProcessor.EXECUTE_ASYNCHRONOUSLY);
                    assertNotNull(execAsyncField);
                    
                    return null;
                }
            });
    }
    
    @Test public void persistForm_executeTransformAction() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    Form form = FORM_SERVICE.getForm(new Item(ActionFormProcessor.ITEM_KIND, "transform"));
                    
                    // This is the actionedUponNodeRef. A special parameter with no prop_ prefix
                    form.addData(AbstractFormProcessor.DESTINATION, testNode.toString());
                    
                    // transform the node (which is text/plain to pdf in the same folder)
                    form.addData(FormFieldConstants.PROP_DATA_PREFIX + TransformActionExecuter.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_PDF);
                    form.addData(FormFieldConstants.PROP_DATA_PREFIX + TransformActionExecuter.PARAM_DESTINATION_FOLDER, REPOSITORY_HELPER.getCompanyHome().toString());
                    form.addData(FormFieldConstants.PROP_DATA_PREFIX + TransformActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CONTAINS.toPrefixString(NAMESPACE_SERVICE));
                    form.addData(FormFieldConstants.PROP_DATA_PREFIX + TransformActionExecuter.PARAM_ASSOC_QNAME, ContentModel.ASSOC_CONTAINS.toPrefixString(NAMESPACE_SERVICE));
                    
                    FORM_SERVICE.saveForm(form.getItem(), form.getFormData());
                    
                    for (ChildAssociationRef chAssRef : NODE_SERVICE.getChildAssocs(REPOSITORY_HELPER.getCompanyHome()))
                    {
                        System.err.println(NODE_SERVICE.getProperty(chAssRef.getChildRef(), ContentModel.PROP_NAME));
                    }
                    
                    Serializable cmName = NODE_SERVICE.getProperty(testNode, ContentModel.PROP_NAME);
                    String transformedNodeName = ((String)cmName).replace(".txt", ".pdf");
                    
                    NodeRef expectedTransformedNode = NODE_SERVICE.getChildByName(REPOSITORY_HELPER.getCompanyHome(), ContentModel.ASSOC_CONTAINS, transformedNodeName);
                    assertNotNull("transformed node was missing", expectedTransformedNode);
                    
                    testNodesToBeTidiedUp.add(expectedTransformedNode);
                    
                    return null;
                }
            });
    }
}
