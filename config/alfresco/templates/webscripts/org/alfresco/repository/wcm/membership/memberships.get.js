/**
 * Get the memberships for a web project
 */  
function main()
{
	var shortName = url.extension.split("/")[0];

	var webproject = webprojects.getWebProject(shortName);
	if (webproject == null)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The webproject " + shortName + " does not exist.");
		return;
	}

	// Get all the memberships
	var memberships = webproject.listMembers();

	// Get a list of all the users resolved to person nodes
	var peopleList = Array();
	for (userName in memberships)
	{
		var person = people.getPerson(userName);
		peopleList["_" + userName] = person; // make sure the keys are strings
	}

	// also copy over the memberships.
	var mems = {};
	var pos = 0; // memberships[userName] won't return the correct value if userName is a digit-only value
	for (userName in memberships)
	{
		var membershipType = memberships[pos];
		mems["_" + userName] = membershipType; // make sure the keys are strings
		pos++;
	}

	// Pass the information to the template
	model.webproject = webproject;
	model.memberships = mems;
	model.peoplelist = peopleList;
}

main()