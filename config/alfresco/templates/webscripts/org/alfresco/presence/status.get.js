main();

function main()
{
	var nodeRef = args["nodeRef"];
	var space = search.findNode(nodeRef);
	var tokens, user, group;
	var results = new Array();

	for each(perm in space.permissions)
	{
		tokens = perm.split(";");
		if (tokens[0] == "ALLOWED")
		{
			if (("CollaboratorContributorCoordinatorEditor").indexOf(tokens[2]) != -1)
			{
				user = people.getPerson(tokens[1]);
				if (user != null)
				{
					pushUnique(results, user, presence.getDetails(user));
				}
				else
				{
					group = people.getGroup(tokens[1]);
					if (group != null)
					{
						for each(user in people.getMembers(group))
						{
							pushUnique(results, user, presence.getDetails(user));
						}
					}
				}
			}
		}	
	}
	
	model.space = space;
	model.presenceResults = results;
}

function pushUnique(results, user, details)
{
	var fullName = user.properties["firstName"] + " " + user.properties["lastName"];
	
	for (i=0; i < results.length; i++)
	{
		if (results[i][0] == fullName)
		{
			return;
		}
	}
	results.push(new Array(fullName, details));
}
