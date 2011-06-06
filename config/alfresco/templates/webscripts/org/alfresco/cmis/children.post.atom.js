<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/constants.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/modify.lib.js">

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
    
    // versioning state
    var versioningState = args[cmisserver.ARG_VERSIONING_STATE];
    if (versioningState === null || versioningState.length == 0)
    {
       versioningState = "major";
    }
    
    // is this a create or move? 
    var object = entry.getExtension(atom.names.cmisra_object);
    var objectIdProp = (object !== null) ? object.objectId : null;
    var objectId = (objectIdProp !== null) ? objectIdProp.nativeValue : null;
    var sourceFolderId = args[cmisserver.ARG_SOURCE_FOLDER_ID];
    var node = null;

    if (objectId == null)
    {
        // create node
        node = createNode(model.parent, entry, slug, versioningState);
        if (node == null)
        {
            break script;
        }
    }
    else if (sourceFolderId == null || sourceFolderId.length == 0)
    {
       // Add node
       var object = getObjectFromObjectId(objectId);
       if (object.node == null)
       {
           break script;
       }
       node = object.node;
       
       cmisserver.addObjectToFolder(node, model.parent);
    }
    else
    {
        // move node

        // locate node
        var object = getObjectFromObjectId(objectId);
        if (object.node == null)
        {
            break script;
        }
        node = object.node;
    
        // perform move
        cmisserver.moveObject(node, model.parent, sourceFolderId);
    }
    
    // success
    model.node = node;
    // TODO: set Content-Location
    status.code = 201;
    status.location = url.server + url.serviceContext + "/cmis/s/" + node.nodeRef.storeRef.protocol + ":" + node.nodeRef.storeRef.identifier + "/i/" + node.nodeRef.id;
    status.redirect = true;
}
