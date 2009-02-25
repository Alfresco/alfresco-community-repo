function main()
{
	// Extract template args
	var ta_storeType = url.templateArgs['store_type'];
	var ta_storeId = url.templateArgs['store_id'];
	var ta_id = url.templateArgs['id'];
	var ta_path = url.templateArgs['path'];
	
    if (logger.isLoggingEnabled())
    {
       logger.log("ta_storeType = " + ta_storeType);
       logger.log("ta_storeId = " + ta_storeId);
       logger.log("ta_id = " + ta_id);
       logger.log("ta_path = " + ta_path);
    }

	var formUrl = '';
	// The template argument 'path' only appears in the second URI template.
	if (ta_path != null)
	{
		//TODO Need to test this path.
		formUrl = ta_path;
	}
	else
	{
		formUrl = ta_storeType + '://' + ta_storeId + '/' + ta_id;
	}

    if (logger.isLoggingEnabled())
    {
       logger.log("formUrl = " + formUrl);
    }
    
    // Determine whether this is a standard GET or an application/x-www-form-urlencoded
    // GET-based submit.
    // In the former case, there will be an "alf_ticket" query parameter on the URL
    // and in the latter case there will also be a number of field name/values.
    //
    // TODO Is there a better way to recognise urlencoded submits than examining the
    //      names of the parameters?
    if (logger.isLoggingEnabled())
    {
       logger.log("url.args = " + url.args);
    }
    var isUrlEncodedSubmit = false;
    var repoFormData = new Packages.org.alfresco.repo.forms.FormData();
    
    var queryParams = url.args.split("&");
    for (var i = 0; i < queryParams.length; i++)
    {
       if (queryParams[i].startsWith("prop") ||
          queryParams[i].startsWith("assoc"))
       {
    	   isUrlEncodedSubmit = true;
    	   
    	   var singleQueryParam = queryParams[i].split("=");
     	   // Replace the first 2 underscores with colons.
     	   var alteredFieldName = singleQueryParam[0].replaceFirst("_", ":").replaceFirst("_", ":");
     	   var decodedFieldValue = Packages.java.net.URLDecoder.decode(singleQueryParam[1], "UTF-8");
     	   
           if (logger.isLoggingEnabled())
           {
     	      logger.log("Field: " + alteredFieldName + " = " + decodedFieldValue);
           }
     	   repoFormData.addData(alteredFieldName, decodedFieldValue);
       }
    }

    if (isUrlEncodedSubmit)
    {
    	var formScriptObj = formService.saveForm(formUrl, repoFormData);
    }
    else
    {
    	var formScriptObj = formService.getForm(formUrl);
    }
    
    
	
	if (formScriptObj == null)
	{
        var message = "The form for item \"" + formUrl + "\" could not be found.";
        if (logger.isWarnLoggingEnabled())
        {
           logger.warn(message);
        }
        status.setCode(404, message);
        return;
	}
	
    var formModel = {};
    formModel.data = {};

    formModel.data.item = '/api/node/' + ta_storeType + '/' + ta_storeId + '/' + ta_id;
    formModel.data.submissionUrl = '/api/forms/node/' + ta_storeType + '/' + ta_storeId + '/' + ta_id;
    formModel.data.type = formScriptObj.type;
    
    formModel.data.definition = {};
    formModel.data.definition.fields = {};
    for (var fieldName in formScriptObj.fieldDefinitionData)
    {
    	// We're explicitly listing the object fields of FieldDefinition.java and its
    	// subclasses here.
    	// I don't see a way to get these dynamically at runtime.
    	var supportedBaseFieldNames = ['name', 'label', 'description', 'binding',
 	                               'defaultValue', 'group', 'protectedField'];
    	var supportedPropertyFieldNames = ['dataType', 'mandatory',
    	                                   'repeats', 'constraints'];
    	var supportedAssociationFieldNames = ['endpointType', 'endpointDirection',
    	                                      'endpointMandatory', 'endpointMany'];
    	
    	var allSupportedFieldNames = supportedBaseFieldNames
    	    .concat(supportedPropertyFieldNames)
    	    .concat(supportedAssociationFieldNames);
    	
    	formModel.data.definition.fields[fieldName] = {};
    	for (var i = 0; i < allSupportedFieldNames.length; i++) {
    		var nextSupportedName = allSupportedFieldNames[i];
    		var nextValue = formScriptObj.fieldDefinitionData[fieldName][nextSupportedName];
    		
    		if (nextValue != null) {
    			formModel.data.definition.fields[fieldName][nextSupportedName] = nextValue;
    		}
    	}
    	
    	// Special handling for the 'type' property
    	// For now, this can have a value of 'property' or 'association'
    	
    	//TODO Temporary impl here.
    	if (formModel.data.definition.fields[fieldName]['dataType'] != null)
    	{
    		formModel.data.definition.fields[fieldName]['type'] = 'property';
    	}
    	else
    	{
    		formModel.data.definition.fields[fieldName]['type'] = 'association';
    	}
    }

    formModel.data.formData = {};
    for (var k in formScriptObj.formData.data)
    {
        var value = formScriptObj.formData.data[k].value;

        if (value instanceof java.util.Date)
        {
            formModel.data.formData[k.replace(/:/g, "_")] = utils.toISO8601(value);
        }
        else
        {
            formModel.data.formData[k.replace(/:/g, "_")] = value;
        }
    }

    model.form = formModel;
}

main();
