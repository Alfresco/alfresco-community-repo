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
package org.alfresco.repo.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.forms.AssociationFieldDefinition.Direction;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.repo.forms.processor.AbstractFormProcessor;
import org.alfresco.repo.forms.processor.node.FormFieldConstants;
import org.alfresco.repo.forms.processor.workflow.TransitionFieldProcessor;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.springframework.util.StringUtils;

/**
 * Form service implementation unit test.
 * 
 * @author Gavin Cornwell
 * @author Nick Smith
 */
public class FormServiceImplTest extends BaseAlfrescoSpringTest 
{
    private FormService formService;
    private NamespaceService namespaceService;
    private ScriptService scriptService;
    private ContentService contentService;
    private WorkflowService workflowService;
    private TestPersonManager personManager;
    
    private NodeRef document;
    private NodeRef associatedDoc;
    private NodeRef childDoc;
    private NodeRef folder;
    private String documentName;
    private String folderName;
    
    private static String VALUE_TITLE = "This is the title for the test document";
    private static String VALUE_DESCRIPTION = "This is the description for the test document";
    private static String VALUE_ORIGINATOR = "fred@customer.com";
    private static String VALUE_ADDRESSEE = "bill@example.com";
    private static String VALUE_ADDRESSEES1 = "harry@example.com";
    private static String VALUE_ADDRESSEES2 = "jane@example.com";
    private static String VALUE_ADDRESSEES3 = "alice@example.com";
    private static String VALUE_SUBJECT = "The subject is...";
    private static String VALUE_MIMETYPE = MimetypeMap.MIMETYPE_TEXT_PLAIN;
    private static String VALUE_ENCODING = "UTF-8";
    private static String VALUE_CONTENT = "This is the content for the test document";
    private static String VALUE_ASSOC_CONTENT = "This is the content for the associated document";
    private static Date VALUE_SENT_DATE = new Date();
    
    private static String LABEL_NAME = "Name";
    private static String LABEL_TITLE = "Title";
    private static String LABEL_DESCRIPTION = "Description";
    private static String LABEL_AUTHOR = "Author";
    private static String LABEL_MODIFIED = "Modified Date";
    private static String LABEL_MODIFIER = "Modifier";
    private static String LABEL_MIMETYPE = "Mimetype";
    private static String LABEL_ENCODING = "Encoding";
    private static String LABEL_SIZE = "Size";
    private static String LABEL_ORIGINATOR = "Originator";
    private static String LABEL_ADDRESSEE = "Addressee";
    private static String LABEL_ADDRESSEES = "Addressees";
    private static String LABEL_SUBJECT = "Subject";
    private static String LABEL_SENT_DATE = "Sent Date";
    private static String LABEL_REFERENCES = "References";
    private static String LABEL_CONTAINS = "Contains";
    
    private static final String USER_ONE = "UserOne_FormServiceImplTest";
    private static final String USER_TWO = "UserTwo_FormServiceImplTest";
    private static final String NODE_FORM_ITEM_KIND = "node";
    private static final String TYPE_FORM_ITEM_KIND = "type";
    private static final String WORKFLOW_FORM_ITEM_KIND = "workflow";
    private static final String TASK_FORM_ITEM_KIND = "task";
    
    /**
     * Called during the transaction setup
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the required services
        this.formService = (FormService)this.applicationContext.getBean("FormService");
        this.namespaceService = (NamespaceService)this.applicationContext.getBean("NamespaceService");
        this.scriptService = (ScriptService)this.applicationContext.getBean("ScriptService");
        PersonService personService = (PersonService)this.applicationContext.getBean("PersonService");
        this.contentService = (ContentService)this.applicationContext.getBean("ContentService");
        this.workflowService = (WorkflowService)this.applicationContext.getBean("WorkflowService");
        
        MutableAuthenticationService mutableAuthenticationService = (MutableAuthenticationService)applicationContext.getBean("AuthenticationService");
        this.personManager = new TestPersonManager(mutableAuthenticationService, personService, nodeService);

        // create users
        personManager.createPerson(USER_ONE);
        personManager.createPerson(USER_TWO);
        
        // Do the tests as userOne
        personManager.setUser(USER_ONE);
        
        String guid = GUID.generate();
        
        NodeRef rootNode = this.nodeService.getRootNode(
                    new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        this.folderName = "testFolder" + guid;
        folderProps.put(ContentModel.PROP_NAME, this.folderName);
        this.folder = this.nodeService.createNode(
                rootNode, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder" + guid),
                ContentModel.TYPE_FOLDER,
                folderProps).getChildRef();
        
        // Create a node
        Map<QName, Serializable> docProps = new HashMap<QName, Serializable>(1);
        this.documentName = "testDocument" + guid + ".txt";
        docProps.put(ContentModel.PROP_NAME, this.documentName);
        this.document = this.nodeService.createNode(
                this.folder, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testDocument" + guid + ".txt"), 
                ContentModel.TYPE_CONTENT,
                docProps).getChildRef();    
       
        // create a node to use as target of association
        docProps.put(ContentModel.PROP_NAME, "associatedDocument" + guid + ".txt");
        this.associatedDoc = this.nodeService.createNode(
                    this.folder, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "associatedDocument" + guid + ".txt"), 
                    ContentModel.TYPE_CONTENT,
                    docProps).getChildRef();
        
        // create a node to use as a 2nd child node of the folder
        docProps.put(ContentModel.PROP_NAME, "childDocument" + guid + ".txt");
        this.childDoc = this.nodeService.createNode(
                    this.folder, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "childDocument" + guid + ".txt"), 
                    ContentModel.TYPE_CONTENT,
                    docProps).getChildRef();
        
        // add some content to the nodes
        ContentWriter writer = this.contentService.getWriter(this.document, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(VALUE_MIMETYPE);
        writer.setEncoding(VALUE_ENCODING);
        writer.putContent(VALUE_CONTENT);
        
        ContentWriter writer2 = this.contentService.getWriter(this.associatedDoc, ContentModel.PROP_CONTENT, true);
        writer2.setMimetype(VALUE_MIMETYPE);
        writer2.setEncoding(VALUE_ENCODING);
        writer2.putContent(VALUE_ASSOC_CONTENT);
        
        // add standard titled aspect
        Map<QName, Serializable> aspectProps = new HashMap<QName, Serializable>(2);
        aspectProps.put(ContentModel.PROP_TITLE, VALUE_TITLE);
        aspectProps.put(ContentModel.PROP_DESCRIPTION, VALUE_DESCRIPTION);
        this.nodeService.addAspect(this.document, ContentModel.ASPECT_TITLED, aspectProps);
        
        // add emailed aspect (has multiple value field)
        aspectProps = new HashMap<QName, Serializable>(5);
        aspectProps.put(ContentModel.PROP_ORIGINATOR, VALUE_ORIGINATOR);
        aspectProps.put(ContentModel.PROP_ADDRESSEE, VALUE_ADDRESSEE);
        List<String> addressees = new ArrayList<String>(2);
        addressees.add(VALUE_ADDRESSEES1);
        addressees.add(VALUE_ADDRESSEES2);
        aspectProps.put(ContentModel.PROP_ADDRESSEES, (Serializable)addressees);
        aspectProps.put(ContentModel.PROP_SUBJECT, VALUE_SUBJECT);
        aspectProps.put(ContentModel.PROP_SENTDATE, VALUE_SENT_DATE);
        this.nodeService.addAspect(this.document, ContentModel.ASPECT_EMAILED, aspectProps);
        
        // add referencing aspect (has association)
        aspectProps.clear();
        this.nodeService.addAspect(document, ContentModel.ASPECT_REFERENCING, aspectProps);
        this.nodeService.createAssociation(this.document, this.associatedDoc, ContentModel.ASSOC_REFERENCES);
    }
    
    @SuppressWarnings("unchecked")
	public void testGetAllDocForm() throws Exception
    {
        Form form = this.formService.getForm(new Item(NODE_FORM_ITEM_KIND, this.document.toString()));
        
        // check a form got returned
        assertNotNull("Expecting form to be present", form);
        
        // check item identifier matches
        assertEquals(NODE_FORM_ITEM_KIND, form.getItem().getKind());
        assertEquals(this.document.toString(), form.getItem().getId());
        
        // check the type is correct
        assertEquals(ContentModel.TYPE_CONTENT.toPrefixString(this.namespaceService), 
                    form.getItem().getType());
        
        // check there is no group info
        assertNull("Expecting the form groups to be null!", form.getFieldGroups());
        
        // check the field definitions
        Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertNotNull("Expecting to find fields", fieldDefs);
        assertEquals("Wrong number of fields", 19, fieldDefs.size());
        
        // create a Map of the field definitions
        // NOTE: we can safely do this as we know there are no duplicate field names and we're not
        //       concerned with ordering!
        Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
        for (FieldDefinition fieldDef : fieldDefs)
        {
            fieldDefMap.put(fieldDef.getName(), fieldDef);
        }
        
        // find the fields
        PropertyFieldDefinition nameField = (PropertyFieldDefinition)fieldDefMap.get("cm:name");
        PropertyFieldDefinition titleField = (PropertyFieldDefinition)fieldDefMap.get("cm:title");
        PropertyFieldDefinition descField = (PropertyFieldDefinition)fieldDefMap.get("cm:description");
        PropertyFieldDefinition mimetypeField = (PropertyFieldDefinition)fieldDefMap.get("mimetype");
        PropertyFieldDefinition encodingField = (PropertyFieldDefinition)fieldDefMap.get("encoding");
        PropertyFieldDefinition sizeField = (PropertyFieldDefinition)fieldDefMap.get("size");
        PropertyFieldDefinition originatorField = (PropertyFieldDefinition)fieldDefMap.get("cm:originator");
        PropertyFieldDefinition addresseeField = (PropertyFieldDefinition)fieldDefMap.get("cm:addressee");
        PropertyFieldDefinition addresseesField = (PropertyFieldDefinition)fieldDefMap.get("cm:addressees");
        PropertyFieldDefinition subjectField = (PropertyFieldDefinition)fieldDefMap.get("cm:subjectline");
        PropertyFieldDefinition sentDateField = (PropertyFieldDefinition)fieldDefMap.get("cm:sentdate");
        AssociationFieldDefinition referencesField = (AssociationFieldDefinition)fieldDefMap.get("cm:references");
        
        // check fields are present
        assertNotNull("Expecting to find the cm:name field", nameField);
        assertNotNull("Expecting to find the cm:title field", titleField);
        assertNotNull("Expecting to find the cm:description field", descField);
        assertNotNull("Expecting to find the mimetype field", mimetypeField);
        assertNotNull("Expecting to find the encoding field", encodingField);
        assertNotNull("Expecting to find the size field", sizeField);
        assertNotNull("Expecting to find the cm:originator field", originatorField);
        assertNotNull("Expecting to find the cm:addressee field", addresseeField);
        assertNotNull("Expecting to find the cm:addressees field", addresseesField);
        assertNotNull("Expecting to find the cm:subjectline field", subjectField);
        assertNotNull("Expecting to find the cm:sentdate field", sentDateField);
        assertNotNull("Expecting to find the cm:references field", referencesField);
        
        // check the system properties are not present by default
        assertFalse(fieldDefMap.containsKey("sys:node-dbid"));
        assertFalse(fieldDefMap.containsKey("sys:store-identifier"));
        assertFalse(fieldDefMap.containsKey("sys:node-uuid"));
        assertFalse(fieldDefMap.containsKey("sys:store-protocol"));
        
        // check the labels of all the fields
        assertEquals("Expecting cm:name label to be " + LABEL_NAME, 
                    LABEL_NAME, nameField.getLabel());
        assertEquals("Expecting cm:title label to be " + LABEL_TITLE, 
                    LABEL_TITLE, titleField.getLabel());
        assertEquals("Expecting cm:description label to be " + LABEL_DESCRIPTION, 
                    LABEL_DESCRIPTION, descField.getLabel());
        assertEquals("Expecting mimetype label to be " + LABEL_MIMETYPE, 
                    LABEL_MIMETYPE, mimetypeField.getLabel());
        assertEquals("Expecting encoding label to be " + LABEL_ENCODING, 
                    LABEL_ENCODING, encodingField.getLabel());
        assertEquals("Expecting size label to be " + LABEL_SIZE, 
                    LABEL_SIZE, sizeField.getLabel());
        assertEquals("Expecting cm:originator label to be " + LABEL_ORIGINATOR, 
                    LABEL_ORIGINATOR, originatorField.getLabel());
        assertEquals("Expecting cm:addressee label to be " + LABEL_ADDRESSEE, 
                    LABEL_ADDRESSEE, addresseeField.getLabel());
        assertEquals("Expecting cm:addressees label to be " + LABEL_ADDRESSEES, 
                    LABEL_ADDRESSEES, addresseesField.getLabel());
        assertEquals("Expecting cm:subjectline label to be " + LABEL_SUBJECT, 
                    LABEL_SUBJECT, subjectField.getLabel());
        assertEquals("Expecting cm:sentdate label to be " + LABEL_SENT_DATE, 
                    LABEL_SENT_DATE, sentDateField.getLabel());
        assertEquals("Expecting cm:references label to be " + LABEL_REFERENCES, 
                    LABEL_REFERENCES, referencesField.getLabel());
        
        // check details of name field
        assertEquals("Expecting cm:name type to be text", "text", nameField.getDataType());
        assertTrue("Expecting cm:name to be mandatory", nameField.isMandatory());
        assertFalse("Expecting cm:name to be single valued", nameField.isRepeating());
        
        // get the constraint for the name field and check
        List<FieldConstraint> constraints = nameField.getConstraints();
        assertEquals("Expecting 1 constraint for cm:name", 1, constraints.size());
        FieldConstraint constraint = constraints.get(0);
        assertEquals("Expecting name of constraint to be 'REGEX'", "REGEX", constraint.getType());
        Map<String, Object> params = constraint.getParameters();
        assertNotNull("Expecting constraint parameters", params);
        assertEquals("Expecting 2 constraint parameters", 2, params.size());
        assertNotNull("Expecting an 'expression' constraint parameter", params.get("expression"));
        assertNotNull("Expecting an 'requiresMatch' constraint parameter", params.get("requiresMatch"));
        
        // check details of the addressees field
        assertEquals("Expecting cm:addressees type to be text", "text", addresseesField.getDataType());
        assertFalse("Expecting cm:addressees to be mandatory", addresseesField.isMandatory());
        assertTrue("Expecting cm:addressees to be multi valued", addresseesField.isRepeating());
        assertNull("Expecting constraints for cm:addressees to be null", addresseesField.getConstraints());
        
        // check the details of the association field
        assertEquals("Expecting cm:references endpoint type to be cm:content", "cm:content", 
                    referencesField.getEndpointType());
        assertEquals("Expecting cm:references endpoint direction to be TARGET", 
                    Direction.TARGET.toString(),
                    referencesField.getEndpointDirection().toString());
        assertFalse("Expecting cm:references endpoint to be optional", 
                    referencesField.isEndpointMandatory());
        assertTrue("Expecting cm:references endpoint to be 1 to many",
                    referencesField.isEndpointMany());
        
        // check the form data
        FormData data = form.getFormData();
        assertNotNull("Expecting form data", data);
        assertEquals(VALUE_TITLE, data.getFieldData(titleField.getDataKeyName()).getValue());
        assertEquals(VALUE_DESCRIPTION, data.getFieldData(descField.getDataKeyName()).getValue());
        assertEquals(VALUE_MIMETYPE, data.getFieldData(mimetypeField.getDataKeyName()).getValue());
        assertEquals(VALUE_ENCODING, data.getFieldData(encodingField.getDataKeyName()).getValue());
        assertEquals(VALUE_ORIGINATOR, data.getFieldData(originatorField.getDataKeyName()).getValue());
        assertEquals(VALUE_ADDRESSEE, data.getFieldData(addresseeField.getDataKeyName()).getValue());
        assertEquals(VALUE_SUBJECT, data.getFieldData(subjectField.getDataKeyName()).getValue());
        assertTrue("Expecting size to be > 0", ((Long)data.getFieldData(sizeField.getDataKeyName()).getValue()).longValue() > 0);
        
        String addressees = (String)data.getFieldData(addresseesField.getDataKeyName()).getValue();
        assertNotNull(addressees);
        assertTrue("Expecting the addressees value to have at least 1 comma", addressees.indexOf(",") != -1);
        String[] addresseesArr = StringUtils.delimitedListToStringArray(addressees, ",");
        assertEquals("Expecting 2 addressees", 2, addresseesArr.length);
        assertEquals(VALUE_ADDRESSEES1, addresseesArr[0]);
        assertEquals(VALUE_ADDRESSEES2, addresseesArr[1]);
        
        Calendar calTestValue = Calendar.getInstance();
        calTestValue.setTime(VALUE_SENT_DATE);
        Calendar calServiceValue = Calendar.getInstance();
        calServiceValue.setTime((Date)data.getFieldData(sentDateField.getDataKeyName()).getValue());
        assertEquals(calTestValue.getTimeInMillis(), calServiceValue.getTimeInMillis());
        
        List<String> targets = (List<String>)data.getFieldData(referencesField.getDataKeyName()).getValue();
        assertEquals("Expecting 1 target", 1, targets.size());
        assertEquals(this.associatedDoc.toString(), targets.get(0));
    }
    
    @SuppressWarnings("unchecked")
    public void testGetSelectedFieldsDocForm() throws Exception
    {
        // define a list of fields to retrieve from the node
        List<String> fields = new ArrayList<String>(8);
        fields.add("cm:name");
        fields.add("cm:title");
        fields.add("mimetype");
        fields.add("cm:modified");
        fields.add("cm:modifier");
        fields.add("cm:subjectline");
        fields.add("cm:sentdate");
        fields.add("cm:references");
        
        Form form = this.formService.getForm(new Item(NODE_FORM_ITEM_KIND, this.document.toString()), fields);
        
        // check a form got returned
        assertNotNull("Expecting form to be present", form);
        
        // check item identifier matches
        assertEquals(NODE_FORM_ITEM_KIND, form.getItem().getKind());
        assertEquals(this.document.toString(), form.getItem().getId());
        
        // check the type is correct
        assertEquals(ContentModel.TYPE_CONTENT.toPrefixString(this.namespaceService), 
                    form.getItem().getType());
        
        // check there is no group info
        assertNull("Expecting the form groups to be null!", form.getFieldGroups());
        
        // check the field definitions
        Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertNotNull("Expecting to find fields", fieldDefs);
        assertEquals("Expecting to find " + fields.size() + " fields", fields.size(), fieldDefs.size());
        
        // create a Map of the field definitions
        // NOTE: we can safely do this as we know there are no duplicate field names and we're not
        //       concerned with ordering!
        Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
        for (FieldDefinition fieldDef : fieldDefs)
        {
            fieldDefMap.put(fieldDef.getName(), fieldDef);
        }
        
        // find the fields
        PropertyFieldDefinition nameField = (PropertyFieldDefinition)fieldDefMap.get("cm:name");
        PropertyFieldDefinition titleField = (PropertyFieldDefinition)fieldDefMap.get("cm:title");
        PropertyFieldDefinition mimetypeField = (PropertyFieldDefinition)fieldDefMap.get("mimetype");
        PropertyFieldDefinition modifiedField = (PropertyFieldDefinition)fieldDefMap.get("cm:modified");
        PropertyFieldDefinition modifierField = (PropertyFieldDefinition)fieldDefMap.get("cm:modifier");
        PropertyFieldDefinition subjectField = (PropertyFieldDefinition)fieldDefMap.get("cm:subjectline");
        PropertyFieldDefinition sentDateField = (PropertyFieldDefinition)fieldDefMap.get("cm:sentdate");
        AssociationFieldDefinition referencesField = (AssociationFieldDefinition)fieldDefMap.get("cm:references");
        
        // check fields are present
        assertNotNull("Expecting to find the cm:name field", nameField);
        assertNotNull("Expecting to find the cm:title field", titleField);
        assertNotNull("Expecting to find the mimetype field", mimetypeField);
        assertNotNull("Expecting to find the cm:modified field", modifiedField);
        assertNotNull("Expecting to find the cm:modifier field", modifierField);
        assertNotNull("Expecting to find the cm:subjectline field", subjectField);
        assertNotNull("Expecting to find the cm:sentdate field", sentDateField);
        assertNotNull("Expecting to find the cm:references field", referencesField);
        
        // check the labels of all the fields
        assertEquals("Expecting cm:name label to be " + LABEL_NAME, 
                    LABEL_NAME, nameField.getLabel());
        assertEquals("Expecting cm:title label to be " + LABEL_TITLE, 
                    LABEL_TITLE, titleField.getLabel());
        assertEquals("Expecting mimetype label to be " + LABEL_MIMETYPE, 
                    LABEL_MIMETYPE, mimetypeField.getLabel());
        assertEquals("Expecting cm:modified label to be " + LABEL_MODIFIED, 
                    LABEL_MODIFIED, modifiedField.getLabel());
        assertEquals("Expecting cm:modifier label to be " + LABEL_MODIFIER, 
                    LABEL_MODIFIER, modifierField.getLabel());
        assertEquals("Expecting cm:subjectline label to be " + LABEL_SUBJECT, 
                    LABEL_SUBJECT, subjectField.getLabel());
        assertEquals("Expecting cm:sentdate label to be " + LABEL_SENT_DATE, 
                    LABEL_SENT_DATE, sentDateField.getLabel());
        assertEquals("Expecting cm:references label to be " + LABEL_REFERENCES, 
                    LABEL_REFERENCES, referencesField.getLabel());
        
        // check the details of the modified field
        assertEquals("Expecting cm:modified type to be datetime", "datetime", modifiedField.getDataType());
        assertTrue("Expecting cm:modified to be mandatory", modifiedField.isMandatory());
        assertFalse("Expecting cm:modified to be single valued", modifiedField.isRepeating());
        
        // check the details of the association field
        assertEquals("Expecting cm:references endpoint type to be cm:content", "cm:content", 
                    referencesField.getEndpointType());
        assertEquals("Expecting cm:references endpoint direction to be TARGET", 
                    Direction.TARGET.toString(),
                    referencesField.getEndpointDirection().toString());
        assertFalse("Expecting cm:references endpoint to be optional", 
                    referencesField.isEndpointMandatory());
        assertTrue("Expecting cm:references endpoint to be 1 to many",
                    referencesField.isEndpointMany());
        
        // check the form data
        FormData data = form.getFormData();
        assertNotNull("Expecting form data", data);
        assertEquals(this.documentName, data.getFieldData(nameField.getDataKeyName()).getValue());
        assertEquals(VALUE_TITLE, data.getFieldData(titleField.getDataKeyName()).getValue());
        assertEquals(VALUE_MIMETYPE, data.getFieldData(mimetypeField.getDataKeyName()).getValue());
        assertEquals(VALUE_SUBJECT, data.getFieldData(subjectField.getDataKeyName()).getValue());
        assertEquals(USER_ONE, data.getFieldData(modifierField.getDataKeyName()).getValue());

        Date modifiedDate = (Date)data.getFieldData(modifiedField.getDataKeyName()).getValue();
        assertNotNull("Expecting to find modified date", modifiedDate);
        
        Calendar calTestValue = Calendar.getInstance();
        calTestValue.setTime(VALUE_SENT_DATE);
        Calendar calServiceValue = Calendar.getInstance();
        calServiceValue.setTime((Date)data.getFieldData(sentDateField.getDataKeyName()).getValue());
        assertEquals(calTestValue.getTimeInMillis(), calServiceValue.getTimeInMillis());
        
        List<String> targets = (List<String>)data.getFieldData(referencesField.getDataKeyName()).getValue();
        assertEquals("Expecting 1 target", 1, targets.size());
        assertEquals(this.associatedDoc.toString(), targets.get(0));
    }
    
    public void testMissingFieldsDocForm() throws Exception
    {
        // define a list of fields to retrieve from the node
        List<String> fields = new ArrayList<String>(8);
        fields.add("cm:name");
        fields.add("cm:title");
        
        // add fields that will not be present
        fields.add("cm:author");
        fields.add("wrong-name");
        
        Form form = this.formService.getForm(new Item(NODE_FORM_ITEM_KIND, this.document.toString()), fields);
        
        // check a form got returned
        assertNotNull("Expecting form to be present", form);
        
        // check the field definitions
        Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertNotNull("Expecting to find fields", fieldDefs);
        assertEquals("Expecting to find " + (fields.size()-2) + " fields", fields.size()-2, fieldDefs.size());
        
        // create a Map of the field definitions
        // NOTE: we can safely do this as we know there are no duplicate field names and we're not
        //       concerned with ordering!
        Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
        for (FieldDefinition fieldDef : fieldDefs)
        {
            fieldDefMap.put(fieldDef.getName(), fieldDef);
        }
        
        // find the fields
        PropertyFieldDefinition nameField = (PropertyFieldDefinition)fieldDefMap.get("cm:name");
        PropertyFieldDefinition titleField = (PropertyFieldDefinition)fieldDefMap.get("cm:title");
        PropertyFieldDefinition authorField = (PropertyFieldDefinition)fieldDefMap.get("cm:author");
        
        // check fields are present
        assertNotNull("Expecting to find the cm:name field", nameField);
        assertNotNull("Expecting to find the cm:title field", titleField);
        assertNull("Expecting cm:author field to be missing", authorField);
        
        // check the labels of all the fields
        assertEquals("Expecting cm:name label to be " + LABEL_NAME, 
                    LABEL_NAME, nameField.getLabel());
        assertEquals("Expecting cm:title label to be " + LABEL_TITLE, 
                    LABEL_TITLE, titleField.getLabel());
        
        // check the form data
        FormData data = form.getFormData();
        assertNotNull("Expecting form data", data);
        assertEquals(this.documentName, data.getFieldData(nameField.getDataKeyName()).getValue());
        assertEquals(VALUE_TITLE, data.getFieldData(titleField.getDataKeyName()).getValue());
    }
    
    public void testForcedFieldsDocForm() throws Exception
    {
        // define a list of fields to retrieve from the node
        List<String> fields = new ArrayList<String>(4);
        fields.add("cm:name");
        fields.add("cm:title");
        
        // add fields that will not be present
        fields.add("cm:author");
        fields.add("cm:never");
        fields.add("wrong-name");
        
        // try and force the missing fields to appear
        List<String> forcedFields = new ArrayList<String>(2);
        forcedFields.add("cm:author");
        forcedFields.add("cm:never");
        forcedFields.add("wrong-name");
        
        Form form = this.formService.getForm(new Item(NODE_FORM_ITEM_KIND, this.document.toString()), fields, forcedFields);
        
        // check a form got returned
        assertNotNull("Expecting form to be present", form);
        
        // check the field definitions
        Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertNotNull("Expecting to find fields", fieldDefs);
        assertEquals("Expecting to find " + (fields.size()-2) + " fields", fields.size()-2, fieldDefs.size());
        
        // create a Map of the field definitions
        // NOTE: we can safely do this as we know there are no duplicate field names and we're not
        //       concerned with ordering!
        Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
        for (FieldDefinition fieldDef : fieldDefs)
        {
            fieldDefMap.put(fieldDef.getName(), fieldDef);
        }
        
        // find the fields
        PropertyFieldDefinition nameField = (PropertyFieldDefinition)fieldDefMap.get("cm:name");
        PropertyFieldDefinition titleField = (PropertyFieldDefinition)fieldDefMap.get("cm:title");
        PropertyFieldDefinition authorField = (PropertyFieldDefinition)fieldDefMap.get("cm:author");
        PropertyFieldDefinition neverField = (PropertyFieldDefinition)fieldDefMap.get("cm:never");
        PropertyFieldDefinition wrongField = (PropertyFieldDefinition)fieldDefMap.get("wrong-name");
        
        // check fields are present
        assertNotNull("Expecting to find the cm:name field", nameField);
        assertNotNull("Expecting to find the cm:title field", titleField);
        assertNotNull("Expecting to find the cm:author field", authorField);
        assertNull("Expecting cm:never field to be missing", neverField);
        assertNull("Expecting wrong-name field to be missing", wrongField);
        
        // check the labels of all the fields
        assertEquals("Expecting cm:name label to be " + LABEL_NAME, 
                    LABEL_NAME, nameField.getLabel());
        assertEquals("Expecting cm:title label to be " + LABEL_TITLE, 
                    LABEL_TITLE, titleField.getLabel());
        assertEquals("Expecting cm:author label to be " + LABEL_AUTHOR, 
                    LABEL_AUTHOR, authorField.getLabel());
        
        // check the form data
        FormData data = form.getFormData();
        assertNotNull("Expecting form data", data);
        assertEquals(this.documentName, data.getFieldData(nameField.getDataKeyName()).getValue());
        assertEquals(VALUE_TITLE, data.getFieldData(titleField.getDataKeyName()).getValue());
        assertNull("Didn't expect to find a value for cm:author", data.getFieldData(authorField.getDataKeyName()));
    }
    
    @SuppressWarnings("unchecked")
    public void testGetAllFolderForm() throws Exception
    {
        Form form = this.formService.getForm(new Item(NODE_FORM_ITEM_KIND, this.folder.toString()));
        
        // check a form got returned
        assertNotNull("Expecting form to be present", form);
        
        // check item identifier matches
        assertEquals(NODE_FORM_ITEM_KIND, form.getItem().getKind());
        assertEquals(this.folder.toString(), form.getItem().getId());
        
        // check the type is correct
        assertEquals(ContentModel.TYPE_FOLDER.toPrefixString(this.namespaceService), 
                    form.getItem().getType());
        
        // check there is no group info
        assertNull("Expecting the form groups to be null!", form.getFieldGroups());
        
        // check the field definitions
        Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertNotNull("Expecting to find fields", fieldDefs);
        assertEquals("Wrong number of fields", 8, fieldDefs.size());
        
        // create a Map of the field definitions
        // NOTE: we can safely do this as we know there are no duplicate field names and we're not
        //       concerned with ordering!
        Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
        for (FieldDefinition fieldDef : fieldDefs)
        {
            fieldDefMap.put(fieldDef.getName(), fieldDef);
        }
        
        // find the fields
        PropertyFieldDefinition nameField = (PropertyFieldDefinition)fieldDefMap.get("cm:name");
        AssociationFieldDefinition containsField = (AssociationFieldDefinition)fieldDefMap.get("cm:contains");
        
        // check fields are present
        assertNotNull("Expecting to find the cm:name field", nameField);
        assertNotNull("Expecting to find the cm:contains field", containsField);
        
        // check the system properties are not present by default
        assertFalse(fieldDefMap.containsKey("sys:node-dbid"));
        assertFalse(fieldDefMap.containsKey("sys:store-identifier"));
        assertFalse(fieldDefMap.containsKey("sys:node-uuid"));
        assertFalse(fieldDefMap.containsKey("sys:store-protocol"));
        
        // check the labels of all the fields
        assertEquals("Expecting cm:name label to be " + LABEL_NAME, 
                    LABEL_NAME, nameField.getLabel());
        assertEquals("Expecting cm:contains label to be " + LABEL_CONTAINS, 
                    LABEL_CONTAINS, containsField.getLabel());
        
        // check details of name field
        assertEquals("Expecting cm:name type to be text", "text", nameField.getDataType());
        assertTrue("Expecting cm:name to be mandatory", nameField.isMandatory());
        assertFalse("Expecting cm:name to be single valued", nameField.isRepeating());
        
        // check the details of the association field
        assertEquals("Expecting cm:contains endpoint type to be sys:base", "sys:base", 
                    containsField.getEndpointType());
        assertEquals("Expecting cm:contains endpoint direction to be TARGET", 
                    Direction.TARGET.toString(),
                    containsField.getEndpointDirection().toString());
        assertFalse("Expecting cm:contains endpoint to be optional", 
                    containsField.isEndpointMandatory());
        assertTrue("Expecting cm:contains endpoint to be 1 to many",
                    containsField.isEndpointMany());
        
        // check the form data
        FormData data = form.getFormData();
        assertNotNull("Expecting form data", data);
        assertEquals(this.folderName, data.getFieldData(nameField.getDataKeyName()).getValue());
        
        List<String> children = (List<String>)data.getFieldData(containsField.getDataKeyName()).getValue();
        assertEquals("Expecting 3 children", 3, children.size());
        assertEquals(this.document.toString(), children.get(0));
        assertEquals(this.associatedDoc.toString(), children.get(1));
        assertEquals(this.childDoc.toString(), children.get(2));
    }
    
    @SuppressWarnings("unchecked")
    public void testSaveNodeForm() throws Exception
    {
        // create FormData object containing the values to update
        FormData data = new FormData();
        
        // update the name
        String newName = "new-" + this.documentName;
        data.addFieldData("prop_cm_name", newName);
        
        // update the title property
        String newTitle = "This is the new title property";
        data.addFieldData("prop_cm_title", newTitle);
        
        // update the mimetype
        String newMimetype = MimetypeMap.MIMETYPE_HTML;
        data.addFieldData("prop_mimetype", newMimetype);
        
        // update the author property (this is on an aspect not applied)
        String newAuthor = "Gavin Cornwell";
        data.addFieldData("prop_cm_author", newAuthor);
        
        // update the originator
        String newOriginator = "jane@example.com";
        data.addFieldData("prop_cm_originator", newOriginator);
        
        // update the adressees, add another
        String newAddressees = VALUE_ADDRESSEES1 + "," + VALUE_ADDRESSEES2 + "," + VALUE_ADDRESSEES3;
        data.addFieldData("prop_cm_addressees", newAddressees);
        
        // set the date to null (using an empty string)
        data.addFieldData("prop_cm_sentdate", "");
        
        // add an association to the child doc (as an attachment which is defined on an aspect not applied)
        //data.addField("assoc_cm_attachments_added", this.childDoc.toString());
        
        // try and update non-existent properties and assocs (make sure there are no exceptions)
        data.addFieldData("prop_cm_wrong", "This should not be persisted");
        data.addFieldData("cm_wrong", "This should not be persisted");
        data.addFieldData("prop_cm_wrong_property", "This should not be persisted");
        data.addFieldData("prop_cm_wrong_property_name", "This should not be persisted");
        data.addFieldData("assoc_cm_wrong_association", "This should be ignored");
        data.addFieldData("assoc_cm_wrong_association_added", "This should be ignored");
        data.addFieldData("assoc_cm_wrong_association_removed", "This should be ignored");
        data.addFieldData("assoc_cm_added", "This should be ignored");
        
        // persist the data
        this.formService.saveForm(new Item(NODE_FORM_ITEM_KIND, this.document.toString()), data);
        
        // retrieve the data directly from the node service to ensure its been changed
        Map<QName, Serializable> updatedProps = this.nodeService.getProperties(this.document);
        String updatedName = (String)updatedProps.get(ContentModel.PROP_NAME);
        String updatedTitle = (String)updatedProps.get(ContentModel.PROP_TITLE);
        String updatedAuthor = (String)updatedProps.get(ContentModel.PROP_AUTHOR);
        String updatedOriginator = (String)updatedProps.get(ContentModel.PROP_ORIGINATOR);
        List<String> updatedAddressees = (List<String>)updatedProps.get(ContentModel.PROP_ADDRESSEES);
        String wrong = (String)updatedProps.get(QName.createQName("cm", "wrong", this.namespaceService));
        Date sentDate = (Date)updatedProps.get(ContentModel.PROP_SENTDATE);
        
        assertEquals(newName, updatedName);
        assertEquals(newTitle, updatedTitle);
        assertEquals(newAuthor, updatedAuthor);
        assertEquals(newOriginator, updatedOriginator);
        assertNull("Expecting sentdate to be null", sentDate);
        assertNull("Expecting my:wrong to be null", wrong);
        assertNotNull("Expected there to be addressees", updatedAddressees);
        assertTrue("Expected there to be 3 addressees", updatedAddressees.size() == 3);
        assertEquals(VALUE_ADDRESSEES1, updatedAddressees.get(0));
        assertEquals(VALUE_ADDRESSEES2, updatedAddressees.get(1));
        assertEquals(VALUE_ADDRESSEES3, updatedAddressees.get(2));
        
        // check the titled aspect was automatically applied
        assertTrue("Expecting the cm:titled to have been applied", 
                    this.nodeService.hasAspect(this.document, ContentModel.ASPECT_TITLED));
        
        // check the author aspect was automatically applied
        assertTrue("Expecting the cm:author to have been applied", 
                    this.nodeService.hasAspect(this.document, ContentModel.ASPECT_AUTHOR));
        
        // check mimetype was updated
        ContentData contentData = (ContentData)updatedProps.get(ContentModel.PROP_CONTENT);
        if (contentData != null)
        {
            String updatedMimetype = contentData.getMimetype();
            assertEquals(MimetypeMap.MIMETYPE_HTML, updatedMimetype);
        }
        
        // check the association was added and the aspect it belongs to applied
        /*
        List<AssociationRef> assocs = this.nodeService.getTargetAssocs(this.document, 
                    ContentModel.ASSOC_ATTACHMENTS);
        assertEquals("Expecting 1 attachment association", 1, assocs.size());
        assertEquals(assocs.get(0).getTargetRef().toString(), this.childDoc.toString());
        assertTrue("Expecting the cm:attachable to have been applied", 
                    this.nodeService.hasAspect(this.document, ContentModel.ASPECT_ATTACHABLE));
        */
    }
    
    public void testGetAllCreateForm() throws Exception
    {
        // get a form for the cm:content type
        Form form = this.formService.getForm(new Item(TYPE_FORM_ITEM_KIND, "cm:content"));
        
        // check a form got returned
        assertNotNull("Expecting form to be present", form);
        
        // check item identifier matches
        assertEquals(TYPE_FORM_ITEM_KIND, form.getItem().getKind());
        assertEquals("cm:content", form.getItem().getId());
        
        // check the type is correct
        assertEquals(ContentModel.TYPE_CONTENT.toPrefixString(this.namespaceService), 
                    form.getItem().getType());
        
        // check there is no group info
        assertNull("Expecting the form groups to be null!", form.getFieldGroups());
        
        // check the field definitions
        Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertNotNull("Expecting to find fields", fieldDefs);
        assertEquals("Wrong number of fields", 8, fieldDefs.size());
        
        // create a Map of the field definitions
        // NOTE: we can safely do this as we know there are no duplicate field names and we're not
        //       concerned with ordering!
        Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
        for (FieldDefinition fieldDef : fieldDefs)
        {
            fieldDefMap.put(fieldDef.getName(), fieldDef);
        }
        
        // find the fields
        PropertyFieldDefinition nameField = (PropertyFieldDefinition)fieldDefMap.get("cm:name");
        PropertyFieldDefinition createdField = (PropertyFieldDefinition)fieldDefMap.get("cm:created");
        PropertyFieldDefinition creatorField = (PropertyFieldDefinition)fieldDefMap.get("cm:creator");
        PropertyFieldDefinition modifiedField = (PropertyFieldDefinition)fieldDefMap.get("cm:modified");
        PropertyFieldDefinition modifierField = (PropertyFieldDefinition)fieldDefMap.get("cm:modifier");
        
        // check fields are present
        assertNotNull("Expecting to find the cm:name field", nameField);
        assertNotNull("Expecting to find the cm:created field", createdField);
        assertNotNull("Expecting to find the cm:creator field", creatorField);
        assertNotNull("Expecting to find the cm:modified field", modifiedField);
        assertNotNull("Expecting to find the cm:modifier field", modifierField);
    }
    
    public void testGetSelectedFieldsCreateForm() throws Exception
    {
        // define a list of fields to retrieve from the node
        List<String> fields = new ArrayList<String>(8);
        fields.add("cm:name");
        fields.add("cm:title");
        
        // get a form for the cm:content type
        Form form = this.formService.getForm(new Item(TYPE_FORM_ITEM_KIND, "cm:content"), fields);
        
        // check a form got returned
        assertNotNull("Expecting form to be present", form);
        
        // check item identifier matches
        assertEquals(TYPE_FORM_ITEM_KIND, form.getItem().getKind());
        assertEquals("cm:content", form.getItem().getId());
        
        // check the type is correct
        assertEquals(ContentModel.TYPE_CONTENT.toPrefixString(this.namespaceService), 
                    form.getItem().getType());
        
        // check there is no group info
        assertNull("Expecting the form groups to be null!", form.getFieldGroups());
        
        // check the field definitions
        Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertNotNull("Expecting to find fields", fieldDefs);
        assertEquals("Expecting to find 1 field", 1, fieldDefs.size());
        
        // create a Map of the field definitions
        // NOTE: we can safely do this as we know there are no duplicate field names and we're not
        //       concerned with ordering!
        Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
        for (FieldDefinition fieldDef : fieldDefs)
        {
            fieldDefMap.put(fieldDef.getName(), fieldDef);
        }
        
        // find the fields
        PropertyFieldDefinition nameField = (PropertyFieldDefinition)fieldDefMap.get("cm:name");
        assertNotNull("Expecting to find the cm:name field", nameField);
        
        // now force the title field to be present and check
        List<String> forcedFields = new ArrayList<String>(2);
        forcedFields.add("cm:title");
        // get a form for the cm:content type
        form = this.formService.getForm(new Item(TYPE_FORM_ITEM_KIND, "cm:content"), fields, forcedFields);
        fieldDefs = form.getFieldDefinitions();
        assertNotNull("Expecting to find fields", fieldDefs);
        assertEquals("Expecting to find 2 fields", 2, fieldDefs.size());
        
    }
    
    public void testSaveTypeForm() throws Exception
    {
        // create FormData object containing the values to update
        FormData data = new FormData();
        
        // supply the name
        String name = "new-" + this.documentName;
        data.addFieldData("prop_cm_name", name);
        
        // supply the title property
        String title = "This is the title property";
        data.addFieldData("prop_cm_title", title);
        
        // persist the data (without a destination and make sure it fails)
        try
        {
            this.formService.saveForm(new Item(TYPE_FORM_ITEM_KIND, "cm:content"), data);
            
            fail("Expected the persist to fail as there was no destination");
        }
        catch (FormException fe)
        {
            // expected
        }
        
        // supply the destination
        data.addFieldData(AbstractFormProcessor.DESTINATION, this.folder.toString());
        
        // persist the data
        NodeRef newNode = (NodeRef)this.formService.saveForm(new Item(TYPE_FORM_ITEM_KIND, "cm:content"), data);
        
        // retrieve the data directly from the node service to ensure its there
        Map<QName, Serializable> props = this.nodeService.getProperties(newNode);
        String newName = (String)props.get(ContentModel.PROP_NAME);
        String newTitle = (String)props.get(ContentModel.PROP_TITLE);
        assertEquals(name, newName);
        assertEquals(title, newTitle);
        
        // check the titled aspect was automatically applied
        assertTrue("Expecting the cm:titled to have been applied", 
                    this.nodeService.hasAspect(this.document, ContentModel.ASPECT_TITLED));
        
        // test different forms of itemId's
        data.addFieldData(AbstractFormProcessor.DESTINATION, this.folder.toString());
        newNode = (NodeRef)this.formService.saveForm(new Item(TYPE_FORM_ITEM_KIND, "cm:content"), data);
        assertNotNull("Expected new node to be created using itemId cm_content", newNode);
        
        data.addFieldData(AbstractFormProcessor.DESTINATION, this.folder.toString());
        newNode = (NodeRef)this.formService.saveForm(new Item(TYPE_FORM_ITEM_KIND, ContentModel.TYPE_CONTENT.toString()), data);
        assertNotNull("Expected new node to be created using itemId " + ContentModel.TYPE_CONTENT.toString(), newNode);
    }
    
    public void testContentForms() throws Exception
    {
        // create FormData object 
        String name = "created-" + this.documentName;
        String title = "This is the title property";
        String mimetype = "text/html";
        String content = "This is the content.";
        FormData data = createContentFormData(name, title, mimetype, content);
        
        // supply the destination
        data.addFieldData(AbstractFormProcessor.DESTINATION, this.folder.toString());
        
        // persist the data
        NodeRef newNode = (NodeRef)this.formService.saveForm(new Item(TYPE_FORM_ITEM_KIND, "cm:content"), data);
        
        // check the node was created correctly
        checkContentDetails(newNode, name, title, mimetype, content);
        
        // get the form for the new content and check the form data
        List<String> fields = new ArrayList<String>(2);
        fields.add("cm:name");
        fields.add("cm:content");
        Form form = this.formService.getForm(new Item(NODE_FORM_ITEM_KIND, newNode.toString()), fields);
        assertNotNull(form);
        assertEquals(name, form.getFormData().getFieldData("prop_cm_name").getValue().toString());
        String contentUrl = form.getFormData().getFieldData("prop_cm_content").getValue().toString();
        assertTrue("Expected content url to contain mimetype", (contentUrl.indexOf("mimetype=") != -1));
        assertTrue("Expected content url to contain encoding", (contentUrl.indexOf("encoding=") != -1));
        assertTrue("Expected content url to contain size", (contentUrl.indexOf("size=") != -1));
        
        // create another node without supplying the mimetype and check the details
        String name2 = "created2-" + this.documentName;
        data = createContentFormData(name2, title, null, content);
        data.addFieldData(AbstractFormProcessor.DESTINATION, this.folder.toString());
        NodeRef newNode2 = (NodeRef)this.formService.saveForm(new Item(TYPE_FORM_ITEM_KIND, "cm:content"), data);
        checkContentDetails(newNode2, name2, title, "text/plain", content);
        
        // update the content and the mimetype
        Item item = new Item(NODE_FORM_ITEM_KIND, newNode.toString());
        String updatedContent = "This is the updated content";
        String updatedMimetype = "text/plain";
        data = createContentFormData(name, title, updatedMimetype, updatedContent);
        this.formService.saveForm(item, data);
        
        // check the node was updated correctly
        checkContentDetails(newNode, name, title, updatedMimetype, updatedContent);
        
        // update the content and mimetype again but ensure the content is updated last
        // to check the mimetype change is not lost
        updatedContent = "<element>The content is now XML</content>";
        updatedMimetype = "text/xml";
        data = createContentFormData(name, title, updatedMimetype, updatedContent);
        // remove and add content to ensure it's last
        data.removeFieldData("prop_cm_content");
        data.addFieldData("prop_cm_content", updatedContent);
        this.formService.saveForm(item, data);
        
        // check the details
        checkContentDetails(newNode, name, title, updatedMimetype, updatedContent);
        
        // update just the content
        updatedContent = "<element attribute=\"true\">The content is still XML</content>";
        data = createContentFormData(null, null, null, updatedContent);
        this.formService.saveForm(item, data);
        checkContentDetails(newNode, name, title, updatedMimetype, updatedContent);
    }
    
    private FormData createContentFormData(String name, String title, String mimetype, String content)
    {
        FormData data = new FormData();
        
        if (name != null)
        {
            data.addFieldData("prop_cm_name", name);
        }
        
        if (title != null)
        {
            data.addFieldData("prop_cm_title", title);
        }
        
        if (content != null)
        {
            data.addFieldData("prop_cm_content", content);
        }
        
        if (mimetype != null)
        {
            data.addFieldData("prop_mimetype", mimetype);
        }
        
        return data;
    }
    
    private void checkContentDetails(NodeRef node, String expectedName, String expectedTitle, 
                String expectedMimetype, String expectedContent)
    {
        Map<QName, Serializable> props = this.nodeService.getProperties(node);
        String name = (String)props.get(ContentModel.PROP_NAME);
        String title = (String)props.get(ContentModel.PROP_TITLE);
        assertEquals(expectedName, name);
        assertEquals(expectedTitle, title);
        
        ContentData contentData = (ContentData) this.nodeService.getProperty(node, ContentModel.PROP_CONTENT);
        assertNotNull(contentData);
        String mimetype = contentData.getMimetype();
        assertEquals(expectedMimetype, mimetype);
        
        ContentReader reader = this.contentService.getReader(node, ContentModel.PROP_CONTENT);
        assertNotNull(reader);
        String content = reader.getContentString();
        assertEquals(expectedContent, content);
    }
    
    @SuppressWarnings({ "deprecation" })
    public void disabledTestFDKModel() throws Exception
    {
        // NOTE: The FDK is not loaded by default, for this test to work you must
        //       import and make the "Forms Development Kit" project a dependency 
        //       of the "Repository" project.
        
        DictionaryService dictionary = (DictionaryService)this.applicationContext.getBean("DictionaryService");
        try
        {
            dictionary.getType(QName.createQName("fdk", "everything", this.namespaceService));
        }
        catch (NamespaceException ne)
        {
            fail("FDK namespace is missing, ensure you've made the 'Forms Development Kit' project a dependency of the 'Repository' project when enabling this test!");
        }
        
        // from the check above we know the 'fdk' namespace is present so we can safely
        // use the FDK model, firstly create an instance of an everything node
        String fdkUri = "http://www.alfresco.org/model/fdk/1.0";
        QName everythingType = QName.createQName(fdkUri, "everything");
        QName textProperty = QName.createQName(fdkUri, "text");
        QName underscoreProperty = QName.createQName(fdkUri, "with_underscore");
        QName dashProperty = QName.createQName(fdkUri, "with-dash");
        QName duplicateProperty = QName.createQName(fdkUri, "duplicate");
        QName periodProperty = QName.createQName(fdkUri, "period");
        
        String guid = GUID.generate();
        String name = "everything" + guid + ".txt";
        String textValue = "This is some text.";
        String underscoreValue = "Property with an underscore in the name.";
        String dashValue = "Property with a dash in the name.";
        String duplicateValue = "Property with the same name as an association.";
        String periodValue = "day|1";
        
        Map<QName, Serializable> docProps = new HashMap<QName, Serializable>(4);
        docProps.put(ContentModel.PROP_NAME, name);
        docProps.put(textProperty, textValue);
        docProps.put(underscoreProperty, underscoreValue);
        docProps.put(dashProperty, dashValue);
        docProps.put(duplicateProperty, duplicateValue);
        docProps.put(periodProperty, periodValue);
        NodeRef everythingNode = this.nodeService.createNode(this.folder, ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), everythingType,
                    docProps).getChildRef();
        
        // define a list of fields to retrieve from the node
        List<String> fields = new ArrayList<String>(4);
        fields.add("cm:name");
        fields.add("fdk:text");
        fields.add("fdk:with_underscore");
        fields.add("fdk:with-dash");
        fields.add("prop:fdk:duplicate");
        fields.add("assoc:fdk:duplicate");
        fields.add("fdk:period");
        
        Form form = this.formService.getForm(new Item(NODE_FORM_ITEM_KIND, everythingNode.toString()), fields);
        
        // check a form got returned
        assertNotNull("Expecting form to be present", form);
        
        // check the type is correct
        assertEquals("fdk:everything", form.getItem().getType());
        
        // check the field definitions
        Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertNotNull("Expecting to find fields", fieldDefs);
        assertEquals("Expecting to find " + fields.size() + " fields", fields.size(), fieldDefs.size());
        
        // find the fields, as we have a duplicate we can't use a Map
        PropertyFieldDefinition nameField = null;
        PropertyFieldDefinition textField = null;
        PropertyFieldDefinition underscoreField = null;
        PropertyFieldDefinition dashField = null;
        PropertyFieldDefinition periodField = null;
        PropertyFieldDefinition duplicatePropField = null;
        AssociationFieldDefinition duplicateAssocField = null;
        
        for (FieldDefinition field : fieldDefs)
        {
            if (field.getName().equals("cm:name"))
            {
                nameField = (PropertyFieldDefinition)field;
            }
            else if (field.getName().equals("fdk:text"))
            {
                textField = (PropertyFieldDefinition)field;
            }
            else if (field.getName().equals("fdk:with_underscore"))
            {
                underscoreField = (PropertyFieldDefinition)field;
            }
            else if (field.getName().equals("fdk:with-dash"))
            {
                dashField = (PropertyFieldDefinition)field;
            }
            else if (field.getName().equals("fdk:duplicate"))
            {
                if (field instanceof PropertyFieldDefinition)
                {
                    duplicatePropField = (PropertyFieldDefinition)field;
                }
                else if (field instanceof AssociationFieldDefinition)
                {
                    duplicateAssocField = (AssociationFieldDefinition)field;
                }
            }
            else if (field.getName().equals("fdk:period"))
            {
                periodField = (PropertyFieldDefinition)field;
            }
        }
        
        assertNotNull("Expected to find nameField", nameField);
        assertNotNull("Expected to find textField", textField);
        assertNotNull("Expected to find underscoreField", underscoreField);
        assertNotNull("Expected to find dashField", dashField);
        assertNotNull("Expected to find periodField", periodField);
        assertNotNull("Expected to find duplicatePropField", duplicatePropField);
        assertNotNull("Expected to find duplicateAssocField", duplicateAssocField);
        
        // check the field values
        FormData values = form.getFormData();
        assertEquals(name, values.getFieldData(nameField.getDataKeyName()).getValue());
        assertEquals(textValue, values.getFieldData(textField.getDataKeyName()).getValue());
        assertEquals(underscoreValue, values.getFieldData(underscoreField.getDataKeyName()).getValue());
        assertEquals(dashValue, values.getFieldData(dashField.getDataKeyName()).getValue());
        assertEquals(periodValue, values.getFieldData(periodField.getDataKeyName()).getValue().toString());
        assertEquals(duplicateValue, values.getFieldData(duplicatePropField.getDataKeyName()).getValue());
        FieldData fieldData = values.getFieldData(duplicateAssocField.getDataKeyName());
        assertNotNull(fieldData);
        List<?> assocs = (List<?>)fieldData.getValue();
        assertNotNull(assocs);
        assertEquals(0, assocs.size());
        
        // check the period property data type parameters were returned
        DataTypeParameters dtp = periodField.getDataTypeParameters();
        assertNotNull("Expected to find data type parameters for the fdk:period field", dtp);
        
        // update the properties via FormService
        FormData data = new FormData();
        
        // setup the new property values
        String newText = "This is the new text property";
        data.addFieldData(textField.getDataKeyName(), newText);
        
        String newUnderscore = "This is the new value for the underscore property.";
        data.addFieldData(underscoreField.getDataKeyName(), newUnderscore);
        
        String newDash = "This is the new value for the dash property.";
        data.addFieldData(dashField.getDataKeyName(), newDash);
        
        String newDuplicateProp = "This is the new value for the duplicate property.";
        data.addFieldData(duplicatePropField.getDataKeyName(), newDuplicateProp);
        
        // add new association value
        data.addFieldData(duplicateAssocField.getDataKeyName() + "_added", this.document.toString());
        
        // persist the data
        this.formService.saveForm(new Item(NODE_FORM_ITEM_KIND, everythingNode.toString()), data);
        
        // retrieve the data directly from the node service to ensure its been changed
        Map<QName, Serializable> updatedProps = this.nodeService.getProperties(everythingNode);
        String updatedText = (String)updatedProps.get(textProperty);
        String updatedUnderscore = (String)updatedProps.get(underscoreProperty);
        String updatedDash = (String)updatedProps.get(dashProperty);
        String updatedDuplicate = (String)updatedProps.get(duplicateProperty);
        
        // check values were updated
        assertEquals(newText, updatedText);
        assertEquals(newUnderscore, updatedUnderscore);
        assertEquals(newDash, updatedDash);
        assertEquals(newDuplicateProp, updatedDuplicate);
        
        // retrieve the association that should now be present
        assocs = this.nodeService.getTargetAssocs(everythingNode, duplicateProperty);
        assertEquals(1, assocs.size());
        
        // request a form for a type with an underscore in it's name
        fields = new ArrayList<String>(4);
        fields.add("cm:name");

        form = this.formService.getForm(new Item(TYPE_FORM_ITEM_KIND, "fdk:with_underscore"), fields);
        assertNotNull(form);
        
        // make sure there are 3 fields
        fieldDefs = form.getFieldDefinitions();
        assertNotNull(fieldDefs);
        assertEquals(1, fieldDefs.size());
        
        // save the form to ensure persistence works too
        String nodeName = GUID.generate() + ".txt";
        data = new FormData();
        data.addFieldData("prop_cm_name", nodeName);
        data.addFieldData(AbstractFormProcessor.DESTINATION, this.folder.toString());
        NodeRef newNode = (NodeRef)this.formService.saveForm(new Item(TYPE_FORM_ITEM_KIND, "fdk:with_underscore"), data);
        assertNotNull(newNode);
    }
    
    public void testGetFormForJbpmTask() throws Exception
    {
        checkGetFormForTask("jbpm$wf:review");
    }

    public void testGetFormForActivitiTask() throws Exception
    {
        checkGetFormForTask("activiti$activitiReview");
    }
    
    private void checkGetFormForTask(String defName)
    {
        WorkflowTask task = getWorkflowTask(defName);
        Item item = new Item("task", task.getId());

        Form form = formService.getForm(item);
        assertNotNull(form);
        assertEquals(item.getKind(), form.getItem().getKind());
        assertEquals(item.getId(), form.getItem().getId());
        List<String> fieldDefNames = form.getFieldDefinitionNames();
        assertTrue(fieldDefNames.size() > 0);

        // Check the correct field names are present.
        List<String> expFields = getExpectedTaskFields();
        assertTrue(fieldDefNames.containsAll(expFields));
        
        // Check default value for priority is correct.
        List<FieldDefinition> definitions = form.getFieldDefinitions();
        String priorityName = WorkflowModel.PROP_PRIORITY.toPrefixString(namespaceService);
        for (FieldDefinition definition : definitions)
        {
            if(priorityName.equals(definition.getName()))
            {
                assertEquals("2", definition.getDefaultValue());
                break;
            }
        }
    }

    public void testSaveJbpmTask() throws Exception
    {
        checkSaveTask("jbpm$wf:review");
    }

    public void testSaveActivitiTask() throws Exception
    {
        checkSaveTask("activiti$activitiReview");
    }
    
    private void checkSaveTask(String defName)
    {
        WorkflowTask task = getWorkflowTask(defName);
        QName descName = WorkflowModel.PROP_DESCRIPTION;
        Serializable initialDesc = task.getProperties().get(descName);
        String testDesc = "Foo-Bar-Test-String";
        assertFalse(testDesc.equals(initialDesc));

        Item item = new Item("task", task.getId());
        FormData data = new FormData();
        String descFieldName = FormFieldConstants.PROP_DATA_PREFIX
                    + descName.toPrefixString(namespaceService).replace(":", "_");
        data.addFieldData(descFieldName, testDesc, true);
        formService.saveForm(item, data);

        WorkflowTask newTask = workflowService.getTaskById(task.getId());
        assertEquals(testDesc, newTask.getProperties().get(descName));
    }
    
    public void testTransitionJbpmTask() throws Exception
    {
        checkTransitionTask("jbpm$wf:review", "approve", "approve");
    }
    
    public void testTransitionActivitiTask() throws Exception
    {
        checkTransitionTask("activiti$activitiReview", ActivitiConstants.DEFAULT_TRANSITION_NAME, "Approve");
    }
    
    private void checkTransitionTask(String defName, String transitionId, String expOutcome)
    {
        WorkflowTask task = getWorkflowTask(defName);
        QName descName = WorkflowModel.PROP_DESCRIPTION;
        Serializable initialDesc = task.getProperties().get(descName);
        String testDesc = "Foo-Bar-Test-String";
        assertFalse(testDesc.equals(initialDesc));
        
        Item item = new Item("task", task.getId());
        FormData data = new FormData();
        String descFieldName = FormFieldConstants.PROP_DATA_PREFIX + descName.toPrefixString(namespaceService).replace(":", "_");
        data.addFieldData(descFieldName, testDesc, true);
        
        String reviewOutcomeFieldName = FormFieldConstants.PROP_DATA_PREFIX + "wf_reviewOutcome";
        data.addFieldData(reviewOutcomeFieldName, "Approve", true);
        
        String transitionDataKey = FormFieldConstants.PROP_DATA_PREFIX + TransitionFieldProcessor.KEY;
        data.addFieldData(transitionDataKey, transitionId);
        
        formService.saveForm(item, data);
        WorkflowTask newTask = workflowService.getTaskById(task.getId());
        assertEquals("The description should have been updated!", testDesc, newTask.getProperties().get(descName));

        // Check the task is completed
        assertEquals("The task should have been completed!", WorkflowTaskState.COMPLETED, newTask.getState());

        Serializable outcome = newTask.getProperties().get(WorkflowModel.PROP_OUTCOME);
        assertEquals("The transition is wrong!", expOutcome, outcome);
    }

    private WorkflowTask getWorkflowTask(String definitionName)
    {
        WorkflowDefinition reviewDef = workflowService.getDefinitionByName(definitionName);
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER_ONE));
        properties.put(WorkflowModel.ASSOC_PACKAGE, folder);
        WorkflowPath path = workflowService.startWorkflow(reviewDef.getId(), properties);
        WorkflowTask task = getTaskForPath(path);
        String startTaskId = reviewDef.getStartTaskDefinition().getId();
        if (startTaskId.equals(task.getDefinition().getId()))
        {
            workflowService.endTask(task.getId(), null);
            task = getTaskForPath(path);
        }
        return task;
    }

    private WorkflowTask getTaskForPath(WorkflowPath path)
    {
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks);
        assertTrue(tasks.size() > 0);
        WorkflowTask task = tasks.get(0);
        return task;
    }

    private List<String> getExpectedTaskFields()
    {
        ArrayList<String> fields = new ArrayList<String>(4);
        fields.add(WorkflowModel.PROP_DESCRIPTION.toPrefixString(namespaceService));
        fields.add(WorkflowModel.PROP_STATUS.toPrefixString(namespaceService));
        fields.add(WorkflowModel.PROP_PRIORITY.toPrefixString(namespaceService));
        fields.add(WorkflowModel.PROP_COMMENT.toPrefixString(namespaceService));
        fields.add(WorkflowModel.PROP_DUE_DATE.toPrefixString(namespaceService));
        return fields;
    }
    
    public void testJbpmWorkflowForm() throws Exception
    {
        checkWorkflowForms("jbpm$wf:adhoc", "|Task Done");
    }
    
    public void testActivitiWorkflowForm() throws Exception
    {
        checkWorkflowForms("activiti$activitiAdhoc", "Next|Next");
    }
    
    private void checkWorkflowForms(String workflowDefName, String transitionLabels) throws Exception
    {
        // generate a form for a well known workflow-definition supplying
        // a legitimate set of fields for the workflow
        List<String> fields = new ArrayList<String>(8);
        String taskIdName = WorkflowModel.PROP_TASK_ID.toPrefixString(namespaceService);
        String workflowDescName = WorkflowModel.PROP_WORKFLOW_DESCRIPTION.toPrefixString(namespaceService);
        String workflowDueDateName = WorkflowModel.PROP_WORKFLOW_DUE_DATE.toPrefixString(namespaceService);
        String packageItemsName = "packageItems";

        fields.add(taskIdName);
        fields.add(workflowDescName);
        fields.add(workflowDueDateName);
        fields.add(packageItemsName);
        
        // Use URL-friendly format.
        Form form = this.formService.getForm(new Item(WORKFLOW_FORM_ITEM_KIND, workflowDefName), fields);
        
        // check a form got returned
        assertNotNull("Expecting form to be present", form);
        
        // check item identifier matches
        assertEquals(WORKFLOW_FORM_ITEM_KIND, form.getItem().getKind());
        assertEquals(workflowDefName, form.getItem().getId());
        
        // check the field definitions
        Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertNotNull("Expecting to find fields", fieldDefs);
        assertEquals("Expecting to find " + fields.size() + " fields", fields.size(), fieldDefs.size());
        
        // check the fields are returned correctly
        Map<String, FieldDefinition> fieldDefMap = new HashMap<String, FieldDefinition>(fieldDefs.size());
        for (FieldDefinition fieldDef : fieldDefs)
        {
            fieldDefMap.put(fieldDef.getName(), fieldDef);
        }
        
        // find the fields
        PropertyFieldDefinition idField = (PropertyFieldDefinition)fieldDefMap.get(taskIdName);
        PropertyFieldDefinition descriptionField = (PropertyFieldDefinition)fieldDefMap.get(workflowDescName);
        PropertyFieldDefinition dueDateField = (PropertyFieldDefinition)fieldDefMap.get(workflowDueDateName);
        AssociationFieldDefinition packageItemsField = (AssociationFieldDefinition)fieldDefMap.get(packageItemsName);
        
        // check fields are present
        assertNotNull("Expecting to find the bpm:taskId field", idField);
        assertNotNull("Expecting to find the bpm:workflowDescription field", descriptionField);
        assertNotNull("Expecting to find the bpm:workflowDueDate field", dueDateField);
        assertNotNull("Expecting to find the packageItems field", packageItemsField);
        
        // get the number of tasks now
        List<WorkflowTask> tasks = workflowService.getAssignedTasks(USER_ONE, 
                    WorkflowTaskState.IN_PROGRESS);
        int tasksBefore = tasks.size();
        
        // persist the form
        FormData data = new FormData();
        data.addFieldData("prop_bpm_workflowDescription", "This is a new adhoc task");
        data.addFieldData("assoc_bpm_assignee_added", personManager.get(USER_ONE).toString());
        data.addFieldData("assoc_packageItems_added", document.toString());
        
//        data.addFieldData("prop_bpm_workflowDueDate", new Date());
//        data.addFieldData("prop_bpm_workflowPriority", 1);
        
        // persist the data
        WorkflowInstance workflow = (WorkflowInstance)formService.saveForm(
                    new Item(WORKFLOW_FORM_ITEM_KIND, workflowDefName), data);
        
        // verify that the workflow was started by checking the user has one 
        // more task and the details on the workflow instance
        tasks = workflowService.getAssignedTasks(USER_ONE, WorkflowTaskState.IN_PROGRESS);
        int tasksAfter = tasks.size();
        assertTrue("Expecting there to be more tasks", tasksAfter > tasksBefore);
        
        // check workflow instance details
        assertEquals(workflowDefName, workflow.getDefinition().getName());
        
        // get the task form and verify data
        String taskId = tasks.get(0).getId();
        fields.clear();
        fields.add(taskIdName);
        fields.add("transitions");
        fields.add("message");
        fields.add("taskOwner");
        fields.add(packageItemsName);
        form = formService.getForm(new Item(TASK_FORM_ITEM_KIND, taskId), fields);
        
        FormData taskData = form.getFormData();
        assertEquals(taskId.substring(taskId.indexOf('$')+1), taskData.getFieldData("prop_bpm_taskId").getValue().toString());
        assertEquals(transitionLabels, taskData.getFieldData("prop_transitions").getValue());
        String expOwner = USER_ONE + "|" + personManager.getFirstName(USER_ONE) + "|" + personManager.getLastName(USER_ONE);
        assertEquals(expOwner, taskData.getFieldData("prop_taskOwner").getValue());
        assertEquals("This is a new adhoc task", taskData.getFieldData("prop_message").getValue());
        assertNotNull(taskData.getFieldData("assoc_packageItems").getValue());
        
        // update the first task in the users list
        String comment = "This is a comment";
        data = new FormData();
        data.addFieldData("prop_bpm_comment", comment);
        formService.saveForm(new Item(TASK_FORM_ITEM_KIND, taskId), data);
        
        // check the comment was updated
        WorkflowTask task = workflowService.getTaskById(taskId);
        String taskComment = (String)task.getProperties().get(WorkflowModel.PROP_COMMENT);
        assertEquals(comment, taskComment);
        
        // make sure unauthorized user can not update the task
        personManager.setUser(USER_TWO);
        
        try
        {
            // try and update task
            this.formService.saveForm(new Item(TASK_FORM_ITEM_KIND, taskId), data);
            
            fail("Task was updated by an unauthorized user");
        }
        catch (AccessDeniedException ade)
        {
            // expected
        }
    }
    
    public void testNoForm() throws Exception
    {
        // test that a form can not be retrieved for a non-existent item
        try
        {
            this.formService.getForm(new Item("Invalid Kind", "Invalid Id"));
            fail("Expecting getForm for 'Invalid Kind/Item' to fail");
        }
        catch (Exception e)
        {
            // expected
        }
        
        String missingNode = this.document.toString().replace("-", "x");
        
        try
        {
            this.formService.getForm(new Item(NODE_FORM_ITEM_KIND, missingNode));
            fail("Expecting getForm for a missing node to fail");
        }
        catch (FormNotFoundException fnne)
        {
            // expected
        }
        
        // test that a form can not be saved for a non-existent item
        try
        {
            this.formService.saveForm(new Item("Invalid Kind", "Invalid Id"), new FormData());
            fail("Expecting saveForm for 'Invalid Kind/Item' to fail");
        }
        catch (Exception e)
        {
            // expected
        }
        
        try
        {
            this.formService.saveForm(new Item(NODE_FORM_ITEM_KIND, missingNode), new FormData());
            fail("Expecting saveForm for a missing node to fail");
        }
        catch (Exception e)
        {
            // expected
        }
        
        
        // Tests to make sure that form processors are no longer decoding _ in the itemId
        
        try
        {
            FormData data = new FormData();
            data.addFieldData(AbstractFormProcessor.DESTINATION, this.folder.toString());
            this.formService.saveForm(new Item(TYPE_FORM_ITEM_KIND, "cm_content"), data);
            fail("Expecting saveForm for a 'type' item kind containing an underscore to fail");
        }
        catch (Exception e)
        {
            // expected
        }
        
        try
        {
            FormData data = new FormData();
            data.addFieldData("prop_bpm_workflowDescription", "This is a new adhoc task");
            data.addFieldData("assoc_bpm_assignee_added", 
                        this.personManager.get(USER_ONE).toString());
            data.addFieldData("assoc_packageItems_added", this.document.toString());
            this.formService.saveForm(new Item(WORKFLOW_FORM_ITEM_KIND, "jbpm$wf_adhoc"), data);
            fail("Expecting saveForm for a 'workflow' item kind containing an underscore to fail");
        }
        catch (Exception e)
        {
            // expected
        }
    }
    
    public void testFormData() throws Exception
    {
        FormData formData = new FormData();
        
        // test single value goes in and comes out successfully
        formData.addFieldData("singleValue", "one");
        assertEquals("Expecting value of 'one'", "one", formData.getFieldData("singleValue").getValue());
        
        // test adding multiple values to the same field
        formData.addFieldData("multipleValues", "one");
        
        Object value = formData.getFieldData("multipleValues").getValue();
        assertTrue("Expecting 'multipleValues' to be a String object", (value instanceof String));
        
        formData.addFieldData("multipleValues", "two");
        value = formData.getFieldData("multipleValues").getValue();
        assertTrue("Expecting 'multipleValues' to be a List object", (value instanceof List));
        
        formData.addFieldData("multipleValues", "three");
        List<?> list = (List<?>)formData.getFieldData("multipleValues").getValue();
        assertEquals("Expecting 'multipleValues' List to have 3 items", 3, list.size());
        
        // add a List initially then add a value to it
        formData.addFieldData("listValue", new ArrayList<Object>());
        formData.addFieldData("listValue", "one");
        formData.addFieldData("listValue", "two");
        list = (List<?>)formData.getFieldData("listValue").getValue();
        assertEquals("Expecting 'listValue' List to have 2 items", 2, list.size());
        
        // test overwrite parameter
        formData.addFieldData("overwritten", "one", true);
        formData.addFieldData("overwritten", "two", true);
        formData.addFieldData("overwritten", "three", true);
        value = formData.getFieldData("overwritten").getValue();
        assertTrue("Expecting 'overwritten' to be a String object", (value instanceof String));
        assertEquals("Expecting 'overwritten' value to be 'three'", "three", value);
    }
    
    public void testFormContext() throws Exception
    {
        Map<String, Object> context = new HashMap<String, Object>(2);
        context.put("nodeRef", this.folder);
        context.put("name", "Gavin Cornwell");
        
        Form form = this.formService.getForm(new Item(NODE_FORM_ITEM_KIND, this.document.toString()), context);
        assertNotNull(form);
    }
    
    public void testJavascriptAPI() throws Exception
    {
    	Map<String, Object> model = new HashMap<String, Object>();
    	model.put("testDoc", this.document.toString());
    	model.put("testDocName", this.documentName);
    	model.put("testAssociatedDoc", this.associatedDoc.toString());
    	model.put("folder", this.folder.toString());
    	model.put("folderName", this.folderName);
    	
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/forms/script/test_formService.js");
        this.scriptService.executeScript(location, model);
    }
}
