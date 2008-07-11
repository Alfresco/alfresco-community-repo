/** 
 * Limits the number of events that get returned.
 * Would be nice to have this supported in the Lucene query syntax
 */
model.limit = args.limit;

// Get the username of the currently logged in person
var username = person.properties["cm:userName"];

var range = {};
var dateFilter = args.from;
if (dateFilter)
{
	range["fromdate"] = dateFilter;
}

model.events = getUserEvents(username, range);

function getUserEvents(user, range)
{
	if (!user)
	{
		return [];
	}
	
	var paths = [];
	/**
	 * This part is inefficient as it looks through all of the sites
	 * and tries to determine if the user is a member or not; however, until something like
	 * /people/{userid}/sites is exposed through the JavaScript API, it will have to do.
	 * 
	 */
	var availableSites = siteService.listSites(null, null);
	for (var j=0; j < availableSites.length; j++)
	{
		var site = availableSites[j];
		if (site.isMember(user))
		{
			paths.push("PATH:\"/app:company_home/st:sites/cm:" + site.shortName + "/cm:calendar/*\"");
		}
	}
	
	var results = [];
	
	if (paths.length > 0)
	{
		var luceneQuery = "+(" + paths.join(" OR ") + ") +TYPE:\"{com.infoaxon.alfresco.calendar}calendarEvent\"";
		if (range.fromdate)
		{
			// Expects the date in the format yyyy/mm/dd
			var from = range.fromdate.split("/").join("\\-"); 
			var dateClause = " +@ia\\:fromDate:[" + from + "T00:00:00 TO 2032\\-1\\-1T00:00:00]";
			luceneQuery += dateClause;
		}
		results = search.luceneSearch(luceneQuery, "ia:fromDate", true);
	}

	return results;
}

