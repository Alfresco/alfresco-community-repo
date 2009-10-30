<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    var object = getObjectFromUrl();
    if (object.node === null)
    {
        break script;
    }
    model.node = object.node;
 
    // TODO: handle version??
    
    // property filter 
    model.filter = args[cmis.ARG_FILTER];
    if (model.filter === null || model.filter == "")
    {
        model.filter = "*";
    }
   
    // include allowable actions
    var includeAllowableActions = args[cmis.ARG_INCLUDE_ALLOWABLE_ACTIONS];
    model.includeAllowableActions = (includeAllowableActions == "true" ? true : false);
}
