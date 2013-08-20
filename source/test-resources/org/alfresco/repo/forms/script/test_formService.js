function testGetFormForNonExistentContentNode()
{
   // Replace all the digits in the ID with an 'x'.
   // Surely that node will not exist...
   var corruptedTestDoc = testDoc.replace(/\d/g, "x");
   var form = null;
   
   try
   {
      form = formService.getForm("node", corruptedTestDoc);
   }
   catch (e)
   {
      // expected
   }
   
   test.assertNull(form, "Form should have not been found: " + testDoc);
}

function testGetFormForContentNode()
{
    // Get a known form and check its various attributes/properties.
    var form = formService.getForm("node", testDoc);
    test.assertNotNull(form, "Form should have been found for: " + testDoc);

    test.assertEquals("node", form.itemKind);
    test.assertEquals(testDoc, form.itemId);
    test.assertEquals('cm:content', form.itemType);
    test.assertEquals('/api/node/' + testDoc.replace(":/", ""), form.itemUrl);

    test.assertNull(form.submissionUrl, "form.submissionUrl should be null.");

    var fieldDefs = form.fieldDefinitions;
    test.assertNotNull(fieldDefs, "field definitions should not be null.");
    test.assertEquals(19, fieldDefs.length);

    // as we know there are no duplicates we can safely create a map of the 
    // field definitions for the purposes of this test
    var fieldDefnDataHash = {};
    var fieldDef = null;
    for (var x = 0; x < fieldDefs.length; x++)
    {
       fieldDef = fieldDefs[x];
       fieldDefnDataHash[fieldDef.name] = fieldDef;
    }

    var nameField = fieldDefnDataHash['cm:name'];
    var titleField = fieldDefnDataHash['cm:title'];
    var descField = fieldDefnDataHash['cm:description'];
    var originatorField = fieldDefnDataHash['cm:originator'];
    var addresseeField = fieldDefnDataHash['cm:addressee'];
    var addresseesField = fieldDefnDataHash['cm:addressees'];
    var subjectField = fieldDefnDataHash['cm:subjectline'];
    var sentDateField = fieldDefnDataHash['cm:sentdate'];
    var referencesField = fieldDefnDataHash['cm:references'];

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
    test.assertEquals("Name", nameField.label);
    test.assertEquals("Title", titleField.label);
    test.assertEquals("Description", descField.label);
    test.assertEquals("Originator", originatorField.label);
    test.assertEquals("Addressee", addresseeField.label);
    test.assertEquals("Addressees", addresseesField.label);
    test.assertEquals("Subject", subjectField.label);
    test.assertEquals("Sent Date", sentDateField.label);
    test.assertEquals("References", referencesField.label);
    
    // check details of name field
    test.assertEquals("text", nameField.dataType);
    test.assertTrue(nameField.mandatory);
    // Expecting cm:name to be single-valued.
    test.assertFalse(nameField.repeating, "nameField.repeating was not false.");
    
    // get the constraint for the name field and check
    var constraints = nameField.constraints;
    test.assertEquals(1, constraints.size());
    var constraint = constraints.get(0);
    test.assertEquals("REGEX", constraint.type);
    var params = constraint.parameters;
    test.assertNotNull(params, "params should not be null.");
    test.assertEquals(2, params.length);
    test.assertNotNull(params["expression"], "params['expression'] should not be null.");
    test.assertNotNull(params["requiresMatch"], "params['requiresMatch'] should not be null.");
    
    // check details of the addressees field
    test.assertEquals("text", addresseesField.dataType);
    test.assertFalse(addresseesField.mandatory, "addresseesField.mandatory was not false.");
    // Expecting cm:addressees to be multi-valued.
    test.assertTrue(addresseesField.repeating);
  
    // check the details of the association field
    test.assertEquals("cm:content", referencesField.endpointType);
    
    //TODO A raw comparison fails. Is this a JS vs. Java string?
    test.assertEquals("TARGET", "" + referencesField.endpointDirection);
    test.assertFalse(referencesField.endpointMandatory, "referencesField.endpointMandatory was not false.");
    test.assertTrue(referencesField.endpointMany, "referencesField.endpointMany was not true.");
    
    // check the form data
    var formData = form.formData;
    test.assertNotNull(formData, "formData should not be null.");
    var fieldData = formData.data;
    test.assertNotNull(fieldData, "fieldData should not be null.");
    test.assertNotNull(fieldData.length, "fieldData.length should not be null.");
    
    test.assertEquals(testDocName, fieldData[nameField.dataKeyName].value);
    test.assertEquals("This is the title for the test document", fieldData[titleField.dataKeyName].value);
    test.assertEquals("This is the description for the test document", fieldData[descField.dataKeyName].value);
    test.assertEquals("fred@customer.com", fieldData[originatorField.dataKeyName].value);
    test.assertEquals("bill@example.com", fieldData[addresseeField.dataKeyName].value);
    test.assertEquals("The subject is...", fieldData[subjectField.dataKeyName].value);
    
    var addressees = fieldData[addresseesField.dataKeyName].value;
    test.assertNotNull(addressees);
    test.assertTrue(addressees.indexOf(",") != -1);
    var addresseesArr = addressees.split(",");
    test.assertEquals(2, addresseesArr.length);
    test.assertEquals("harry@example.com", addresseesArr[0]);
    test.assertEquals("jane@example.com", addresseesArr[1]);

    var sentDate = fieldData[sentDateField.dataKeyName].getValue();
    test.assertTrue((typeof sentDate === "object"), "Expecting sentData to be an object");
    var month = sentDate.getMonth();
    test.assertTrue((month >= 0 && month < 12), "Expecting valid month");
    
    var targets = fieldData[referencesField.dataKeyName].value;
    
    test.assertNotNull(targets, "targets should not be null.");
    test.assertEquals(testAssociatedDoc, targets);
}

function testGetFormForFolderNode()
{
    // Get a known form and check its various attributes/properties.
    var form = formService.getForm("node", folder);
    test.assertNotNull(form, "Form should have been found for: " + testDoc);

    test.assertEquals("node", form.itemKind);
    test.assertEquals(folder, form.itemId);
    test.assertEquals('cm:folder', form.itemType);
    test.assertEquals('/api/node/' + folder.replace(":/", ""), form.itemUrl);

    test.assertNull(form.submissionUrl, "form.submissionUrl should be null.");

    var fieldDefs = form.fieldDefinitions;
    test.assertNotNull(fieldDefs, "field definitions should not be null.");
    test.assertEquals(8, fieldDefs.length);

    // as we know there are no duplicates we can safely create a map of the 
    // field definitions for the purposes of this test
    var fieldDefnDataHash = {};
    var fieldDef = null;
    for (var x = 0; x < fieldDefs.length; x++)
    {
        fieldDef = fieldDefs[x];
        fieldDefnDataHash[fieldDef.name] = fieldDef;
    }

    var nameField = fieldDefnDataHash['cm:name'];
    var containsField = fieldDefnDataHash['cm:contains'];

    test.assertNotNull(nameField, "Expecting to find the cm:name field");
    test.assertNotNull(containsField, "Expecting to find the cm:contains field");

    // check the labels of all the fields
    test.assertEquals("Name", nameField.label);
    test.assertEquals("Contains", containsField.label);
 
    // check details of name field
    test.assertEquals("text", nameField.dataType);
    test.assertTrue(nameField.mandatory);
    // Expecting cm:name to be single-valued.
    test.assertFalse(nameField.repeating, "nameField.repeating was not false.");
 
    // compare the association details
    test.assertEquals("TARGET", "" + containsField.endpointDirection);
    test.assertFalse(containsField.endpointMandatory, "containsField.endpointMandatory was not false.");
    test.assertTrue(containsField.endpointMany, "constainsField.endpointMany was not true.");
 
    // check the form data
    var formData = form.formData;
    test.assertNotNull(formData, "formData should not be null.");
    var fieldData = formData.data;
    test.assertNotNull(fieldData, "fieldData should not be null.");
    test.assertNotNull(fieldData.length, "fieldData.length should not be null.");
 
    test.assertEquals(folderName, fieldData[nameField.dataKeyName].value);
    var children = fieldData[containsField.dataKeyName].value;
    test.assertNotNull(children, "children should not be null.");
    var childrenArr = children.split(",");
    test.assertTrue(childrenArr.length == 3, "Expecting there to be 3 children");
}

function testGetFormWithSelectedFields()
{
    // Define list of fields to retrieve
    var fields = [];
    fields.push("cm:name");
    fields.push("cm:title");
    fields.push("mimetype");
    fields.push("cm:modified");
    // add field that will be missing
    fields.push("cm:author");
    
    // Get a a form for the given fields
    var form = formService.getForm("node", testDoc, fields);
    test.assertNotNull(form, "Form should have been found for: " + testDoc);
    
    var fieldDefs = form.fieldDefinitions;
    test.assertNotNull(fieldDefs, "field definitions should not be null.");
    test.assertEquals(4, fieldDefs.length);

    // as we know there are no duplicates we can safely create a map of the 
    // field definitions for the purposes of this test
    var fieldDefnDataHash = {};
    var fieldDef = null;
    for (var x = 0; x < fieldDefs.length; x++)
    {
        fieldDef = fieldDefs[x];
        fieldDefnDataHash[fieldDef.name] = fieldDef;
    }

    var nameField = fieldDefnDataHash['cm:name'];
    var titleField = fieldDefnDataHash['cm:title'];
    var mimetypeField = fieldDefnDataHash['mimetype'];
    var modifiedField = fieldDefnDataHash['cm:modified'];
    var authorField = fieldDefnDataHash['cm:author'];

    test.assertNotNull(nameField, "Expecting to find the cm:name field");
    test.assertNotNull(titleField, "Expecting to find the cm:title field");
    test.assertNotNull(mimetypeField, "Expecting to find the mimetype field");
    test.assertNotNull(modifiedField, "Expecting to find the cm:modified field");
    test.assertTrue(authorField === undefined, "Expecting cm:author field to be missing");

    // check the labels of all the fields
    test.assertEquals("Name", nameField.label);
    test.assertEquals("Title", titleField.label);
    test.assertEquals("Mimetype", mimetypeField.label);
    test.assertEquals("Modified Date", modifiedField.label);
    
    // check the form data
    var formData = form.formData;
    test.assertNotNull(formData, "formData should not be null.");
    var fieldData = formData.data;
    test.assertNotNull(fieldData, "fieldData should not be null.");
    test.assertNotNull(fieldData.length, "fieldData.length should not be null.");
 
    test.assertEquals(testDocName, fieldData[nameField.dataKeyName].value);
    test.assertEquals("This is the title for the test document", fieldData[titleField.dataKeyName].value);
}

function testGetFormWithForcedFields()
{
    // Define list of fields to retrieve
    var fields = [];
    fields.push("cm:name");
    fields.push("cm:title");
    fields.push("mimetype");
    fields.push("cm:modified");
    fields.push("cm:author");
    fields.push("cm:wrong");
    
    // define a list of fields to force
    var force = [];
    force.push("cm:author");
    force.push("cm:wrong");
    
    // Get a a form for the given fields
    var form = formService.getForm("node", testDoc, fields, force);
    test.assertNotNull(form, "Form should have been found for: " + testDoc);
    
    var fieldDefs = form.fieldDefinitions;
    test.assertNotNull(fieldDefs, "field definitions should not be null.");
    test.assertEquals(5, fieldDefs.length);

    // as we know there are no duplicates we can safely create a map of the 
    // field definitions for the purposes of this test
    var fieldDefnDataHash = {};
    var fieldDef = null;
    for (var x = 0; x < fieldDefs.length; x++)
    {
        fieldDef = fieldDefs[x];
        fieldDefnDataHash[fieldDef.name] = fieldDef;
    }

    var nameField = fieldDefnDataHash['cm:name'];
    var titleField = fieldDefnDataHash['cm:title'];
    var mimetypeField = fieldDefnDataHash['mimetype'];
    var modifiedField = fieldDefnDataHash['cm:modified'];
    var authorField = fieldDefnDataHash['cm:author'];
    var wrongField = fieldDefnDataHash['cm:wrong'];

    test.assertNotNull(nameField, "Expecting to find the cm:name field");
    test.assertNotNull(titleField, "Expecting to find the cm:title field");
    test.assertNotNull(mimetypeField, "Expecting to find the mimetype field");
    test.assertNotNull(modifiedField, "Expecting to find the cm:modified field");
    test.assertNotNull(authorField, "Expecting to find the cm:author field");
    test.assertTrue(wrongField === undefined, "Expecting cm:wrong field to be missing");

    // check the labels of all the fields
    test.assertEquals("Name", nameField.label);
    test.assertEquals("Title", titleField.label);
    test.assertEquals("Mimetype", mimetypeField.label);
    test.assertEquals("Modified Date", modifiedField.label);
    test.assertEquals("Author", authorField.label);
    
    // check the form data
    var formData = form.formData;
    test.assertNotNull(formData, "formData should not be null.");
    var fieldData = formData.data;
    test.assertNotNull(fieldData, "fieldData should not be null.");
    test.assertNotNull(fieldData.length, "fieldData.length should not be null.");
 
    test.assertEquals(testDocName, fieldData[nameField.dataKeyName].value);
    test.assertEquals("This is the title for the test document", fieldData[titleField.dataKeyName].value);
    test.assertNull(fieldData[authorField.dataKeyName], "Expecting cm:author to be null");
}

// Execute tests
testGetFormForNonExistentContentNode();
testGetFormForContentNode();
testGetFormForFolderNode();
testGetFormWithSelectedFields();
testGetFormWithForcedFields();
