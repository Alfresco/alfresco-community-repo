<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/constants.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/read.lib.js">

script:
{
    var rel = getAssocFromUrl();
    if (rel.assoc == null)
    {
        break script;
    }
    var assoc = rel.assoc;

    // TODO: check permission
//    if (!assoc.source.hasPermission("DeleteAssociations"))
//    {
//        status.setCode(403, "Permission to delete is denied");
//        break script;
//    }

    // delete
    assoc.source.removeAssociation(assoc.target, assoc.type);
    
    status.code = 204;  // Success, but no response content
    status.redirect = true;
}
