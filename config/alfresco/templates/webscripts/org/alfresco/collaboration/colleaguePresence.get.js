/*
 * colleaguePresence
 *
 * Inputs:
 *  mandatory: nodeRef = parent space nodeRef
 *
 * Outputs: colleaguePresence - object containing presence data model
 */
model.colleaguePresence = main(args["nodeRef"]);

function main(nodeRef)
{
   var space = search.findNode(nodeRef);
	var colleagues = {};

   if (space != null)
   {
      colleagues = parsePermissions(space);
   }

   if (person.assocs["cm:avatar"] != null)
   {
      model.img =  person.assocs["cm:avatar"][0].url;
   }
   
   var colleaguePresence =
   {
      "colleagues": colleagues
   };
   return colleaguePresence;
}

function parsePermissions(space)
{
	var tokens, user, group;
	var userHash = {};

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
					   userHash[user.name] = user;
					}
					else
					{
						group = people.getGroup(tokens[1]);
						if (group != null)
						{
							for each(user in people.getMembers(group))
							{
                        userHash[user.name] = user;
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
	
	return userHash;
}
