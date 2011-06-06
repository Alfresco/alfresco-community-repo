<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    // ensure atom entry is posted
    if (entry === null)
    {
        status.code = 400;
        status.message = "Expected Atom entry";
        status.redirect = true;
        break script;
    }

    // extract object id from atom entry
    var object = entry.getExtension(atom.names.cmisra_object);
    var objectId = (object !== null) ? object.objectId.stringValue : null;
    if (objectId === null)
    {
        status.code = 400;
        status.message = "Atom entry does not specify repository object id";
        status.redirect = true;
        break script;
    }
    
    // locate node
    var object = getObjectFromObjectId(objectId);
    if (object.node === null)
    {
        break script;
    }
    model.node = object.node; 
    
    // checkout
    model.pwc = cmisserver.checkOut(objectId);

    // setup for 201 Created response
    // TODO: set Content-Location
    status.code = 201;
    status.location = url.server + url.serviceContext + "/cmis/pwc/s/" + model.pwc.nodeRef.storeRef.protocol + ":" + model.pwc.nodeRef.storeRef.identifier + "/i/" + model.pwc.nodeRef.id;
    status.redirect = true;
}
