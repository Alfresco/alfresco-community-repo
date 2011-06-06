<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    // locate node and parent
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    model.node = object.node;
    
    var parents = [];
    for each (var parent in model.node.parents)
    {
        if (parent.hasPermission("Read") && parent.isContainer)
        {
            parents.push(parent);
        }
    }
    model.parents = parents;
 
    // property filter 
    model.filter = args[cmisserver.ARG_FILTER];
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
    // rendition filter
    model.renditionFilter = args[cmisserver.ARG_RENDITION_FILTER];
    if (model.renditionFilter === null || model.renditionFilter.length == 0)
    {
        model.renditionFilter = "cmis:none";
    }

    // include allowable actions
    var includeAllowableActions = args[cmisserver.ARG_INCLUDE_ALLOWABLE_ACTIONS];
    model.includeAllowableActions = (includeAllowableActions == "true" ? true : false);
}
