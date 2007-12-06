//
// Modified to include events from subscribed calendars.
//
// Author: simon@webteq.eu
//

var dateString = args.d;
var requiredDate = new Date(dateString);

var spaceRef = args.s;
var currentBaseSpace = findNodeByNodeRef(spaceRef);

var calendarSpaceArray = function() {
	var c = null;
  	var x = new Array();
	var y = currentBaseSpace.assocs["ia:subscribedCalendarList"];
	if (y != null)
	{
		for (i=0; i<y.length; i++)
		{
			c = y[i].childByNamePath("CalEvents");
			if (c != null) {
				x[i] = c;
                        }
		}
	}
	c = currentBaseSpace.childByNamePath("CalEvents");
	if (c != null) {
		x[x.length] = c;
        }
	return x;
};
calendarSpaces = calendarSpaceArray();

function Interval(start, end) {
  this.start = start;
  this.end = end;
};

Interval.prototype.overlaps = function(interval) {
  var time = interval.start.getTime();
  return (this.start.getTime() <= time) && (time < this.end.getTime());
};

function Event(name, start, end) {
  this.name = name;
  this.start = start;
  this.end = end;
  this.tabs = 0;
};

Event.prototype.getInterval = function() {
  return new Interval(this.start, this.end);
};

Event.prototype.color = "#FF0000";  // default color

Event.prototype.setColor = function(color) {
  this.color = color;
};

// Returns a list of events for a given date
function getEvents(eventdate) {
  var datestr = eventdate.toDateString();
  var eventsList = new Array();
  for (var j=0; j <calendarSpaces.length; j++) {
     var space = calendarSpaces[j];
     var color = space.parent.properties["ia:colorEventDefault"];  // (default) color of the calendar
     var edit_p = 0;
     if (currentBaseSpace.name == space.parent.name) {
           edit_p = 1;
     }
     for (var i=0; i<space.children.length; i++) {
	var child = space.children[i];
	if (child.type=="{com.infoaxon.alfresco.calendar}calendarEvent") {
               if (datestr == child.properties["ia:fromDate"].toDateString()) {
		 var startdate = child.properties["ia:fromDate"];
		 var enddate = child.properties["ia:toDate"];
                 var name = child.properties["ia:whatEvent"];
                 var event = new Event(name, startdate, enddate);
                 event.nodeRef = child.nodeRef; 
                 event.edit_p = edit_p;
                 if (color != null && color.length != "") {
                      event.setColor(color); // the color that is used to render the event
                 }
                 eventsList.push(event);
               }
	}
     }
  }
  return eventsList;
};

var events = getEvents(requiredDate);
// Sort the array in order of start time
events.sort(eventCompare);

function eventCompare(a,b) {
  return a.start.getTime() - b.start.getTime();
};

var HoursArray = function() {
    var _arr = new Array();
    _arr[0] = "00:00";
    _arr[1] = "00:30";
    for (i=1; i<24; i++) {
        _arr[i*2] = i + ":00";
    	_arr[i*2+1] = i + ":30";
    }	
    return _arr;
};

var MINUTES = 60 * 1000; // ms in a minute 
var INTERVAL = 30 * MINUTES; // half an hour is the current display interval
var intervals = new Array();
//
// Construct the intervals list based on the display hours
//
var _hours = HoursArray();
for (idx in _hours) {
  var time = _hours[idx].split(":");

  var startdate = new Date();
  startdate.setTime(requiredDate.valueOf()); // Copy date value
  startdate.setHours(time[0]);
  startdate.setMinutes(time[1]);
 
  var enddate = new Date();
  enddate.setTime(startdate.getTime() + INTERVAL);
  intervals[idx] = new Interval(startdate, enddate);
}

function getIntervalEvents(interval) {
   var html = "<div>";
   var tabs = 0;
   // Loop through the events list
   for (j in events) {
      var event = events[j];
      // If the end time of the interval is less than or equal to the start time of the current event
      // jump out of the loop; there is no point in checking the rest of the events as the list is sorted.
      if (interval.end.getTime() <= event.start.getTime()) {
           break;
      }
      var event_interval = event.getInterval();
      if (event_interval.overlaps(interval)) {
           // We have found an event in this interval
           if (tabs > event.tabs) {
                event.tabs = tabs;
           }
           // Add spacer tabs if necessary
           if (event.tabs - tabs > 0) {
                   html += addPadding(event.tabs - tabs);
           }
           var style = new Array();
           style[0] = "border-left: 6px solid " + event.color;
          var content = "&nbsp;";
           if (interval.start.getTime() == event_interval.start.getTime()) {
                  style[1] = "border-top: 1px solid black";
                  style[2] = "padding-top: 0px";
                  content = event.name;
           } else if (interval.end.getTime() == event_interval.end.getTime()) {
                  style[1] = "border-bottom: 1px solid black";
                  style[2] = "padding-top: 0px";
           } 
           // Add the event
           html += "<div class=\"calendar_entry\"";
           if (event.edit_p) {
                  html += " onclick=\"editEvent('" + event.nodeRef + "');\"";
           }
           html += " style=\"" + style.join(" ; ") + "\">" + content + "</div>";
           tabs = event.tabs + 1;
      } 
   }
   html += "</div>";
   return html;
}

function addPadding(amount) {
   var spacertext = "";
   for(z=0; z<amount; z++) {
       spacertext += "<div class=\"calendar_spacer\">&nbsp;</div>";
   }
   return spacertext;
}

String.prototype.pad = function(l, s, t) {
	return s || (s = " "), (l -= this.length) > 0 ? (s = new Array(Math.ceil(l / s.length)
		+ 1).join(s)).substr(0, t = !t ? l : t == 1 ? 0 : Math.ceil(l / 2))
		+ this + s.substr(0, l - t) : this;
};

function getGUIDFromNodeRef(nodeRef) {
	var str = "" + nodeRef;
	if (str.length > 24) {
		return str.substring(24);
	}
	else {
		return "Not a NodeRef";
	}
}

function findNodeByNodeRef(nodeRef) {
	var resultsArray = search.luceneSearch("ID:workspace\\://SpacesStore/" + getGUIDFromNodeRef(nodeRef));
	 
	if (resultsArray != null && resultsArray.length > 0) {
		return resultsArray[0];
	} 
	else {
		return null;
	}
}

var response = "<table id=\"tabDayView\" bordercolor=\"#FF00FF\" bordercolordark=\"#FFFFFF\" bordercolorlight=\"#99CCFF\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">";

var _arr = HoursArray();
for (i=0; i<_arr.length; i++)
{
	var tdclass = "";
	if (i % 2 == 0)
		tdclass = "alternateRow";
	else
		tdclass = "";
	response += "<TR class='" + tdclass + "'>";
	response += "<TD align='right'>" + _arr[i] + "</TD>";
	response += "<TD onclick='createDayTextBoxNode(event);' width='90%' style=\"border:0\">" + getIntervalEvents(intervals[i]) + "</TD>";
	response += "</TR>";
}

response += "</table>";

model.result = response;

model.intervals = intervals;
model.eventList = events;