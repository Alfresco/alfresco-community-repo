var id = args["id"];

var dmType = "{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder";	
var query = "TYPE:\"" + dmType + "\"";
	
var webprojects = search.luceneSearch(query);
	
var result = { };
	
// walk through the projects, get the avm store id (for staging)
for(var i = 0; i < webprojects.length; i++)
{
	var projName = webprojects[i].name;
	if(projName == id)
	{
		result["name"] = projName;
		result["webProjectId"] = id;
		result["storeId"] = webprojects[i].properties["{http://www.alfresco.org/model/wcmappmodel/1.0}avmstore"];
		result["sandboxId"] = webprojects[i].properties["{http://www.alfresco.org/model/wcmappmodel/1.0}avmstore"];
	}
}
	
model.result = result;