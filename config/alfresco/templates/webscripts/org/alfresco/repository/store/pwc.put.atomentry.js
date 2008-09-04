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

    if (!node.hasPermission("CheckIn"))
    {
        status.code = 403;
        status.message = "Permission to checkin is denied";
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
	        node.name = name;
	        updated = true;
	    }
	    
	    // update content, if provided in-line
	    var content = entry.content;
	    if (content !== null)
	    {
	        if (!node.isDocument)
	        {
	            status.code = 400;
	            status.message = "Cannot update content on folder " + pathSegments[2] + " " + reference.join("/");
	            status.redirect = true;
	            break script;
	        }
	    
	        node.content = content;
	        node.properties.content.encoding = "UTF-8";
	        node.properties.content.mimetype = atom.toMimeType(entry);
	        updated = true;
	    }
	    
	    // only save if an update actually occurred
	    if (updated)
	    {
	        node.save();
	    }
    }
    
    // checkin
    var comment = cmis.findArg(args.checkinComment, headers["CMIS-checkinComment"]);
    var major = cmis.findArg(args.major, headers["CMIS-major"]);
    major = (major === null || major == "true") ? true : false;
    model.node = node.checkin(comment === null ? "" : comment, major);
}
