/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
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
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.experimental.categories.Category;
import org.alfresco.util.BaseAlfrescoSpringTest;

/**
 * Test class for the {@link ActionFormProcessor}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
@Category(BaseSpringTestsCategory.class)
public class ActionFormProcessorTest extends BaseAlfrescoSpringTest
{
    private RetryingTransactionHelper transactionHelper;
    private NamespaceService namespaceService;
    private Repository repositoryHelper;
    private FormService formService;

    private NodeRef testNode;
    private List<NodeRef> testNodesToBeTidiedUp;


    @Override
    protected String[] getConfigLocations()
    {
        String[] existingConfigLocations = ApplicationContextHelper.CONFIG_LOCATIONS;

        List<String> locations = Arrays.asList(existingConfigLocations);
        List<String> mutableLocationsList = new ArrayList<String>(locations);
        mutableLocationsList.add("classpath:org/alfresco/repo/forms/MNT-7383-context.xml");

        String[] result = mutableLocationsList.toArray(new String[mutableLocationsList.size()]);
        return result;
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();

        this.formService = (FormService)this.applicationContext.getBean("FormService");
        this.namespaceService = (NamespaceService)this.applicationContext.getBean("NamespaceService");
        this.repositoryHelper = (Repository)this.applicationContext.getBean("repositoryHelper");
        this.transactionHelper = (RetryingTransactionHelper)this.applicationContext.getBean("retryingTransactionHelper");

        NodeRef companyHome = repositoryHelper.getCompanyHome();
        testNode = createNode(companyHome,
                "testDoc" + ActionFormProcessorTest.class.getSimpleName() + ".txt",
                ContentModel.TYPE_CONTENT);
        ContentWriter writer = contentService.getWriter(testNode, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("Irrelevant content");

        testNodesToBeTidiedUp = new ArrayList<NodeRef>();
        testNodesToBeTidiedUp.add(testNode);

        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }

    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        for (NodeRef node : testNodesToBeTidiedUp)
        {
            if (nodeService.exists(node)) nodeService.deleteNode(node);
        }
        authenticationService.clearCurrentSecurityContext();
        super.onTearDownInTransaction();
    }

    /**
     * Create a node of the specified content type, under the specified parent node with the specified cm:name.
     */
    protected NodeRef createNode(NodeRef parentNode, String name, QName type)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, name);
        QName docContentQName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, name);
        NodeRef node = this.nodeService.createNode(parentNode,
                    ContentModel.ASSOC_CONTAINS,
                    docContentQName,
                    type,
                    props).getChildRef();
        return node;
    }

    public void testRequestFormForNonExistentAction() throws Exception
    {
        try
        {
            this.formService.getForm(new Item(ActionFormProcessor.ITEM_KIND, "noSuchActionBean"));
            fail("Expected FormNotFoundException");
        }
        catch(FormNotFoundException e)
        {
            //NOOP
        }

    }

    public void testGenerateDefaultFormForParameterlessAction() throws Exception
    {
        this.transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    Form form = formService.getForm(new Item(ActionFormProcessor.ITEM_KIND, "extract-metadata"));
                    
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
    
    public void testGenerateDefaultFormForActionWithNodeRefParam() throws Exception
    {
        this.transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable {
                Form form = formService.getForm(new Item(ActionFormProcessor.ITEM_KIND, "script"));

                // check a form got returned
                assertNotNull("Expecting form to be present", form);

                // get the fields into a Map
                Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();

                assertEquals("Wrong number of fieldDefs", 2, fieldDefs.size());
                Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
                for (FieldDefinition fieldDef : fieldDefs) {
                    fieldDefMap.put(fieldDef.getName(), fieldDef);
                }

                // First of all, we'll check the fields that come from the Action class.
                validateExecuteAsynchronouslyField(fieldDefMap);

                // One defined parameter for this action.
                PropertyFieldDefinition scriptRef = (PropertyFieldDefinition) fieldDefMap.get("script-ref");
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
    
    
    public void testGenerateFormWithSelectedFields() throws Exception
    {
        this.transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    // request a certain set of fields
                    List<String> fields = new ArrayList<String>();
                    fields.add(MoveActionExecuter.PARAM_DESTINATION_FOLDER);
                    fields.add(ActionFormProcessor.EXECUTE_ASYNCHRONOUSLY);
                    
                    Form form = formService.getForm(new Item(ActionFormProcessor.ITEM_KIND, "move"), fields);
                    
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
    
    public void testPersistForm_executeTransformAction() throws Exception
    {
        this.transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable {
                Form form = formService.getForm(new Item(ActionFormProcessor.ITEM_KIND, "transform"));

                // This is the actionedUponNodeRef. A special parameter with no prop_ prefix
                form.addData(AbstractFormProcessor.DESTINATION, testNode.toString());

                // transform the node (which is text/plain to xml in the same folder)
                form.addData(FormFieldConstants.PROP_DATA_PREFIX + TransformActionExecuter.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_XML);
                form.addData(FormFieldConstants.PROP_DATA_PREFIX + TransformActionExecuter.PARAM_DESTINATION_FOLDER, repositoryHelper.getCompanyHome().toString());
                form.addData(FormFieldConstants.PROP_DATA_PREFIX + TransformActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CONTAINS.toPrefixString(namespaceService));
                form.addData(FormFieldConstants.PROP_DATA_PREFIX + TransformActionExecuter.PARAM_ASSOC_QNAME, ContentModel.ASSOC_CONTAINS.toPrefixString(namespaceService));

                formService.saveForm(form.getItem(), form.getFormData());

                for (ChildAssociationRef chAssRef : nodeService.getChildAssocs(repositoryHelper.getCompanyHome())) {
                    System.err.println(nodeService.getProperty(chAssRef.getChildRef(), ContentModel.PROP_NAME));
                }

                Serializable cmName = nodeService.getProperty(testNode, ContentModel.PROP_NAME);
                String transformedNodeName = ((String) cmName).replace(".txt", ".xml");

                NodeRef expectedTransformedNode = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, transformedNodeName);
                assertNotNull("transformed node was missing", expectedTransformedNode);

                testNodesToBeTidiedUp.add(expectedTransformedNode);

                return null;
            }
        });
    }

    public void testMNT7383() throws Exception
    {
        this.transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                Form form = formService.getForm(new Item(ActionFormProcessor.ITEM_KIND, "actionFormProcessorTestActionExecuter"));

                assertNotNull("Expecting form to be present", form);

                form.addData(FormFieldConstants.PROP_DATA_PREFIX + "check", Boolean.TRUE);
                form.addData(FormFieldConstants.PROP_DATA_PREFIX + "date", new Date());
                ArrayList<QName> qnameList = new ArrayList<QName>();
                qnameList.add(ContentModel.TYPE_PERSON);
                qnameList.add(ContentModel.TYPE_FOLDER);
                form.addData(FormFieldConstants.PROP_DATA_PREFIX + "qname", qnameList);
                ArrayList<NodeRef> nodeRefList = new ArrayList<NodeRef>();
                nodeRefList.add(repositoryHelper.getCompanyHome());
                nodeRefList.add(repositoryHelper.getRootHome());
                form.addData(FormFieldConstants.PROP_DATA_PREFIX + "nodeRefs", nodeRefList);

                formService.saveForm(form.getItem(), form.getFormData());

                return null;
            }
        });
    }


    public static class ActionFormProcessorTestActionExecuter extends ActionExecuterAbstractBase
    {
        public static final String NAME = "actionFormProcessorTestActionExecuter";

        @Override protected void addParameterDefinitions(List<ParameterDefinition> paramList)
        {
            paramList.add(new ParameterDefinitionImpl("check", DataTypeDefinition.BOOLEAN, false, "Check"));
            paramList.add(new ParameterDefinitionImpl("date", DataTypeDefinition.DATE, false, "Date"));
            paramList.add(new ParameterDefinitionImpl("qname", DataTypeDefinition.QNAME, false, "QName", true));
            paramList.add(new ParameterDefinitionImpl("nodeRefs", DataTypeDefinition.NODE_REF, false, "NodeRefs", true));
        }

        @SuppressWarnings("unchecked")
		@Override protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
        {
            Object checkValue = action.getParameterValue("check");
            assertEquals("Parameter value should be Boolean", checkValue.getClass(), Boolean.class);

            Object dateValue = action.getParameterValue("date");
            assertEquals("Parameter value should be Date", dateValue.getClass(), Date.class);

            Object qnameValue = action.getParameterValue("qname");
            assertEquals("Parameter value should be ArrayList", qnameValue.getClass(), ArrayList.class);
            for (QName qname : (ArrayList<QName>)qnameValue)
            {
                assertEquals("The item value should be QName", qname.getClass(), QName.class);
            }

            Object nodeRefsValue = action.getParameterValue("nodeRefs");
            assertEquals("Parameter value should be ArrayList", nodeRefsValue.getClass(), ArrayList.class);
            for (NodeRef nodeRef : (ArrayList<NodeRef>)nodeRefsValue)
            {
                assertEquals("The item value should be NodeRef", nodeRef.getClass(), NodeRef.class);
            }
        }
    }

}
