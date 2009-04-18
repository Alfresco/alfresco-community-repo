function main()
{
    // Extract template args
    var itemKind = url.templateArgs['item_kind'];
    var itemId = url.templateArgs['item_id'];

    if (logger.isLoggingEnabled())
    {
        logger.log("itemKind = " + itemKind);
        logger.log("itemId = " + itemId);
    }
    
    // TODO: Return error if item kind and/or id is missing?

    // extract optional data from request body (if present)
    var count = 0;
    var fields = null; 
    if (json.has("fields"))
    {
       // convert the JSONArray object into a native JavaScript array
       fields = [];
       var jsonFields = json.get("fields");
       var numFields = jsonFields.length();
       for (count = 0; count < numFields; count++)
       {
          fields.push(jsonFields.get(count));
       }
       
       if (logger.isLoggingEnabled())
           logger.log("fields = " + fields);
    }
    
    var forcedFields = null;
    if (json.has("force"))
    {
        // convert the JSONArray object into a native JavaScript array
        forcedFields = [];
        var jsonForcedFields = json.get("force");
        var numForcedFields = jsonForcedFields.length();
        for (count = 0; count < numForcedFields; count++)
        {
           forcedFields.push(jsonForcedFields.get(count));
        }
        
        if (logger.isLoggingEnabled())
            logger.log("forcedFields = " + forcedFields);
    }
    
    var formScriptObj = null;
    
    try
    {
        // attempt to get the form for the item
        formScriptObj = formService.getForm(itemKind, itemId, fields, forcedFields);
    }
    catch (error)
    {
        var msg = error.message;
        
        if (logger.isLoggingEnabled())
            logger.log(msg);
        
        // determine if the exception was a FormNotFoundException, if so return
        // 404 status code otherwise return 500
        if (msg.indexOf("FormNotFoundException") != -1)
        {
            status.setCode(404, msg);
           
            if (logger.isLoggingEnabled())
                logger.log("Returning 404 status code");
        }
        else
        {
            status.setCode(500, msg);
           
            if (logger.isLoggingEnabled())
                logger.log("Returning 500 status code");
        }
        
        return;
    }
    
    var formModel = {};
    formModel.data = {};

    // TODO: retrieve the item URL from the response?
    formModel.data.item = '/api/form/definition/' + itemKind + '/' + itemId;
    // TODO: look for overridden submission url
    formModel.data.submissionUrl = '/api/form/' + itemKind + '/' + itemId;
    formModel.data.type = formScriptObj.type;
    
    formModel.data.definition = {};
    formModel.data.definition.fields = [];
    
    // We're explicitly listing the object fields of FieldDefinition.java and its subclasses here.
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

    var fieldDefs = formScriptObj.fieldDefinitions;
    for (var x = 0; x < fieldDefs.length; x++)
    {
        var fieldDef = fieldDefs[x];
        var field = {};

        for (var i = 0; i < allSupportedFieldNames.length; i++) 
        {
            var nextSupportedName = allSupportedFieldNames[i];
            var nextValue = fieldDef[nextSupportedName];

            if (nextValue != null) 
            {
                field[nextSupportedName] = nextValue;
            }
        }

        field.type = (fieldDef.dataType != null) ? "property" : "association";
        formModel.data.definition.fields.push(field);
    }

    formModel.data.formData = {};
    for (var k in formScriptObj.formData.data)
    {
        var value = formScriptObj.formData.data[k].value;

        if (value instanceof java.util.Date)
        {
            formModel.data.formData[k.replace(/:/g, "_")] = utils.toISO8601(value);
        }
        // There is no need to handle java.util.List instances here as they are
        // returned from ScriptFormData.java as Strings
        else
        {
            formModel.data.formData[k.replace(/:/g, "_")] = value;
        }
    }

    model.form = formModel;
}

main();
