<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    // locate node
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    var node = object.node;

    // Intentionally pass false as allVersions flag for now. There is no binding.
    cmisserver.deleteObject(node, false);
    
    status.code = 204;  // Success, but no response content
    status.redirect = true;
}
