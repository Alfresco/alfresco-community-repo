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
    // TODO: property filters 
   
    // retrieve children
    var page = paging.createPageOrWindow(args, headers);
    var paged = cmis.queryChildren(model.node, model.types, page);
    model.results = paged.results;
    model.cursor = paged.cursor;
}
