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
    else if (!model.node.hasPermission("WriteProperties") || !model.node.hasPermission("WriteContent"))
    {
        status.code = 403;
        status.message = "Permission to update is denied";
        status.redirect = true;
        break script;
    }    

    if (entry !== null)
    {
        var updated = false;
    
	    // update properties
	    // NOTE: Only CMIS property name is updatable
	    // TODO: support for custom properties
	    var name = entry.title;
	    if (name !== null)
	    {
	        model.node.name = name;
	        updated = true;
	    }
	    
	    // update content, if provided in-line
	    var content = entry.content;
	    if (content !== null)
	    {
	        if (!model.node.isDocument)
	        {
	            status.code = 400;
	            status.message = "Cannot update content on folder " + pathSegments[2] + " " + reference.join("/");
	            status.redirect = true;
	            break script;
	        }
	    
	        model.node.content = content;
	        model.node.properties.content.encoding = "UTF-8";
	        model.node.properties.content.mimetype = atom.toMimeType(entry);
	        updated = true;
	    }
	    
	    // only save if an update actually occurred
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
