<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/read.lib.js">

script:
{
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script
    }
    model.node = object.node;
    if (model.node == null)
    {
        break script;
    }

    // TODO: handle version??
    
}
