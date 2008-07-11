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

   var calendar = site.getContainer("calendar");
   if (calendar === null)
   {
      return [];
   }
   
   if (!calendar.isTagScope)
   {
      calendar.isTagScope = true;
   }

   var query = "+PATH:\"/app:company_home/st:sites/cm:" + site.shortName + "/cm:calendar/*\" ";
   query += "+TYPE:\"{com.infoaxon.alfresco.calendar}calendarEvent\"";

   return search.luceneSearch(query, "ia:fromDate", true);
};


