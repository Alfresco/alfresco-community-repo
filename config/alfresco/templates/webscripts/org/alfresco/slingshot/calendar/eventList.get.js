
var siteId = args["site"];
model.events = getEvents(siteId);

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

  return calendar.children.sort(function(a,b) {
	return a.properties["ia:fromDate"] - b.properties["ia:fromDate"];
  });
};


