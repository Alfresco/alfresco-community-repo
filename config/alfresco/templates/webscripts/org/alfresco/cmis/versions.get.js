<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    // NOTE: version series is identified by noderef (as this is constant during lifetime of node)
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        status.message = "Versions series " + object.ref + " not found";
        break script;
    }
    model.node = object.node;
 
    // property filter 
    model.filter = args[cmisserver.ARG_FILTER];
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
    // retrieve versions
    model.nodes = cmisserver.getAllVersions(model.node);
}
