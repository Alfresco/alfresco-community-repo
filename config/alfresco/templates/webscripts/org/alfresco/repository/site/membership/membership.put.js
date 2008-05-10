function main()
{
	// Get the site 
	var shortName = url.extension.split("/")[0];
	var site = siteService.getSite(shortName);
	if (site == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The site " + shortName + " does not exist.");
		return;
	}
	
	// Get the role 
	var role = json.get("role");
	if (role == null)
	{
		status.setCode(status.STATUS_BAD_REQUEST, "The role has not been set.");
		return;
	}
	
	// Get the user name
	var userName = json.getJSONObject("person").get("userName");
	if (userName == null)
	{
		status.setCode(status.STATUS_BAD_REQUEST, "The user name has not been set.");
		return;
	}
	var person = people.getPerson(userName);
	if (person == null)
	{
		status.setCode(status.STATUS_BAD_REQUEST, "The person with user name " + userName + " could not be found.");
		return;
	}
	
	// Set the membership details
	site.setMembership(userName, role);
	
	// Pass the details to the template
	model.site = site;
	model.role = role;
	model.person = person;
}

main();