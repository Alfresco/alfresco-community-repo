model.command = url.templateArgs["command"];
model.objectType = url.templateArgs["objectType"];

if("browse" == model.command)
{
	if("node" == model.objectType)
	{
		var storeType = url.templateArgs["storeType"];
		var storeId = url.templateArgs["storeId"];
		var nodeId = url.templateArgs["nodeId"];
	
		model.redirectUrl = "/alfresco/n/browse/"+storeType+"/"+storeId+"/"+nodeId;
	}
	
	if("webproject" == model.objectType)
	{
		model.webProjectId = url.templateArgs["webProjectId"];
		
		// look up the web project
		var dmType = "{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder";	
		var query = "TYPE:\"" + dmType + "\"";
	
		var webprojects = search.luceneSearch(query);
	
		// walk through the projects, get the avm store id (for staging)
		for(var i = 0; i < webprojects.length; i++)
		{
			var projId = webprojects[i].name;
			if(projId == model.webProjectId)
			{
				var storeType = webprojects[i].properties["{http://www.alfresco.org/model/system/1.0}store-protocol"];
				var storeId = webprojects[i].properties["{http://www.alfresco.org/model/system/1.0}store-identifier"];
				var nodeId = webprojects[i].properties["{http://www.alfresco.org/model/system/1.0}node-uuid"];
				
				model.redirectUrl = "/alfresco/n/browse/"+storeType+"/"+storeId+"/"+nodeId;
			}
		}
	}
}

