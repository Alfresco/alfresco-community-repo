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

    // update properties
    var updated = updateNode(model.node, entry, null, function(propDef) {return patchValidator(propDef, false);});
    if (updated)
    {
        model.node.save();
    }
}
