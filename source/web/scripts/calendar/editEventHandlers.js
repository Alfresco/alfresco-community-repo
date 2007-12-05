var _currentActiveEditingEvent = null;

function editEvent(eventId)
{
	_currentActiveEditingEvent = eventId;
	if (_currentActiveEditingEvent == null)		return false;
	retrieveCalendarEvent();
}

function populateEventDetailsForEdit(response)
{
	var _details = response.split("^");
	if (_details.length <=0)
	{
		alert("Unable to Retrieve details of Event");
		return false;
	}
	
	var _fromDate = new Date(_details[1]);
	var _toDate = new Date(_details[3]);
	
	document.getElementById("txtWhatEvent").value = _details[0];
	document.getElementById("hidFromDate").value = _details[1];
	document.getElementById("txtFromDate").value = _fromDate.toDateString();
	document.getElementById("lstFromTime").value = _details[2];
	document.getElementById("hidToDate").value = _details[3];
	document.getElementById("txtToDate").value = _toDate.toDateString();
	document.getElementById("lstToTime").value = _details[4];
	document.getElementById("txtWhereEvent").value = _details[5];
	document.getElementById("txtDescriptionEvent").value = _details[6];
	document.getElementById("txtColor").value = _details[7];

	tabView.set('activeIndex', 3);		//tabView: the main YUI TabView Control - defined in FTL.
	document.getElementById("txtWhatEvent").focus();
}



var handleSuccessRetrieveEvent = function(o)
{
	populateEventDetailsForEdit(o.responseText);
}

var handleFailureRetrieveEvent = function(o)
{
	alert("Unable to Retrieve Event Details, " + o.statusText);
}

var callbackRetrieveEvent =
{
	success:handleSuccessRetrieveEvent,
	failure:handleFailureRetrieveEvent,
	argument: { foo:"foo", bar:"bar" }
};

function retrieveCalendarEvent()
{
	var sUrl = getContextPath() + '/wcservice/calendar/RetrieveEventDetails?';
	var postData = "e=" + _currentActiveEditingEvent;
	sUrl += postData;
	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callbackRetrieveEvent, null);
}


