script:
{
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

    // TODO: check for appropriate permission
    
    // ensure atom entry is posted
    if (entry === null)
    {
        status.code = 400;
        status.message = "Expected atom entry";
        status.redirect = true;
        break script;
    }

    // update properties
    // NOTE: Only CMIS property name is updatable
    // TODO: support for custom properties
    var updated = false;
    
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
