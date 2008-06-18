var siteId = args["site"];

model.events = getEvents(siteId);
model.siteId = siteId;

function getEvents(siteId)
{
  var site = siteService.getSite(siteId);
  if (site === null)
  {
    return [];
  }

  var calendar = site.getContainer("calendar");
  if (calendar === null)
  {
    return [];
  }

  var events = calendar.children;
  if (events.length > 0)
  {
    events  = events.sort(function(a,b) {
			    return a.properties["ia:fromDate"] - b.properties["ia:fromDate"];
			  });
  }

  return events;
};


