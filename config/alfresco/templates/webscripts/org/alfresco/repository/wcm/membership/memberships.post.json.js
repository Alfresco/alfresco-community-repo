/**
 * Create a new web project membership
 * @return
 */

function main()
{
	// Get the web project
	var shortName = url.extension.split("/")[0];
	var webproject = webprojects.getWebProject(shortName);
	if (webproject == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The web project " + shortName + " does not exist.");
		return;
	}
	
	// Get the role 

	if (! json.has("role"))
	{
		status.setCode(status.STATUS_BAD_REQUEST, "The role has not been set.");
		return;
	}
	var role = json.get("role");
	
	// Get the person 
	if(! json.has("person"))
	{
		status.setCode(status.STATUS_BAD_REQUEST, "The person has not been set.");
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
		status.setCode(status.STATUS_NOT_FOUND, "The person with user name " + userName + " could not be found.");
		return;
	}
	
	// Set the membership details
	webproject.addMembership(userName, role);
	
	// Pass the details to the template
	model.webproject = webproject;
	model.role = role;
	model.person = person;
}

main();