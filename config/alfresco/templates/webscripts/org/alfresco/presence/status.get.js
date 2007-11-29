var results = new Array();
main();

function main()
{
	var nodeRef = args["nodeRef"];
	var space = search.findNode(nodeRef);
	model.space = space;

	parsePermissions(space);
	
	while ((space.inheritsPermissions) && (space.parent != null))
	{
		space = space.parent;
		parsePermissions(space);
	}
	
	model.presenceResults = results;
}

function parsePermissions(space)
{
	var tokens, user, group;

	try
	{
		for each(perm in space.permissions)
		{
			tokens = perm.split(";");
			if (tokens[0] == "ALLOWED")
			{
				if (("AllCollaboratorContributorCoordinatorEditor").indexOf(tokens[2]) != -1)
				{
					user = people.getPerson(tokens[1]);
					if (user != null)
					{
						pushUnique(user, presence.getDetails(user));
					}
					else
					{
						group = people.getGroup(tokens[1]);
						if (group != null)
						{
							for each(user in people.getMembers(group))
							{
								pushUnique(user, presence.getDetails(user));
							}
						}
					}
				}
			}	
		}
	}
	catch (e)
	{
	}
}

function pushUnique(user, details)
{
	var provider = String(details).split("|")[0];
	if (provider == "null")
	{
		provider = "none";
	}
	var fullName = user.properties["firstName"] + " " + user.properties["lastName"];
	
	for (i=0; i < results.length; i++)
	{
		if (results[i][1] == fullName)
		{
			return;
		}
	}
	results.push(new Array(provider, fullName, details));
}
