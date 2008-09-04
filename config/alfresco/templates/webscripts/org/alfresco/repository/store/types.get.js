script:
{
    // process paging
    var page = paging.createPageOrWindow(args, headers);
        
    // query types
    // TODO: spec issue 34
    var typeId = cmis.findArg(args.type, headers["CMIS-type"]);
    if (typeId === null)
    {
        // query all types
        var paged = cmis.queryTypes(page);
        model.results = paged.results;
        model.cursor = paged.cursor;
        model.type = "all";
    }
    else
    {
        // query a specific type and its descendants
        var typedef = cmis.queryType(typeId);
	    if (typedef === null)
	    {
	        status.code = 404;
	        status.message = "Type " + typeId + " not found";
	        status.redirect = true;
	        break script;   
	    }
        var paged = cmis.queryTypeHierarchy(typedef, true, page);
        model.results = paged.results;
        model.cursor = paged.cursor;
        model.type = typeId;
    }

    // handle property definitions
    // TODO: spec issue 34
    var returnPropertyDefinitions = cmis.findArg(args.includePropertyDefinitions, headers["CMIS-includePropertyDefinitions"]);
    model.returnPropertyDefinitions = returnPropertyDefinitions == "true" ? true : false;
}
