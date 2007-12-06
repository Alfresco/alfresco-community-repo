var eventId = args.e;
var eventNode = search.findNode(eventId);

if (eventNode == null)
{
	model.result = "Event not Found";
}

else
{
	var response = "";
	var _fromDate = eventNode.properties["ia:fromDate"];
	var _toDate = eventNode.properties["ia:toDate"];
	var _fromMonth = _fromDate.getMonth() + 1;
	var _toMonth = _toDate.getMonth() + 1;

	response += eventNode.properties["ia:whatEvent"] + "^";
	response += _fromMonth + "/" + _fromDate.getDate() + "/" + _fromDate.getFullYear() + "^";
	var frmHour = _fromDate.getHours();
	if (frmHour == 0) frmHour += "0";
	var frmMin = _fromDate.getMinutes();
	if (frmMin == 0) frmMin += "0";
	response += frmHour + ":" + frmMin + "^";
	response += _toMonth + "/" + _toDate.getDate() + "/" + _toDate.getFullYear() + "^";
	var toHour = _toDate.getHours();
	if (toHour == 0) toHour += "0";
	var toMin = _toDate.getMinutes();
	if (toMin == 0) toMin += "0";
	response += toHour + ":" + toMin + "^";
	response += eventNode.properties["ia:whereEvent"] + "^";
	response += eventNode.properties["ia:descriptionEvent"] + "^";
	response += eventNode.properties["ia:colorEvent"];

	model.result=response;
}
