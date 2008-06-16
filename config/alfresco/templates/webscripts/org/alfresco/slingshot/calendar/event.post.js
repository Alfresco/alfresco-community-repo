var siteId = args["site"];

model.result = createEvent(siteId, args);

function createEvent(siteId, params)
{
  if (siteId === null)
  {
    return {
      "error": "Site identifier is undefined"
    };
  }

  var site = siteService.getSite(siteId);
  if (site === null)
  {
    return {
      "error": "Could not find specified site"
    };
  }

  var calendar = site.getContainer("calendar");
  if (calendar === null)
  {
    return {
      "error": "Could not get container"
    };
  }

  var timestamp = new Date().getTime();
  var event = calendar.createNode(timestamp + ".ics", "ia:calendarEvent");

  if (event === null)
  {
    return {
      "error": "Could not create event"
    };
  }

  event.properties["ia:whatEvent"] = params["what"];
  event.properties["ia:whereEvent"] = params["where"];
  event.properties["ia:descriptionEvent"] = params["desc"];

  var fromDate = params["from"] + " " + params["start"];
  var from = new Date(fromDate);
  event.properties["ia:fromDate"] = from;

  var toDate = params["to"] + " " + params["end"];
  var to = new Date(toDate);
  event.properties["ia:toDate"] = to;
  event.save();
  
	try {
		activities.postActivity("org.alfresco.calendar.event-created", siteId, "calendar", '{ "eventName" : ' + params["what"] + ' }');	
	}
	catch(e) {
		if (logger.isLoggingEnabled()) 
		{
			logger.log(e);
		}
	}

  return {
    "name": params["what"],
    "from": from,
    "to": to
  };
};



