
/**
 * Delete a wcm web project
 */ 

function getWebProject(webprojectref){
	
	var service = webprojects;
	
	var data = service.getWebProject(webprojectref)
	
	return data;
}

function main() {

	// Get the webproject ref
	var webprojectref = url.extension;
	
	if (webprojectref == null || webprojectref.length == 0) 
	{
		status.setCode(status.STATUS_BAD_REQUEST, "Delete web project.   webprojectref missing or blank");
		return;
	}
	
	var webproject = webprojects.getWebProject(webprojectref);
	
	if(webproject != null)
	{
		webproject.deleteWebProject();
		status.setCode(status.STATUS_OK, "Webproject " + webprojectref + " deleted");
	}
    else
    {
	    // Return 404
	    status.setCode(status.STATUS_NOT_FOUND, "Unable to delete : Webproject: " + webprojectref + " does not exist");
	    return;
    }
}

main()
