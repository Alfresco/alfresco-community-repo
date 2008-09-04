script:
{
    // locate node
    var pathSegments = url.match.split("/");
    var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
    model.node = cmis.findNode(pathSegments[2], reference);
    if (model.node === null)
    {
        status.code = 404;
        status.message = "Repository " + pathSegments[2] + " " + reference.join("/") + " not found";
        status.redirect = true;
        break script;
    }
 
    // handle filters
    model.types = cmis.findArg(args.types, headers["CMIS-types"]) === null ? cmis.defaultTypesFilter : cmis.findArg(args.types, headers["CMIS-types"]);
    if (!cmis.isValidTypesFilter(model.types))
    {
        status.code = 400;
        status.message = "Types filter '" + model.types + "' unknown";
        status.redirect = true;
        break script;
    }
    
    // property filter 
    model.filter = cmis.findArg(args.filter, headers["CMIS-filter"]);
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
    // depth
    var depth = cmis.findArg(args.depth, headers["CMIS-depth"]);
    model.depth = (depth === null) ? 1 : parseInt(depth);
}
