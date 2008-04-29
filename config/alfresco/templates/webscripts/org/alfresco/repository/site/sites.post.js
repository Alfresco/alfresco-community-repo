// Get the details of the site
var shortName = json.get("shortName");
var sitePreset = json.get("sitePreset");
var title = json.get("title");
var description = json.get("description");
var isPublic = json.getBoolean("isPublic");

// Create the site 
var site = siteService.createSite(sitePreset, shortName, title, description, isPublic);

// Put the created site into the model
model["site"] = site;