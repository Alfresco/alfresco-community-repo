// Get the site id
var shortName = url.extension.split("/")[0];
var site = siteService.getSite(shortName);

// get the filters
var nameFilter = args["nf"];
var roleFilter = args["rf"];
var sizeString = args["size"];

// Get the filtered memeberships
var memberships = site.listMembers(nameFilter, roleFilter, sizeString != null ? parseInt(sizeString) : 0);

// Get a list of all the users resolved to person nodes
var peopleList = Array(memberships.length);
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
model.site = site;
model.memberships = mems;
model.people = peopleList;