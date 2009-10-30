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
        model.typedef = null;
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
        model.typedef = typedef;
    }

    // handle property definitions
    var includePropertyDefinitions = args[cmis.ARG_INCLUDE_PROPERTY_DEFINITIONS];
    model.includePropertyDefinitions = includePropertyDefinitions == "true" ? true : false;
}