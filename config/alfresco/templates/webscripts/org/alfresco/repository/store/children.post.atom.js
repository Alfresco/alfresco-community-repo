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
    model.parent = cmis.findNode(pathSegments[2], reference);
    if (model.parent === null)
    {
        status.code = 404;
        status.message = "Repository " + pathSegments[2] + " " + reference.join("/") + " not found";
        status.redirect = true;
        break script;
    }

    // pull apart atom entry
    // TODO: creation of folder
    // TODO: creation of file/folder sub-types
    // TODO: cmis properties
    
    var id = entry.id;
    var name = (slug !== null) ? slug : entry.title;
    var title = entry.title;
    var description = entry.summary;    
    var updated = entry.updated;
    var author = (entry.author !== null) ? entry.author.name : null;
    var content = entry.content;
    
    // create the item
    // TODO: author/updated/id
    var node = model.parent.createFile(name);
    node.properties.title = title;
    node.properties.description = description;
    if (content !== null)
    {
        node.content = content;
        node.properties.content.encoding = "UTF-8";
        node.properties.content.mimetype = atom.toMimeType(entry);
    }
    node.save();
    model.node = node;
    
    // setup for 201 Created response
    // TODO: set Content-Location
    status.code = 201;
    status.location = url.server + url.serviceContext + "/api/node/" + node.nodeRef.storeRef.protocol + "/" + node.nodeRef.storeRef.identifier + "/" + node.nodeRef.id + "/properties";
    status.redirect = true;
}
