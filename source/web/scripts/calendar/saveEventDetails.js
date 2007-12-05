
var handleSuccessSaveEvent = function(o)
{
	hideSavingIndicator('c');
	alert("Calendar updated!");
	resetEventDetails();
	refreshAllViews();
}

var handleFailureSaveEvent = function(o)
{
	hideSavingIndicator('c');
	alert("Unable to Save, " + o.statusText);
}

var callbackSaveEvent =
{
	success:handleSuccessSaveEvent,
	failure:handleFailureSaveEvent,
	argument: { foo:"foo", bar:"bar" }
};

function saveCalendarEvent(toDelete)
{
	if (!validateEventDetails(toDelete))	return;
	showSavingIndicator('c');

	var sUrl = getContextPath() + '/wcservice/calendar/SaveCalendarEvent?';
	var postData = constructCalendarData(toDelete);
	sUrl += postData;
	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callbackSaveEvent, null);
}

function constructCalendarData(toDelete)
{
	var data = "";
	data += "s=" + spaceRef;
	data += "&";
	data += "what=" + document.getElementById("txtWhatEvent").value;
	data += "&";
	data += "fd=" + document.getElementById("hidFromDate").value;
	data += "&";
	data += "ft=" + document.getElementById("lstFromTime").value;
	data += "&";
	data += "td=" + document.getElementById("hidToDate").value;
	data += "&";
	data += "tt=" + document.getElementById("lstToTime").value;
	data += "&";
	data += "where=" + document.getElementById("txtWhereEvent").value;
	data += "&";
	data += "desc=" + document.getElementById("txtDescriptionEvent").value;
	data += "&";
	data += "color=" + document.getElementById("txtColor").value;
	data += "&";
	if (toDelete)
	{
		data += "d=true";
	}
	else
	{
		data += "d=false";
	}
	if (_currentActiveEditingEvent != null)
	{
		data += "&";
		data += "e=" + _currentActiveEditingEvent;
	}
	
	return data;
}

function resetEventDetails()
{
	document.getElementById("txtWhatEvent").value = defaultCaption;
	document.getElementById("hidFromDate").value = defaultFromDateString;
	document.getElementById("txtFromDate").value = defaultFromDate.toDateString();
	document.getElementById("lstFromTime").value = defaultFromTime;
	document.getElementById("hidToDate").value = defaultToDateString;
	document.getElementById("txtToDate").value = defaultToDate.toDateString();
	document.getElementById("lstToTime").value = defaultToTime;
	document.getElementById("txtWhereEvent").value = defaultPlace;
	document.getElementById("txtColor").value = defaultColor;

	/*document.getElementById("txtWhatEvent").value = "";
	document.getElementById("hidFromDate").value = "";
	document.getElementById("txtFromDate").value = "";
	document.getElementById("lstFromTime").selectedIndex = 0;
	document.getElementById("hidToDate").value = "";
	document.getElementById("txtToDate").value = "";
	document.getElementById("lstToTime").selectedIndex = 0;
	document.getElementById("txtWhereEvent").value = "";
	document.getElementById("txtDescriptionEvent").value = "";
	document.getElementById("txtColor").value = "";*/

	document.getElementById("txtWhatEvent").focus();
	
	_currentActiveEditingEvent = null;
}

function validateEventDetails(toDelete)
{
	if (toDelete)
		if (_currentActiveEditingEvent == null)
		{
			alert("No Event Currently selected.\rPlease select an Event first from Month/Week/Day view to delete.");
			return false;
		}
		
	if (trim(document.getElementById("txtWhatEvent").value) == "")
	{
		alert("Please enter Event Information!");
		document.getElementById("txtWhatEvent").focus();
		return false;
	}
	if (!ValidateString(trim(document.getElementById("txtWhatEvent").value)))
	{
		document.getElementById("txtWhatEvent").focus();
		return false;
	}
	if (trim(document.getElementById("txtFromDate").value) == "")
	{
		alert("Please select a valid Starting Date!");
		document.getElementById("txtFromDate").focus();
		return false;
	}
	if (trim(document.getElementById("txtToDate").value) == "")
	{
		alert("Please select a valid Ending Date!");
		document.getElementById("txtToDate").focus();
		return false;
	}
	if (!ValidateDateSet())
	{
		alert("Ending Date must be greater than Starting Date!");
		document.getElementById("txtToDate").focus();
		return false;
	}
	return true;
}

function ValidateString(dataValue)
{
	for(var j=0; j<dataValue.length; j++)
	{
		var alphaa = dataValue.charAt(j);
		var hh = alphaa.charCodeAt(0);
		/*
			Allowed Characters:
				48-57 -> 0-9
				64-91 -> A-Z
				96-123 -> a-z
				32 -> space bar
				44 -> ,
				59 -> ;
				40 -> (
				41 -> )
				63 -> ?
				33 -> !
				45 -> -
				95 -> _
		*/
		if((hh>=48 && hh<=57) || (hh>64 && hh<91) || (hh>96 && hh<123) || (hh==32) || (hh==44) || (hh==59) || (hh==40) || (hh==41) || (hh==63) || (hh==33) || (hh==45) || (hh==95))
		{
		}
		else
		{
				alert("Event Information has some invalid characters.\rAllowed Characters are 0-9 a-z A-Z , ; ( ) ? ! - _");
				return false;
		}
	}
	return true;
}

function ValidateDateSet()
{
	var fromDate = new Date(document.getElementById("hidFromDate").value);
	var fromTime = document.getElementById("lstFromTime").value.split(":");
	fromDate.setHours(fromTime[0]);
	fromDate.setMinutes(fromTime[1]);

	var toDate = new Date(document.getElementById("hidToDate").value);
	var toTime = document.getElementById("lstToTime").value.split(":");
	toDate.setHours(toTime[0]);
	toDate.setMinutes(toTime[1]);
	
	return !(toDate <= fromDate);
}
