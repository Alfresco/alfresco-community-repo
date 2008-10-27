
/**
 * PUT (UPDATE) wcm web project
 * @return the updatedwcm web project 
 */ 

function main() {
	
	// Get the webproject ref
	var webprojectref = url.extension;
	
	if (webprojectref == null || webprojectref.length == 0) 
	{
		status.setCode(status.STATUS_BAD_REQUEST, "Delete web project.   webprojectref missing or blank");
		return;
	}
	
	var webproject = webprojects.getWebProject(webprojectref);
	
	if(webproject == null)
	{
	    // Return 404
	    status.setCode(status.STATUS_NOT_FOUND, "Webproject " + webprojectref + " does not exist");
	    return;
    }

	if (json.has("name") )
	{
		var name = json.get("name");
		webproject.setName(name);
	}
	if (json.has("title"))
	{
		var title = json.get("title");
		webproject.setTitle(title);
	}
	if(json.has("description"))
	{
		var description = json.get("description");
		webproject.setDescription(description);
	}
	
	// update the web project
	webproject.save();
    status.setCode(status.STATUS_OK, "Webproject " + webprojectref + " updated");
	
	// Set Return value
	model.webproject = webproject;	
}

main()
