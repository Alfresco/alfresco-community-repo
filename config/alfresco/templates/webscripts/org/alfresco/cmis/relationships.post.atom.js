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

    // locate source node
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    model.source = object.node;

    // create 
    var assoc = createAssociation(model.source, entry);
    if (assoc == null)
    {
        break script;
    }
    
    // success
    model.assoc = assoc;
    // TODO: set Content-Location
    status.code = 201;
    // TODO: complete url mapping
    status.location = url.server + url.serviceContext + "/cmis/rel/" + model.source.nodeRef.storeRef.protocol + "/" + model.source.nodeRef.storeRef.identifier + "/" + model.source.nodeRef.id;
    status.redirect = true;
}
