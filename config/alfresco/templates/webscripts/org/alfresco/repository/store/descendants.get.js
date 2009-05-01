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
    model.types = args[cmis.ARG_TYPES] === null ? cmis.defaultTypesFilter : args[ARG_TYPES];
    if (!cmis.isValidTypesFilter(model.types))
    {
        status.code = 400;
        status.message = "Types filter '" + model.types + "' unknown";
        status.redirect = true;
        break script;
    }
    
    // property filter 
    model.filter = args[cmis.ARG_FILTER];
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
    // depth
    var depth = args[cmis.ARG_DEPTH];
    model.depth = (depth === null) ? 1 : parseInt(depth);
    
    // include allowable actions
    var includeAllowableActions = args[cmis.ARG_INCLUDE_ALLOWABLE_ACTIONS];
    model.includeAllowableActions = (includeAllowableActions == "true" ? true : false);
}
