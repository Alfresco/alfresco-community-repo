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

    // is this a create or move? 
    var object = entry.getExtension(atom.names.cmisra_object);
    var objectIdProp = (object !== null) ? object.objectId : null;
    var objectId = (objectIdProp !== null) ? objectIdProp.nativeValue : null;
    var node = null;

    if (objectId == null)
    {
        // create node
        node = createNode(model.parent, entry, slug);
        if (node == null)
        {
            break script;
        }
    }
    else
    {
        // move node
        var sourceFolderId = args[cmis.ARG_SOURCE_FOLDER_ID];
        if (sourceFolderId == null)
        {
            status.code = 400;
            status.message = "Move of object " + objectId + " requires sourceFolderId argument";
            status.redirect = true;
            break script;
        }
        
        // locate node and its source folder
        node = search.findNode(objectId);
        if (node == null)
        {
            status.code = 400;
            status.message = "Object " + objectId + " does not exist";
            status.redirect = true;
            break script;
        }
        sourceFolder = search.findNode(sourceFolderId);
        if (sourceFolder == null || !(sourceFolder.nodeRef.equals(node.parent.nodeRef)))
        {
            status.code = 400;
            status.message = "Source Folder " + sourceFolderId + " is not valid for object " + objectId;
            status.redirect = true;
            break script;
        }

        // perform move
        var success = node.move(model.parent);
        if (!success)
        {
            status.code = 500;
            status.message = "Failed to move object " + objectId + " from folder " + sourceFolderId + " to folder " + model.parent.nodeRef;
            status.redirect = true;
            break script;
        }
    }
    
    // success
    node.save();
    model.node = node;
    // TODO: set Content-Location
    status.code = 201;
    status.location = url.server + url.serviceContext + "/api/node/" + node.nodeRef.storeRef.protocol + "/" + node.nodeRef.storeRef.identifier + "/" + node.nodeRef.id;
    status.redirect = true;
}
