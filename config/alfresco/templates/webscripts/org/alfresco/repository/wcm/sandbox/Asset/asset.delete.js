/*
 *  Delete the specified asset from within a sandbox
 */  
function main()
{
	var urlElements = url.extension.split("/");
	var shortName = urlElements[0];
	var boxName = urlElements[2];
	var path = url.extension


	
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
	
	
	if(webApp != null)
	{
		sandbox.					   
	}
	else
	{

	}
	
	// set model properties
	model.sandbox = sandbox;
	model.webproject = webproject;
	
}

main()
	
