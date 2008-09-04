script:
{
    // locate version series
    // NOTE: version series is identified by noderef (as this is constant during lifetime of node)
    var pathSegments = url.match.split("/");
    var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
    model.node = cmis.findNode(pathSegments[2], reference);
    if (model.node === null || !model.node.isVersioned)
    {
        status.code = 404;
        status.message = "Versions series " + pathSegments[2] + " " + reference.join("/") + " not found";
        status.redirect = true;
        break script;
    }
 
    // TODO: property filters 
   
    // retrieve versions
    model.versions = model.node.versionHistory;
    model.nodes = new Array(model.versions.length);
    for (i = 0; i < model.versions.length; i++)
    {
       model.nodes[i] = model.versions[i].node;
    }
}
