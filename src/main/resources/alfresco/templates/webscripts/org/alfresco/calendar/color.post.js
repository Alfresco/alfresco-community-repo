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
logger.log("COLOR: " + args.color + " REF: " + args.ref);
var spaceRef = args.ref;
var calendar = findNodeByNodeRef(spaceRef);

if (calendar != null) {
    calendar.properties["ia:colorEventDefault"] = args.color;
    calendar.save(); 
}


