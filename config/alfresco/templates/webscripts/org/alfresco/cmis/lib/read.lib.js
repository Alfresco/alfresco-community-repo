//
// Get Node from URL
//
// @return  node (or null, if not found)
function getObjectFromUrl()
{
    var ret = new Object();
    ret.ref = cmisserver.createObjectReferenceFromUrl(args, url.templateArgs);
    if (ret.ref == null)
    {
        status.setCode(400, "Cannot determine object reference from URL");
        return ret;
    }
    ret.node = cmisserver.getNode(ret.ref);
    if (ret.node === null)
    {
        status.setCode(404, "Cannot find object for " + ret.ref.toString());
    }
    return ret;
}

//
// Get Node from Object Id
//
// @return  node (or null, if not found)
function getObjectFromObjectId(objectId)
{
    var ret = new Object();
    ret.ref = cmisserver.createObjectIdReference(objectId);
    if (ret.ref == null)
    {
        status.setCode(400, "Cannot create object id reference from " + objectId);
        return ret;
    }
    ret.node = cmisserver.getNode(ret.ref);
    if (ret.node === null)
    {
        status.setCode(404, "Cannot find object for " + ret.ref.toString());
    }
    return ret;
}

//
// Get Association from URL
//
// @return  association (or null, if not found)
function getAssocFromUrl()
{
    var ret = new Object();
    ret.ref = cmisserver.createRelationshipReferenceFromUrl(args, url.templateArgs);
    if (ret.ref == null)
    {
        status.setCode(400, "Cannot determine association reference from URL");
        return ret;
    }
    ret.assoc = cmisserver.getAssociation(ret.ref);
    if (ret.assoc === null)
    {
        status.setCode(404, "Cannot find association for " + ret.ref.toString());
    }
    return ret;
}

//
//Get Node or Association from URL
//
//@return  node or association (or null, if not found)
function getObjectOrAssocFromUrl()
{
    var id = url.templateArgs.id;

    if (id === null)
    {
    	id = args.noderef;
    }

    if (id != null && id.indexOf("assoc:") === 0)
    {
        return getAssocFromUrl();
    }

    return getObjectFromUrl();
}
