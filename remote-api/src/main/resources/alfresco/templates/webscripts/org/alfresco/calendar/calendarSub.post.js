function getGUIDFromNodeRef(nodeRef) {
	var str = "" + nodeRef;
	if (str.length > 24) {
		return str.substring(24);
	} else {
		return "";
	}
}

function findNodeByNodeRef(nodeRef) {
	var resultsArray = search.luceneSearch("ID:workspace\\://SpacesStore/" + getGUIDFromNodeRef(nodeRef));
	if (resultsArray != null && resultsArray.length > 0) {
		return resultsArray[0];
	} else {
		return null;
	}
}

var spaceRef = args.ref;
var space = findNodeByNodeRef(spaceRef);
// Resolve the calendar reference
var calendar = findNodeByNodeRef(args.calendar);
if (calendar != null) {
   space.createAssociation(calendar, "ia:subscribedCalendarList");
   space.save();
}

var  calendars = space.assocs["ia:subscribedCalendarList"];
model.resultset = calendars;
