function main()
{
	// Get the url values
	var urlElements = url.extension.split("/");
	var webProjectRef = urlElements[0];
	var userName = urlElements[2];
	
	// Get the web project
	var webproject = webprojects.getWebProject(webProjectRef);
	if (webproject == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The webproject: (" + webProjectRef + ") does not exist.");
		return;
	}
	
	var person = people.getPerson(userName);
	if (person == null)
	{
		// Person cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The person with user name (" + userName + ") does not exist.");
		return;
	}
	
	// Get the role of the user
	var role = webproject.getMembersRole(userName);
	if (role == null)
	{
		// Person is not a member of the site
		status.setCode(status.STATUS_NOT_FOUND, "The person with user name (" + userName + ") is not a member of the webproject " + webProjectRef );
		return;
	}
	
	// Pass the values to the template
	model.webproject = webproject;
	model.person = person;
	model.role = role;
}

main();
	