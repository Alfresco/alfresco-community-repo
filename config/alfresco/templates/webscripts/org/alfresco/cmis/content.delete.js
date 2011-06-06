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

    cmisserver.deleteContentStream(node);

    status.code = 204;  // Success, but no response content
    status.redirect = true;
}
