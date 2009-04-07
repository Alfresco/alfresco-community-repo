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
   // make sure the keys are strings - as usernames may be all numbers!
   peopleList['_' + userName] = person;
}

// also copy over the memberships.
var mems = {};
for (userName in memberships)
{
   var membershipType = memberships[userName];
   // make sure the keys are strings - as usernames may be all numbers!
   mems['_' + userName] = membershipType;
}

// Pass the information to the template
model.site = site;
model.memberships = mems;
model.peoplelist = peopleList;