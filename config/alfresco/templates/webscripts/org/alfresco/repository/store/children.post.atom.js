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

    // pull apart atom entry
    // TODO: creation of file/folder sub-types
    // TODO: cmis properties
    
    var id = entry.id;
    var name = (slug !== null) ? slug : entry.title;
    var title = entry.title;
    var description = entry.summary;    
    var updated = entry.updated;
    var author = (entry.author !== null) ? entry.author.name : null;
    var object = entry.getExtension(atom.names.cmis_object);
    var typeId = (object !== null) ? object.objectTypeId.value : null;
    
    // create the item
    // TODO: author/updated/id
    
    if (typeId === null || typeId.toLowerCase() == "document")
    {
        // TODO: objectTypeId to Alfresco content type
        var node = model.parent.createFile(name);
        node.properties.title = title;
        node.properties.description = description;

        // write entry content
        if (entry.content != null)
        {
            if (entry.contentType != null && entry.contentType == "MEDIA")
            {
                node.properties.content.write(entry.contentStream);
            }
            else
            {
                node.content = entry.content;
            }
            node.properties.content.encoding = "UTF-8";
            node.properties.content.mimetype = atom.toMimeType(entry);
        }        
                
        node.save();
        model.node = node;
        
        // TODO: versioningState argument (CheckedOut/CheckedInMinor/CheckedInMajor)
    }
    else if (typeId.toLowerCase() == "folder")
    {
        // TODO: objectTypeId to Alfresco content type
        var node = model.parent.createFolder(name);
        node.properties.title = title;
        node.properties.description = description;
        node.save();
        model.node = node;
    }
    else
    {
        status.code = 400;
        status.message = "CMIS object type " + typeId + " not understood";
        status.redirect = true;
        break script;
    }
    
    // setup for 201 Created response
    // TODO: set Content-Location
    status.code = 201;
    status.location = url.server + url.serviceContext + "/api/node/" + node.nodeRef.storeRef.protocol + "/" + node.nodeRef.storeRef.identifier + "/" + node.nodeRef.id;
    status.redirect = true;
}
