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
    var paths = [];
    
    var sites = siteService.listUserSites(user);
    for (var j=0; j < sites.length; j++)
    {
        paths.push("PATH:\"/app:company_home/st:sites/cm:" + search.ISO9075Encode(sites[j].shortName) + "/cm:calendar/*\"");
    }
    
    var results = [];
    
    if (paths.length != 0)
    {
        var luceneQuery = "+(" + paths.join(" OR ") + ") +TYPE:\"{http\://www.alfresco.org/model/calendar}calendarEvent\"";
        if (range.fromdate)
        {
            // Expects the date in the format yyyy/mm/dd
            var from = range.fromdate.split("/").join("\\-"); 
            var dateClause = " +@ia\\:fromDate:[" + from + "T00:00:00 TO 2099\\-1\\-1T00:00:00]";
            luceneQuery += dateClause;
        }
        results = search.luceneSearch(luceneQuery, "ia:fromDate", true);
    }
    
    return results;
}