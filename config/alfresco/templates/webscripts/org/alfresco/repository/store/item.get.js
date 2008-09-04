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
 
    // TODO: handle version??
    
    // property filter 
    model.filter = cmis.findArg(args.filter, headers["CMIS-filter"]);
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
}
