var calendar = search.findNode("workspace://SpacesStore/" + url.extension);

if (calendar !== null)
{
  var eventsFolder = calendar.childByNamePath("CalEvents");
  if (eventsFolder !== null)
  {
	model.events = eventsFolder.children.sort(function(a,b) {
		return a.properties["ia:fromDate"] - b.properties["ia:fromDate"];
	});
  }
}

