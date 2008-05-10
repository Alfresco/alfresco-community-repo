function main()
{
	// Get the url values
	var urlElements = url.extension.split("/");
	var shortName = urlElements[0];
	var userName = urlElements[2];
	
	// Get the site
	var site = siteService.getSite(shortName);
	if (site == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The site " + shortName + " does not exist.");
		return;
	}
	
	var person = people.getPerson(userName);
	if (person == null)
	{
		// Person cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The person with user name " + userName + " does not exist.");
		return;
	}
	
	// Get the role of the user
	var role = site.getMembersRole(userName);
	if (role == null)
	{
		// Person is not a member of the site
		status.setCode(status.STATUS_NOT_FOUND, "The person with user name " + userName + " is not a member of the site " + shortName);
		return;
	}
	
	// Pass the values to the template
	model.site = site;
	model.person = person;
	model.role = role;
}

main();
	