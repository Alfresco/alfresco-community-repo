var dateString = args.d;
var _currentDateForMonthView= new Date(dateString);

var currentBaseSpace = search.findNode(args.s);
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

String.prototype.pad = function(l, s, t) {
	return s || (s = " "), (l -= this.length) > 0 ? (s = new Array(Math.ceil(l / s.length)
		+ 1).join(s)).substr(0, t = !t ? l : t == 1 ? 0 : Math.ceil(l / 2))
		+ this + s.substr(0, l - t) : this;
};

// utils.pad(s, length)

var calendarSpaceArray = function() {
    var color;
    var defaultColor = "#FF0000";

	var c = null;
  	var calendars = new Array();

	var assoc = currentBaseSpace.assocs["ia:subscribedCalendarList"];
	if (assoc !== null) {
		for (i=0; i<assoc.length; i++) 
		{
        	c = assoc[i].childByNamePath("CalEvents");
            if (c !== null)
			{
				if ((color = assoc[i].properties["ia:colorEventDefault"]) === null) 
				{
                	color = defaultColor;
                }
			}                   
			calendars[i] = new editableObject(c, 0, color);
		}
	} 
	else 
	{
    	logger.log("NOT SUBSCRIBED TO CALENDARS");
    }

	c = currentBaseSpace.childByNamePath("CalEvents");
	if (c !== null) 
	{
    	if ((color = currentBaseSpace.properties["ia:colorEventDefault"]) === null) {
        	color = defaultColor;
        }
		calendars[calendars.length] = new editableObject(c, 1, color);
    }
	
	return calendars;
};

/* A list of the events folders for the current calendar and ALL the calendars the user is subscribed to */
calendarSpaces = calendarSpaceArray();

function getDayEvents(requiredDate) {
	var eventsArr = new Array();
	var events = "";
	
	if (currentBaseSpace === null)
	{
		return events;
	}
	
	for (var j=0; j<calendarSpaces.length; j++)
	{
		var currentSpace = calendarSpaces[j].object;
		logger.log("QNAME PATH: " + currentSpace.qnamePath);
		/* Do the Lucene date range query here */
		for (var i=0; i<currentSpace.children.length; i++)
		{
			var child = currentSpace.children[i];
			if (child.type=="{http://www.alfresco.org/model/calendar}calendarEvent")
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
	return eventsArr;
}

function getEventsQuery(fromdate, todate)
{
	/* Construct the PATH part of Lucene query string */
	var query = "";
	for (var j=0; j<calendarSpaces.length; j++)
	{
		query += "+PATH:\"" + calendarSpaces[j].object.qnamePath + "/*\" ";
	}
	
	/* Construct the date range */
	var from = fromdate.getFullYear() + "\\-" + (fromdate.getMonth() + 1) + "\\-" + fromdate.getDate();
	var to = todate.getFullYear() + "\\-" + (todate.getMonth() + 1) + "\\-" + todate.getDate();
	
	query += "+@ia\\:fromDate:[" + from + "T00:00:00 TO " + to + "T00:00:00]";
	
	//var results = search.luceneSearch(query);
	//logger.log("RESULTS: " + results.length);
	
	return query;
}

var fromdate = new Date(args.d);
fromdate.setDate(1);
var todate = new Date(args.d);
todate.setDate(31);

logger.log("QUERY: " + getEventsQuery(fromdate, todate));

function SortCalendarEvents(child1, child2)
{
	return (child1.object.properties["ia:fromDate"] - child2.object.properties["ia:fromDate"]);
}

if (currentBaseSpace !== null)
{
	calendarSpaces = calendarSpaceArray();

	var DAYS_IN_WEEK = 7;
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
		for (j = 0; j < DAYS_IN_WEEK; j++) 
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

model.eventList = eventList;
