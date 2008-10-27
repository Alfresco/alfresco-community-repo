
/**
 * Get wcm web project
 * @return the wcm web project
 */ 

function getWebProject(webprojectref){
	
	var service = webprojects;
	
	var data = service.getWebProject(webprojectref);
	
	return data;
}

function main() {

	// Get the siteref
	var webprojectref = url.extension;

	//var webproject = data;
	var webproject = getWebProject(webprojectref);

	if (webproject != null)
	{
		// Pass the webproject to the diaplay template
		model.webproject = webproject;	
    }
    else
    {
	    // Return 404
	    status.setCode(404, "Webproject " + webprojectref + " does not exist");
	    return;
    }
}

main()
