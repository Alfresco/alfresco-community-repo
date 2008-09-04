script:
{
    // query type
    var typeId = url.templateArgs.typeId;
    model.typedef = cmis.queryType(typeId);
    if (model.typedef === null)
    {
        status.code = 404;
        status.message = "Type " + typeId + " not found";
        status.redirect = true;
        break script;   
    }

    // query type descendants    
    var page = paging.createPageOrWindow(args, headers);
    var paged = cmis.queryTypeHierarchy(model.typedef, true, page);
    model.results = paged.results;
    model.cursor = paged.cursor;

    // handle property definitions
    // TODO: spec issue 34
    var returnPropertyDefinitions = cmis.findArg(args.includePropertyDefinitions, headers["CMIS-includePropertyDefinitions"]);
    model.returnPropertyDefinitions = returnPropertyDefinitions == "true" ? true : false;
}
