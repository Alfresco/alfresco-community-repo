script:
{
    // locate node
    var pathSegments = url.match.split("/");
    var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
    var node = cmis.findNode("node", reference);
    if (node === null || !node.hasAspect("cm:workingcopy"))
    {
        status.code = 404;
        status.message = "Private working copy " + reference.join("/") + " not found";
        status.redirect = true;
        break script;
    }

    if (!node.hasPermission("CancelCheckOut"))
    {
        status.code = 403;
        status.message = "Permission to cancel checkout is denied";
        status.redirect = true;
        break script;
    }

    node.cancelCheckout();
    
    status.code = 204;  // Success, but no response content
    status.redirect = true;
}
