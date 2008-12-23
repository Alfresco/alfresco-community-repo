/*
 *  Update asset - put method
 */  
function main()
{
	var urlElements = url.extension.split("/");
	var shortName = urlElements[0];
	var boxName = urlElements[2];
	var pathArray = urlElements.slice(4);
	var path = pathArray.join("/");
	
	var webproject = webprojects.getWebProject(shortName);
	if (webproject == null)
	{
		// webproject cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The webproject, " + shortName + ", does not exist.");
		return;
	}
	var sandbox;	
	sandbox = webproject.getSandbox(boxName);
	if (sandbox == null)
	{
		// sandbox cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The sandbox, " + boxName + ", in webproject, "  +  shortName + ", does not exist.");
		return;
	}
	
	var webApp = args["webApp"];
	
	var asset = null;
	
	if(webApp != null)
	{
		asset = sandbox.getAssetWebApp(webApp, path);
	}
	else
	{
		asset = sandbox.getAsset(path); 
	}
	
    if (asset == null)
    {
	    // parent cannot be found
	    status.setCode(status.STATUS_NOT_FOUND, "The asset, " + path + ", in webproject, "  +  shortName + ", does not exist.");
	    return;
    }
    
    // Is this a rename ??
    if(json.has("name") && json.get("name").length() > 0)
    {
    	asset = asset.rename(json.get("name"));
    }
    
    // Is this a move ??
    if(json.has("path") && json.get("path").length() > 0)
    {
    	asset = asset.move(json.get("path"));
    }
    
    // Is this a set properties?
    if(json.has("properties"))
    {
    	var properties = json.getJSONObject("properties");
    	asset.setProperties(properties);
    }
	
	// set model properties
	model.asset = asset;
	model.sandbox = sandbox;
	model.webproject = webproject;		
}

main()
