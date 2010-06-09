<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/calendar/lib/calendar.lib.js">
/** 
 * Limits the number of events that get returned.
 * TODO: have this supported in the Lucene query syntax
 */
model.limit = args.limit;

var DAY_MS = 24*60*60*1000; 

var days = new Object();
days["SU"]=0;
days["MO"]=1;
days["TU"]=2;
days["WE"]=3;
days["TH"]=4;
days["FR"]=5;
days["SA"]=6;

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
   
   var isSiteCalandarDashlet = (range.fromdate && range.fromdate.indexOf("-") != -1) ? true : false;
   
   if (paths.length > 0)
   {
      var luceneQuery = "+(" + paths.join(" OR ") + ") +TYPE:\"{http\://www.alfresco.org/model/calendar}calendarEvent\"";
      if (range.fromdate)
      {
         var dateClause = " +@ia\\:fromDate:[2008\\-1\\-1T00:00:00 TO 2099\\-1\\-1T00:00:00]";
         luceneQuery += dateClause;
      }
      results = search.luceneSearch(luceneQuery, "ia:fromDate", true);
   }
   
   // repurpose results into custom array so as to add custom properties
   var events = [];
   for (var i=0;i<results.length;i++)
   {
      var e = results[i];
      if (!isSiteCalandarDashlet)
      {
         var d  = new Date();
         var fromDate = range.fromdate.split("/");
         d.setMonth(fromDate[1] - 1);
         d.setYear(fromDate[0]);
         d.setDate(fromDate[2]);
         var d1 = cloneDate(e.properties["ia:fromDate"]);
         d1.setHours(0, 0, 0, 0);
         var d2 = cloneDate(d);
         d2.setHours(0, 0, 0, 0);
         if (d1 < d2 && e.properties["ia:recurrenceRule"] == null)
            continue;
      }

      var event = {};
      event.isoutlook = false;
      if (e.properties["ia:isOutlook"] != null && e.properties["ia:isOutlook"])
      {
         event.isoutlook = true;
      }

      event.name  = e.name;
      event.title = e.properties["ia:whatEvent"];
      event.where = e.properties["ia:whereEvent"] == null ? "" : e.properties["ia:whereEvent"];
      event.when = e.properties["ia:fromDate"]; 
      event.start = e.properties["ia:fromDate"];
      event.end = e.properties["ia:toDate"];
      event.site = e.parent.parent.name;
      event.siteTitle = siteTitles[event.site];    
      event.allday = (isAllDayEvent(e)) ? 'true' : 'false';
      event.tags = e.tags.join(' ');
      event.duration = getDuration(event.start,event.end);

      if (e.properties["ia:recurrenceRule"] != null)
      {
         event.recurrenceRule = e.properties["ia:recurrenceRule"];	
         if (e.properties["ia:recurrenceLastMeeting"] != null)
         {
            event.recurrenceLastMeeting = e.properties["ia:recurrenceLastMeeting"];
         }

         // Expects the date in the format yyyy-mm-dd
         var startMonth = new Date();
         var endMonth = new Date();
         var eventStartDates = [];
         if (isSiteCalandarDashlet)
         {
            var fromDate = range.fromdate.split("-"); 
            startMonth.setMonth(fromDate[1] - 1);
            startMonth.setYear(fromDate[0]);
            startMonth.setDate(fromDate[2]*1 + 10);
            startMonth.setDate(1);

            endMonth.setTime(startMonth.getTime());
            endMonth.setMonth(endMonth.getMonth() +1);

            eventStartDates = getNextEventStartDates(event, startMonth, endMonth, e, isSiteCalandarDashlet);
        }
        else
        {
           var fromDate = range.fromdate.split("/");
           startMonth.setMonth(fromDate[1] - 1);
           startMonth.setYear(fromDate[0]);
           startMonth.setDate(fromDate[2]);

           eventStartDates = getNextEventStartDates(event, startMonth, endMonth, e, isSiteCalandarDashlet);

           endMonth.setYear(2099);
           endMonth.setMonth(0);
       }

         for (var j=0; j<eventStartDates.length; j++ )
         {
            var recurEvent ={};
            recurEvent.name  = event.name;
            recurEvent.isoutlook = event.isoutlook;
            recurEvent.title = event.title;
            recurEvent.where = event.where;
            recurEvent.site = event.site;
            recurEvent.siteTitle = event.siteTitle;
            recurEvent.allday = event.allday;
            recurEvent.tags = event.tags;
            recurEvent.duration = event.duration;

            recurEvent.when = eventStartDates[j];
            recurEvent.start =  eventStartDates[j];

            var eventEnd = new Date();
            eventEnd.setTime(recurEvent.start.getTime());
            eventEnd.setTime(eventEnd.getTime() + (event.end.getTime() - event.start.getTime()));

            recurEvent.end = eventEnd;

            events.push(recurEvent);

            if (!isSiteCalandarDashlet)
            {
               recurEvent.title = event.title + " (Recurring)";
               break;
            }
         }
      }
      else
      {
         events.push(event);
      }
   }
   
   return events;
};

/**
* Clone provided date objet
*/
function cloneDate(date)
{

   var result = new Date();
   result.setTime(date.getTime());

   return result; // Date or null
};
        
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
};


/**
*	Return next occurence of provided event after currentDate.
*/
function getNextEventStartDates(event, currentDate, endMonth, eventNode, isSiteCalandarDashlet)
{       
   var rubeResult = [];
   var result = [];
   var recurrenceRule = event.recurrenceRule;
   if (recurrenceRule)
   {
      var evStart = cloneDate(event.when);

      //If event starts in future, then start search from its start date
      if (evStart >= currentDate && !isSiteCalandarDashlet)
      {
         currentDate = evStart;
      }
      var parts = recurrenceRule.split(";");
      var eventParam = new Object();

      for (var i = 0; i < parts.length; i++)
      {
         var part = parts[i].split("=");
         eventParam[part[0]]= part[1];
      }

      var endDate = new Date();
      endDate.setTime(currentDate.getTime());
      var needCycle = true;

      while(needCycle)
      {
         //Get all events between currentDate and currentDate + event interval.
         //There must be at least one event.
         if (eventParam['FREQ']=="WEEKLY")
         {
            endDate.setTime(endDate.getTime() + eventParam['INTERVAL'] * DAY_MS * 7);
            if (isSiteCalandarDashlet)
            {
               endDate = endMonth;
            }

            rubeResult =  this.resolveStartDateWeekly(event, eventParam, currentDate, endDate);
         }
         else  if (eventParam['FREQ']=="DAILY")
         {
            endDate.setTime(endDate.getTime() + eventParam['INTERVAL'] * DAY_MS);
            if (isSiteCalandarDashlet)
            {
               endDate = endMonth;
            }

            rubeResult =  this.resolveStartDaily(event, eventParam, currentDate, endDate);
         }
         else  if (eventParam['FREQ']=="MONTHLY")
         {
            endDate.setMonth(endDate.getMonth() + eventParam['INTERVAL'] * 1);
            if (isSiteCalandarDashlet)
            {
               endDate = endMonth;
            }

            rubeResult =  this.resolveStartMonthly(event, eventParam, currentDate, endDate, isSiteCalandarDashlet);
         }

         if (rubeResult.length > 0)
         {
            //sort rubeResult
            for (var i = 0; i < rubeResult.length -1; i++)
            {
               for(var j = i + 1; j < rubeResult.length; j++)
               {
                  if(rubeResult[i] > rubeResult[j])
                  {
                     var tmp = rubeResult[i];
                     rubeResult[i] = rubeResult[j];
                     rubeResult[j] = tmp;
                  }
               }
            }

            var ignoreEvents = {};
            if (eventNode.children != null)
            {
               var childrenEvents = eventNode.children; 

               for (var j=0; j<childrenEvents.length; j++)
               {
                  var fullDate = childrenEvents[j].properties["ia:date"];
                  ignoreEvents[fullDate.getFullYear()+ " " + fullDate.getMonth()+ " " + fullDate.getDate()] = 1;
               }

               for(var i=0; i<rubeResult.length; i++)
               {
                  var dateForCheck = rubeResult[i];
                  if (!ignoreEvents[dateForCheck.getFullYear()+ " " + dateForCheck.getMonth()+ " " + dateForCheck.getDate()])
                  {
                     result.push(rubeResult[i]);
                  }
               }
            }
            else {result = rubeResult;}

            if (result.length == 1)
            {
               needCycle = false;
            }
            else
            {
               currentDate = endDate;
            }
         }
         else
         {
            needCycle = false;
         }
      }
   }
   else
   {
      var eventDate = cloneDate(event.when);

      if ((eventDate.getTime() >= startMonth.getTime()) && (eventDate.getTime() < endMonth.getTime())) 
      {
         result.push(eventDate.getDate());
      }
   }

   return result;
};

/**
 * Return all days between currentDate and endDate when weekly event occurs. 
 * 
 * 
 * @method resolveStartDatesWeekly 
 * @param ev {Object} object that represent weekly event
 * @eventParam Map of event parameters taken from RRULE
 * @param currentDate {Date} first day that event may occur.
 * @param endDate {Date} last day that event may occur.
 *
 * @return {Array} Array that contains days beetwing currentDate and endDate on wich weekly event occurs
 */
function resolveStartDateWeekly (ev, eventParam, currentDate, endDate)
{
   var result = [];

   var eventDays = eventParam['BYDAY'].split(",");
   var interval = eventParam['INTERVAL'];
   var lastEventDay = this.getLastEventDay(ev, currentDate, endDate);

   if (lastEventDay == -1)
   {
      return result;
   }

   var eventStart = cloneDate(ev.when);

   //Add as much full event cycles as need
   if (eventStart.getTime() < currentDate.getTime())
   {
      var duration = Math.floor((currentDate.getTime() - eventStart.getTime())/(interval * 7 * DAY_MS));
      var offset = duration * DAY_MS;
      eventStart.setTime(eventStart.getTime() + offset * interval * 7);

   }
   if (eventStart.getTime() > endDate.getTime())
   {
      return result;
   }

   var eventStartDay = eventStart.getDay();
   eventStart.setTime(eventStart.getTime() - eventStartDay * DAY_MS);

   var eventStartDays   = [];
   for(var i = 0; i < eventDays.length; i++)
   {
      dayOfWeek = days[eventDays[i]];
      eventStartDays.push(dayOfWeek);
   }

   for (var i = 0; i < eventStartDays.length; i++)
   {
      var eventDate = new Date();   
      eventDate.setTime(eventStart.getTime() + eventStartDays[i] * DAY_MS);

      while (eventDate.getTime() - lastEventDay.getTime() < DAY_MS)
      {
         if (eventDate.getTime() >= currentDate.getTime() && eventDate.getTime() >= cloneDate(ev.when).getTime())
         {
            var dateToAdd = new Date();
            dateToAdd.setTime(eventDate.getTime())
            result.push(dateToAdd);
         }
         eventDate.setTime(eventDate.getTime() + 7 * interval * DAY_MS);
      }
   }
   return result;
};

/**
 * Return all days between currentDate and endDate when daily event occurs. 
 * 
 * 
 * @method resolveStartDaily 
 * @param ev {Object} object that represent daily event
 * @eventParam Map of event parameters taken from RRULE
 * @param currentDate {Date} first day when event may occur.
 * @param endDate {Date} last day when event may occur.
 *
 * @return {Array} Array that contains days beetwing currentDate and endDate on wich daily event occurs
 */
function resolveStartDaily (ev, eventParam, currentDate, endDate)
{  
   var result = [];

   var interval = eventParam['INTERVAL'];
   var lastEventDay = this.getLastEventDay(ev, currentDate, endDate);

   if (lastEventDay == -1)
   {
      return result;
   }
//--------------------
   var eventStart = cloneDate(ev.when);

   //Add as much full event cycles as need
   if (eventStart.getTime() < currentDate.getTime())
   {
      var duration = Math.floor((currentDate.getTime() - eventStart.getTime())/(interval * DAY_MS));
      var offset = duration * DAY_MS;
      eventStart.setTime(eventStart.getTime() + offset * interval);
      if (eventStart.getTime() < currentDate.getTime())
      {
         eventStart.setTime(eventStart.getTime() + interval * DAY_MS);
      }
   }

   if (eventStart.getTime() > endDate.getTime())
   {
      return result;
   }
   var eventDate = eventStart;
//-------------------
   while (eventDate.getTime() - lastEventDay.getTime() < DAY_MS)
   {
      var dateToAdd = new Date();
      dateToAdd.setTime(eventDate.getTime())
      result.push(dateToAdd);
      eventDate.setTime(eventDate.getTime() + interval * DAY_MS);
   }

   return result;
};

/**
 * Return all days between currentDate and endDate when monthly event occurs. 
 * 
 * 
 * @method resolveStartMonthly 
 * @param ev {Object} object that represent monthly event
 * @eventParam Map of event parameters taken from RRULE
 * @param currentDate {Date} first day when event may occur
 * @param endDate {Date} last day when event may occur
 * @param isSiteCalandarDashlet {boolean} if current dashlet is SiteCalendar
 *
 * @return {Array} Array that contains days beetwing currentDate and endDate on wich monthly event occurs
 */
function resolveStartMonthly (ev, eventParam, currentDate, endDate, isSiteCalandarDashlet)
{    
   var result = [];

   var interval = eventParam['INTERVAL'];

   var eventStart = cloneDate(ev.when);

   var lastEventDay = this.getLastEventDay(ev, currentDate, endDate);
   if (lastEventDay == -1)
   {
      return result;
   }

   var offset = ((currentDate.getFullYear() * 12 + currentDate.getMonth()) - (eventStart.getFullYear() * 12 + eventStart.getMonth())) % interval;
   if (offset > 0)
   {
      if (isSiteCalandarDashlet)
      {
         return result();
      }
      var monthToAdd = interval - offset;
      currentDate.setMonth(currentDate.getMonth() + monthToAdd);
   }

   var resultDate = currentDate;
   resultDate.setDate(eventStart.getDate());
   if (eventParam['BYDAY'])
   {
      var allowedDayNames = eventParam['BYDAY'].split(",");
      var allowedDays = new Object();

      for (var i = 0; i < allowedDayNames.length; i++)
      {
         allowedDays[days[allowedDayNames[i]]] = 1;
      }

      var dayInWeek = eventParam['BYSETPOS'] * 1;

      currentDate.setDate(1);

      while (dayInWeek > 0)
      {
         if (allowedDays[currentDate.getDay()] == 1)
         {
            dayInWeek--;
         }

         if (dayInWeek > 0)
         {
            currentDate.setDate(currentDate.getDate() + 1);
         }
      }

      if (dayInWeek == -1)
      {
         currentDate.setMonth(currentDate.getMonth() + 1);
         currentDate.setTime(currentDate.getTime() - DAY_MS);
                                    
         while (allowedDays[currentDate.getDay()] == undefined)
         {
            currentDate.setTime(currentDate.getTime() - DAY_MS);
         }

      }
      resultDate = currentDate;
   }

   if (resultDate.getTime() - lastEventDay.getTime() < DAY_MS)
   {
      if (currentDate > resultDate && !isSiteCalandarDashlet)
      {
         resultDate.setMonth(resultDate.getMonth() + interval);
      }
      result.push(resultDate);
   }

   return result;
};

/**
 * Return last day between currentDate and endDate when event may occur.
 * 
 * 
 * @method getLastEventDay 
 * @param ev {Object} object that represent monthly event
 * @param currentDate {Date} first day when event may occur.
 * @param endDate {Date} last day when event may occur.
 *
 * @return {Date} last day in current month whe event may occur.
 */	
function getLastEventDay (ev, currentDate, endDate)
{
   var lastEventDay = new Date(endDate);

   if ((ev.recurrenceLastMeeting) )
   {
      var lastAllowedDay = cloneDate(ev.recurrenceLastMeeting);

      if (lastAllowedDay.getTime() < currentDate.getTime() || cloneDate(ev.when).getTime() > endDate.getTime())
      {
         return -1;
      }
      if ((lastAllowedDay.getTime() < endDate.getTime()))
      {
         lastEventDay = new Date(lastAllowedDay);
      }
   }

   return lastEventDay;

};