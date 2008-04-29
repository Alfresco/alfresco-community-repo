var now  = new Date();
var day  = now.getDay();
var month = now.getMonth();
var year = now.getYear();
var hour   = now.getHours();
var minute = now.getMinutes();
var second = now.getSeconds();

var stamp = day + month + year + hour.toString() + minute + second;
logger.log("DATE: " + args.fd + " " + args.ft);
var fromDateString = args.fd + " " + args.ft;
var fromDateDate = new Date(fromDateString);

var toDateString = args.td + " " + args.tt;
var toDateDate = new Date(toDateString);

var content = "What: " + args.what + "<BR>";
content += "When: " + args.fd + " " + args.ft + " to " + args.td + " " + args.tt + "<BR>";
content += "Where: " + args.where + "<BR>";
content += "Description: " + args.desc + "<BR>";
content += "Color: " + args.color;

var fileNode = null;
var eventId = args.e;
var toDelete = args.d;

var spaceRef = args.s;
var nodeWhereToCreate = findNodeByNodeRef(spaceRef);

var response;

if (nodeWhereToCreate != null)
{
	var newFolder = nodeWhereToCreate.childByNamePath("CalEvents");
	if (newFolder == null)
		nodeWhereToCreate = nodeWhereToCreate.createFolder("CalEvents");
	else
		nodeWhereToCreate = newFolder;

	if (eventId == null)
	{
		fileNode = nodeWhereToCreate.createNode(stamp + ".ics", "ia:calendarEvent");
		saveNodeDetails();
	}
	else
	{
		fileNode = search.findNode(eventId);
		if (toDelete == 'true')
		{
			var status = fileNode.remove();
			if (status)
				response = "DELETED";
			else
				response = "NOT DELETED";
		}
		else
		{
			saveNodeDetails();
		}
	}
}
else
{
	response = "SPACE not found with Ref " + spaceRef;
}

model.result = response;


function saveNodeDetails()
{
	fileNode.properties["ia:whatEvent"] = args.what;
	fileNode.properties["ia:fromDate"] = fromDateDate;
	fileNode.properties["ia:toDate"] = toDateDate;
	fileNode.properties["ia:whereEvent"] = args.where;
	fileNode.properties["ia:descriptionEvent"] = args.desc;
	fileNode.properties["ia:colorEvent"] = args.color;

	fileNode.save();
	fileNode.content = content;

	response = fileNode.content;
}

function getGUIDFromNodeRef(nodeRef) 
{
	var str = "" + nodeRef;
	return str.substring(str.lastIndexOf("/")+1);
}

function findNodeByNodeRef(nodeRef)
{
	var resultsArray = search.luceneSearch("ID:workspace\\://SpacesStore/" + getGUIDFromNodeRef(nodeRef));
	 
	if (resultsArray != null && resultsArray.length > 0)
	{
		return resultsArray[0];
	} 
	else
	{
		return null;
	}
}
