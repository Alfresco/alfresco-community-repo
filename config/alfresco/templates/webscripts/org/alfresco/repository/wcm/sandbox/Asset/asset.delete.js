/*
 *  Delete the specified asset from within a sandbox
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
	
	var asset ;
	
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
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The asset, " + path + ", in webproject, "  +  shortName + ", does not exist.");
		return;
	}
	
	// now do the delete of the asset
	asset.deleteAsset();
	
	// set model properties
	model.sandbox = sandbox;
	model.webproject = webproject;
	
}

main()
	
