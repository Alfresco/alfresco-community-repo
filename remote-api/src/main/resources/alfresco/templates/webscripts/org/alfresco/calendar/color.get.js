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
var color = null;
var calendar = findNodeByNodeRef(spaceRef);

if (calendar != null) {
    color = calendar.properties["ia:colorEventDefault"]; 
}

if (color == null || color.length == 0) {
  color = "#FF0000"; // red
}

model.color = color;
