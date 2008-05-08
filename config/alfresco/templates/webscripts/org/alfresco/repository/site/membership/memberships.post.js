// Get the site 
var shortName = url.extension.split("/")[0];
var site = siteService.getSite(shortName);

// Get the role 
var role = json.get("role");
var userName = json.getJSONObject("person").get("userName");

// Set the membership details
site.setMembership(userName, role);

// Pass the details to the template
model.site = site;
model.role = role;
model.person = people.getPerson(userName);