/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.AssociationFieldDefinition.Direction;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;

/**
 * Form service implementation unit test.
 * 
 * @author Gavin Cornwell
 */
public class FormServiceImplTest extends BaseAlfrescoSpringTest 
{
    private FormService formService;
    private NamespaceService namespaceService;
    private NodeRef document;
    private NodeRef associatedDoc;
    
    private static String VALUE_TITLE = "This is the title for the test document";
    private static String VALUE_DESCRIPTION = "This is the description for the test document";
    private static String VALUE_ORIGINATOR = "fred@customer.com";
    private static String VALUE_ADDRESSEE = "bill@example.com";
    private static String VALUE_ADDRESSEES1 = "harry@example.com";
    private static String VALUE_ADDRESSEES2 = "jane@example.com";
    private static String VALUE_SUBJECT = "The subject is...";
    private static Date VALUE_SENT_DATE = new Date();
    
    private static String LABEL_NAME = "Name";
    private static String LABEL_TITLE = "Title";
    private static String LABEL_DESCRIPTION = "Description";
    private static String LABEL_ORIGINATOR = "Originator";
    private static String LABEL_ADDRESSEE = "Addressee";
    private static String LABEL_ADDRESSEES = "Addressees";
    private static String LABEL_SUBJECT = "Subject";
    private static String LABEL_SENT_DATE = "Sent Date";
    private static String LABEL_REFERENCES = "References";
    
    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the required services
        this.formService = (FormService)this.applicationContext.getBean("FormService");
        this.namespaceService = (NamespaceService)this.applicationContext.getBean("NamespaceService");
        
        // Authenticate as the system user
        AuthenticationComponent authenticationComponent = (AuthenticationComponent) this.applicationContext
                .getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();
        
        String guid = GUID.generate();
        
        NodeRef rootNode = this.nodeService.getRootNode(
                    new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder" + guid);
        NodeRef folder = this.nodeService.createNode(
                rootNode, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder" + guid),
                ContentModel.TYPE_FOLDER,
                folderProps).getChildRef();
        
        // Create a node
        Map<QName, Serializable> docProps = new HashMap<QName, Serializable>(1);
        docProps.put(ContentModel.PROP_NAME, "testDocument" + guid + ".txt");
        this.document = this.nodeService.createNode(
                folder, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testDocument" + guid + ".txt"), 
                ContentModel.TYPE_CONTENT,
                docProps).getChildRef();    
       
        // create a node to use as target of association
        docProps.put(ContentModel.PROP_NAME, "associatedDocument" + guid + ".txt");
        this.associatedDoc = this.nodeService.createNode(
                    folder, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "associatedDocument" + guid + ".txt"), 
                    ContentModel.TYPE_CONTENT,
                    docProps).getChildRef();
        
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
        this.nodeService.addAspect(this.document, ContentModel.ASPECT_MAILED, aspectProps);
        
        // add referencing aspect (has association)
        aspectProps.clear();
        this.nodeService.addAspect(document, ContentModel.ASPECT_REFERENCING, aspectProps);
        this.nodeService.createAssociation(this.document, this.associatedDoc, ContentModel.ASSOC_REFERENCES);
        
        setComplete();
        endTransaction();
    }
    
    public void testGetForm() throws Exception
    {
        Form form = this.formService.getForm(this.document.toString());
        
        // check a form got returned
        assertNotNull("Expecting form to be present", form);
        
        // check item identifier matches
        assertEquals(this.document.toString(), form.getItem());
        
        // check the type is correct
        assertEquals(ContentModel.TYPE_CONTENT.toPrefixString(this.namespaceService), 
                    form.getType());
        
        // check there is no group info
        assertNull("Expecting the form groups to be null!", form.getFieldGroups());
        
        // check the field definitions
        Collection<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertNotNull("Expecting to find fields", fieldDefs);
        assertEquals("Expecting to find 19 fields", 19, fieldDefs.size());
        
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
        assertNotNull("Expecting to find the cm:originator field", originatorField);
        assertNotNull("Expecting to find the cm:addressee field", addresseeField);
        assertNotNull("Expecting to find the cm:addressees field", addresseesField);
        assertNotNull("Expecting to find the cm:subjectline field", subjectField);
        assertNotNull("Expecting to find the cm:sentdate field", sentDateField);
        assertNotNull("Expecting to find the cm:references field", referencesField);
        
        // check the labels of all the fields
        assertEquals("Expecting cm:name label to be " + LABEL_NAME, 
                    LABEL_NAME, nameField.getLabel());
        assertEquals("Expecting cm:title label to be " + LABEL_TITLE, 
                    LABEL_TITLE, titleField.getLabel());
        assertEquals("Expecting cm:description label to be " + LABEL_DESCRIPTION, 
                    LABEL_DESCRIPTION, descField.getLabel());
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
        assertEquals("Expecting cm:name type to be d:text", "d:text", nameField.getDataType());
        assertTrue("Expecting cm:name to be mandatory", nameField.isMandatory());
        assertFalse("Expecting cm:name to be single valued", nameField.isRepeating());
        
        // get the constraint for the name field and check
        List<FieldConstraint> constraints = nameField.getConstraints();
        assertEquals("Expecting 1 constraint for cm:name", 1, constraints.size());
        FieldConstraint constraint = constraints.get(0);
        assertEquals("Expecting name of constraint to be 'REGEX'", "REGEX", constraint.getType());
        Map<String, String> params = constraint.getParams();
        assertNotNull("Expecting constraint parameters", params);
        assertEquals("Expecting 2 constraint parameters", 2, params.size());
        assertNotNull("Expecting an 'expression' constraint parameter", params.get("expression"));
        assertNotNull("Expecting an 'requiresMatch' constraint parameter", params.get("requiresMatch"));
        
        // check details of the addressees field
        assertEquals("Expecting cm:addressees type to be d:text", "d:text", addresseesField.getDataType());
        assertFalse("Expecting cm:addressees to be mandatory", addresseesField.isMandatory());
        assertTrue("Expecting cm:addressees to be multi valued", addresseesField.isRepeating());
        assertNull("Expecting constraints for cm:addressees to be null", addresseesField.getConstraints());
        
        // check the details of the association field
        assertEquals("Expecting cm:references endpoint type to be cm:content", "cm:content", 
                    referencesField.getEndpointType());
        assertEquals("Expecting cm:references endpoint direction to be TARGET", 
                    Direction.TARGET.toString(),
                    referencesField.getEnpointDirection().toString());
        assertFalse("Expecting cm:references endpoint to be optional", 
                    referencesField.isEndpointMandatory());
        assertTrue("Expecting cm:references endpoint to be 1 to many",
                    referencesField.isEndpointMany());
        
        // check the form data
        FormData data = form.getFormData();
        assertNotNull("Expecting form data", data);
        Map<String, FormData.FieldData> fieldData = data.getData();
        assertEquals(VALUE_TITLE, fieldData.get("cm:title").getValue());
        assertEquals(VALUE_DESCRIPTION, fieldData.get("cm:description").getValue());
        assertEquals(VALUE_ORIGINATOR, fieldData.get("cm:originator").getValue());
        assertEquals(VALUE_ADDRESSEE, fieldData.get("cm:addressee").getValue());
        assertEquals(VALUE_ADDRESSEES1, fieldData.get("cm:addressees_0").getValue());
        assertEquals(VALUE_ADDRESSEES2, fieldData.get("cm:addressees_1").getValue());
        assertEquals(VALUE_SUBJECT, fieldData.get("cm:subjectline").getValue());
        
        Calendar calTestValue = Calendar.getInstance();
        calTestValue.setTime(VALUE_SENT_DATE);
        Calendar calServiceValue = Calendar.getInstance();
        calServiceValue.setTime((Date)fieldData.get("cm:sentdate").getValue());
        assertEquals(calTestValue.getTimeInMillis(), calServiceValue.getTimeInMillis());
        
        List<String> targets = (List<String>)fieldData.get("cm:references").getValue();
        assertEquals("Expecting 1 target", 1, targets.size());
        assertEquals(this.associatedDoc.toString(), targets.get(0));
    }
    
    // == Test the JavaScript API ==
    
//    public void testJSAPI() throws Exception
//    {
//        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/forms/script/test_formService.js");
//        this.scriptService.executeScript(location, new HashMap<String, Object>(0));
//    }
}
