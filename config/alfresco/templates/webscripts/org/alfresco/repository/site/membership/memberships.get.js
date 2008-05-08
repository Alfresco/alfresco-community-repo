// Get the site id
var shortName = url.extension.split("/")[0];
var site = siteService.getSite(shortName);

// TODO get the filters

// Get all the memeberships
var memberships = site.listMembers(null, null);

// Get a list of all the users resolved to person nodes
var peopleList = Array();
for (userName in memberships)
{
	var person = people.getPerson(userName);
	peopleList[userName] = person;
}

// Pass the information to the template
model.site = site;
model.memberships = memberships;
model.people = peopleList;