<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/read.lib.js">

script:
{
    // locate node
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    model.node = object.node;
    
    // TODO: property filters
    
}
