var dateString = args.d;
var _currentDateForMonthView= new Date(dateString);

var spaceRef = args.s;
var currentBaseSpace = findNodeByNodeRef(spaceRef);

var eventList = new Array();

function editableObject(obj, iseditable, color) {
	this.object = obj;
	this.isEditable = iseditable;
        this.color = color;
}

function eventType(datepart, obj) {
	this.datePart = datepart;
	this.object = obj;
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
    _arr[0] = "January";
    _arr[1] = "February";
    _arr[2] = "March";
    _arr[3] = "April";
    _arr[4] = "May";
    _arr[5] = "June";
    _arr[6] = "July";
    _arr[7] = "August";
    _arr[8] = "September";
    _arr[9] = "October";
    _arr[10] = "November";
    _arr[11] = "December";
    
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
                        if (c != null)
                               if ((color = y[i].properties["ia:colorEventDefault"]) == null) {
                                      color = defaultColor;
                                }
				x[i] = new editableObject(c, 0, color);
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

function getDayEvents(requiredDate) {
	var eventsArr = new Array();
	var events = "";
	var _months = MonthsArray();
	
	if (currentBaseSpace == null)
		return events;
	
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
				toDate.setHours(11,59,59,0);
				if (fromDate <= requiredDate && toDate >= requiredDate)
				{
					eventsArr.push(new editableObject(child, calendarSpaces[j].isEditable, calendarSpaces[j].color));
				}
			}
		}
	}
	
	eventsArr.sort(SortCalendarEvents);
	var tempEvents = new Array();
	
	for (var j=0; j<eventsArr.length; j++)
	{
		var child = eventsArr[j].object;
		var fromDate = new Date(child.properties["ia:fromDate"]);
		fromDate.setHours(0,0,0,0);
		var toDate = new Date(child.properties["ia:toDate"]);
		toDate.setHours(11,59,59,0);

		var showTimeLine = "";
		
		if (fromDate.toDateString() == requiredDate.toDateString() && toDate.toDateString() == requiredDate.toDateString())
			showTimeLine = child.properties["ia:fromDate"].getHours() + ":" + child.properties["ia:fromDate"].getMinutes().toString().pad(2, "0", 1) + " - " + child.properties["ia:toDate"].getHours() + ":" + child.properties["ia:toDate"].getMinutes().toString().pad(2, "0", 1);
		else
			showTimeLine = child.properties["ia:fromDate"].getDate() + " " + _months[child.properties["ia:fromDate"].getMonth()] + ", " + child.properties["ia:fromDate"].getHours() + ":" + child.properties["ia:fromDate"].getMinutes().toString().pad(2, "0", 1) + " - " + child.properties["ia:toDate"].getDate() + " " + _months[child.properties["ia:toDate"].getMonth()] + ", " + child.properties["ia:toDate"].getHours() + ":" + child.properties["ia:toDate"].getMinutes().toString().pad(2, "0", 1);

		tempEvents.push(new editableObject(child, eventsArr[j].isEditable, eventsArr[j].color));
	}
	
	return tempEvents;
}

function SortCalendarEvents(child1, child2)
{
	return (child1.object.properties["ia:fromDate"] - child2.object.properties["ia:fromDate"]);
}


function GetMonthName()
{
	var _arr = MonthsArray();
	return _arr[_currentDateForMonthView.getMonth()];
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

if (currentBaseSpace == null)
{
	response = "Parameters passed:<BR>";
	response += "Current Date: " + dateString + "<BR>";
	response += "Current Space: " + spaceRef + "<BR>";
	response += "<BR>Error: No Space found by this Ref";
}
else
{
	calendarSpaces = calendarSpaceArray();

	var _arrDay = DaysArray();
	var tmpDate;
	var i, j;
	
	
	// Start with the first day of the month and go back if necessary to the previous Sunday.
	tmpDate = new Date(Date.parse(_currentDateForMonthView));
	tmpDate.setDate(1);
	tmpDate.setHours(0,0,0,0);
	while (tmpDate.getDay() != 0) 
	{
		tmpDate.setDate(tmpDate.getDate() - 1);
	}
	
	for (i = 2; i <= 7; i++) 
	{
		// Loop through a week.
		for (j = 0; j < _arrDay.length; j++) 
		{
			if (tmpDate.getMonth() == _currentDateForMonthView.getMonth()) 
			{
				eventList.push(new eventType(tmpDate.getDate(), getDayEvents(tmpDate)));
			}
			else
			{
				eventList.push(null);
			}
	
			// Go to the next day.
			tmpDate.setDate(tmpDate.getDate() + 1);
		}
	}
	
}

model.DaysArray = DaysArray();
model.eventList = eventList;
