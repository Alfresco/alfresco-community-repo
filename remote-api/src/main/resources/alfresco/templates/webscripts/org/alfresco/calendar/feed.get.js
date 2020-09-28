var node = search.findNode("workspace://SpacesStore/" + url.extension);

//TODO: add a privacy check. If a calendar is private then don't display it

if (node !== null)
{
  var eventsFolder = node.childByNamePath("CalEvents");
  if (eventsFolder !== null)
  {
     model.events = eventsFolder.children;  
  }
}
