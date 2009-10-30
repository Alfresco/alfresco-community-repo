<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    // NOTE: version series is identified by noderef (as this is constant during lifetime of node)
    var object = getObjectFromUrl();
    if (object.node == null || !object.node.isVersioned)
    {
        status.message = "Versions series " + object.ref + " not found";
        break script;
    }
    model.node = object.node;
 
    // property filter 
    model.filter = args[cmis.ARG_FILTER];
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
    // retrieve versions
    model.versions = model.node.versionHistory;
    model.nodes = new Array(model.versions.length);
    for (i = 0; i < model.versions.length; i++)
    {
       model.nodes[i] = model.versions[i].node;
    }
}
