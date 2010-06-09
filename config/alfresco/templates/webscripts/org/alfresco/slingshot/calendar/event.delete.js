<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/calendar/lib/calendar.lib.js">
/**
 * Delete event action
 * @method DELETE
 * @param uri {string} /{siteid}/{eventname}
 */
var result = deleteEvent();
status.code = result;

function deleteEvent()
{
   var params = getTemplateParams();
   if (params === null)
   {
	   return status.STATUS_BAD_REQUEST;
   }

   var site = siteService.getSite(params.siteid);
   if (site === null)
   {
	   return status.STATUS_NOT_FOUND;
   }

   var eventsFolder = getCalendarContainer(site);
   if (eventsFolder === null)
   {
	   return status.STATUS_NOT_FOUND;
   }

   var event = eventsFolder.childByNamePath(params.eventname);
   var editedEvent = event;
   
   if (event === null)
   {
	   return status.STATUS_NOT_FOUND;
   }

   if (editedEvent.properties["ia:recurrenceRule"] != null)
   {
      var prop = new Array();
      var fromParts = params.date.split("-");
      prop["ia:date"] = new Date(fromParts[0],fromParts[1] - 1,fromParts[2]);
      editedEvent.createNode(null, "ia:ignoreEvent", prop, "ia:ignoreEventList");
	  
      return status.STATUS_NO_CONTENT;
   }
   
   var whatEvent = event.properties["ia:whatEvent"]; 
	
   if (!event.remove())
   {
	   return status.STATUS_INTERNAL_SERVER_ERROR;
   }

	try 
	{
	   var data =
	   {
	      title: whatEvent,
         page: decodeURIComponent(args["page"])
	   }
		activities.postActivity("org.alfresco.calendar.event-deleted", params.siteid, "calendar", jsonUtils.toJSONString(data));
	}
	catch(e) 
	{
		if (logger.isLoggingEnabled())
		{
			logger.log(e);
		}
	}

   // Success
   return status.STATUS_NO_CONTENT;
}

function getTemplateParams()
{
     // Grab the URI parameters
     var siteid = "" + url.templateArgs.siteid;
     var eventname = "" + url.templateArgs.eventname;
     var date = args["date"];

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
	  		"eventname": eventname,
	  		"date": date
     };
}
