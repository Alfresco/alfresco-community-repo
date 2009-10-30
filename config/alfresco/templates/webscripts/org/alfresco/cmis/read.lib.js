//
// Get Node from URL
//
// @return  node (or null, if not found)
function getObjectFromUrl()
{
    var ret = new Object();
    ret.ref = cmis.createObjectReferenceFromUrl(args, url.templateArgs);
    if (ret.ref == null)
    {
        status.setCode(400, "Cannot determine object reference from URL");
        return ret;
    }
    ret.node = cmis.getNode(ret.ref);
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
    ret.ref = cmis.createObjectIdReference(objectId);
    if (ret.ref == null)
    {
        status.setCode(400, "Cannot create object id reference from " + objectId);
        return ret;
    }
    ret.node = cmis.getNode(ret.ref);
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
    ret.ref = cmis.createRelationshipReferenceFromUrl(args, url.templateArgs);
    if (ret.ref == null)
    {
        status.setCode(400, "Cannot determine association reference from URL");
        return ret;
    }
    ret.assoc = cmis.getAssociation(ret.ref);
    if (ret.assoc === null)
    {
        status.setCode(404, "Cannot find association for " + ret.ref.toString());
    }
    return ret;
}
