
/**
 * Delete a wcm web project sandbox
 */ 

function main() {


	var urlElements = url.extension.split("/");
	var webprojectref = urlElements[0];
	var boxName = urlElements[2];
	
	if (webprojectref == null || webprojectref.length == 0) 
	{
		status.setCode(status.STATUS_BAD_REQUEST, "Delete sandbox.   webprojectref missing or blank");
		return;
	}
	
	var webproject = webprojects.getWebProject(webprojectref);

	
	if(webproject != null)
	{
		var sandbox = webproject.getSandbox(boxName);
		if(sandbox != null)
		{
			sandbox.deleteSandbox();
			status.setCode(status.STATUS_OK, "Webproject " + webprojectref + " deleted");
		}
		else
		{
			status.setCode(status.STATUS_NOT_FOUND, "Unable to delete : Webproject: " + webprojectref + "Sandbox: " + boxName + ", does not exist");	
		}
	}
    else
    {
	    // Return 404
	    status.setCode(status.STATUS_NOT_FOUND, "Unable to delete : Webproject: " + webprojectref + " does not exist");
	    return;
    }
}

main()
