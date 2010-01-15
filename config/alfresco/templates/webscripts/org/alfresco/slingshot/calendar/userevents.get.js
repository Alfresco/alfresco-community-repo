<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/calendar/lib/calendar.lib.js">
/** 
 * Limits the number of events that get returned.
 * TODO: have this supported in the Lucene query syntax
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
var siteId = url.templateArgs.site;

model.events = getUserEvents(username, siteId, range);

/**
 * calculates duration based on specified start and end dates
 * 
 * 
 * @method getDuration 
 * @param dtStartDate {Date} start date
 * @param dtEndDate {Date} end date
 * @return {String} Duration in ical format eg PT2H15M
 */
function getDuration(dtStartDate,dtEndDate)
{

    var DAY = "D";
    var WEEK = "W";
    var YEAR = "Y";
    var MONTH = "M";
    var HOUR = 'H';
    var SECOND = 'S';
    var MINUTE = 'Mn';
    
    var diff = dtEndDate.getTime() - dtStartDate.getTime() ;
    var dateDiff = {};
    var duration = 'P';
    var diff = new Date();
    diff.setTime(Math.abs(dtStartDate.getTime() - dtEndDate.getTime()));
    var timediff = diff.getTime();

    dateDiff[WEEK] = Math.floor(timediff / (1000 * 60 * 60 * 24 * 7));
    timediff -= dateDiff[WEEK] * (1000 * 60 * 60 * 24 * 7);

    dateDiff[DAY] = (Math.floor(timediff / (1000 * 60 * 60 * 24))); 
    timediff -= dateDiff[DAY] * (1000 * 60 * 60 * 24);

    dateDiff[HOUR] = Math.floor(timediff / (1000 * 60 * 60)); 
    timediff -= dateDiff[HOUR] * (1000 * 60 * 60);

    dateDiff[MINUTE] = Math.floor(timediff / (1000 * 60)); 
    timediff -= dateDiff[MINUTE] * (1000 * 60);

    dateDiff[SECOND] = Math.floor(timediff / 1000); 
    timediff -= dateDiff[SECOND] * 1000;

    if (dateDiff[WEEK]>0){
        duration+=dateDiff[WEEK]+WEEK;
    }
    if (dateDiff[DAY]>0){
        duration+=dateDiff[DAY]+DAY;
    }
    duration+='T';
    if (dateDiff[HOUR]>0){
        duration+=dateDiff[HOUR]+HOUR;
    }
    if (dateDiff[MINUTE]>0){
        duration+=dateDiff[MINUTE]+'M';
    }
    if (dateDiff[SECOND]>0){
        duration+=dateDiff[SECOND]+SECOND;
    }
    return duration;
};

function getUserEvents(user, siteId, range)
{
   if (!user)
   {
      return [];
   }
   
   var paths = [], site, siteTitles = {}, sites = [];
   
   if (siteId == null)
   {
      sites = siteService.listUserSites(user);
   }
   else
   {
      site = siteService.getSite(siteId);
      if (site != null)
      {
         sites.push(site);
      }
   }
   for (var j=0; j < sites.length; j++)
   {
      site = sites[j];
      paths.push("PATH:\"/app:company_home/st:sites/cm:" + search.ISO9075Encode(sites[j].shortName) + "/cm:calendar/*\"");
      siteTitles[site.shortName] = site.title;
   }
   
   var results = [];
   
   if (paths.length > 0)
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
   
   // repurpose results into custom array so as to add custom properties
   var events = [];
   for (var i=0;i<results.length;i++)
   {
      var event = {};
      var e = results[i];
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
      event.duration = getDuration(event.start,event.end);
      events.push(event);
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
   
   if (logger.isLoggingEnabled())
      logger.log("STARTTIME: " + startTime + " " + endTime + " " + (startTime == "0:0" && (startTime == endTime)));
   
   return (startTime == "0:0" && (startTime == endTime));
}