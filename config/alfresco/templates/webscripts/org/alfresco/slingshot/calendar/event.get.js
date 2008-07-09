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

   var eventsFolder = site.getContainer("calendar");
   if (eventsFolder === null)
   {
      return jsonError("Could not locate events container");
   }

   var event = eventsFolder.childByNamePath(params.eventname);
   if (event === null)
   {
      return jsonError("Could not find event: " + params.eventname);
   }
   
   var result = {
      "name": event.name,
      "what": event.properties["ia:whatEvent"], 
      "description": event.properties["ia:descriptionEvent"],
   	"location": event.properties["ia:whereEvent"],
      "from": event.properties["ia:fromDate"],
      "to": event.properties["ia:toDate"],
   };
 
   // Figure out if this an all day event
   if(isAllDayEvent(event))
   {
      result["allday"] = true;
   }

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
  
   return (startTime === "0:0" && (startTime === endTime));
}
