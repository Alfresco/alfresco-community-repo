<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/read.lib.js">

script:
{
    // locate node
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    node = object.node;

    if (!node.hasPermission("Delete"))
    {
        status.code = 403;
        status.message = "Permission to delete is denied";
        status.redirect = true;
        break script;
    }

    // if deleting a folder, only delete when the folder is empty or
    // a force delete has been specified
    // NOTE: force delete is not part of CMIS specification
    if (node.isFolder)
    {
       if (node.children.length > 0 && !args.includeChildren)
       {
          status.code = 403;
          status.message = "Cannot delete folder " + pathSegments[2] + " " + reference.join("/") + " as it's not empty";
          status.redirect = true;
          break script;
       }
    }

    // Note: checked-out documents are automatically unlocked if a private working copy is deleted 
    if (!node.remove())
    {
        status.code = 500;
        status.message = "Failed to delete node " + pathSegments[2] + " " + reference.join("/");
        status.redirect = true;
        break script;
    }
    
    status.code = 204;  // Success, but no response content
    status.redirect = true;
}
