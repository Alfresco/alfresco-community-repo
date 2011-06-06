<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    model.node = object.node; 

    // handle filters
    model.types = args[cmisserver.ARG_TYPES] === null ? cmisserver.defaultTypesFilter : args[ARG_TYPES];
    if (!cmisserver.isValidTypesFilter(model.types))
    {
        status.code = 400;
        status.message = "Types filter '" + model.types + "' unknown";
        status.redirect = true;
        break script;
    }
    
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
   
    // depth
    var depth = args[cmisserver.ARG_DEPTH];
    model.depth = (depth === null) ? 1 : parseInt(depth);
    
    // include allowable actions
    var includeAllowableActions = args[cmisserver.ARG_INCLUDE_ALLOWABLE_ACTIONS];
    model.includeAllowableActions = (includeAllowableActions == "true" ? true : false);

    // include relationships
    model.includeRelationships = args[cmisserver.ARG_INCLUDE_RELATIONSHIPS];
    if (model.includeRelationships == null || model.includeRelationships.length == 0)
    {
        model.includeRelationships = "none";
    }    
}
