script:
{
    // locate node
    var pathSegments = url.match.split("/");
    var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
    model.node = cmis.findNode("node", reference);
    if (model.node === null || !model.node.hasAspect("cm:workingcopy"))
    {
        status.code = 404;
        status.message = "Private working copy " + reference.join("/") + " not found";
        status.redirect = true;
        break script;
    }
 
    // TODO: property filters
    
}
