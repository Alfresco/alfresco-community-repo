<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/read.lib.js">

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
    
    // ensure node can be checked-out
    if (!model.node.isDocument)
    {
        status.code = 400;
        status.message = "Cannot checkout node " + object.ref + " as it is not a document";
        status.redirect = true;
        break script;
    }

    // TODO: need to test for isCheckedOut not isLocked
    if (model.node.isLocked || model.node.hasAspect("cm:workingCopy"))
    {
        status.code = 400;
        status.message = "Cannot checkout node " + object.ref + " as it is already checked-out";
        status.redirect = true;
        break script;
    }

    // switch on versioning 
    if (!model.node.hasAspect("cm:versionable"))
    {
        // create an initial version of the current document
        model.node.createVersion("Initial Version", true);
    }
    
    // checkout
    model.pwc = model.node.checkout();

    // setup for 201 Created response
    // TODO: set Content-Location
    status.code = 201;
    status.location = url.server + url.serviceContext + "/cmis/pwc/s/" + model.pwc.nodeRef.storeRef.protocol + ":" + model.pwc.nodeRef.storeRef.identifier + "/i/" + model.pwc.nodeRef.id;
    status.redirect = true;
}
