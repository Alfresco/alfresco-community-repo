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
    
    if (model.node.isDocument)
    {
        status.code = 404;
        status.message = "Object " + object.ref + " is not a folder";
        status.redirect = true;
        break script;
    }
 
    // property filter 
    model.filter = args[cmis.ARG_FILTER];
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
    // depth
    var depth = args[cmis.ARG_DEPTH];
    model.depth = (depth === null) ? 1 : parseInt(depth);
    
    // include allowable actions
    var includeAllowableActions = args[cmis.ARG_INCLUDE_ALLOWABLE_ACTIONS];
    model.includeAllowableActions = (includeAllowableActions == "true" ? true : false);
}
