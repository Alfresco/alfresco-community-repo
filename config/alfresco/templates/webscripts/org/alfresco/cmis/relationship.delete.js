<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/constants.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    var rel = getAssocFromUrl();
    if (rel.assoc == null)
    {
        break script;
    }
    var assoc = rel.assoc;

    cmisserver.deleteObject(assoc);
    
    status.code = 204;  // Success, but no response content
    status.redirect = true;
}
