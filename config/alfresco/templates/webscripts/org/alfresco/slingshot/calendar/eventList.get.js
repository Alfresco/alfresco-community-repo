<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/calendar/lib/calendar.lib.js">

var siteId = args["site"];

model.events = getEvents(siteId);
model.siteId = siteId;

function getEvents(siteId)
{
   var site = siteService.getSite(siteId);
   if (site === null)
   {
      return [];
   }

   var calendar = getCalendarContainer(site);
   if (calendar === null)
   {
      return [];
   }

   var query = "+PATH:\"/app:company_home/st:sites/cm:" + search.ISO9075Encode(site.shortName) + "/cm:calendar/*\" ";
   query += "+TYPE:\"{com.infoaxon.alfresco.calendar}calendarEvent\"";

   var results = search.luceneSearch(query, "ia:fromDate", true);
   var e, events = [];
   
   for (var i=0; i < results.length; i++)
   {
      e = results[i];
      events.push({
         "event": e,
         "fromDate": e.properties["ia:fromDate"],
         "tags": e.tags
      });
   }
   
   return events;
};


