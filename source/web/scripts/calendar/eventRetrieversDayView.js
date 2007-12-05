var _currentDateForDayView = new Date();

var handleSuccessDayView = function(o)
{
	var response = o.responseText;
	var _divDayView = document.getElementById("divDayView");
	if (_divDayView != null) {
		_divDayView.innerHTML = response;
 		document.getElementById("spnCurrentDisplayDayDayView").innerHTML = _currentDateForDayView.toDateString();
	}
}

var handleFailureDayView = function(o)
{
	alert("Unable to retrieve, " + o.statusText);
}

var callbackDayView =
{
	success:handleSuccessDayView,
	failure:handleFailureDayView,
	argument: { foo:"foo", bar:"bar" }
};

function callEventRetrieverDayView()
{
	var sUrl = getContextPath() + '/wcservice/calendar/RetrieveDayEvents?';
	var month = _currentDateForDayView.getMonth() + 1;
	var postData = "s=" + spaceRef + "&d=" + _currentDateForDayView.getFullYear()  + "/" + month + "/" + _currentDateForDayView.getDate();
	sUrl += postData;
	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callbackDayView, null);
}


function callLoadersDayView(change)
{
	if (isNaN(change))   change = 0;
	if (change == 0)        _currentDateForDayView = new Date();
	_currentDateForDayView.setDate(_currentDateForDayView.getDate() + change);
	//document.getElementById("spnCurrentDisplayDayDayView").innerHTML = _currentDateForDayView.toDateString();
	callEventRetrieverDayView();
}

YAHOO.util.Event.addListener(window, "load", callLoadersDayView);
