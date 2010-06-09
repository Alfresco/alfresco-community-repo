/*
 *  Get asset content script
 */  
function main()
{
	var urlElements = url.extension.split("/");
	var shortName = urlElements[0];
	var boxName = urlElements[2];
	var pathArray = urlElements.slice(5);
	var path = pathArray.join("/");

	var webproject = webprojects.getWebProject(shortName);
	if (null == webproject)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The webproject '" + shortName + "' does not exist.");
		return;
	}

	var sandbox;
	sandbox = webproject.getSandbox(boxName);
	if (null == sandbox)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The sandbox '" + boxName + "' in webproject '"  +  shortName + "' does not exist.");
		return;
	}

	var webApp = args["webApp"];

	var asset;

	if(null != webApp)
	{
		asset = sandbox.getAssetWebApp(webApp, path);
	}
	else
	{
		asset = sandbox.getAsset(path);
	}

    if (null == asset)
    {
	    // Site cannot be found
	    status.setCode(status.STATUS_NOT_FOUND, "The asset '" + path + "' in webproject '"  +  shortName + "' does not exist.");
	    return;
    }

	// set model properties
	model.sandbox = sandbox;
	model.webproject = asset.getContent();   
	model.asset = asset;
}

main();
