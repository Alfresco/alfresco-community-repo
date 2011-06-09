function main()
{
    // check that required data is present in request body
    if (json.has("itemKind") === false)
    {
        status.setCode(status.STATUS_BAD_REQUEST, "itemKind parameter is not present");
        return;
    }
    
    if (json.has("itemId") === false)
    {
        status.setCode(status.STATUS_BAD_REQUEST, "itemId parameter is not present");
        return;
    }

    // extract required data from request body
    var itemKind = json.get("itemKind");
    var itemId = json.get("itemId");
       
    if (logger.isLoggingEnabled())
    {
        logger.log("Generating form for item:");
        logger.log("\tkind = " + itemKind);
        logger.log("\tid = " + itemId);
    }
    
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
    
    // ensure there is a submission url
    var submissionUrl = formScriptObj.submissionUrl;
    if (submissionUrl === null)
    {
        // encode the item id and item kind using URI encoding scheme, however, the encoded / character
        // causes problems when posting back to Apache so change these back
        submissionUrl = '/api/' + encodeURIComponent(itemKind) + '/' + encodeURIComponent(itemId).replace(/%2f/g, "/") + '/formprocessor';
    }
    
    // create form model
    var formModel = 
    {
        item : formScriptObj.itemUrl,
        submissionUrl: submissionUrl,
        type : formScriptObj.itemType,
        fields : formScriptObj.fieldDefinitions,
        formData : {}
    };

    // populate the form data model
    for (var k in formScriptObj.formData.data)
    {
        var value = formScriptObj.formData.data[k].value;

        if (value instanceof java.util.Date)
        {
            formModel.formData[k] = utils.toISO8601(value);
        }
        // There is no need to handle java.util.List instances here as they are
        // returned from ScriptFormData.java as Strings
        else
        {
            formModel.formData[k] = value;
        }
    }
    
    if (logger.isLoggingEnabled())
        logger.log("formModel = " + jsonUtils.toJSONString(formModel));

    model.form = formModel;
}

main();
