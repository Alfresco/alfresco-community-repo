script:
{
    // extract type
    var typeId = args[cmisserver.ARG_TYPE_ID];
    if (typeId === null)
    {
        typeId = url.templateArgs[cmisserver.ARG_TYPE_ID];
    }
    
    // descend from root
    if (typeId === null)
    {
        var paged = cmisserver.queryTypeChildren(null, paging.createUnlimitedPage());
        model.basetypes = paged.results;
    }
    else
    {
        // query a specific type
        var typedef = cmisserver.queryType(typeId);
        if (typedef === null)
        {
            status.code = 404;
            status.message = "Type " + typeId + " not found";
            status.redirect = true;
            break script;
        }
        model.typedef = typedef;
    }

    // depth
    var depth = args[cmisserver.ARG_DEPTH];
    if (depth == 0)
    {
        status.code = 500;
        status.message = "Depth cannot be 0";
        status.redirect = true;
        break script;
    }
    model.depth = (depth === null) ? -1 : parseInt(depth);

    // handle property definitions
    var includePropertyDefinitions = args[cmisserver.ARG_INCLUDE_PROPERTY_DEFINITIONS];
    model.includePropertyDefinitions = includePropertyDefinitions == "true" ? true : false;
}