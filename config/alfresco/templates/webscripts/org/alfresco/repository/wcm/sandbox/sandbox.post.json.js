
/**
 * Post (create) wcm web project
 * @return the wcm web project ref
 */ 

function main() {
	
	if (!json.has("userName"))
	{
		status.setCode(status.STATUS_BAD_REQUEST, "property 'userName' missing when creating sandbox");
		return;
	}

	var userName = json.get("userName");
	
	if (userName.length == 0)
	{
		status.setCode(status.STATUS_BAD_REQUEST, "property 'userName' blank when creating sandbox");
		return;
	}
	
	var shortName = url.extension.split("/")[0];
	
	var webproject = webprojects.getWebProject(shortName);
	if (webproject == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The webproject " + shortName + " does not exist.");
		return;
	}
	
	var sandbox = webproject.createSandbox(userName);	
	
	// Set Return value
	model.webproject = webproject;	
	model.sandbox = sandbox;
}

main()
