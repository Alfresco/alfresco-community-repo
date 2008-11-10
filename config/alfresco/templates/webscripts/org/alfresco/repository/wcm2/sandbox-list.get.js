var webproject = args["webproject"];

var dmType = "{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder";	
var query = "TYPE:\"" + dmType + "\"";
	
var webprojects = search.luceneSearch(query);
	
var results = null;
var avmStoreId = null;
	
// walk through the projects, get the avm store id (for staging)
for(var i = 0; i < webprojects.length; i++)
{
	var projName = webprojects[i].name;
	if(projName == webproject)
	{
		avmStoreId = webprojects[i].properties["{http://www.alfresco.org/model/wcmappmodel/1.0}avmstore"];
	}
}
	
if(avmStoreId != null)
{
	results = new Array();
	results[results.length] = avm.lookupStore(avmStoreId);
	results[results.length] = avm.lookupStore(avmStoreId + "--admin");
}	
	
model.results = results;

