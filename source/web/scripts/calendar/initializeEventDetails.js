
var currentBox = null;
var currentVisibleBox = null;

// Variables to hold Event Default values.
var defaultCaption = "";
var defaultPlace = "";
var defaultFromDate = new Date();
var defaultFromDateString = "";
var defaultFromTime = "";
var defaultToDate = new Date();
var defaultToDateString = "";
var defaultToTime = "";
var defaultColor = "";


YAHOO.namespace("example.calendar");

function handleSelect(type, args, obj) 
{
	var dates = args[0]; 
	var date = dates[0];
	var year = date[0], month = date[1], day = date[2];
	var dateValue = month + "/" + day + "/" + year;
	var dateData = new Date();
	dateData.setFullYear(year, month-1, day);
	
	if (currentBox != null)
	{
		currentBox.value = dateValue;
	}
	if (currentVisibleBox != null)
	{
		currentVisibleBox.value = dateData.toDateString();
	}
	
	ToggleCalendar(0, null);
}


function init() 
{
	YAHOO.example.calendar.cal1 = new YAHOO.widget.Calendar("cal1","cal1Container", { mindate:"1/1/2006", maxdate:"12/31/2019" });
	YAHOO.example.calendar.cal1.selectEvent.subscribe(handleSelect, YAHOO.example.calendar.cal1, true);
	YAHOO.example.calendar.cal1.render();
	
	tabView = new YAHOO.widget.TabView('divCalendarMainContainer');
	
	document.onclick = function(e)		{	HideCalendar(e);	}

	PopulateTimes();
	retrieveEventDefaults();
	initCalendarProperties(); 
}


function ToggleCalendar(mode, txtBox)
{
	if (isNaN(mode))	mode = 0;
	var calendar = document.getElementById("cal1Container");
	if (calendar == null)	return;
	calendar.style.display = mode == 1 ? "inline" : "none";
	
	if (txtBox == null)     return;
	
	if (mode == 1)
	{
		calendar.style.left = txtBox.offsetParent.offsetLeft + txtBox.offsetLeft;
		calendar.style.top = txtBox.offsetParent.offsetTop + txtBox.offsetHeight; 
	}
	
	currentVisibleBox = txtBox;
	if (txtBox.id == "txtFromDate")
		currentBox = document.getElementById("hidFromDate");
	else if (txtBox.id == "txtToDate")
		currentBox = document.getElementById("hidToDate");
}

function HideCalendar(e)
{
	var obj = null;
	if (window.event)
		obj = window.event.srcElement;
	else
		obj = e.target;
	
	if (obj.id != "txtFromDate" && obj.id != "txtToDate")
		ToggleCalendar(0);
}

function PopulateTimes()
{
	var _arr = HoursArray();
	for (i=0; i<_arr.length; i++)
	{
		var _option1 = document.createElement("OPTION");
		_option1.text = _arr[i];
		_option1.value = _arr[i];
		document.getElementById("lstFromTime").options.add(_option1);

		var _option2 = document.createElement("OPTION");
		_option2.text = _arr[i];
		_option2.value = _arr[i];
		document.getElementById("lstToTime").options.add(_option2);
	}
}

/////////////

function initSuccessHandler(o) {
	var result = o.responseText;  
	var _div = document.getElementById("calendarSubscribe");
	if (_div != null) {
	    _div.innerHTML = result;
	}
}

function initFailureHandler(o) {
	alert("Failed to initialise calendar properties");
}

function initCalendarProperties() {
    var url = getContextPath() + "/wcservice/calendar/calendarInit";
    var initCallback = {
	success:initSuccessHandler,
	failure:initFailureHandler
    };
    var request = YAHOO.util.Connect.asyncRequest("GET", url + "?ref=" + spaceRef, initCallback, null);
}

/////////////

YAHOO.util.Event.addListener(window, "load", init);




var handleSuccessInitEvent = function(o)
{
	populateEventDefaults(o.responseText);
}

var handleFailureInitEvent = function(o)
{
	alert("Unable to Retrieve Default values for Events, " + o.statusText);
}

var callbackInitEvent =
{
	success:handleSuccessInitEvent,
	failure:handleFailureInitEvent,
	argument: { foo:"foo", bar:"bar" }
};

function retrieveEventDefaults()
{
	var sUrl = getContextPath() + '/wcservice/calendar/RetrieveEventDefaults?';
	var postData = "s=" + spaceRef;
	sUrl += postData;
	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callbackInitEvent, null);
}

function populateEventDefaults(response)
{
	var _details = response.split("^");
	if (_details.length <=0)
	{
		alert("Unable to Retrieve Event Defaults");
		return false;
	}
	
	
	defaultCaption = _details[0];
	
	var _fromDate = new Date(_details[1]);
	defaultFromDate = new Date(_fromDate);
	var x = _fromDate.getMonth() + 1;
	defaultFromDateString =  x + "/" + _fromDate.getDate() + "/" + _fromDate.getFullYear();
	defaultFromTime = _fromDate.getHours() + ":" + _fromDate.getMinutes().toString().pad(2, "0", 1);
	
	var _toDate = new Date(_details[2]);
	defaultToDate = new Date(_toDate);
	var y = _toDate.getMonth() + 1;
	defaultToDateString =  y + "/" + _toDate.getDate() + "/" + _toDate.getFullYear();
	defaultToTime = _toDate.getHours() + ":" + _toDate.getMinutes().toString().pad(2, "0", 1);
	
	defaultPlace = _details[3];
	defaultColor = _details[4];


	document.getElementById("txtWhatEvent").value = defaultCaption;
	document.getElementById("hidFromDate").value = defaultFromDateString;
	document.getElementById("txtFromDate").value = _fromDate.toDateString();
	document.getElementById("lstFromTime").value = defaultFromTime;
	document.getElementById("hidToDate").value = defaultToDateString;
	document.getElementById("txtToDate").value = _toDate.toDateString();
	document.getElementById("lstToTime").value = defaultToTime;
	document.getElementById("txtWhereEvent").value = defaultPlace;
	document.getElementById("txtColor").value = defaultColor;
}
