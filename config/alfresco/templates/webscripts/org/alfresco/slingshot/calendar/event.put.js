<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/calendar/lib/calendar.lib.js">
/**
 * Update event properties
 * @method PUT
 * @param uri {string} /calendar/event/{siteid}/{eventname}
 */
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
      return status.STATUS_BAD_REQUEST;
   }

   // Get the site
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
	if (event === null)
   {
      return status.STATUS_NOT_FOUND;
   }

	var props = [
		"what",
		"desc",
		"where"
	];

   var propsmap = {
      "what" : "ia:whatEvent",
	  	"desc" : "ia:descriptionEvent",
	  	"where" : "ia:whereEvent"
   };

   for (var i=0; i < props.length; i++)
   {
	   var prop = props[i];
		if (!json.isNull(prop))
		{
         var value = json.get(prop);
         event.properties[ propsmap[prop] ] = value;
		}
   }
	
	try 
	{
		// Handle date formatting as a separate case		
		var from = json.get("from");
      var to = json.get("to");
     
      if (json.isNull("allday"))
      {
         from += " " + json.get("start");
         to += " " + json.get("end");
      }
      
      event.properties["ia:fromDate"] = new Date(from);
		event.properties["ia:toDate"] = new Date(to);
		
		var eventName = json.get("what");		
		activities.postActivity("org.alfresco.calendar.event-updated", params.siteid, "calendar", '{ "eventName" : ' + eventName + ' }');
	}
	catch(e)
	{
		if (logger.isLoggingEnabled())
		{
			logger.log(e);
		}
	}

    event.save();
    return status.STATUS_NO_CONTENT;
}

var response = main();
status.code = response;
