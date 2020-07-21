function getGUIDFromNodeRef(nodeRef) 
{
	var str = "" + nodeRef;
	return str.substring(str.lastIndexOf("/")+1);
}

function findNodeByNodeRef(nodeRef)
{
	var resultsArray= search.luceneSearch("ID:workspace\\://SpacesStore/" + getGUIDFromNodeRef(nodeRef));
	 
	if (resultsArray != null && resultsArray.length > 0)
	{
		return resultsArray[0];
	} 
	else
	{
		return null;
	}
}

var spaceRef = args.s;
var currentBaseSpace = findNodeByNodeRef(spaceRef);
var response = ""
var _today = new Date();

if (currentBaseSpace != null)
{
	if (currentBaseSpace.properties["ia:whatEventDefault"] != null)
		response += currentBaseSpace.properties["ia:whatEventDefault"] + "^";
	else
		response += "" + "^";
	
	if (currentBaseSpace.properties["ia:fromDateDefault"] != null)
		response += currentBaseSpace.properties["ia:fromDateDefault"] + "^";
	else
		response += _today.toString() + "^";
	
	if (currentBaseSpace.properties["ia:toDateDefault"] != null)
		response += currentBaseSpace.properties["ia:toDateDefault"] + "^";
	else
		response += _today.toString() + "^";
	
	if (currentBaseSpace.properties["ia:whereEventDefault"] != null)
		response += currentBaseSpace.properties["ia:whereEventDefault"] + "^";
	else
		response += "" + "^";
	
	if (currentBaseSpace.properties["ia:colorEventDefault"] != null)
		response += currentBaseSpace.properties["ia:colorEventDefault"];
	else
		response += "";
}
logger.log("RESPONSE: " + response);
model.result = response;
