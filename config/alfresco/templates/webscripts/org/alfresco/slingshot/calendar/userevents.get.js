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

   var paths = [], events = [], event, results, result, j, jj, luceneQuery, siteTitles = {};

   var sites = siteService.listUserSites(user), site;
   for (j = 0, jj = sites.length; j < jj; j++)
   {
      site = sites[j];
      paths.push("PATH:\"/app:company_home/st:sites/cm:" + search.ISO9075Encode(site.shortName) + "/cm:calendar/*\"");
      siteTitles[site.shortName] = site.title;
   }

   if (paths.length != 0)
   {
      luceneQuery = "+(" + paths.join(" OR ") + ") +TYPE:\"{http\://www.alfresco.org/model/calendar}calendarEvent\"";
      if (range.fromdate)
      {
         // Expects the date in the format yyyy/mm/dd
         var from = range.fromdate.split("/").join("\\-"); 
         var dateClause = " +@ia\\:fromDate:[" + from + "T00:00:00 TO 2099\\-1\\-1T00:00:00]";
         luceneQuery += dateClause;
      }
      results = search.luceneSearch(luceneQuery, "ia:fromDate", true);

      for (j = 0, jj = events.length; j < jj; j++)
      {
    		event = {};
    		result = results[i];
         event.name  = e.name;
         event.title = e.properties["ia:whatEvent"];
         event.where = e.properties["ia:whereEvent"];
         event.when = e.properties["ia:fromDate"];
         event.start = e.properties["ia:fromDate"];
         event.end = e.properties["ia:toDate"];
         event.site = e.parent.parent.name;
         event.siteTitle = siteTitles[event.site];
         event.allday = (isAllDayEvent(e)) ? 'true' : 'false';
         event.tags = e.tags.join(' ');
         events.push(event);
      }
   }

   return events;
}

/**
 * NOTE: Another option would be to add an "all day" property to the
 * existing calendar model.
 */
function isAllDayEvent(event)
{
   var startDate = event.properties["ia:fromDate"];
   var endDate = event.properties["ia:toDate"];
   
   var startTime = startDate.getHours() + ":" + startDate.getMinutes();
   var endTime = endDate.getHours() + ":" + endDate.getMinutes();
   
   logger.log("STARTTIME: " + startTime + " " + endTime + " " + (startTime == "0:0" && (startTime == endTime)));
  
   return (startTime == "0:0" && (startTime == endTime));
}