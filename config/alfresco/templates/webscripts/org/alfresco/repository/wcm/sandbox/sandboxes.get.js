/*
 *  Get the sandboxes for a web project
 */  
function main()
{
	var shortName = url.extension.split("/")[0];

	var sandboxes;
	
	var webproject = webprojects.getWebProject(shortName);
	if (webproject == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The webproject " + shortName + " does not exist.");
		return;
	}
	
	var userFilter = args["userName"];
	
	if(userFilter != null)
	{
		sandboxes = webproject.getSandboxes(userFilter);
	}
	else
	{
		sandboxes = webproject.getSandboxes();
	}
	model.sandboxes = sandboxes;
	model.webproject = webproject;
}

main()
	
