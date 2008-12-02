/*
 *  Get the modified items within a sandbox
 */  
function main()
{
	var urlElements = url.extension.split("/");
	var shortName = urlElements[0];
	var boxName = urlElements[2];

	var sandbox;
	
	var webproject = webprojects.getWebProject(shortName);
	if (webproject == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The webproject, " + shortName + ", does not exist.");
		return;
	}
	
	sandbox = webproject.getSandbox(boxName);
	if (sandbox == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The sandbox, " + boxName + ", in webproject, "  +  shortName + ", does not exist.");
		return;
	}
	
	var webApp = args["webApp"];

	// set model properties
	model.sandbox = sandbox;
	model.webproject = webproject;
	
	if(webApp != null)
	{
							   
		model.assets = sandbox.getModifiedAssetsWebApp(webApp);
	}
	else
	{
		model.assets = sandbox.modifiedAssets;
	}
}

main()
	
