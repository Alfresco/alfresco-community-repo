<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/constants.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/read.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/modify.lib.js">

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
    var parent = getObjectFromUrl();
    if (parent.node == null)
    {
        break script;
    }
    model.parent = parent.node;

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
        // locate node and its source folder
        var object = getObjectFromObjectId(objectId);
        if (object.node == null)
        {
            break script;
        }
        node = object.node;
    
        // move node
        var sourceFolderId = args[cmis.ARG_SOURCE_FOLDER_ID];
        if (sourceFolderId == null)
        {
            status.code = 400;
            status.message = "Move of object " + object.ref + " requires sourceFolderId argument";
            status.redirect = true;
            break script;
        }
        
        var sourceFolderObject = getObjectFromObjectId(sourceFolderId);
        if (sourceFolderObject.node == null)
        {
            status.code = 400;
            break script;
        }
        if (!sourceFolderObject.node.nodeRef.equals(node.parent.nodeRef))
        {
            status.code = 400;
            status.message = "Source Folder " + sourceFolderObject.ref + " is not parent of object " + object.ref;
            status.redirect = true;
            break script;
        }
        var sourceFolder = sourceFolderObject.node;

        // perform move
        var success = node.move(model.parent);
        if (!success)
        {
            status.code = 500;
            status.message = "Failed to move object " + object.ref + " from folder " + sourceFolderObject.ref + " to folder " + parent.ref;
            status.redirect = true;
            break script;
        }
    }
    
    // success
    node.save();
    model.node = node;
    // TODO: set Content-Location
    status.code = 201;
    status.location = url.server + url.serviceContext + "/cmis/s/" + node.nodeRef.storeRef.protocol + ":" + node.nodeRef.storeRef.identifier + "/i/" + node.nodeRef.id;
    status.redirect = true;
}
