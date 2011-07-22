<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
	var object = getObjectOrAssocFromUrl();
	if (object.node !== null && typeof(object.node) != 'undefined')
	{
		model.node = object.node;
		
	    // return version
        var returnVersion = args[cmisserver.ARG_RETURN_VERSION];
	    if (returnVersion === null || returnVersion.length == 0)
	    {
	        returnVersion = "this";
	    }
	    model.node = cmisserver.getReturnVersion(model.node, returnVersion);
	}
	else
	{
	    if (object.assoc === null)
	    {
        	break script;
    	}
	    model.assoc = object.assoc;
	}
	
    // property filter 
    model.filter = args[cmisserver.ARG_FILTER];
    if (model.filter === null || model.filter == "")
    {
        model.filter = "*";
    }
   
    // ACL
    model.includeACL = args[cmisserver.ARG_INCLUDE_ACL] == "true";

    // rendition filter
    model.renditionFilter = args[cmisserver.ARG_RENDITION_FILTER];
    if (model.renditionFilter === null || model.renditionFilter.length == 0)
    {
        model.renditionFilter = "cmis:none";
    }

    // include allowable actions
    model.includeAllowableActions = args[cmisserver.ARG_INCLUDE_ALLOWABLE_ACTIONS] == "true";
    
    // include relationships
    model.includeRelationships = args[cmisserver.ARG_INCLUDE_RELATIONSHIPS];
    if (model.includeRelationships == null || model.includeRelationships.length == 0)
    {
        model.includeRelationships = "none";
    }    
}
