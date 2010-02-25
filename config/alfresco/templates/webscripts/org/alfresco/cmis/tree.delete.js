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
    
    // unfileObjects
    var unfileObjects = args[cmis.ARG_UNFILE_OBJECTS];
    if (unfileObjects === null || unfileObjects.length == 0)
    {
       unfileObjects = "delete";
    }
    
    // We only support delete and deletesinglefiled
    if (unfileObjects == "unfile")
    {
       status.code = 405;
       status.message = "Unfiling is not supported";
       status.redirect = true;
       break script;       
    }
    
    var continueOnFailure = (args[cmis.ARG_CONTINUE_ON_FAILURE] == "true");

    // Intentionally pass allVersions=false, even though this isn't the default!
    cmis.deleteTree(node, status, continueOnFailure, unfileObjects != "delete", false);
}
