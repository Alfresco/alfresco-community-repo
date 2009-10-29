script:
{
    var page = paging.createPageOrWindow(args);
    var typeId = args[cmis.ARG_TYPE_ID];
    if (typeId === null)
    {
        typeId = url.templateArgs[cmis.ARG_TYPE_ID];
    }
    if (typeId === null)
    {
        // query for base types
        var paged = cmis.queryTypeChildren(null, page);
        model.results = paged.results;
        model.cursor = paged.cursor;
        model.type = "base";
    }
    else
    {
        // query a specific type and its children
        var typedef = cmis.queryType(typeId);
        if (typedef === null)
        {
            status.code = 404;
            status.message = "Type " + typeId + " not found";
            status.redirect = true;
            break script;
        }
        var paged = cmis.queryTypeChildren(typedef, page);
        model.results = paged.results;
        model.cursor = paged.cursor;
        model.type = typeId;
    }

    // handle property definitions
    var returnPropertyDefinitions = args[cmis.ARG_INCLUDE_PROPERTY_DEFINITIONS];
    model.returnPropertyDefinitions = returnPropertyDefinitions == "true" ? true : false;
}