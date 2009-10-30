<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/read.lib.js">

script:
{
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    var node = object.node;
    
    if (!node.hasPermission("CancelCheckOut"))
    {
        status.code = 403;
        status.message = "Permission to cancel checkout of " + object.ref + " is denied";
        status.redirect = true;
        break script;
    }

    node.cancelCheckout();
    
    status.code = 204;  // Success, but no response content
    status.redirect = true;
}
