<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/calendar/lib/calendar.lib.js">
/**
 * Update event properties
 *
 * NOTE: Known issue; the recurrence rule strings are not localized. See ALF-6173
 *
 * @method GET
 * @param uri {string} /calendar/event/{siteid}/{eventname}
 */

/* Format and return error object */
function jsonError(errorString)
{
   return (
   {
      error: errorString
   });
}

// TODO: refactor as this method is used in several places
function getTemplateParams()
{
   // Grab the URI parameters
   var siteid = "" + url.templateArgs.siteid,
      eventname = "" + url.templateArgs.eventname;

   if (siteid === null || siteid.length === 0)
   {
      return null;
   }

   if (eventname === null || eventname.length === 0)
   {
      return null;
   }

   return (
   {
      siteid: siteid,
      eventname: eventname
   });
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

   var isoutlook = (event.properties["ia:isOutlook"] != null && event.properties["ia:isOutlook"]);

   return (
   {
      name: event.name,
      what: event.properties["ia:whatEvent"], 
      description: event.properties["ia:descriptionEvent"],
      location: event.properties["ia:whereEvent"] == null ? "" : event.properties["ia:whereEvent"],
      from: event.properties["ia:fromDate"],
      to: event.properties["ia:toDate"],
      tags: event.tags,
      allday: isAllDayEvent(event),
      docfolder: docfolder,
      recurrence: recurrence,
      isoutlook: isoutlook
   });
}

model.result = main();

/**
 * NOTE: Another option would be to add an "all day" property to the
 * existing calendar model.
 */
function isAllDayEvent(event)
{
   var startDate = event.properties["ia:fromDate"],
      endDate = event.properties["ia:toDate"],
      startTime = startDate.getHours() + ":" + startDate.getMinutes(),
      endTime = endDate.getHours() + ":" + endDate.getMinutes();
   
   if (logger.isLoggingEnabled())
   {
      logger.log("STARTTIME: " + startTime + " " + endTime + " " + (startTime == endTime));
   }
  
   return (startTime == "0:0" && (startTime == endTime));
}

/**
 * Build recurrence string for share presentation
 
 * @param {String}  The recurrence rule
 * @param {Event}  The recurrence event
 */
function buildRecurrenceString(recurrence, event)
{
   var i18n = new Packages.org.springframework.extensions.surf.util.I18NUtil,
      dfs = new java.text.DateFormatSymbols(i18n.locale),
      weekdays = dfs.getWeekdays(),
      days =
      {
         SU: weekdays[1],
         MO: weekdays[2],
         TU: weekdays[3],
         WE: weekdays[4],
         TH: weekdays[5],
         FR: weekdays[6],
         SA: weekdays[7]
      };

   var finalString = "",
      parts = recurrence.split(";"),
      eventParam = {},
      part;

   for (var i = 0; i < parts.length; i++)
   {
      part = parts[i].split("=");
      eventParam[part[0]] = part[1];
   }
   
   if (eventParam['FREQ'] == "WEEKLY")
   {
      if (eventParam['INTERVAL'] == 1)
      {
         finalString = "Occurs each week on ";
         // finalString = utils.toLocalizedString('occurs.each.week.on');
      }
      else
      {
         finalString = "Occurs every " + eventParam['INTERVAL'] + " weeks on ";
         // finalString = utils.toLocalizedString('occurs.every.weeks.on', eventParam['INTERVAL']);
      }

      currentDays = eventParam['BYDAY'].split(","); 
      for (var i = 0; i < currentDays.length; i++)
      {
         finalString += days[currentDays[i]] + ", ";
      }
   }
   
   if (eventParam['FREQ'] == "DAILY")
   {
      finalString += "Occurs every day ";
      // finalString += utils.toLocalizedString('occurs.every.day');
   }
   
   if (eventParam['FREQ'] == "MONTHLY")
   {
      if (eventParam['BYMONTHDAY'] != null)
      {
         finalString += "Occurs day " + eventParam['BYMONTHDAY'];
         // finalString += utils.toLocalizedString('occurs.day', eventParam['BYMONTHDAY']);
      }

      if (eventParam['BYSETPOS'] != null)
      {
         finalString += "Occurs the" + eventParam['BYSETPOS'] + " " + days[currentDays[i]];
         // finalString += utils.toLocalizedString('occurs.the', eventParam['BYMONTHDAY'], days[currentDays[i]]);
      }
      finalString += " of every " + eventParam['INTERVAL'] + " month(s) ";
      // finalString += utils.toLocalizedString('of.every.month', eventParam['INTERVAL']);
   }
   
   if (eventParam['FREQ'] == "YEARLY")
   {
      if (eventParam['BYMONTHDAY'] != null)
      {
         finalString += "Occurs every " + eventParam['BYMONTHDAY'] + "." + eventParam['BYMONTH'] + " ";
         // finalString += utils.toLocalizedString('occurs.every', eventParam['BYMONTHDAY'], eventParam['BYMONTH']);
      }
      else
      {
        finalString += "Occurs the " + eventParam['BYSETPOS'] + " " + days[currentDays[i]]  + " of " +  eventParam['BYMONTH'] + " month ";
        // finalString += utils.toLocalizedString('occurs.the.of.month', eventParam['BYSETPOS'], days[currentDays[i]], eventParam['BYMONTH']);	 
      }
   }
   
   finalString += "effective " + format(event.properties["ia:fromDate"]);
   // finalString += utils.toLocalizedString('effective', format(event.properties["ia:fromDate"], "dd.mm.yyyy"));

   if (eventParam['COUNT'] != null)
   {
      finalString += " until " + format(event.properties["ia:recurrenceLastMeeting"]);
      // finalString += utils.toLocalizedString('until', format(event.properties["ia:recurrenceLastMeeting"], "dd.mm.yyyy"));
   }

   finalString += " from " + format(event.properties["ia:fromDate"], "hh:nn") + " to " + format(event.properties["ia:toDate"], "hh:nn");
   // finalString += utils.toLocalizedString('from.to', format(event.properties["ia:fromDate"], "hh:nn"), format(event.properties["ia:toDate"], "hh:nn"));

   return finalString;
}

/**
 * Format the date by pattern
 
 * @param date {Date} The date object for format
 * @param pattern {String} [Optional] An optional date pattern. Defaults to DateFormat.DEFAULT otherwise
 * @return {String} Formated date by pattern
 */
function format(date, pattern)
{
   if (!date.valueOf())
   {
      return ' ';
   }

   if (pattern == undefined)
   {
      var i18n = new Packages.org.springframework.extensions.surf.util.I18NUtil;
      return java.text.SimpleDateFormat.getDateInstance(java.text.SimpleDateFormat.MEDIUM, i18n.locale).format(date);
   }

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