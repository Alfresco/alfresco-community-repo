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

     var eventsFolder = site.getContainer("calendar");
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
	  try
	  {
	       var value = json.get(prop);
	       // TODO: deal with formatting date strings correctly
	       if (value)
	       {
		    event.properties[ propsmap[prop] ] = value;
	       }
	  }
	  catch(e)
	  {
	       // Couldn't find the property in the JSON data
	  }
     }

     event.save();
     return status.STATUS_NO_CONTENT;
}

var response = main();
status.code = response;
