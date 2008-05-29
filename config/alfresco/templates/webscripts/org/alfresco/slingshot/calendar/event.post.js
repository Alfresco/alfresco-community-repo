logger.log("DEBUG: create event script called");
logger.log("DEBUG: workspace://SpacesStore/" + args.id);

var node = search.findNode("workspace://SpacesStore/" + args.id);
logger.log("DEBUG: " + node);
if (node !== null)
{
   var eventsFolder = node.childByNamePath("CalEvents");
   if (eventsFolder === null)
   {
      eventsFolder = node.createFolder("CalEvents");
   }

   var timestamp = new Date().getTime();
   var event = eventsFolder.createNode(timestamp + ".ics", "ia:calendarEvent");

   event.properties["ia:whatEvent"] = args.what;
   event.properties["ia:whereEvent"] = args.where;
   event.properties["ia:descriptionEvent"] = args.desc;
   event.properties["ia:colorEvent"] = args.color;

   var fromDate = args.td + " " + args.tt;
   var from = new Date(fromDate);
   event.properties["ia:fromDate"] = from;

   var toDate = args.td + " " + args.tt;
   var to = new Date(toDate);
   event.properties["ia:toDate"] = to;
   event.save();

   var msg = "Event saved";
}
else
{
   var msg = "SPACE not found with Ref " + args.id;
}

model.msg = msg;

