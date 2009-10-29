script:
{
    // locate node
    var pathSegments = url.match.split("/");
    var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
    var node = cmis.findNode(pathSegments[2], reference);
    if (node === null)
    {
        status.code = 404;
        status.message = "Repository " + pathSegments[2] + " " + reference.join("/") + " not found";
        status.redirect = true;
        break script;
    }

    // NOTE: Ignore continueOnDelete as complete tree is deleted in single transaction
    // TODO: Throw error on invalid unfileMultiFiledDocuments error

    if (!node.hasPermission("Delete"))
    {
        status.code = 403;
        status.message = "Permission to delete is denied";
        status.redirect = true;
        break script;
    }

    // TODO: Checked-out documents - are they automatically cancelled?

    if (!node.remove())
    {
        status.code = 500;
        status.message = "Failed to delete node " + pathSegments[2] + " " + reference.join("/");
        status.redirect = true;
        break script;
    }
    
    status.code = 204;  // Success, but no response content
    status.redirect = true;
}
