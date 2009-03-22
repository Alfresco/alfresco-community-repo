<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/atomentry.lib.js">

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

    // check permissions
    model.checkin = cmis.findArg(args.checkin, headers["CMIS-checkin"]) == "true" ? true : false;
    if (model.checkin && !model.node.hasPermission("CheckIn"))
    {
        status.code = 403;
        status.message = "Permission to checkin is denied";
        status.redirect = true;
        break script;
    }
    
    if (entry !== null)
    {
        // update properties
        var updated = updateNode(model.node, entry, null, false);
        if (updated === null)
        {
            break script;
        }
        if (updated)
        {
            model.node.save();
        }
    }
    
    // checkin
    if (model.checkin)
    {
        var comment = cmis.findArg(args.checkinComment, headers["CMIS-checkinComment"]);
        var major = cmis.findArg(args.major, headers["CMIS-major"]);
        major = (major === null || major == "true") ? true : false;
        model.node = model.node.checkin(comment === null ? "" : comment, major);
    }
}
