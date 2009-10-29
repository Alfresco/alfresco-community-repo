<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/constants.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/atomentry.lib.js">

script:
{
    // ensure atom entry is posted
    if (entry === null)
    {
        status.code = 400;
        status.message = "Expected atom entry";
        status.redirect = true;
        break script;
    }

    // locate parent node
    var pathSegments = url.match.split("/");
    var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
    model.parent = cmis.findNode(pathSegments[2], reference);
    if (model.parent === null)
    {
        status.code = 404;
        status.message = "Repository " + pathSegments[2] + " " + reference.join("/") + " not found";
        status.redirect = true;
        break script;
    }

    // create node
    var node = createNode(model.parent, entry, slug);
    if (node == null)
    {
        break script;
    }
    
    // success
    node.save();
    model.node = node;
    // TODO: set Content-Location
    status.code = 201;
    status.location = url.server + url.serviceContext + "/api/node/" + node.nodeRef.storeRef.protocol + "/" + node.nodeRef.storeRef.identifier + "/" + node.nodeRef.id;
    status.redirect = true;
}
