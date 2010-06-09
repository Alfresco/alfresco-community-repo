<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/calendar/lib/calendar.lib.js">
/**
 * Update event properties
 * @method GET
 * @param uri {string} /calendar/event/{siteid}/{eventname}
 */

/* Format and return error object */
function jsonError(errorString)
{
   var obj =
   {
      "error": errorString
   };
   
   return obj;
}

// TODO: refactor as this method is used in several places
function getTemplateParams()
{
   // Grab the URI parameters
   var siteid = "" + url.templateArgs.siteid;
   var eventname = "" + url.templateArgs.eventname;

   if (siteid === null || siteid.length === 0)
   {
      return null;
   }

   if (eventname === null || eventname.length === 0)  
   {
      return null;
   }

   return {
      "siteid": siteid,
      "eventname": eventname
   };
}

function main()
{
   var params = getTemplateParams();
   if (params === null)
   {
      return jsonError("No parameters supplied");
   }

   // Get the site
   var site = siteService.getSite(params.siteid);
   if (site === null)
   {
      return jsonError("Could not find site: " + siteid);
   }

   var eventsFolder = getCalendarContainer(site);
   if (eventsFolder === null)
   {
      return jsonError("Could not locate events container");
   }

   var event = eventsFolder.childByNamePath(params.eventname);
   if (event === null)
   {
      return jsonError("Could not find event: " + params.eventname);
   }
   
   var docfolder = "";
   if (event.properties["ia:docFolder"] != null)
   {
      docfolder = event.properties["ia:docFolder"];
   }
   
   var recurrence = "";
   if (event.properties["ia:recurrenceRule"] != null)
   {
      recurrence = buildRecurrenceString(event.properties["ia:recurrenceRule"], event);
   }
   var isoutlook = false;
   if (event.properties["ia:isOutlook"] != null)
   {
      isoutlook = event.properties["ia:isOutlook"];
   }
   var result =
   {
      "name": event.name,
      "what": event.properties["ia:whatEvent"], 
      "description": event.properties["ia:descriptionEvent"],
      "location": event.properties["ia:whereEvent"] == null ? "" : event.properties["ia:whereEvent"],
      "from": event.properties["ia:fromDate"],
      "to": event.properties["ia:toDate"],
      "tags": event.tags,
      "allday":isAllDayEvent(event),
      "docfolder": docfolder,
      "recurrence": recurrence,
      "isoutlook": isoutlook
   };
 
   return result;
}

var result = main();
model.result = result;

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
   
   logger.log("STARTTIME: " + startTime + " " + endTime + " " + (startTime == endTime));
  
   return (startTime == endTime);
}
/**
 * Build recurrence string for share presentation
 
 * @param {String}  The recurrence rule
 * @param {Event}  The recurrence event
 */
function buildRecurrenceString(recurrence, event)
{
   var days = new Object();
   days["SU"]= utils.toLocalizedString("day.SU");
   days["MO"]= utils.toLocalizedString("day.MO");
   days["TU"]= utils.toLocalizedString("day.TU");
   days["WE"]= utils.toLocalizedString("day.WE");
   days["TH"]= utils.toLocalizedString("day.TH");
   days["FR"]= utils.toLocalizedString("day.FR");
   days["SA"]= utils.toLocalizedString("day.SA");
   
   var finalString = "";
   
   var parts = recurrence.split(";");
   var eventParam = new Object();

   for (var i=0; i < parts.length; i++)
   {
      var part = parts[i].split("=");
      eventParam[part[0]] = part[1];
   }
   
   if (eventParam['FREQ'] == "WEEKLY")
   {
      if (eventParam['INTERVAL'] == 1)
      {
         //finalString = "Occurs each week on ";
         finalString = utils.toLocalizedString('occurs.each.week.on');
      }
      else
      {
         //finalString = "Occurs every " + eventParam['INTERVAL'] + " weeks on ";
         finalString = utils.toLocalizedString('occurs.every.weeks.on', eventParam['INTERVAL']);
      }

      currentDays = eventParam['BYDAY'].split(","); 
      for (var i = 0; i < currentDays.length; i++)
      {
         finalString += days[currentDays[i]] + ", ";
      }
   }
   
   if (eventParam['FREQ'] == "DAILY")
   {
      //finalString += "Occurs every day ";
      finalString += utils.toLocalizedString('occurs.every.day');
   }
   
   if (eventParam['FREQ'] == "MONTHLY")
   {
      if (eventParam['BYMONTHDAY'] != null)
      {
         //finalString += "Occurs day " + eventParam['BYMONTHDAY'];
         finalString += utils.toLocalizedString('occurs.day', eventParam['BYMONTHDAY']);
      }

      if (eventParam['BYSETPOS'] != null)
      {
         //finalString += "Occurs the" + eventParam['BYSETPOS'] + " " + days[currentDays[i]];
         finalString += utils.toLocalizedString('occurs.the', eventParam['BYMONTHDAY'], days[currentDays[i]]);
      }
      //finalString += " of every " + eventParam['INTERVAL'] + " month(s) ";
      finalString += utils.toLocalizedString('of.every.month', eventParam['INTERVAL']);
   }
   
   if (eventParam['FREQ'] == "YEARLY")
   {
      if (eventParam['BYMONTHDAY'] != null)
      {
         //finalString += "Occurs every " + eventParam['BYMONTHDAY'] + "." + eventParam['BYMONTH'] + " ";
         finalString += utils.toLocalizedString('occurs.every', eventParam['BYMONTHDAY'], eventParam['BYMONTH']);
      }
      else
      {
        // finalString += "Occurs the " + eventParam['BYSETPOS'] + " " + days[currentDays[i]]  + " of " +  eventParam['BYMONTH'] + " month ";
        finalString += utils.toLocalizedString('occurs.the.of.month', eventParam['BYSETPOS'], days[currentDays[i]], eventParam['BYMONTH']);	 
      }
   }
   
   //finalString += "effective " + format(event.properties["ia:fromDate"], "dd.mm.yyyy");
   finalString += utils.toLocalizedString('effective', format(event.properties["ia:fromDate"], "dd.mm.yyyy"));
   if (eventParam['COUNT'] != null)
   {
      //finalString += " until " + format(event.properties["ia:recurrenceLastMeeting"], "dd.mm.yyyy");
      finalString += utils.toLocalizedString('until', format(event.properties["ia:recurrenceLastMeeting"], "dd.mm.yyyy"));
   }
   //finalString += " from " + format(event.properties["ia:fromDate"], "hh:nn") + " to " + format(event.properties["ia:toDate"], "hh:nn");
   finalString += utils.toLocalizedString('from.to', format(event.properties["ia:fromDate"], "hh:nn"), format(event.properties["ia:toDate"], "hh:nn"));
   
   return finalString;
}

/**
 * Format the date by pattern
 
 * @param {Date} The date object for format
 * @param {String}   The date pattern
 * @return {String}  Formated date by pattern
 */
function format(date, pattern)
{
   if (!date.valueOf())
      return ' ';

   return pattern.replace(/(yyyy|mm|dd|hh|nn)/gi,
      function($1)
      {
         switch ($1.toLowerCase())
         {
            case 'yyyy': return date.getFullYear();
            case 'mm':   return (date.getMonth() < 9 ? '0' : '') + (date.getMonth() + 1);
            case 'dd':   return (date.getDate() < 10 ? '0' : '') + date.getDate();
            case 'hh':   return (date.getHours() < 10 ? '0' : '') + date.getHours();
            case 'nn':   return (date.getMinutes() < 10 ? '0' : '') + date.getMinutes();
         }
     }
   );
}