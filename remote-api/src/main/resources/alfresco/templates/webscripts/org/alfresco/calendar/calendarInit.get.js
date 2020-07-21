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
var calendar = findNodeByNodeRef(spaceRef);

var color = calendar.properties["ia:colorEventDefault"];
if (color == null || color.length == 0) {
         color = "#FF0000"; // default
}
model.color = color;

// Get the list of calendars the user is subscribed to
var  subscriptions = calendar.assocs["ia:subscribedCalendarList"]
if (subscriptions == null) {
  subscriptions = new Array();
}
model.subscriptions = subscriptions;

// perform search
var nodes = search.luceneSearch('TYPE:\"{http\://www.alfresco.org/model/calendar}calendar\"');

/**
var filtered = new Array();
   if (nodes.length > 0) {
        var re = new RegExp(args.q,"i");
        var j = 0;
        for(i=0; i < nodes.length; i++) {
               var n = nodes[i];
               if (re.test(n.parent.name)) {
                     filtered[j] = n;
                     ++j;
               }
        }
   }
**/

model.available = nodes;