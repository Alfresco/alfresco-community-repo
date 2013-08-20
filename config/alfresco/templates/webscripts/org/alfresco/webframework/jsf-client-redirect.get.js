model.command = url.templateArgs["command"];
model.objectType = url.templateArgs["objectType"];

if("browse" == model.command)
{
	if("node" == model.objectType)
	{
		var storeType = url.templateArgs["storeType"];
		var storeId = url.templateArgs["storeId"];
		var nodeId = url.templateArgs["nodeId"];
	
		model.redirectUrl = url.context + "/n/browse/"+storeType+"/"+storeId+"/"+nodeId;
	}
	
	if("webproject" == model.objectType)
	{
		model.webprojectref = url.templateArgs["id"];
		
		// load the web project metadata
		var service = webprojects;
		var data = service.getWebProject(model.webprojectref);
		
		// look up the node ref for the web project
		// TODO: could this be made to be part of the web projects service?
		
		// look up the web project
		var dmType = "{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder";	
		var query = "TYPE:\"" + dmType + "\"";
	
		var webprojects = search.luceneSearch(query);
	
		// walk through the projects, get the avm store id (for staging)
		for(var i = 0; i < webprojects.length; i++)
		{
			var dns = webprojects[i].properties["{http://www.alfresco.org/model/wcmappmodel/1.0}avmstore"];
			if(dns == model.webprojectref)
			{
				var storeType = webprojects[i].properties["{http://www.alfresco.org/model/system/1.0}store-protocol"];
				var storeId = webprojects[i].properties["{http://www.alfresco.org/model/system/1.0}store-identifier"];
				var nodeId = webprojects[i].properties["{http://www.alfresco.org/model/system/1.0}node-uuid"];
				
				model.redirectUrl = url.context + "/n/browse/"+storeType+"/"+storeId+"/"+nodeId;
			}
		}
	}
}

