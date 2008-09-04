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
 
    // property filter 
    model.filter = cmis.findArg(args.filter, headers["CMIS-filter"]);
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
    // TODO: check returnToRoot is required for getDocumentParents
    // retrieve parent
    var returnToRoot = cmis.findArg(args.returnToRoot, headers["CMIS-returnToRoot"]);
    model.returnToRoot = returnToRoot == "true" ? true : false;
    model.rootNode = cmis.defaultRootFolder;
}
