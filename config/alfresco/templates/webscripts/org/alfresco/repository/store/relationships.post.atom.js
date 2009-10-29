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

    // locate source node
    var pathSegments = url.match.split("/");
    var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
    model.source = cmis.findNode(pathSegments[2], reference);
    if (model.source === null)
    {
        status.code = 404;
        status.message = "Repository " + pathSegments[2] + " " + reference.join("/") + " not found";
        status.redirect = true;
        break script;
    }

    // create 
    var assoc = createAssociation(model.source, entry);
    if (assoc == null)
    {
        break script;
    }
    
    // success
    model.assoc = assoc;
    // TODO: set Content-Location
    status.code = 201;
    // TODO: complete url mapping
    status.location = url.server + url.serviceContext + "/api/rel/" + model.source.nodeRef.storeRef.protocol + "/" + model.source.nodeRef.storeRef.identifier + "/" + model.source.nodeRef.id;
    status.redirect = true;
}
