/*
 *  Post -create a new asset file/folder
 *  optionally created with properties and/or simple content
 */  
function main()
{
	var urlElements = url.extension.split("/");
	var shortName = urlElements[0];
	var boxName = urlElements[2];
	var boxName = urlElements[2];
	var pathArray = urlElements.slice(4);
	var path = pathArray.join("/");
	
	var webproject = webprojects.getWebProject(shortName);
	if (webproject == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The webproject, " + shortName + ", does not exist.");
		return;
	}
	var sandbox;	
	sandbox = webproject.getSandbox(boxName);
	if (sandbox == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The sandbox, " + boxName + ", in webproject, "  +  shortName + ", does not exist.");
		return;
	}
	
	var webApp = args["webApp"];
	
	var parent = null;
	if(webApp != null)
	{
		if(path=="")
		{
			path = "/";
		}
		parent = sandbox.getAssetWebApp(webApp, path);
	}
	else
	{
		parent = sandbox.getAsset(path); 
	}
	
    if (parent == null)
    {
	    // parent cannot be found
	    status.setCode(status.STATUS_NOT_FOUND, "The folder, " + path + ", in webproject, "  +  shortName + ", does not exist.");
	    return;
    }
	
    // Now read the values from the json form
    if(!json.has("type"))
    {
    	status.setCode(status.BAD_REQUEST, "JSON property 'type' must specified");
  	    return;
    }
    if(!json.has("name") || json.get("name").length() == 0)
    {
    	status.setCode(status.BAD_REQUEST, "JSON property 'name' must specified");
  	    return;
    }
    
    var name = json.get("name");
    var type = json.get("type");
    var content = null;
    var properties = null

    if(json.has("content"))
    {
    	content = json.get("content");
    }
    
    if(type == "file")
    {
    	// create a new file
    	parent.createFile(name, content);
    }
    else
    {
    	// create a new folder
    	parent.createFolder(name);
    }
    
    // Get the newly created asset
    var asset = sandbox.getAsset(parent.path + "/" + name);
	
	// set model properties
	model.sandbox = sandbox;
	model.webproject = webproject;
	model.asset = asset;
	
	status.code = status.STATUS_CREATED 
		
}

main()
	
