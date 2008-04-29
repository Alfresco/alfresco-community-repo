var dateString = args.d;
var _currentDateForWeekView= new Date(dateString);

var spaceRef = args.s;
var currentBaseSpace = findNodeByNodeRef(spaceRef);

var eventList = new Array();
var eventListAllDay = new Array();
var days = new Array();

function editableObject(obj, iseditable, color) {
	this.object = obj;
	this.isEditable = iseditable;
        this.color = color;
}

function eventType(obj, timeslot) {
	this.object = obj;
	this.timeSlot = timeslot;
}

var HoursArray = function() {
    var _arr = new Array();
    _arr[0] = "00:00";
    _arr[1] = "00:30";
    for (i=1; i<24; i++) {
        _arr[i*2] = i + ":00";
    	_arr[i*2+1] = i + ":30";
    }	
    return _arr;
}

var DaysArray = function() {
    var _arr = new Array();
    _arr[0] = "Sunday";
    _arr[1] = "Monday";
    _arr[2] = "Tuesday";
    _arr[3] = "Wednesday";
    _arr[4] = "Thursday";
    _arr[5] = "Friday";
    _arr[6] = "Saturday";
    
    return _arr;
}

var MonthsArray = function() {
    var _arr = new Array();
    _arr[0] = "Jan";
    _arr[1] = "Feb";
    _arr[2] = "Mar";
    _arr[3] = "Apr";
    _arr[4] = "May";
    _arr[5] = "Jun";
    _arr[6] = "Jul";
    _arr[7] = "Aug";
    _arr[8] = "Sep";
    _arr[9] = "Oct";
    _arr[10] = "Nov";
    _arr[11] = "Dec";
    
    return _arr;
}

String.prototype.pad = function(l, s, t) {
	return s || (s = " "), (l -= this.length) > 0 ? (s = new Array(Math.ceil(l / s.length)
		+ 1).join(s)).substr(0, t = !t ? l : t == 1 ? 0 : Math.ceil(l / 2))
		+ this + s.substr(0, l - t) : this;
};

var calendarSpaceArray = function() {
        var color;
        var defaultColor = "#FF0000";

	var c = null;
  	var x = new Array();
	var y = currentBaseSpace.assocs["ia:subscribedCalendarList"];
	if (y != null) {
		for (i=0; i<y.length; i++) {
                        c = y[i].childByNamePath("CalEvents");
                        if (c != null) {
                                if ((color = y[i].properties["ia:colorEventDefault"]) == null) {
                                      color = defaultColor;
                                }
				x[i] = new editableObject(c, 0, color);
                        }
		}
	} else {
               logger.log("NOT SUBSCRIBED TO CALENDARS");
        }
	c = currentBaseSpace.childByNamePath("CalEvents");
	if (c != null) {
                if ((color = currentBaseSpace.properties["ia:colorEventDefault"]) == null) {
                         color = defaultColor;
                }
		x[x.length] = new editableObject(c, 1, color);
        }
	return x;
};

calendarSpaces = calendarSpaceArray();

function getDayEvents(requiredDate, requiredTime)
{
	var eventArr = new Array();
	var _months = MonthsArray();
	if (currentBaseSpace == null)
		return null;

	for (var j=0; j<calendarSpaces.length; j++)
	{
		var currentSpace = calendarSpaces[j].object;
		for (var i=0; i<currentSpace.children.length; i++)
		{
			var child = currentSpace.children[i];
			var times = requiredTime.split(":");
			if (child.type=="{com.infoaxon.alfresco.calendar}calendarEvent") {
				var fromDate = new Date(child.properties["ia:fromDate"]);
				fromDate.setHours(0,0,0,0);
				var toDate = new Date(child.properties["ia:toDate"]);
				toDate.setHours(requiredDate.getHours(),
                                                            requiredDate.getMinutes(),
                                                            requiredDate.getSeconds(),
                                                            0);
				if (child.properties["ia:fromDate"].toDateString() == requiredDate.toDateString() && child.properties["ia:fromDate"].getHours() == parseInt(times[0]) && child.properties["ia:fromDate"].getMinutes() == parseInt(times[1]))
				{
					var showTimeLine = "";
					
					if (toDate > requiredDate)
						showTimeLine = child.properties["ia:fromDate"].getDate() + " " + _months[child.properties["ia:fromDate"].getMonth()] + ", " + child.properties["ia:fromDate"].getHours() + ":" + child.properties["ia:fromDate"].getMinutes().toString().pad(2, "0", 1) + " - " + child.properties["ia:toDate"].getDate() + " " + _months[child.properties["ia:toDate"].getMonth()] + ", " + child.properties["ia:toDate"].getHours() + ":" + child.properties["ia:toDate"].getMinutes().toString().pad(2, "0", 1);
					else
						showTimeLine = child.properties["ia:fromDate"].getHours() + ":" + child.properties["ia:fromDate"].getMinutes().toString().pad(2, "0", 1) + " - " + child.properties["ia:toDate"].getHours() + ":" + child.properties["ia:toDate"].getMinutes().toString().pad(2, "0", 1);
					
						
					eventArr.push(new editableObject(child, calendarSpaces[j].isEditable, calendarSpaces[j].color));
				}
			}
		}
	}
	if (eventArr.length == 0) { eventArr = null; }
	return eventArr;
}

function getAllDayEvents(requiredDate)
{
	var eventArr = new Array();
	var _months = MonthsArray();

	if (currentBaseSpace == null)
		return null;

	for (var j=0; j<calendarSpaces.length; j++)
	{
		var currentSpace = calendarSpaces[j].object;
		for (var i=0; i<currentSpace.children.length; i++)
		{
			var child = currentSpace.children[i];
			if (child.type=="{com.infoaxon.alfresco.calendar}calendarEvent")
			{
				var fromDate = new Date(child.properties["ia:fromDate"]);
				fromDate.setHours(0,0,0,0);
				var toDate = new Date(child.properties["ia:toDate"]);
				toDate.setHours(requiredDate.getHours(),requiredDate.getMinutes(),requiredDate.getSeconds(),0);
				if (child.properties["ia:fromDate"].toDateString() != child.properties["ia:toDate"].toDateString() && fromDate <= requiredDate && toDate >= requiredDate)
				{
					eventArr.push(new editableObject(child, calendarSpaces[j].isEditable, calendarSpaces[j].color));
				}
			}
		}
	}
	if (eventArr.length == 0) { eventArr = null; }
	return eventArr;
}

function SortCalendarEvents(child1, child2)
{
	return (child1.properties["ia:fromDate"] - child2.properties["ia:fromDate"]);
}

function AddWeekDayRow()
{
	var _currDay = _currentDateForWeekView.getDay();

	var _arr = DaysArray();
	for (i=0; i<_arr.length; i++)
	{
		var _newDate = new Date(_currentDateForWeekView);
		_newDate.setDate(_currentDateForWeekView.getDate() - _currDay + i);
		days.push(_newDate.toDateString());
		if (i == 0)     _startDateForWeekView = _newDate;
	}
}

function AddAllDayEventsRow()
{
	var _arr = DaysArray();
	var tempDate = new Date(_startDateForWeekView);
	for (i=0; i<_arr.length; i++)
	{
		if (i != 0)      tempDate.setDate(tempDate.getDate() + 1);
		eventListAllDay.push(getAllDayEvents(tempDate));
	}
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

var response;

if (currentBaseSpace == null) {
	response = "Parameters passed:<BR>";
	response += "Current Date: " + dateString + "<BR>";
	response += "Current Space: " + spaceRef + "<BR>";
	response += "<BR>Error: No Space found by this Ref";
} else {
	AddWeekDayRow();
	AddAllDayEventsRow();
	
	var _arr = HoursArray();
	var _arrDay = DaysArray();
	var eventDays = new Array();
	for (i=0; i<_arr.length; i++)
	{
		eventDays = new Array();
		var tempDate = new Date(_startDateForWeekView);
		for (j=0; j<_arrDay.length; j++)
		{
			if (j != 0)      tempDate.setDate(tempDate.getDate() + 1);
	
			var _date = tempDate.getDate();
			var _month = tempDate.getMonth();
			var _year = tempDate.getFullYear();
	
			eventDays.push(new eventType(getDayEvents(tempDate, _arr[i]), new Date(tempDate)));
		}
		eventList.push(new eventType(eventDays, _arr[i]));
	}
	
	var _lastDate = new Date(_startDateForWeekView);
	_lastDate.setDate(_lastDate.getDate() + 6);
	model.dayCaption = _startDateForWeekView.toDateString() + " <I>to</I> " + _lastDate.toDateString();
}

//model.result = response;
model.daysArray = days;
model.eventList = eventList;
model.eventListAllDay = eventListAllDay;
