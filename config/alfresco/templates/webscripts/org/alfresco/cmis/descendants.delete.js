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

    // NOTE: Ignore continueOnDelete as complete tree is deleted in single transaction
    // TODO: Throw error on invalid unfileMultiFiledDocuments error

    if (!node.hasPermission("Delete"))
    {
        status.code = 403;
        status.message = "Permission to delete is denied";
        status.redirect = true;
        break script;
    }

    // TODO: Checked-out documents - are they automatically cancelled?

    if (!node.remove())
    {
        status.code = 500;
        status.message = "Failed to delete object " + object.ref;
        status.redirect = true;
        break script;
    }
    
    status.code = 204;  // Success, but no response content
    status.redirect = true;
}
