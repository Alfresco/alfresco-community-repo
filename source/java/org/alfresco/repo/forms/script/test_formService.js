function testGetFormForContentNode()
{
	// Get a known form and check its various attributes/properties.
	var form = formService.getForm(testDoc);
	test.assertNotNull(form, "Form should have been found: " + testDoc);
	
	test.assertEquals(testDoc, form.getItem());
	
	test.assertEquals('cm:content', form.getType());
	
	test.assertNull(form.getFieldGroups());
	
	var fieldDefs = form.getFieldDefinitions();
	test.assertNotNull(fieldDefs);
	test.assertEquals(19, fieldDefs.size());
	
	var mappedFields = new Array();
	for (var i = 0; i < fieldDefs.size(); i++)
	{
		mappedFields[fieldDefs.get(i).getName()] = fieldDefs.get(i);
	}
	var nameField = mappedFields['cm:name'];
    var titleField = mappedFields['cm:title'];
    var descField = mappedFields['cm:description'];
    var originatorField = mappedFields['cm:originator'];
    var addresseeField = mappedFields['cm:addressee'];
    var addresseesField = mappedFields['cm:addressees'];
    var subjectField = mappedFields['cm:subjectline'];
    var sentDateField = mappedFields['cm:sentdate'];
    var referencesField = mappedFields['cm:references'];

    test.assertNotNull(nameField, "Expecting to find the cm:name field");
    test.assertNotNull(titleField, "Expecting to find the cm:title field");
    test.assertNotNull(descField, "Expecting to find the cm:description field");
    test.assertNotNull(originatorField, "Expecting to find the cm:originator field");
    test.assertNotNull(addresseeField, "Expecting to find the cm:addressee field");
    test.assertNotNull(addresseesField, "Expecting to find the cm:addressees field");
    test.assertNotNull(subjectField, "Expecting to find the cm:subjectline field");
    test.assertNotNull(sentDateField, "Expecting to find the cm:sentdate field");
    test.assertNotNull(referencesField, "Expecting to find the cm:references field");

    // check the labels of all the fields
    test.assertEquals("Name", nameField.getLabel());
    test.assertEquals("Title", titleField.getLabel());
    test.assertEquals("Description", descField.getLabel());
    test.assertEquals("Originator", originatorField.getLabel());
    test.assertEquals("Addressee", addresseeField.getLabel());
    test.assertEquals("Addressees", addresseesField.getLabel());
    test.assertEquals("Subject", subjectField.getLabel());
    test.assertEquals("Sent Date", sentDateField.getLabel());
    test.assertEquals("References", referencesField.getLabel());
    
    // check details of name field
    test.assertEquals("d:text", nameField.getDataType());
    test.assertTrue(nameField.isMandatory());
    // Expecting cm:name to be single-valued.
    test.assertFalse(nameField.isRepeating());
	
    // get the constraint for the name field and check
    var constraints = nameField.getConstraints();
    test.assertEquals(1, constraints.size());
    var constraint = constraints.get(0);
    test.assertEquals("REGEX", constraint.getType());
    var params = constraint.getParams();
    test.assertNotNull(params);
    test.assertEquals(2, params.length);
    test.assertNotNull(params["expression"]);
    test.assertNotNull(params["requiresMatch"]);
    
    // check details of the addressees field
    test.assertEquals("d:text", addresseesField.getDataType());
    test.assertFalse(addresseesField.isMandatory());
    // Expecting cm:addressees to be multi-valued.
    test.assertTrue(addresseesField.isRepeating());
    test.assertNull(addresseesField.getConstraints());
    
    // check the details of the association field
    test.assertEquals("cm:content", referencesField.getEndpointType());
    //TODO Method name typo here "Enpoint"
    test.assertEquals("TARGET", referencesField.getEnpointDirection().toString());
    test.assertFalse(referencesField.isEndpointMandatory());
    test.assertTrue(referencesField.isEndpointMany());
    
    // check the form data
    var formData = form.getFormData();
    test.assertNotNull(formData);
    var fieldData = formData.getData();
    test.assertEquals("This is the title for the test document", fieldData["cm:title"].getValue());
    test.assertEquals("This is the description for the test document", fieldData["cm:description"].getValue());
    test.assertEquals("fred@customer.com", fieldData["cm:originator"].getValue());
    test.assertEquals("bill@example.com", fieldData["cm:addressee"].getValue());
    test.assertEquals("harry@example.com", fieldData["cm:addressees_0"].getValue());
    test.assertEquals("jane@example.com", fieldData["cm:addressees_1"].getValue());
    test.assertEquals("The subject is...", fieldData["cm:subjectline"].getValue());

    //TODO Might add the equivalent of the VALUE_SENT_DATE testing here.
    // In the meantime I'll use JavaScript's own Date object to assert that it is a valid date.
    var sentDate = fieldData["cm:sentdate"].getValue();
    test.assertFalse(isNaN(Date.parse(sentDate)));
    
    var targets = fieldData["cm:references"].getValue();
    test.assertEquals(1, targets.size());
    test.assertEquals(testAssociatedDoc, targets.get(0));
}

// Execute tests
testGetFormForContentNode();