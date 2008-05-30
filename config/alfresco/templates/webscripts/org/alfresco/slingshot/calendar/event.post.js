var siteId = args["site"];

model.result = createEvent(siteId, args);

function createEvent(siteId, params)
{
  if (siteId === null)
  {
    return "Site identifier is undefined";
  }

  var site = siteService.getSite(siteId);
  if (site === null)
  {
    return "Could not find specified site";
  }

  var calendar = site.getContainer("calendar");
  if (calendar === null)
  {
    return ""; /* TODO: return something more meaningful */
  }

  var timestamp = new Date().getTime();
  var event = calendar.createNode(timestamp + ".ics", "ia:calendarEvent");

  if (event === null)
  {
    return "Event creation failed";
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

  return "Event saved";
};



