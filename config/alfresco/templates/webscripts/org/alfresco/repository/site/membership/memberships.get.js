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
	peopleList["_" + userName] = person; // make sure the keys are strings
}

// also copy over the memberships.
var mems = [];
for (userName in memberships)
{
   var membershipType = memberships[userName];
   mems["_" + userName] = membershipType; // make sure the keys are strings
}

// Pass the information to the template
model.site = site;
model.memberships = mems;
model.people = peopleList;

